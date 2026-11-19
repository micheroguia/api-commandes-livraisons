package cm.romiche.commandes.livraison;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LivraisonRepository extends JpaRepository<Livraison, Long> {

    Page<Livraison> findByStatut(StatutLivraison statut, Pageable pageable);
}
