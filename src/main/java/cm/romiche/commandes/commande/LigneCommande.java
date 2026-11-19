package cm.romiche.commandes.commande;

import cm.romiche.commandes.produit.Produit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Une ligne de commande : un produit, une quantité et le prix AU MOMENT de la commande
 * (si le prix du produit change plus tard, la commande garde l'ancien prix).
 */
@Entity
@Table(name = "ligne_commande")
public class LigneCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(nullable = false)
    private int quantite;

    @Column(name = "prix_unitaire_fcfa", nullable = false)
    private long prixUnitaireFcfa;

    protected LigneCommande() {
        // Requis par JPA
    }

    LigneCommande(Commande commande, Produit produit, int quantite, long prixUnitaireFcfa) {
        this.commande = commande;
        this.produit = produit;
        this.quantite = quantite;
        this.prixUnitaireFcfa = prixUnitaireFcfa;
    }

    public long sousTotalFcfa() {
        return prixUnitaireFcfa * quantite;
    }

    public Long getId() {
        return id;
    }

    public Produit getProduit() {
        return produit;
    }

    public int getQuantite() {
        return quantite;
    }

    public long getPrixUnitaireFcfa() {
        return prixUnitaireFcfa;
    }
}
