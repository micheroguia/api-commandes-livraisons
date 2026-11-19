package cm.romiche.commandes.commande;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cm.romiche.commandes.commun.RegleMetierException;
import cm.romiche.commandes.livraison.Livraison;
import cm.romiche.commandes.produit.Produit;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * Une commande client. Toutes les règles (stock, annulation, confirmation)
 * sont écrites ICI, dans l'entité : le service ne fait qu'orchestrer.
 */
@Entity
@Table(name = "commande")
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Référence lisible donnée au client, par exemple CMD-000042. */
    @Column(length = 20, unique = true)
    private String reference;

    @Column(name = "client_nom", nullable = false, length = 120)
    private String clientNom;

    /** Toujours au format +237XXXXXXXXX (voir Telephones). */
    @Column(nullable = false, length = 20)
    private String telephone;

    @Column(nullable = false, length = 120)
    private String quartier;

    @Column(nullable = false, length = 80)
    private String ville;

    /** Repères pour le livreur : « après la pharmacie, portail bleu »… */
    @Column(length = 500)
    private String indications;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutCommande statut = StatutCommande.EN_ATTENTE;

    @Column(name = "total_fcfa", nullable = false)
    private long totalFcfa;

    @Column(name = "motif_annulation", length = 300)
    private String motifAnnulation;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<LigneCommande> lignes = new ArrayList<>();

    @OneToOne(mappedBy = "commande")
    private Livraison livraison;

    protected Commande() {
        // Requis par JPA
    }

    public Commande(String clientNom, String telephone, String quartier, String ville, String indications) {
        this.clientNom = clientNom;
        this.telephone = telephone;
        this.quartier = quartier;
        this.ville = ville;
        this.indications = indications;
        this.creeLe = Instant.now();
    }

    /** Ajoute un produit : le stock est retiré tout de suite pour ne pas vendre deux fois la même chose. */
    public void ajouterLigne(Produit produit, int quantite) {
        produit.retirerDuStock(quantite);
        lignes.add(new LigneCommande(this, produit, quantite, produit.getPrixFcfa()));
        totalFcfa += produit.getPrixFcfa() * quantite;
    }

    /** Appelé après le premier enregistrement, quand la base a donné un numéro (id). */
    public void attribuerReference() {
        this.reference = "CMD-%06d".formatted(id);
    }

    public void confirmer() {
        if (statut != StatutCommande.EN_ATTENTE) {
            throw new RegleMetierException("Seule une commande en attente peut être confirmée (statut actuel : %s)."
                    .formatted(statut.libelle()));
        }
        this.statut = StatutCommande.CONFIRMEE;
    }

    /** Annule la commande et remet les produits en stock, tant que le livreur n'est pas parti. */
    public void annuler(String motif) {
        if (statut == StatutCommande.LIVREE) {
            throw new RegleMetierException("Une commande déjà livrée ne peut plus être annulée.");
        }
        if (statut == StatutCommande.ANNULEE) {
            throw new RegleMetierException("Cette commande est déjà annulée.");
        }
        if (livraison != null && !livraison.peutEtreAnnulee()) {
            throw new RegleMetierException("La livraison est « %s » : la commande ne peut plus être annulée."
                    .formatted(livraison.getStatut().libelle()));
        }
        lignes.forEach(ligne -> ligne.getProduit().remettreEnStock(ligne.getQuantite()));
        this.statut = StatutCommande.ANNULEE;
        this.motifAnnulation = motif;
        if (livraison != null) {
            livraison.annuler(motif);
        }
    }

    /** Appelé par la livraison quand le colis est remis au client. */
    public void marquerLivree() {
        this.statut = StatutCommande.LIVREE;
    }

    /** Garde les deux côtés de la relation à jour (commande ↔ livraison). */
    public void associerLivraison(Livraison livraison) {
        this.livraison = livraison;
    }

    public Long getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public String getClientNom() {
        return clientNom;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getQuartier() {
        return quartier;
    }

    public String getVille() {
        return ville;
    }

    public String getIndications() {
        return indications;
    }

    public StatutCommande getStatut() {
        return statut;
    }

    public long getTotalFcfa() {
        return totalFcfa;
    }

    public String getMotifAnnulation() {
        return motifAnnulation;
    }

    public Instant getCreeLe() {
        return creeLe;
    }

    public List<LigneCommande> getLignes() {
        return Collections.unmodifiableList(lignes);
    }

    public Livraison getLivraison() {
        return livraison;
    }
}
