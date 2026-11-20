# API Commandes & livraisons

API REST pour gérer un catalogue de produits, prendre des commandes et suivre leurs livraisons, **pensée pour le Cameroun** :

- montants en **FCFA** (nombres entiers, sans centimes) ;
- téléphones vérifiés et enregistrés au format **+237** (`6 90 00 00 00` devient `+237690000000`) ;
- adresses par **quartier et ville**, avec des indications pour le livreur (« portail bleu après la pharmacie »).

C'est la base du projet **A1 — Tournées de livraison** de la feuille de route (`../FEUILLE-DE-ROUTE.md`).

## Ce que fait la version 0.2

1. **Produits** : ajouter, modifier, lister par pages, retirer du catalogue (sans effacer l'historique).
2. **Commandes** : le stock est réservé dès la commande ; si un produit manque, rien n'est enregistré (erreur 409).
3. **Confirmation** : une commande confirmée crée automatiquement sa livraison, « à planifier ».
4. **Livraisons** : assigner un livreur, puis faire avancer le statut. Chaque étape est gardée dans un historique.
5. **Annulation** : possible tant que le livreur n'est pas en route ; les produits sont remis en stock.
6. **Comptes et connexion** : on se connecte avec son **numéro de téléphone** et son mot de passe, et on reçoit un **jeton JWT**. Deux rôles : **gérant** et **livreur**.
7. **Erreurs claires** en français, au format standard *Problem Details* (RFC 9457).

### Qui a le droit de faire quoi

| Action | Sans compte | Livreur | Gérant |
|---|:-:|:-:|:-:|
| Voir le catalogue | ✅ | ✅ | ✅ |
| Voir **ses** livraisons à faire (`/api/livraisons/miennes`) | | ✅ | |
| Passer **sa** livraison à « en cours », « livrée » ou « échec » | | ✅ | ✅ |
| Gérer produits, commandes, livraisons et comptes | | | ✅ |

Un livreur ne peut pas toucher à la livraison d'un autre, ni remettre une livraison « à planifier » : c'est une décision du gérant.

### Sécurité, en bref

- Mots de passe stockés sous forme d'**empreinte BCrypt**, jamais en clair, jamais renvoyés par l'API.
- Jetons **signés** (HS256) avec une clé secrète, valables 8 h par défaut. La signature, la date d'expiration et l'émetteur sont vérifiés à chaque requête.
- Même message d'erreur que le numéro soit inconnu ou le mot de passe faux, et même temps de réponse : on ne révèle pas qui a un compte.
- L'application **refuse de démarrer** si la clé `JWT_SECRET` manque ou fait moins de 32 caractères.
- Au premier démarrage, le premier gérant est créé à partir de `GERANT_TELEPHONE` et `GERANT_MOT_DE_PASSE`.

### Cycle de vie d'une livraison

1. **À planifier** → on assigne un livreur → **Assignée**
2. **Assignée** → le livreur part → **En cours** (ou retour à « À planifier » pour changer de livreur)
3. **En cours** → **Livrée** (la commande passe aussi à « Livrée ») ou **Échec**
4. **Échec** → **À planifier** pour une nouvelle tentative
5. **Livrée** et **Annulée** sont définitifs.

## Lancer le projet

Il faut **Java 17 ou plus** et **Maven**.

### Option 1 — Démo rapide, sans Docker

Base de données en mémoire, avec 4 produits et 2 comptes d'exemple :

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

Ensuite, ouvre :

- la documentation interactive (Swagger) : http://localhost:8080/swagger-ui.html
- la console de la base : http://localhost:8080/h2-console (URL JDBC : `jdbc:h2:mem:commandes`, utilisateur `sa`, sans mot de passe)

Comptes de démonstration (publics, **uniquement** pour essayer) :

| Rôle | Téléphone | Mot de passe |
|---|---|---|
| Gérant | `690000001` | `demo-gerant-2026` |
| Livreur | `677000000` | `demo-livreur-2026` |

Pour appeler les routes protégées dans Swagger :

1. Ouvre `POST /api/auth/connexion`, clique sur **Try it out**, saisis un téléphone et un mot de passe, puis **Execute**.
2. Copie la valeur de `jeton` dans la réponse.
3. Clique sur **Authorize** en haut de la page, colle le jeton, valide.

### Option 2 — Avec PostgreSQL (Docker)

Démarre Docker Desktop, puis :

```bash
docker compose up --build
```

Les valeurs par défaut de `docker-compose.yml` servent seulement en local. Pour les changer, crée un fichier `.env` (il est ignoré par Git) :

```
JWT_SECRET=une-longue-phrase-secrete-d-au-moins-32-caracteres
GERANT_TELEPHONE=6XXXXXXXX
GERANT_MOT_DE_PASSE=un-mot-de-passe-solide
```

### Variables d'environnement

| Variable | Rôle | Obligatoire |
|---|---|---|
| `JWT_SECRET` | Clé de signature des jetons (32 caractères minimum) | Oui |
| `JWT_DUREE` | Durée de validité d'un jeton (défaut : `8h`) | Non |
| `GERANT_TELEPHONE`, `GERANT_MOT_DE_PASSE` | Premier compte gérant, créé si la base n'a aucun compte | Au premier démarrage |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Connexion PostgreSQL | Oui (hors démo) |

## Lancer les tests

```bash
mvn verify
```

37 tests couvrent les règles importantes : stock réservé et rendu, total en FCFA, téléphones camerounais, passages de statut autorisés et interdits, parcours complet jusqu'à la livraison, connexion réelle avec jeton, droits du gérant et du livreur.

## Les adresses de l'API

| Méthode | Adresse | Rôle | Qui |
|---|---|---|---|
| POST | `/api/auth/connexion` | Se connecter : renvoie un jeton | Tout le monde |
| GET | `/api/auth/moi` | Voir son compte | Connecté |
| GET | `/api/produits` | Lister les produits (`?actifsSeulement=false` pour tout voir) | Tout le monde |
| GET | `/api/produits/{id}` | Voir un produit | Tout le monde |
| POST / PUT / DELETE | `/api/produits…` | Ajouter, modifier, retirer du catalogue | Gérant |
| POST | `/api/commandes` | Enregistrer une commande | Gérant |
| GET | `/api/commandes` | Lister les commandes (`?statut=EN_ATTENTE`…) | Gérant |
| GET | `/api/commandes/{id}` | Voir une commande | Gérant |
| POST | `/api/commandes/{id}/confirmer` | Confirmer et créer la livraison | Gérant |
| POST | `/api/commandes/{id}/annuler` | Annuler (stock rendu) | Gérant |
| GET | `/api/livraisons` | Lister les livraisons (`?statut=A_PLANIFIER`…) | Gérant |
| GET | `/api/livraisons/miennes` | Mes livraisons à faire | Livreur |
| POST | `/api/livraisons/{id}/assigner` | Confier à un livreur | Gérant |
| POST | `/api/livraisons/{id}/statut` | Passer à `EN_COURS`, `LIVREE`, `ECHEC` (ou `A_PLANIFIER` pour le gérant) | Livreur (les siennes) ou gérant |
| GET | `/api/livraisons/{id}/historique` | Historique de la livraison | Gérant |
| POST | `/api/utilisateurs` | Créer un compte gérant ou livreur | Gérant |
| GET | `/api/utilisateurs` | Lister les comptes | Gérant |
| POST | `/api/utilisateurs/{id}/desactiver` | Désactiver un compte | Gérant |

## Choix techniques

| Choix | Pourquoi |
|---|---|
| Spring Boot 3.5, Java 17 | Version stable et très répandue en entreprise |
| Règles métier dans les entités (`Produit`, `Commande`, `Livraison`) | Une règle est écrite une seule fois, à un seul endroit, et se teste facilement |
| Flyway (`src/main/resources/db/migration`) | Chaque changement de base est versionné, comme le code |
| Verrou optimiste sur `Produit` (`@Version`) | Deux commandes simultanées ne peuvent pas vendre le même stock |
| DTO en `record` (`...Requete`, `...Reponse`) | On n'expose jamais les entités de la base directement |
| H2 pour la démo et les tests, PostgreSQL en vrai | Tester sans rien installer, tourner en production sur une vraie base |
| Jetons avec `spring-boot-starter-oauth2-resource-server` | La brique officielle de Spring vérifie les jetons : pas de code de sécurité « fait maison » |
| Connexion par téléphone | Au Cameroun, tout le monde a un numéro ; beaucoup n'utilisent pas l'e-mail |
| Commandes saisies par le gérant | Les clients commandent surtout par WhatsApp ou par téléphone ; une boutique en ligne publique viendra plus tard |

## Prochaines étapes

- [x] Comptes et rôles avec **Spring Security + JWT** (gérant, livreur)
- [ ] Limiter les tentatives de connexion (protection contre la force brute)
- [ ] Libérer automatiquement le stock des commandes jamais confirmées
- [ ] Tests sur un vrai PostgreSQL avec **Testcontainers**
- [ ] Paiement **Mobile Money** à la livraison (module du socle commun)
- [ ] Notification au client (SMS / WhatsApp) quand le livreur part
- [ ] Mise en ligne de la démo
- [ ] Migration vers **Spring Boot 4** (bon exercice une fois la v1 terminée)
