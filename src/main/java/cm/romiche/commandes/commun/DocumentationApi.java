package cm.romiche.commandes.commun;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Informations affichées en haut de la page Swagger (/swagger-ui.html). */
@Configuration
public class DocumentationApi {

    @Bean
    OpenAPI documentation() {
        return new OpenAPI().info(new Info()
                .title("API Commandes & livraisons")
                .version("0.1.0")
                .description("""
                        Gestion des produits, des commandes et du suivi des livraisons, pensée pour le Cameroun :
                        montants en FCFA, téléphones au format +237, adresses par quartier et par ville.
                        """)
                .contact(new Contact()
                        .name("Romiche GUIAGAIN DJOUGUELA")
                        .url("https://portfolio-romiche.vercel.app")));
    }
}
