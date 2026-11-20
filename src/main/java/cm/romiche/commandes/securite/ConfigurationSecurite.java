package cm.romiche.commandes.securite;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Qui a le droit de faire quoi.
 *
 * Public          : connexion, catalogue en lecture, documentation Swagger.
 * Livreur         : ses livraisons, et les faire avancer (en route, livrée, échec).
 * Gérant          : tout le reste (produits, commandes, livraisons, comptes).
 *
 * Pas de session ni de cookie : chaque requête porte son jeton (« Authorization: Bearer … »).
 */
@Configuration
@EnableWebSecurity
public class ConfigurationSecurite {

    /** Émetteur écrit dans chaque jeton, et vérifié à chaque requête. */
    public static final String EMETTEUR = "api-commandes-livraisons";

    @Bean
    SecurityFilterChain filtresDeSecurite(HttpSecurity http, ReponsesSecurite reponses,
            JwtAuthenticationConverter convertisseur,
            @Value("${spring.h2.console.enabled:false}") boolean consoleH2) throws Exception {

        http.csrf(csrf -> csrf.disable()) // API sans cookie : la protection CSRF ne sert pas ici
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(regles -> {
                    if (consoleH2) {
                        regles.requestMatchers(PathRequest.toH2Console()).permitAll();
                    }
                    regles.requestMatchers("/error", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/auth/connexion").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/produits", "/api/produits/**").permitAll()
                            .requestMatchers("/api/auth/moi").authenticated()
                            .requestMatchers(HttpMethod.GET, "/api/livraisons/miennes").hasRole("LIVREUR")
                            .requestMatchers(HttpMethod.POST, "/api/livraisons/*/statut").hasAnyRole("GERANT", "LIVREUR")
                            .anyRequest().hasRole("GERANT");
                })
                .oauth2ResourceServer(serveur -> serveur
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(convertisseur))
                        .authenticationEntryPoint(reponses))
                .exceptionHandling(erreurs -> erreurs
                        .authenticationEntryPoint(reponses)
                        .accessDeniedHandler(reponses));

        if (consoleH2) {
            // La console H2 (profil demo uniquement) s'affiche dans des cadres de la même origine.
            http.headers(entetes -> entetes.frameOptions(cadres -> cadres.sameOrigin()));
        }
        return http.build();
    }

    /** Le rôle écrit dans le jeton (« role »: « GERANT ») devient l'autorité ROLE_GERANT. */
    @Bean
    JwtAuthenticationConverter convertisseurJwt() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("role");
        roles.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter convertisseur = new JwtAuthenticationConverter();
        convertisseur.setJwtGrantedAuthoritiesConverter(roles);
        return convertisseur;
    }

    /** Clé secrète partagée (HS256). L'application refuse de démarrer si elle manque ou est trop courte. */
    @Bean
    SecretKey cleDeSignature(ProprietesJwt proprietes) {
        String secret = proprietes.secret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "Clé JWT absente ou trop courte : définissez la variable d'environnement JWT_SECRET "
                            + "(32 caractères minimum).");
        }
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey cle) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(cle));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey cle) {
        NimbusJwtDecoder decodeur = NimbusJwtDecoder.withSecretKey(cle).macAlgorithm(MacAlgorithm.HS256).build();
        // Vérifie la signature, la date d'expiration ET l'émetteur.
        decodeur.setJwtValidator(JwtValidators.createDefaultWithIssuer(EMETTEUR));
        return decodeur;
    }

    /** BCrypt par défaut ; le préfixe {bcrypt} permettra de changer d'algorithme plus tard sans tout casser. */
    @Bean
    PasswordEncoder encodeurMotsDePasse() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
