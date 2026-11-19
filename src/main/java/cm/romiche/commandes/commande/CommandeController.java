package cm.romiche.commandes.commande;

import java.net.URI;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/commandes")
@Tag(name = "Commandes", description = "Prise de commande, confirmation et annulation")
public class CommandeController {

    private final CommandeService service;

    public CommandeController(CommandeService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Passer une commande (le stock est réservé immédiatement)")
    public ResponseEntity<CommandeReponse> creer(@Valid @RequestBody CommandeRequete requete) {
        CommandeReponse creee = service.creer(requete);
        return ResponseEntity.created(URI.create("/api/commandes/" + creee.id())).body(creee);
    }

    @GetMapping
    @Operation(summary = "Lister les commandes, les plus récentes d'abord (filtre facultatif par statut)")
    public Page<CommandeReponse> lister(
            @RequestParam(required = false) StatutCommande statut,
            @ParameterObject @PageableDefault(size = 20, sort = "creeLe", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return service.lister(statut, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Voir une commande et ses lignes")
    public CommandeReponse obtenir(@PathVariable Long id) {
        return service.obtenir(id);
    }

    @PostMapping("/{id}/confirmer")
    @Operation(summary = "Confirmer une commande en attente : sa livraison est créée")
    public CommandeReponse confirmer(@PathVariable Long id) {
        return service.confirmer(id);
    }

    @PostMapping("/{id}/annuler")
    @Operation(summary = "Annuler une commande (tant que le livreur n'est pas en route) : le stock est rendu")
    public CommandeReponse annuler(@PathVariable Long id,
            @Valid @RequestBody(required = false) AnnulationRequete requete) {
        return service.annuler(id, requete == null ? null : requete.motif());
    }
}
