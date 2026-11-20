package cm.romiche.commandes.commun;

/**
 * Connexion refusée : renvoie une erreur 401.
 * Le message est volontairement le même que le téléphone soit inconnu ou que le mot de passe soit faux,
 * pour ne pas révéler quels numéros ont un compte.
 */
public class IdentifiantsInvalidesException extends RuntimeException {

    public IdentifiantsInvalidesException() {
        super("Téléphone ou mot de passe incorrect.");
    }
}
