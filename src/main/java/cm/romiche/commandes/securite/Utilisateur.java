package cm.romiche.commandes.securite;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Un compte : gérant ou livreur. Le mot de passe est stocké sous forme d'empreinte BCrypt. */
@Entity
@Table(name = "utilisateur")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nom;

    /** Identifiant de connexion, toujours au format +237XXXXXXXXX. */
    @Column(nullable = false, length = 20, unique = true)
    private String telephone;

    /** Empreinte du mot de passe (ex. « {bcrypt}$2a$10$… »), jamais le mot de passe lui-même. */
    @Column(name = "mot_de_passe", nullable = false, length = 100)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    protected Utilisateur() {
        // Requis par JPA
    }

    public Utilisateur(String nom, String telephone, String empreinteMotDePasse, Role role) {
        this.nom = nom;
        this.telephone = telephone;
        this.motDePasse = empreinteMotDePasse;
        this.role = role;
        this.creeLe = Instant.now();
    }

    public void desactiver() {
        this.actif = false;
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActif() {
        return actif;
    }

    public Instant getCreeLe() {
        return creeLe;
    }
}
