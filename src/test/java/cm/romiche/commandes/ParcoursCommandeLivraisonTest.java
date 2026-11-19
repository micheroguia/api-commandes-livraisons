package cm.romiche.commandes;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/**
 * Teste l'API comme un vrai client : de la commande jusqu'à la livraison.
 * Chaque test est annulé à la fin (@Transactional), la base reste propre.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ParcoursCommandeLivraisonTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    // ---------- petits outils ----------

    private JsonNode lire(ResultActions resultat) throws Exception {
        return json.readTree(resultat.andReturn().getResponse().getContentAsString());
    }

    private long creerProduit(long prixFcfa, int stock) throws Exception {
        return lire(mvc.perform(post("/api/produits").contentType(APPLICATION_JSON).content("""
                        {"nom": "Recharge gaz 12,5 kg", "prixFcfa": %d, "stock": %d}
                        """.formatted(prixFcfa, stock)))
                .andExpect(status().isCreated())).get("id").asLong();
    }

    private ResultActions commander(long produitId, int quantite) throws Exception {
        return mvc.perform(post("/api/commandes").contentType(APPLICATION_JSON).content("""
                {
                  "clientNom": "Client Test",
                  "telephone": "6 90 00 00 00",
                  "quartier": "Bonamoussadi",
                  "ville": "Douala",
                  "indications": "Portail bleu après la pharmacie",
                  "lignes": [{"produitId": %d, "quantite": %d}]
                }
                """.formatted(produitId, quantite)));
    }

    private ResultActions changerStatut(long livraisonId, String statut) throws Exception {
        return mvc.perform(post("/api/livraisons/%d/statut".formatted(livraisonId))
                .contentType(APPLICATION_JSON).content("""
                        {"statut": "%s", "commentaire": "test"}
                        """.formatted(statut)));
    }

    // ---------- tests ----------

    @Test
    @DisplayName("Commander réserve le stock, calcule le total en FCFA et normalise le téléphone")
    void commander_reserve_le_stock() throws Exception {
        long produitId = creerProduit(6500, 10);

        commander(produitId, 3)
                .andExpect(status().isCreated())
                // La référence dépend du numéro donné par la base : on vérifie seulement son format.
                .andExpect(jsonPath("$.reference", matchesPattern("CMD-\\d{6}")))
                .andExpect(jsonPath("$.totalFcfa").value(19500))
                .andExpect(jsonPath("$.telephone").value("+237690000000"))
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE"));

        mvc.perform(get("/api/produits/" + produitId)).andExpect(jsonPath("$.stock").value(7));
    }

    @Test
    @DisplayName("Un stock insuffisant renvoie 409 et ne retire rien du stock")
    void stock_insuffisant() throws Exception {
        long produitId = creerProduit(1200, 2);

        commander(produitId, 5)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        mvc.perform(get("/api/produits/" + produitId)).andExpect(jsonPath("$.stock").value(2));
    }

    @Test
    @DisplayName("Parcours complet : confirmer, assigner un livreur, livrer → la commande passe à LIVREE")
    void parcours_complet() throws Exception {
        long produitId = creerProduit(2500, 20);
        long commandeId = lire(commander(produitId, 2)).get("id").asLong();

        long livraisonId = lire(mvc.perform(post("/api/commandes/%d/confirmer".formatted(commandeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("CONFIRMEE")))
                .get("livraisonId").asLong();

        mvc.perform(post("/api/livraisons/%d/assigner".formatted(livraisonId)).contentType(APPLICATION_JSON)
                        .content("""
                                {"livreurNom": "Livreur Test", "livreurTelephone": "+237 677 00 00 00"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ASSIGNEE"))
                .andExpect(jsonPath("$.livreurTelephone").value("+237677000000"))
                .andExpect(jsonPath("$.quartier").value("Bonamoussadi"));

        changerStatut(livraisonId, "EN_COURS").andExpect(status().isOk());
        changerStatut(livraisonId, "LIVREE").andExpect(status().isOk());

        mvc.perform(get("/api/commandes/" + commandeId)).andExpect(jsonPath("$.statut").value("LIVREE"));
        mvc.perform(get("/api/livraisons/%d/historique".formatted(livraisonId)))
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].statut").value("A_PLANIFIER"))
                .andExpect(jsonPath("$[3].statut").value("LIVREE"));
    }

    @Test
    @DisplayName("Sauter une étape (À planifier → Livrée) est refusé avec 409")
    void transition_interdite() throws Exception {
        long produitId = creerProduit(2500, 5);
        long commandeId = lire(commander(produitId, 1)).get("id").asLong();
        long livraisonId = lire(mvc.perform(post("/api/commandes/%d/confirmer".formatted(commandeId))))
                .get("livraisonId").asLong();

        changerStatut(livraisonId, "LIVREE").andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Annuler une commande remet les produits en stock")
    void annuler_rend_le_stock() throws Exception {
        long produitId = creerProduit(18000, 4);
        long commandeId = lire(commander(produitId, 3)).get("id").asLong();
        mvc.perform(get("/api/produits/" + produitId)).andExpect(jsonPath("$.stock").value(1));

        mvc.perform(post("/api/commandes/%d/annuler".formatted(commandeId)).contentType(APPLICATION_JSON)
                        .content("""
                                {"motif": "Client injoignable"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ANNULEE"));

        mvc.perform(get("/api/produits/" + produitId)).andExpect(jsonPath("$.stock").value(4));
    }

    @Test
    @DisplayName("Une commande ne peut plus être annulée quand le livreur est en route")
    void pas_d_annulation_en_route() throws Exception {
        long produitId = creerProduit(1200, 5);
        long commandeId = lire(commander(produitId, 1)).get("id").asLong();
        long livraisonId = lire(mvc.perform(post("/api/commandes/%d/confirmer".formatted(commandeId))))
                .get("livraisonId").asLong();
        mvc.perform(post("/api/livraisons/%d/assigner".formatted(livraisonId)).contentType(APPLICATION_JSON)
                .content("""
                        {"livreurNom": "Livreur Test", "livreurTelephone": "677000000"}
                        """));
        changerStatut(livraisonId, "EN_COURS").andExpect(status().isOk());

        mvc.perform(post("/api/commandes/%d/annuler".formatted(commandeId)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Un téléphone non camerounais est refusé avec 400")
    void telephone_invalide() throws Exception {
        long produitId = creerProduit(1200, 5);
        mvc.perform(post("/api/commandes").contentType(APPLICATION_JSON).content("""
                        {"clientNom": "Client Test", "telephone": "+33 6 12 34 56 78",
                         "quartier": "Akwa", "ville": "Douala",
                         "lignes": [{"produitId": %d, "quantite": 1}]}
                        """.formatted(produitId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erreurs.telephone").exists());
    }
}
