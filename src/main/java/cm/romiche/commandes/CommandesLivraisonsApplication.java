package cm.romiche.commandes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

/**
 * Point d'entrée de l'API Commandes &amp; livraisons.
 * VIA_DTO : les pages de résultats ont un format JSON stable (content + page).
 */
@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class CommandesLivraisonsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommandesLivraisonsApplication.class, args);
    }
}
