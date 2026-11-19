package cm.romiche.commandes.commande;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandeRepository extends JpaRepository<Commande, Long> {

    Page<Commande> findByStatut(StatutCommande statut, Pageable pageable);
}
