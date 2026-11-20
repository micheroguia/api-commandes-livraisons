package cm.romiche.commandes.securite;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Connexion", description = "Obtenir un jeton et voir son compte")
public class AuthController {

    private final AuthService authService;
    private final UtilisateurService utilisateurService;

    public AuthController(AuthService authService, UtilisateurService utilisateurService) {
        this.authService = authService;
        this.utilisateurService = utilisateurService;
    }

    @PostMapping("/connexion")
    @SecurityRequirements // pas de jeton demandé pour se connecter
    @Operation(summary = "Se connecter avec son téléphone et son mot de passe : renvoie un jeton JWT")
    public JetonReponse connecter(@Valid @RequestBody ConnexionRequete requete) {
        return authService.connecter(requete);
    }

    @GetMapping("/moi")
    @Operation(summary = "Voir le compte de la personne connectée")
    public UtilisateurReponse moi(Authentication authentification) {
        return utilisateurService.moi(authentification.getName());
    }
}
