package cm.romiche.commandes.securite;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Comptes créés au premier démarrage, quand la base ne contient encore aucun compte (app.comptes-initiaux.*).
 * Le livreur n'est utilisé que par le profil « demo ».
 */
@ConfigurationProperties("app.comptes-initiaux")
public record ProprietesComptesInitiaux(
        String gerantTelephone,
        String gerantMotDePasse,
        String livreurTelephone,
        String livreurMotDePasse) {
}
