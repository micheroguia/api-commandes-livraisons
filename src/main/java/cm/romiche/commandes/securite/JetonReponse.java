package cm.romiche.commandes.securite;

/**
 * Réponse d'une connexion réussie. Le client envoie ensuite le jeton dans chaque requête :
 * en-tête « Authorization: Bearer &lt;jeton&gt; ».
 */
public record JetonReponse(String jeton, String type, long expireDansSecondes, UtilisateurReponse utilisateur) {
}
