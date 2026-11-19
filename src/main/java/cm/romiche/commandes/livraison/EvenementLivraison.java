package cm.romiche.commandes.livraison;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Une ligne de l'historique d'une livraison : quel statut, quand, et pourquoi. */
@Entity
@Table(name = "evenement_livraison")
public class EvenementLivraison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "livraison_id", nullable = false)
    private Livraison livraison;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutLivraison statut;

    @Column(length = 300)
    private String commentaire;

    @Column(name = "date_evenement", nullable = false)
    private Instant dateEvenement;

    protected EvenementLivraison() {
        // Requis par JPA
    }

    EvenementLivraison(Livraison livraison, StatutLivraison statut, String commentaire, Instant dateEvenement) {
        this.livraison = livraison;
        this.statut = statut;
        this.commentaire = commentaire;
        this.dateEvenement = dateEvenement;
    }

    public StatutLivraison getStatut() {
        return statut;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public Instant getDateEvenement() {
        return dateEvenement;
    }
}
