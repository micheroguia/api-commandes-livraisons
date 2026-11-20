package cm.romiche.commandes.securite;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/** Teste la vraie connexion : téléphone + mot de passe → jeton JWT → appels protégés. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private UtilisateurRepository utilisateurs;

    @Autowired
    private PasswordEncoder encodeur;

    @BeforeEach
    void creerComptes() {
        utilisateurs.save(new Utilisateur("Gérant Test", "+237690000001", encodeur.encode("motdepasse-gerant"), Role.GERANT));
        utilisateurs.save(new Utilisateur("Livreur Test", "+237677000000", encodeur.encode("motdepasse-livreur"), Role.LIVREUR));
    }

    private ResultActions seConnecter(String telephone, String motDePasse) throws Exception {
        return mvc.perform(post("/api/auth/connexion").contentType(APPLICATION_JSON).content("""
                {"telephone": "%s", "motDePasse": "%s"}
                """.formatted(telephone, motDePasse)));
    }

    private String jeton(String telephone, String motDePasse) throws Exception {
        String reponse = seConnecter(telephone, motDePasse).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(reponse).get("jeton").asText();
    }

    @Test
    @DisplayName("Connexion réussie : le jeton donne accès aux routes du gérant")
    void connexionReussie() throws Exception {
        seConnecter("6 90 00 00 01", "motdepasse-gerant")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.expireDansSecondes").value(3600))
                .andExpect(jsonPath("$.utilisateur.role").value("GERANT"))
                .andExpect(jsonPath("$.utilisateur.motDePasse").doesNotExist());

        String jeton = jeton("690000001", "motdepasse-gerant");
        mvc.perform(get("/api/auth/moi").header("Authorization", jeton))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telephone").value("+237690000001"));
        mvc.perform(get("/api/commandes").header("Authorization", jeton)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Mauvais mot de passe ou numéro inconnu : même réponse 401")
    void identifiantsInvalides() throws Exception {
        String mauvaisMotDePasse = seConnecter("690000001", "pas-le-bon").andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        String numeroInconnu = seConnecter("699999999", "motdepasse-gerant").andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        // On ne révèle pas si le numéro a un compte.
        org.assertj.core.api.Assertions.assertThat(json.readTree(mauvaisMotDePasse).get("detail"))
                .isEqualTo(json.readTree(numeroInconnu).get("detail"));
    }

    @Test
    @DisplayName("Sans jeton ou avec un jeton falsifié : 401")
    void sansJetonOuFalsifie() throws Exception {
        mvc.perform(get("/api/commandes"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(jsonPath("$.title").value("Authentification requise"));
        mvc.perform(get("/api/commandes").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Un livreur connecté n'a pas accès aux routes du gérant : 403")
    void livreurSansDroitsGerant() throws Exception {
        String jeton = jeton("677000000", "motdepasse-livreur");
        mvc.perform(get("/api/commandes").header("Authorization", jeton))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        mvc.perform(get("/api/livraisons/miennes").header("Authorization", jeton)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Un compte désactivé ne peut plus se connecter")
    void compteDesactive() throws Exception {
        Utilisateur livreur = utilisateurs.findByTelephone("+237677000000").orElseThrow();
        livreur.desactiver();
        seConnecter("677000000", "motdepasse-livreur").andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Un gérant crée un compte livreur, qui peut ensuite se connecter")
    void gerantCreeUnLivreur() throws Exception {
        String jetonGerant = jeton("690000001", "motdepasse-gerant");
        mvc.perform(post("/api/utilisateurs").header("Authorization", jetonGerant).contentType(APPLICATION_JSON)
                        .content("""
                                {"nom": "Nouveau Livreur", "telephone": "6 55 00 00 00",
                                 "motDePasse": "un-mot-de-passe-solide", "role": "LIVREUR"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.telephone").value("+237655000000"))
                .andExpect(jsonPath("$.motDePasse").doesNotExist());

        seConnecter("655000000", "un-mot-de-passe-solide").andExpect(status().isOk());
    }

    @Test
    @DisplayName("Un mot de passe trop court est refusé à la création du compte")
    void motDePasseTropCourt() throws Exception {
        String jetonGerant = jeton("690000001", "motdepasse-gerant");
        mvc.perform(post("/api/utilisateurs").header("Authorization", jetonGerant).contentType(APPLICATION_JSON)
                        .content("""
                                {"nom": "Livreur", "telephone": "655000001", "motDePasse": "court", "role": "LIVREUR"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erreurs.motDePasse").exists());
    }
}
