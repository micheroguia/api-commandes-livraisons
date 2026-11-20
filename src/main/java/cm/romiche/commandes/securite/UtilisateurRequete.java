package cm.romiche.commandes.securite;

import cm.romiche.commandes.commun.Telephones;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Création d'un compte par un gérant. */
public record UtilisateurRequete(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 120, message = "120 caractères maximum")
        String nom,

        @NotBlank(message = "Le téléphone est obligatoire")
        @Pattern(regexp = Telephones.MOTIF, message = Telephones.MESSAGE)
        String telephone,

        // 72 caractères maximum : au-delà, BCrypt ignore la fin du mot de passe.
        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, max = 72, message = "Entre 8 et 72 caractères")
        String motDePasse,

        @NotNull(message = "Le rôle est obligatoire (GERANT ou LIVREUR)")
        Role role) {
}
