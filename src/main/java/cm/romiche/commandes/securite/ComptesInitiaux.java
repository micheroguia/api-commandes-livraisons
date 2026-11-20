package cm.romiche.commandes.securite;

import cm.romiche.commandes.commun.Telephones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Au premier démarrage, la base n'a aucun compte : personne ne pourrait se connecter.
 * On crée donc le premier gérant à partir de GERANT_TELEPHONE et GERANT_MOT_DE_PASSE
 * (et un livreur d'exemple dans le profil « demo »).
 */
@Component
public class ComptesInitiaux implements ApplicationRunner {

    private static final Logger journal = LoggerFactory.getLogger(ComptesInitiaux.class);

    private final UtilisateurRepository utilisateurs;
    private final PasswordEncoder encodeur;
    private final ProprietesComptesInitiaux proprietes;

    public ComptesInitiaux(UtilisateurRepository utilisateurs, PasswordEncoder encodeur,
            ProprietesComptesInitiaux proprietes) {
        this.utilisateurs = utilisateurs;
        this.encodeur = encodeur;
        this.proprietes = proprietes;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        if (utilisateurs.count() > 0) {
            return;
        }
        if (!StringUtils.hasText(proprietes.gerantTelephone()) || !StringUtils.hasText(proprietes.gerantMotDePasse())) {
            journal.warn("Aucun compte en base : définissez GERANT_TELEPHONE et GERANT_MOT_DE_PASSE "
                    + "pour créer le premier gérant au démarrage.");
            return;
        }
        creer("Gérant", proprietes.gerantTelephone(), proprietes.gerantMotDePasse(), Role.GERANT);
        if (StringUtils.hasText(proprietes.livreurTelephone()) && StringUtils.hasText(proprietes.livreurMotDePasse())) {
            creer("Livreur démo", proprietes.livreurTelephone(), proprietes.livreurMotDePasse(), Role.LIVREUR);
        }
    }

    private void creer(String nom, String telephone, String motDePasse, Role role) {
        String numero = Telephones.normaliser(telephone);
        utilisateurs.save(new Utilisateur(nom, numero, encodeur.encode(motDePasse), role));
        journal.info("Compte {} créé pour le numéro se terminant par {}.", role, numero.substring(numero.length() - 2));
    }
}
