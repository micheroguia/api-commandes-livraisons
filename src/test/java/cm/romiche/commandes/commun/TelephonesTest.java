package cm.romiche.commandes.commun;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TelephonesTest {

    @ParameterizedTest
    @ValueSource(strings = {"690000000", "6 90 00 00 00", "+237 690000000", "237690000000", "+237 6 90 00 00 00"})
    @DisplayName("Toutes les écritures courantes donnent le même numéro")
    void normalise(String saisie) {
        assertThat(Telephones.normaliser(saisie)).isEqualTo("+237690000000");
        assertThat(saisie).matches(Telephones.MOTIF);
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "590000000", "+33 6 12 34 56 78", "6900000001"})
    @DisplayName("Les numéros non camerounais sont refusés")
    void refuse(String saisie) {
        assertThat(saisie).doesNotMatch(Telephones.MOTIF);
        assertThatThrownBy(() -> Telephones.normaliser(saisie)).isInstanceOf(IllegalArgumentException.class);
    }
}
