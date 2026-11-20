package cm.romiche.commandes.securite;

import java.net.URI;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Gestion des comptes : réservée aux gérants (voir ConfigurationSecurite). */
@RestController
@RequestMapping("/api/utilisateurs")
@Tag(name = "Comptes", description = "Création des comptes gérants et livreurs")
public class UtilisateurController {

    private final UtilisateurService service;

    public UtilisateurController(UtilisateurService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Créer un compte (gérant ou livreur)")
    public ResponseEntity<UtilisateurReponse> creer(@Valid @RequestBody UtilisateurRequete requete) {
        UtilisateurReponse cree = service.creer(requete);
        return ResponseEntity.created(URI.create("/api/utilisateurs/" + cree.id())).body(cree);
    }

    @GetMapping
    @Operation(summary = "Lister les comptes")
    public Page<UtilisateurReponse> lister(@ParameterObject @PageableDefault(size = 20, sort = "nom") Pageable pageable) {
        return service.lister(pageable);
    }

    @PostMapping("/{id}/desactiver")
    @Operation(summary = "Désactiver un compte (il ne pourra plus se connecter)")
    public ResponseEntity<Void> desactiver(@PathVariable Long id, Authentication authentification) {
        service.desactiver(id, Acteur.depuis(authentification));
        return ResponseEntity.noContent().build();
    }
}
