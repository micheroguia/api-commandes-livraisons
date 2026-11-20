package cm.romiche.commandes.commun;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Informations affichées en haut de la page Swagger (/swagger-ui.html),
 * avec le bouton « Authorize » pour coller son jeton JWT.
 */
@Configuration
public class DocumentationApi {

    private static final String JETON = "jeton";

    @Bean
    OpenAPI documentation() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Commandes & livraisons")
                        .version("0.2.0")
                        .description("""
                                Gestion des produits, des commandes et du suivi des livraisons, pensée pour le Cameroun :
                                montants en FCFA, téléphones au format +237, adresses par quartier et par ville.

                                Pour essayer : POST /api/auth/connexion, copiez le « jeton », \
                                cliquez sur « Authorize » et collez-le.
                                """)
                        .contact(new Contact()
                                .name("Romiche GUIAGAIN DJOUGUELA")
                                .url("https://portfolio-romiche.vercel.app")))
                .components(new Components().addSecuritySchemes(JETON, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(JETON));
    }
}
