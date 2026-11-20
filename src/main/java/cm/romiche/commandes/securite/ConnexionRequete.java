package cm.romiche.commandes.securite;

import jakarta.validation.constraints.NotBlank;

public record ConnexionRequete(
        @NotBlank(message = "Le téléphone est obligatoire")
        String telephone,

        @NotBlank(message = "Le mot de passe est obligatoire")
        String motDePasse) {
}
