package cm.romiche.commandes.livraison;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Cycle de vie d'une livraison. Les passages autorisés sont écrits UNE seule fois ici :
 * À planifier → Assignée → En cours → Livrée
 *                                   → Échec → À planifier (nouvelle tentative)
 * L'annulation est possible tant que le livreur n'est pas en route.
 */
public enum StatutLivraison {
    A_PLANIFIER("À planifier"),
    ASSIGNEE("Assignée"),
    EN_COURS("En cours"),
    LIVREE("Livrée"),
    ECHEC("Échec"),
    ANNULEE("Annulée");

    private static final Map<StatutLivraison, Set<StatutLivraison>> TRANSITIONS = new EnumMap<>(StatutLivraison.class);

    static {
        TRANSITIONS.put(A_PLANIFIER, EnumSet.of(ASSIGNEE, ANNULEE));
        TRANSITIONS.put(ASSIGNEE, EnumSet.of(EN_COURS, A_PLANIFIER, ANNULEE));
        TRANSITIONS.put(EN_COURS, EnumSet.of(LIVREE, ECHEC));
        TRANSITIONS.put(ECHEC, EnumSet.of(A_PLANIFIER, ANNULEE));
        TRANSITIONS.put(LIVREE, EnumSet.noneOf(StatutLivraison.class));
        TRANSITIONS.put(ANNULEE, EnumSet.noneOf(StatutLivraison.class));
    }

    private final String libelle;

    StatutLivraison(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }

    public boolean peutPasserA(StatutLivraison suivant) {
        return TRANSITIONS.get(this).contains(suivant);
    }

    public boolean estTerminal() {
        return TRANSITIONS.get(this).isEmpty();
    }
}
