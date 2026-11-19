package cm.romiche.commandes.livraison;

import java.time.Instant;

import cm.romiche.commandes.commande.Commande;

/** Ce que l'API renvoie pour une livraison (avec l'adresse, utile au livreur). */
public record LivraisonReponse(
        Long id,
        Long commandeId,
        String commandeReference,
        StatutLivraison statut,
        String livreurNom,
        String livreurTelephone,
        String clientNom,
        String clientTelephone,
        String quartier,
        String ville,
        String indications,
        Instant creeLe,
        Instant majLe) {

    public static LivraisonReponse depuis(Livraison livraison) {
        Commande commande = livraison.getCommande();
        return new LivraisonReponse(livraison.getId(), commande.getId(), commande.getReference(),
                livraison.getStatut(), livraison.getLivreurNom(), livraison.getLivreurTelephone(),
                commande.getClientNom(), commande.getTelephone(), commande.getQuartier(), commande.getVille(),
                commande.getIndications(), livraison.getCreeLe(), livraison.getMajLe());
    }
}
