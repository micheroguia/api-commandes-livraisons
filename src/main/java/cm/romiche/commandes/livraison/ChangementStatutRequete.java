package cm.romiche.commandes.livraison;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Nouveau statut d'une livraison, avec un commentaire facultatif (« client absent »…). */
public record ChangementStatutRequete(
        @NotNull(message = "Le statut est obligatoire")
        StatutLivraison statut,

        @Size(max = 300, message = "300 caractères maximum")
        String commentaire) {
}
