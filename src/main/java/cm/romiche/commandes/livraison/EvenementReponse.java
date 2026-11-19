package cm.romiche.commandes.livraison;

import java.time.Instant;

public record EvenementReponse(StatutLivraison statut, String commentaire, Instant date) {

    public static EvenementReponse depuis(EvenementLivraison evenement) {
        return new EvenementReponse(evenement.getStatut(), evenement.getCommentaire(), evenement.getDateEvenement());
    }
}
