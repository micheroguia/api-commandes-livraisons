package cm.romiche.commandes.commun;

/** L'utilisateur est connecté mais n'a pas le droit de faire cette action : renvoie une erreur 403. */
public class AccesRefuseException extends RuntimeException {

    public AccesRefuseException(String message) {
        super(message);
    }
}
