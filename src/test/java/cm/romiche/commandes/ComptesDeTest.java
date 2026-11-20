package cm.romiche.commandes;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Simule une personne connectée dans les tests, sans passer par la connexion :
 * mvc.perform(post(...).with(gerant())) ou .with(livreur("+237677000000")).
 */
public final class ComptesDeTest {

    public static final String TELEPHONE_GERANT = "+237690000001";

    private ComptesDeTest() {
    }

    public static RequestPostProcessor gerant() {
        return jwt().jwt(jeton -> jeton.subject(TELEPHONE_GERANT).claim("role", "GERANT"))
                .authorities(new SimpleGrantedAuthority("ROLE_GERANT"));
    }

    public static RequestPostProcessor livreur(String telephone) {
        return jwt().jwt(jeton -> jeton.subject(telephone).claim("role", "LIVREUR"))
                .authorities(new SimpleGrantedAuthority("ROLE_LIVREUR"));
    }
}
