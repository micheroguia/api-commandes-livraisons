package cm.romiche.commandes.commande;

import jakarta.validation.constraints.Size;

/** Motif facultatif d'une annulation (« client injoignable », « erreur de produit »…). */
public record AnnulationRequete(
        @Size(max = 300, message = "300 caractères maximum")
        String motif) {
}
