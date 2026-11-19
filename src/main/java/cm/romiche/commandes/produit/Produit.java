package cm.romiche.commandes.produit;

import java.time.Instant;

import cm.romiche.commandes.commun.RegleMetierException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Un produit du catalogue. Le stock ne peut jamais devenir négatif :
 * c'est l'entité elle-même qui protège cette règle (retirerDuStock).
 */
@Entity
@Table(name = "produit")
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nom;

    @Column(length = 1000)
    private String description;

    /** Prix en FCFA (pas de centimes). */
    @Column(name = "prix_fcfa", nullable = false)
    private long prixFcfa;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    /** Verrou optimiste : empêche deux commandes simultanées de « vendre » le même stock. */
    @Version
    private long version;

    protected Produit() {
        // Requis par JPA
    }

    public Produit(String nom, String description, long prixFcfa, int stock) {
        this.nom = nom;
        this.description = description;
        this.prixFcfa = prixFcfa;
        this.stock = stock;
        this.creeLe = Instant.now();
    }

    public void modifier(String nom, String description, long prixFcfa, int stock) {
        this.nom = nom;
        this.description = description;
        this.prixFcfa = prixFcfa;
        this.stock = stock;
    }

    /** On ne supprime jamais un produit déjà commandé : on le retire simplement du catalogue. */
    public void desactiver() {
        this.actif = false;
    }

    public void retirerDuStock(int quantite) {
        if (!actif) {
            throw new RegleMetierException("Le produit « %s » n'est plus disponible.".formatted(nom));
        }
        if (quantite > stock) {
            throw new RegleMetierException("Stock insuffisant pour « %s » : %d demandé(s), %d disponible(s)."
                    .formatted(nom, quantite, stock));
        }
        this.stock -= quantite;
    }

    public void remettreEnStock(int quantite) {
        this.stock += quantite;
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getDescription() {
        return description;
    }

    public long getPrixFcfa() {
        return prixFcfa;
    }

    public int getStock() {
        return stock;
    }

    public boolean isActif() {
        return actif;
    }

    public Instant getCreeLe() {
        return creeLe;
    }
}
