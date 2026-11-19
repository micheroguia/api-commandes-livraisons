package cm.romiche.commandes.produit;

import java.time.Instant;

/** Ce que l'API renvoie pour un produit (on n'expose jamais l'entité JPA directement). */
public record ProduitReponse(
        Long id,
        String nom,
        String description,
        long prixFcfa,
        int stock,
        boolean actif,
        Instant creeLe) {

    public static ProduitReponse depuis(Produit produit) {
        return new ProduitReponse(produit.getId(), produit.getNom(), produit.getDescription(),
                produit.getPrixFcfa(), produit.getStock(), produit.isActif(), produit.getCreeLe());
    }
}
