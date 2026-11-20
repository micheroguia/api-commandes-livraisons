package cm.romiche.commandes.commun;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Transforme les erreurs en réponses JSON claires, au format standard « Problem Details » (RFC 9457).
 * Exemple : { "title": "Règle métier non respectée", "status": 409, "detail": "Stock insuffisant…" }
 */
@RestControllerAdvice
public class GestionnaireErreurs extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ProblemDetail introuvable(RessourceIntrouvableException ex) {
        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        probleme.setTitle("Ressource introuvable");
        return probleme;
    }

    @ExceptionHandler(RegleMetierException.class)
    public ProblemDetail regleMetier(RegleMetierException ex) {
        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        probleme.setTitle("Règle métier non respectée");
        return probleme;
    }

    @ExceptionHandler(IdentifiantsInvalidesException.class)
    public ProblemDetail identifiantsInvalides(IdentifiantsInvalidesException ex) {
        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        probleme.setTitle("Connexion refusée");
        return probleme;
    }

    @ExceptionHandler(AccesRefuseException.class)
    public ProblemDetail accesRefuse(AccesRefuseException ex) {
        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        probleme.setTitle("Accès refusé");
        return probleme;
    }

    /** Deux personnes ont modifié le même produit au même moment (verrou optimiste). */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail conflit(ObjectOptimisticLockingFailureException ex) {
        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Les données ont été modifiées en même temps par quelqu'un d'autre : réessayez.");
        probleme.setTitle("Conflit de mise à jour");
        return probleme;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail argumentInvalide(IllegalArgumentException ex) {
        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        probleme.setTitle("Données invalides");
        return probleme;
    }

    /** Champs invalides (@NotBlank, @Pattern…) : on renvoie la liste champ par champ. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> erreurs = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(erreur -> erreurs.putIfAbsent(erreur.getField(), erreur.getDefaultMessage()));

        ProblemDetail probleme = ex.getBody();
        probleme.setTitle("Données invalides");
        probleme.setDetail("Certains champs sont invalides : voir « erreurs ».");
        probleme.setProperty("erreurs", erreurs);
        return handleExceptionInternal(ex, probleme, headers, status, request);
    }
}
