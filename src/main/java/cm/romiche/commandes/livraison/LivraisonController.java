package cm.romiche.commandes.livraison;

import java.util.List;

import cm.romiche.commandes.securite.Acteur;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/livraisons")
@Tag(name = "Livraisons", description = "Planification et suivi des livraisons")
public class LivraisonController {

    private final LivraisonService service;

    public LivraisonController(LivraisonService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lister les livraisons (filtre facultatif par statut, ex. A_PLANIFIER)")
    public Page<LivraisonReponse> lister(
            @RequestParam(required = false) StatutLivraison statut,
            @ParameterObject @PageableDefault(size = 20, sort = "majLe", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return service.lister(statut, pageable);
    }

    @GetMapping("/miennes")
    @Operation(summary = "Livreur : mes livraisons à faire (assignées ou en cours)")
    public Page<LivraisonReponse> miennes(Authentication authentification,
            @ParameterObject @PageableDefault(size = 20, sort = "majLe") Pageable pageable) {
        return service.miennes(authentification.getName(), pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Voir une livraison et l'adresse du client")
    public LivraisonReponse obtenir(@PathVariable Long id) {
        return service.obtenir(id);
    }

    @PostMapping("/{id}/assigner")
    @Operation(summary = "Confier la livraison à un livreur (ou le remplacer)")
    public LivraisonReponse assigner(@PathVariable Long id, @Valid @RequestBody AssignationRequete requete) {
        return service.assigner(id, requete);
    }

    @PostMapping("/{id}/statut")
    @Operation(summary = "Faire avancer la livraison : EN_COURS, LIVREE, ECHEC (livreur ou gérant) "
            + "ou retour à A_PLANIFIER (gérant)")
    public LivraisonReponse changerStatut(@PathVariable Long id, @Valid @RequestBody ChangementStatutRequete requete,
            Authentication authentification) {
        return service.changerStatut(id, requete, Acteur.depuis(authentification));
    }

    @GetMapping("/{id}/historique")
    @Operation(summary = "Historique complet de la livraison, du plus ancien au plus récent")
    public List<EvenementReponse> historique(@PathVariable Long id) {
        return service.historique(id);
    }
}
