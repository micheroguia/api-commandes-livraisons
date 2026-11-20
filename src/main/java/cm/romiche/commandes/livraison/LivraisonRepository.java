package cm.romiche.commandes.livraison;

import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LivraisonRepository extends JpaRepository<Livraison, Long> {

    Page<Livraison> findByStatut(StatutLivraison statut, Pageable pageable);

    /** Les livraisons d'un livreur (repéré par son téléphone) dans certains statuts. */
    Page<Livraison> findByLivreurTelephoneAndStatutIn(String livreurTelephone, Collection<StatutLivraison> statuts,
            Pageable pageable);
}
