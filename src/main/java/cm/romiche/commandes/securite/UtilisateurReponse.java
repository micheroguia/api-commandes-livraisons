package cm.romiche.commandes.securite;

import java.time.Instant;

/** Ce que l'API renvoie pour un compte : jamais le mot de passe, même sous forme d'empreinte. */
public record UtilisateurReponse(Long id, String nom, String telephone, Role role, boolean actif, Instant creeLe) {

    public static UtilisateurReponse depuis(Utilisateur utilisateur) {
        return new UtilisateurReponse(utilisateur.getId(), utilisateur.getNom(), utilisateur.getTelephone(),
                utilisateur.getRole(), utilisateur.isActif(), utilisateur.getCreeLe());
    }
}
