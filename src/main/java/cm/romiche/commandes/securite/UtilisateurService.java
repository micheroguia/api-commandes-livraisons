package cm.romiche.commandes.securite;

import cm.romiche.commandes.commun.IdentifiantsInvalidesException;
import cm.romiche.commandes.commun.RegleMetierException;
import cm.romiche.commandes.commun.RessourceIntrouvableException;
import cm.romiche.commandes.commun.Telephones;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UtilisateurService {

    private final UtilisateurRepository utilisateurs;
    private final PasswordEncoder encodeur;

    public UtilisateurService(UtilisateurRepository utilisateurs, PasswordEncoder encodeur) {
        this.utilisateurs = utilisateurs;
        this.encodeur = encodeur;
    }

    public UtilisateurReponse creer(UtilisateurRequete requete) {
        String telephone = Telephones.normaliser(requete.telephone());
        if (utilisateurs.existsByTelephone(telephone)) {
            throw new RegleMetierException("Ce numéro de téléphone a déjà un compte.");
        }
        Utilisateur utilisateur = new Utilisateur(requete.nom().trim(), telephone,
                encodeur.encode(requete.motDePasse()), requete.role());
        return UtilisateurReponse.depuis(utilisateurs.save(utilisateur));
    }

    @Transactional(readOnly = true)
    public Page<UtilisateurReponse> lister(Pageable pageable) {
        return utilisateurs.findAll(pageable).map(UtilisateurReponse::depuis);
    }

    public void desactiver(Long id, Acteur acteur) {
        Utilisateur utilisateur = utilisateurs.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Compte", id));
        if (utilisateur.getTelephone().equals(acteur.telephone())) {
            throw new RegleMetierException("Vous ne pouvez pas désactiver votre propre compte.");
        }
        utilisateur.desactiver();
    }

    /** Le compte de la personne connectée (lu à partir du téléphone contenu dans son jeton). */
    @Transactional(readOnly = true)
    public UtilisateurReponse moi(String telephone) {
        return utilisateurs.findByTelephone(telephone)
                .map(UtilisateurReponse::depuis)
                .orElseThrow(IdentifiantsInvalidesException::new);
    }
}
