package cm.romiche.commandes.livraison;

import static cm.romiche.commandes.livraison.StatutLivraison.A_PLANIFIER;
import static cm.romiche.commandes.livraison.StatutLivraison.ANNULEE;
import static cm.romiche.commandes.livraison.StatutLivraison.ASSIGNEE;
import static cm.romiche.commandes.livraison.StatutLivraison.ECHEC;
import static cm.romiche.commandes.livraison.StatutLivraison.EN_COURS;
import static cm.romiche.commandes.livraison.StatutLivraison.LIVREE;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class StatutLivraisonTest {

    @Test
    @DisplayName("Le parcours normal est autorisé : à planifier → assignée → en cours → livrée")
    void parcoursNormal() {
        assertThat(A_PLANIFIER.peutPasserA(ASSIGNEE)).isTrue();
        assertThat(ASSIGNEE.peutPasserA(EN_COURS)).isTrue();
        assertThat(EN_COURS.peutPasserA(LIVREE)).isTrue();
    }

    @Test
    @DisplayName("Après un échec, on peut replanifier la livraison")
    void replanifierApresEchec() {
        assertThat(EN_COURS.peutPasserA(ECHEC)).isTrue();
        assertThat(ECHEC.peutPasserA(A_PLANIFIER)).isTrue();
    }

    @Test
    @DisplayName("On ne saute pas d'étape et on n'annule pas un livreur déjà en route")
    void passagesInterdits() {
        assertThat(A_PLANIFIER.peutPasserA(LIVREE)).isFalse();
        assertThat(A_PLANIFIER.peutPasserA(EN_COURS)).isFalse();
        assertThat(EN_COURS.peutPasserA(ANNULEE)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(names = {"LIVREE", "ANNULEE"})
    @DisplayName("Livrée et annulée sont des statuts définitifs")
    void statutsDefinitifs(StatutLivraison statut) {
        assertThat(statut.estTerminal()).isTrue();
        for (StatutLivraison autre : StatutLivraison.values()) {
            assertThat(statut.peutPasserA(autre)).isFalse();
        }
    }
}
