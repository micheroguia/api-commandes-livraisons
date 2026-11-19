package cm.romiche.commandes.livraison;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cm.romiche.commandes.commande.Commande;
import cm.romiche.commandes.commun.RegleMetierException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * La livraison d'une commande confirmée. Chaque changement de statut est
 * contrôlé par StatutLivraison et ajouté à l'historique (evenements).
 */
@Entity
@Table(name = "livraison")
public class Livraison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commande_id", nullable = false, unique = true)
    private Commande commande;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutLivraison statut;

    @Column(name = "livreur_nom", length = 120)
    private String livreurNom;

    @Column(name = "livreur_telephone", length = 20)
    private String livreurTelephone;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @Column(name = "maj_le", nullable = false)
    private Instant majLe;

    @OneToMany(mappedBy = "livraison", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dateEvenement ASC, id ASC")
    private List<EvenementLivraison> evenements = new ArrayList<>();

    protected Livraison() {
        // Requis par JPA
    }

    public Livraison(Commande commande) {
        this.commande = commande;
        this.statut = StatutLivraison.A_PLANIFIER;
        this.creeLe = Instant.now();
        commande.associerLivraison(this);
        enregistrer(StatutLivraison.A_PLANIFIER, "Commande confirmée, livraison à planifier");
    }

    /** Confie la livraison à un livreur (ou le remplace s'il était déjà assigné). */
    public void assigner(String nom, String telephone) {
        if (statut != StatutLivraison.A_PLANIFIER && statut != StatutLivraison.ASSIGNEE) {
            throw new RegleMetierException("Impossible d'assigner un livreur : la livraison est « %s »."
                    .formatted(statut.libelle()));
        }
        boolean remplacement = statut == StatutLivraison.ASSIGNEE;
        this.livreurNom = nom;
        this.livreurTelephone = telephone;
        this.statut = StatutLivraison.ASSIGNEE;
        enregistrer(StatutLivraison.ASSIGNEE, (remplacement ? "Livreur remplacé : " : "Livreur assigné : ") + nom);
    }

    public void changerStatut(StatutLivraison suivant, String commentaire) {
        if (suivant == StatutLivraison.ASSIGNEE) {
            throw new RegleMetierException("Pour assigner un livreur, utilisez POST /api/livraisons/{id}/assigner.");
        }
        if (suivant == StatutLivraison.ANNULEE) {
            throw new RegleMetierException("Pour annuler, annulez la commande : POST /api/commandes/{id}/annuler.");
        }
        if (!statut.peutPasserA(suivant)) {
            throw new RegleMetierException("Passage impossible de « %s » à « %s »."
                    .formatted(statut.libelle(), suivant.libelle()));
        }
        if (suivant == StatutLivraison.A_PLANIFIER) {
            // Retour à la planification : le livreur précédent est libéré.
            this.livreurNom = null;
            this.livreurTelephone = null;
        }
        this.statut = suivant;
        enregistrer(suivant, commentaire);
        if (suivant == StatutLivraison.LIVREE) {
            commande.marquerLivree();
        }
    }

    /** On peut encore annuler tant que le livreur n'est pas en route. */
    public boolean peutEtreAnnulee() {
        return statut.peutPasserA(StatutLivraison.ANNULEE);
    }

    /** Appelé uniquement par Commande.annuler(), qui remet aussi le stock. */
    public void annuler(String motif) {
        this.statut = StatutLivraison.ANNULEE;
        enregistrer(StatutLivraison.ANNULEE, motif == null ? "Commande annulée" : "Commande annulée : " + motif);
    }

    private void enregistrer(StatutLivraison nouveauStatut, String commentaire) {
        this.majLe = Instant.now();
        evenements.add(new EvenementLivraison(this, nouveauStatut, commentaire, majLe));
    }

    public Long getId() {
        return id;
    }

    public Commande getCommande() {
        return commande;
    }

    public StatutLivraison getStatut() {
        return statut;
    }

    public String getLivreurNom() {
        return livreurNom;
    }

    public String getLivreurTelephone() {
        return livreurTelephone;
    }

    public Instant getCreeLe() {
        return creeLe;
    }

    public Instant getMajLe() {
        return majLe;
    }

    public List<EvenementLivraison> getEvenements() {
        return Collections.unmodifiableList(evenements);
    }
}
