package cm.romiche.commandes.securite;

import java.time.Instant;
import java.util.Optional;

import cm.romiche.commandes.commun.IdentifiantsInvalidesException;
import cm.romiche.commandes.commun.Telephones;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Vérifie téléphone + mot de passe et fabrique le jeton JWT. */
@Service
public class AuthService {

    private final UtilisateurRepository utilisateurs;
    private final PasswordEncoder encodeur;
    private final JwtEncoder jwtEncoder;
    private final ProprietesJwt proprietes;

    /**
     * Empreinte factice : quand le téléphone est inconnu, on vérifie quand même un mot de passe.
     * Ainsi, la réponse met le même temps qu'un compte existe ou non (on ne révèle pas les numéros inscrits).
     */
    private final String empreinteFactice;

    public AuthService(UtilisateurRepository utilisateurs, PasswordEncoder encodeur, JwtEncoder jwtEncoder,
            ProprietesJwt proprietes) {
        this.utilisateurs = utilisateurs;
        this.encodeur = encodeur;
        this.jwtEncoder = jwtEncoder;
        this.proprietes = proprietes;
        this.empreinteFactice = encodeur.encode("mot-de-passe-factice-pour-un-temps-de-reponse-constant");
    }

    @Transactional(readOnly = true)
    public JetonReponse connecter(ConnexionRequete requete) {
        Optional<Utilisateur> compte = chercher(requete.telephone());
        String empreinte = compte.map(Utilisateur::getMotDePasse).orElse(empreinteFactice);
        boolean motDePasseCorrect = encodeur.matches(requete.motDePasse(), empreinte);

        if (compte.isEmpty() || !motDePasseCorrect || !compte.get().isActif()) {
            throw new IdentifiantsInvalidesException();
        }
        return emettreJeton(compte.get());
    }

    private Optional<Utilisateur> chercher(String telephoneSaisi) {
        try {
            return utilisateurs.findByTelephone(Telephones.normaliser(telephoneSaisi));
        } catch (IllegalArgumentException numeroInvalide) {
            return Optional.empty();
        }
    }

    private JetonReponse emettreJeton(Utilisateur utilisateur) {
        Instant maintenant = Instant.now();
        JwtClaimsSet contenu = JwtClaimsSet.builder()
                .issuer(ConfigurationSecurite.EMETTEUR)
                .subject(utilisateur.getTelephone())
                .issuedAt(maintenant)
                .expiresAt(maintenant.plus(proprietes.duree()))
                .claim("role", utilisateur.getRole().name())
                .claim("nom", utilisateur.getNom())
                .build();
        JwsHeader entete = JwsHeader.with(MacAlgorithm.HS256).build();
        String jeton = jwtEncoder.encode(JwtEncoderParameters.from(entete, contenu)).getTokenValue();
        return new JetonReponse(jeton, "Bearer", proprietes.duree().toSeconds(), UtilisateurReponse.depuis(utilisateur));
    }
}
