package cm.romiche.commandes.securite;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Réglages des jetons, lus dans application.yml (app.jwt.*).
 *
 * @param secret clé de signature HS256 : 32 caractères minimum
 * @param duree  durée de validité d'un jeton
 */
@ConfigurationProperties("app.jwt")
public record ProprietesJwt(String secret, @DefaultValue("8h") Duration duree) {
}
