package cm.romiche.commandes.commun;

/** Levée quand on demande un élément qui n'existe pas : renvoie une erreur 404. */
public class RessourceIntrouvableException extends RuntimeException {

    public RessourceIntrouvableException(String type, Long id) {
        super("%s n° %d introuvable.".formatted(type, id));
    }
}
