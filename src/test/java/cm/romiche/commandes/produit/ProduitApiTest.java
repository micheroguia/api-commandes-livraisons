package cm.romiche.commandes.produit;

import static cm.romiche.commandes.ComptesDeTest.gerant;
import static cm.romiche.commandes.ComptesDeTest.livreur;
import static org.hamcrest.Matchers.hasKey;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProduitApiTest {

    private static final String PRODUIT = """
            {"nom": "Sac de riz 25 kg", "prixFcfa": 18000, "stock": 10}
            """;

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("Un gérant crée un produit : 201 et son adresse")
    void creer() throws Exception {
        mvc.perform(post("/api/produits").with(gerant()).contentType(APPLICATION_JSON).content(PRODUIT))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.prixFcfa").value(18000))
                .andExpect(jsonPath("$.actif").value(true));
    }

    @Test
    @DisplayName("Le catalogue se consulte sans être connecté")
    void cataloguePublic() throws Exception {
        mvc.perform(get("/api/produits")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Sans jeton, on ne peut pas ajouter de produit : 401")
    void creerSansJeton() throws Exception {
        mvc.perform(post("/api/produits").contentType(APPLICATION_JSON).content(PRODUIT))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(jsonPath("$.title").value("Authentification requise"));
    }

    @Test
    @DisplayName("Un livreur ne peut pas modifier le catalogue : 403")
    void creerEnTantQueLivreur() throws Exception {
        mvc.perform(post("/api/produits").with(livreur("+237677000000")).contentType(APPLICATION_JSON).content(PRODUIT))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Des champs invalides renvoient 400 avec la liste des erreurs")
    void invalide() throws Exception {
        mvc.perform(post("/api/produits").with(gerant()).contentType(APPLICATION_JSON).content("""
                        {"nom": "", "prixFcfa": 0, "stock": -1}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erreurs", hasKey("nom")))
                .andExpect(jsonPath("$.erreurs", hasKey("prixFcfa")))
                .andExpect(jsonPath("$.erreurs", hasKey("stock")));
    }

    @Test
    @DisplayName("Un produit inconnu renvoie 404")
    void introuvable() throws Exception {
        mvc.perform(get("/api/produits/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Un produit retiré du catalogue n'apparaît plus dans la liste par défaut")
    void desactiver() throws Exception {
        String reponse = mvc.perform(post("/api/produits").with(gerant()).contentType(APPLICATION_JSON).content("""
                        {"nom": "Produit éphémère", "prixFcfa": 500, "stock": 3}
                        """))
                .andReturn().getResponse().getContentAsString();
        String id = reponse.replaceAll(".*\"id\":(\\d+).*", "$1");

        mvc.perform(delete("/api/produits/" + id).with(gerant())).andExpect(status().isNoContent());
        mvc.perform(get("/api/produits/" + id)).andExpect(jsonPath("$.actif").value(false));
    }
}
