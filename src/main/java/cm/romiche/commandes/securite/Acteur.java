package cm.romiche.commandes.securite;

import org.springframework.security.core.Authentication;

/**
 * La personne connectée qui fait l'action, lue depuis son jeton.
 *
 * @param telephone son identifiant (+237…), qui est le « sujet » du jeton
 * @param gerant    vrai si elle a le rôle GERANT
 */
public record Acteur(String telephone, boolean gerant) {

    public static Acteur depuis(Authentication authentification) {
        boolean estGerant = authentification.getAuthorities().stream()
                .anyMatch(autorite -> "ROLE_GERANT".equals(autorite.getAuthority()));
        return new Acteur(authentification.getName(), estGerant);
    }
}
