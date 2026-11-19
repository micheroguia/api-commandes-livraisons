package cm.romiche.commandes.commande;

public enum StatutCommande {
    EN_ATTENTE("En attente de confirmation"),
    CONFIRMEE("Confirmée"),
    LIVREE("Livrée"),
    ANNULEE("Annulée");

    private final String libelle;

    StatutCommande(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
