package cm.romiche.commandes.produit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Données envoyées pour créer ou modifier un produit. */
public record ProduitRequete(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 120, message = "120 caractères maximum")
        String nom,

        @Size(max = 1000, message = "1000 caractères maximum")
        String description,

        @NotNull(message = "Le prix est obligatoire")
        @Positive(message = "Le prix doit être supérieur à 0 FCFA")
        Long prixFcfa,

        @NotNull(message = "Le stock est obligatoire")
        @PositiveOrZero(message = "Le stock ne peut pas être négatif")
        Integer stock) {
}
