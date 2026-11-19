package cm.romiche.commandes.commun;

/**
 * Numéros de téléphone camerounais.
 * Accepte « 6 90 00 00 00 », « 690000000 », « +237 690000000 » ou « 237690000000 »
 * et les enregistre toujours au même format : « +237690000000 ».
 */
public final class Telephones {

    /** Expression régulière utilisée par les annotations @Pattern des requêtes. */
    public static final String MOTIF = "^(\\+?237\\s?)?[26](\\s?\\d){8}$";

    public static final String MESSAGE =
            "Numéro camerounais attendu, par exemple 6 90 00 00 00 ou +237 690000000";

    private Telephones() {
    }

    public static String normaliser(String brut) {
        if (brut == null) {
            throw new IllegalArgumentException(MESSAGE);
        }
        String chiffres = brut.replaceAll("\\D", "");
        if (chiffres.length() == 12 && chiffres.startsWith("237")) {
            chiffres = chiffres.substring(3);
        }
        if (chiffres.length() != 9 || !(chiffres.startsWith("6") || chiffres.startsWith("2"))) {
            throw new IllegalArgumentException(MESSAGE);
        }
        return "+237" + chiffres;
    }
}
