package cm.romiche.commandes.livraison;

import cm.romiche.commandes.commun.Telephones;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Le livreur à qui l'on confie la livraison. */
public record AssignationRequete(
        @NotBlank(message = "Le nom du livreur est obligatoire")
        @Size(max = 120, message = "120 caractères maximum")
        String livreurNom,

        @NotBlank(message = "Le téléphone du livreur est obligatoire")
        @Pattern(regexp = Telephones.MOTIF, message = Telephones.MESSAGE)
        String livreurTelephone) {
}
