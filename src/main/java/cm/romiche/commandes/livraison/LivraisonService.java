package cm.romiche.commandes.livraison;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import cm.romiche.commandes.commun.AccesRefuseException;
import cm.romiche.commandes.commun.RessourceIntrouvableException;
import cm.romiche.commandes.commun.Telephones;
import cm.romiche.commandes.securite.Acteur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LivraisonService {

    /** Ce qu'un livreur peut faire lui-même : partir, livrer, ou signaler un échec. */
    private static final Set<StatutLivraison> STATUTS_AUTORISES_LIVREUR =
            EnumSet.of(StatutLivraison.EN_COURS, StatutLivraison.LIVREE, StatutLivraison.ECHEC);

    /** Ce qu'un livreur voit dans « mes livraisons » : ce qu'il a encore à faire. */
    private static final Set<StatutLivraison> STATUTS_A_FAIRE =
            EnumSet.of(StatutLivraison.ASSIGNEE, StatutLivraison.EN_COURS);

    private final LivraisonRepository livraisons;

    public LivraisonService(LivraisonRepository livraisons) {
        this.livraisons = livraisons;
    }

    @Transactional(readOnly = true)
    public Page<LivraisonReponse> lister(StatutLivraison statut, Pageable pageable) {
        Page<Livraison> page = statut == null ? livraisons.findAll(pageable) : livraisons.findByStatut(statut, pageable);
        return page.map(LivraisonReponse::depuis);
    }

    @Transactional(readOnly = true)
    public Page<LivraisonReponse> miennes(String telephoneLivreur, Pageable pageable) {
        return livraisons.findByLivreurTelephoneAndStatutIn(telephoneLivreur, STATUTS_A_FAIRE, pageable)
                .map(LivraisonReponse::depuis);
    }

    @Transactional(readOnly = true)
    public LivraisonReponse obtenir(Long id) {
        return LivraisonReponse.depuis(trouver(id));
    }

    public LivraisonReponse assigner(Long id, AssignationRequete requete) {
        Livraison livraison = trouver(id);
        livraison.assigner(requete.livreurNom().trim(), Telephones.normaliser(requete.livreurTelephone()));
        return LivraisonReponse.depuis(livraison);
    }

    public LivraisonReponse changerStatut(Long id, ChangementStatutRequete requete, Acteur acteur) {
        Livraison livraison = trouver(id);
        if (!acteur.gerant()) {
            if (!acteur.telephone().equals(livraison.getLivreurTelephone())) {
                throw new AccesRefuseException("Cette livraison n'est pas assignée à votre compte.");
            }
            if (!STATUTS_AUTORISES_LIVREUR.contains(requete.statut())) {
                throw new AccesRefuseException("Un livreur peut seulement passer une livraison à EN_COURS, LIVREE ou ECHEC.");
            }
        }
        livraison.changerStatut(requete.statut(), requete.commentaire());
        return LivraisonReponse.depuis(livraison);
    }

    @Transactional(readOnly = true)
    public List<EvenementReponse> historique(Long id) {
        return trouver(id).getEvenements().stream().map(EvenementReponse::depuis).toList();
    }

    private Livraison trouver(Long id) {
        return livraisons.findById(id).orElseThrow(() -> new RessourceIntrouvableException("Livraison", id));
    }
}
