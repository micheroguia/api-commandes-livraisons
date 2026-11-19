package cm.romiche.commandes.commande;

import java.util.List;

import cm.romiche.commandes.commun.Telephones;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Données envoyées pour passer une commande. */
public record CommandeRequete(
        @NotBlank(message = "Le nom du client est obligatoire")
        @Size(max = 120, message = "120 caractères maximum")
        String clientNom,

        @NotBlank(message = "Le téléphone est obligatoire")
        @Pattern(regexp = Telephones.MOTIF, message = Telephones.MESSAGE)
        String telephone,

        @NotBlank(message = "Le quartier est obligatoire (ex. Bonamoussadi)")
        @Size(max = 120, message = "120 caractères maximum")
        String quartier,

        @NotBlank(message = "La ville est obligatoire (ex. Douala)")
        @Size(max = 80, message = "80 caractères maximum")
        String ville,

        @Size(max = 500, message = "500 caractères maximum")
        String indications,

        @NotEmpty(message = "Une commande doit contenir au moins un produit")
        List<@Valid @NotNull LigneRequete> lignes) {

    public record LigneRequete(
            @NotNull(message = "Le produit est obligatoire")
            Long produitId,

            @NotNull(message = "La quantité est obligatoire")
            @Min(value = 1, message = "Au moins 1")
            @Max(value = 1000, message = "1000 maximum par ligne")
            Integer quantite) {
    }
}
