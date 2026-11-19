package cm.romiche.commandes.produit;

import java.net.URI;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/produits")
@Tag(name = "Produits", description = "Catalogue des produits vendus")
public class ProduitController {

    private final ProduitService service;

    public ProduitController(ProduitService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lister les produits (par pages)")
    public Page<ProduitReponse> lister(
            @RequestParam(defaultValue = "true") boolean actifsSeulement,
            @ParameterObject @PageableDefault(size = 20, sort = "nom") Pageable pageable) {
        return service.lister(actifsSeulement, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Voir un produit")
    public ProduitReponse obtenir(@PathVariable Long id) {
        return service.obtenir(id);
    }

    @PostMapping
    @Operation(summary = "Ajouter un produit au catalogue")
    public ResponseEntity<ProduitReponse> creer(@Valid @RequestBody ProduitRequete requete) {
        ProduitReponse cree = service.creer(requete);
        return ResponseEntity.created(URI.create("/api/produits/" + cree.id())).body(cree);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un produit (nom, prix, stock)")
    public ProduitReponse modifier(@PathVariable Long id, @Valid @RequestBody ProduitRequete requete) {
        return service.modifier(id, requete);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Retirer un produit du catalogue (il reste dans l'historique des commandes)")
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        service.desactiver(id);
        return ResponseEntity.noContent().build();
    }
}
