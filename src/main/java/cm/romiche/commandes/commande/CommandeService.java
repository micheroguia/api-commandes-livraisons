package cm.romiche.commandes.commande;

import java.util.LinkedHashMap;
import java.util.Map;

import cm.romiche.commandes.commun.RessourceIntrouvableException;
import cm.romiche.commandes.commun.Telephones;
import cm.romiche.commandes.livraison.Livraison;
import cm.romiche.commandes.livraison.LivraisonRepository;
import cm.romiche.commandes.produit.ProduitService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestration des commandes. Chaque méthode publique est une transaction :
 * si une règle échoue au milieu (stock insuffisant…), RIEN n'est enregistré.
 */
@Service
@Transactional
public class CommandeService {

    private final CommandeRepository commandes;
    private final LivraisonRepository livraisons;
    private final ProduitService produits;

    public CommandeService(CommandeRepository commandes, LivraisonRepository livraisons, ProduitService produits) {
        this.commandes = commandes;
        this.livraisons = livraisons;
        this.produits = produits;
    }

    public CommandeReponse creer(CommandeRequete requete) {
        Commande commande = new Commande(
                requete.clientNom().trim(),
                Telephones.normaliser(requete.telephone()),
                requete.quartier().trim(),
                requete.ville().trim(),
                requete.indications());

        // Si le même produit apparaît sur deux lignes, on additionne les quantités.
        Map<Long, Integer> quantitesParProduit = new LinkedHashMap<>();
        requete.lignes().forEach(ligne -> quantitesParProduit.merge(ligne.produitId(), ligne.quantite(), Integer::sum));
        quantitesParProduit.forEach((produitId, quantite) -> commande.ajouterLigne(produits.trouver(produitId), quantite));

        commandes.save(commande);
        commande.attribuerReference();
        return CommandeReponse.depuis(commande);
    }

    /** Confirme la commande et crée sa livraison, à planifier. */
    public CommandeReponse confirmer(Long id) {
        Commande commande = trouver(id);
        commande.confirmer();
        livraisons.save(new Livraison(commande));
        return CommandeReponse.depuis(commande);
    }

    public CommandeReponse annuler(Long id, String motif) {
        Commande commande = trouver(id);
        commande.annuler(motif);
        return CommandeReponse.depuis(commande);
    }

    @Transactional(readOnly = true)
    public Page<CommandeReponse> lister(StatutCommande statut, Pageable pageable) {
        Page<Commande> page = statut == null ? commandes.findAll(pageable) : commandes.findByStatut(statut, pageable);
        return page.map(CommandeReponse::depuis);
    }

    @Transactional(readOnly = true)
    public CommandeReponse obtenir(Long id) {
        return CommandeReponse.depuis(trouver(id));
    }

    private Commande trouver(Long id) {
        return commandes.findById(id).orElseThrow(() -> new RessourceIntrouvableException("Commande", id));
    }
}
