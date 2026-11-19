package cm.romiche.commandes.commun;

/**
 * Levée quand une action viole une règle du métier
 * (stock insuffisant, passage de statut interdit…) : renvoie une erreur 409.
 */
public class RegleMetierException extends RuntimeException {

    public RegleMetierException(String message) {
        super(message);
    }
}
