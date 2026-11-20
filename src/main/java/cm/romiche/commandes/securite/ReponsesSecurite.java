package cm.romiche.commandes.securite;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Réponses JSON en français quand Spring Security bloque une requête,
 * au même format que les autres erreurs de l'API (Problem Details).
 */
@Component
public class ReponsesSecurite implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper json;

    public ReponsesSecurite(ObjectMapper json) {
        this.json = json;
    }

    /** 401 : pas de jeton, jeton invalide ou expiré. */
    @Override
    public void commence(HttpServletRequest requete, HttpServletResponse reponse, AuthenticationException ex)
            throws IOException {
        reponse.setHeader("WWW-Authenticate", "Bearer");
        ecrire(reponse, HttpStatus.UNAUTHORIZED, "Authentification requise",
                "Connectez-vous avec POST /api/auth/connexion, puis envoyez le jeton dans l'en-tête "
                        + "« Authorization: Bearer <jeton> ». Un jeton expiré doit être renouvelé.");
    }

    /** 403 : connecté, mais le rôle ne permet pas cette action. */
    @Override
    public void handle(HttpServletRequest requete, HttpServletResponse reponse, AccessDeniedException ex)
            throws IOException {
        ecrire(reponse, HttpStatus.FORBIDDEN, "Accès refusé", "Votre rôle ne permet pas cette action.");
    }

    private void ecrire(HttpServletResponse reponse, HttpStatus statut, String titre, String detail)
            throws IOException {
        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(statut, detail);
        probleme.setTitle(titre);
        reponse.setStatus(statut.value());
        reponse.setCharacterEncoding("UTF-8");
        reponse.setContentType("application/problem+json");
        json.writeValue(reponse.getOutputStream(), probleme);
    }
}
