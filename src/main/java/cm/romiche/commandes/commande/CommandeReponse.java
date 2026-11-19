package cm.romiche.commandes.commande;

import java.time.Instant;
import java.util.List;

/** Ce que l'API renvoie pour une commande. */
public record CommandeReponse(
        Long id,
        String reference,
        String clientNom,
        String telephone,
        String quartier,
        String ville,
        String indications,
        StatutCommande statut,
        long totalFcfa,
        String motifAnnulation,
        Instant creeLe,
        Long livraisonId,
        List<LigneReponse> lignes) {

    public record LigneReponse(
            Long produitId,
            String produitNom,
            int quantite,
            long prixUnitaireFcfa,
            long sousTotalFcfa) {
    }

    public static CommandeReponse depuis(Commande commande) {
        List<LigneReponse> lignes = commande.getLignes().stream()
                .map(ligne -> new LigneReponse(
                        ligne.getProduit().getId(),
                        ligne.getProduit().getNom(),
                        ligne.getQuantite(),
                        ligne.getPrixUnitaireFcfa(),
                        ligne.sousTotalFcfa()))
                .toList();
        Long livraisonId = commande.getLivraison() == null ? null : commande.getLivraison().getId();
        return new CommandeReponse(commande.getId(), commande.getReference(), commande.getClientNom(),
                commande.getTelephone(), commande.getQuartier(), commande.getVille(), commande.getIndications(),
                commande.getStatut(), commande.getTotalFcfa(), commande.getMotifAnnulation(), commande.getCreeLe(),
                livraisonId, lignes);
    }
}
