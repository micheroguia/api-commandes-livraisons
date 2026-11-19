package cm.romiche.commandes.produit;

import cm.romiche.commandes.commun.RessourceIntrouvableException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProduitService {

    private final ProduitRepository produits;

    public ProduitService(ProduitRepository produits) {
        this.produits = produits;
    }

    @Transactional(readOnly = true)
    public Page<ProduitReponse> lister(boolean actifsSeulement, Pageable pageable) {
        Page<Produit> page = actifsSeulement ? produits.findByActifTrue(pageable) : produits.findAll(pageable);
        return page.map(ProduitReponse::depuis);
    }

    @Transactional(readOnly = true)
    public ProduitReponse obtenir(Long id) {
        return ProduitReponse.depuis(trouver(id));
    }

    public ProduitReponse creer(ProduitRequete requete) {
        Produit produit = new Produit(requete.nom().trim(), requete.description(), requete.prixFcfa(), requete.stock());
        return ProduitReponse.depuis(produits.save(produit));
    }

    public ProduitReponse modifier(Long id, ProduitRequete requete) {
        Produit produit = trouver(id);
        produit.modifier(requete.nom().trim(), requete.description(), requete.prixFcfa(), requete.stock());
        return ProduitReponse.depuis(produit);
    }

    public void desactiver(Long id) {
        trouver(id).desactiver();
    }

    /** Utilisé aussi par les commandes pour récupérer un produit ou renvoyer une 404. */
    public Produit trouver(Long id) {
        return produits.findById(id).orElseThrow(() -> new RessourceIntrouvableException("Produit", id));
    }
}
