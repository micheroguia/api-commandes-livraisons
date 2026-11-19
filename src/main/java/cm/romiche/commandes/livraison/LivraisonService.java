package cm.romiche.commandes.livraison;

import java.util.List;

import cm.romiche.commandes.commun.RessourceIntrouvableException;
import cm.romiche.commandes.commun.Telephones;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LivraisonService {

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
    public LivraisonReponse obtenir(Long id) {
        return LivraisonReponse.depuis(trouver(id));
    }

    public LivraisonReponse assigner(Long id, AssignationRequete requete) {
        Livraison livraison = trouver(id);
        livraison.assigner(requete.livreurNom().trim(), Telephones.normaliser(requete.livreurTelephone()));
        return LivraisonReponse.depuis(livraison);
    }

    public LivraisonReponse changerStatut(Long id, ChangementStatutRequete requete) {
        Livraison livraison = trouver(id);
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
