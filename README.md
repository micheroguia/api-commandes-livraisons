# API Commandes & livraisons

API REST pour gérer un catalogue de produits, prendre des commandes et suivre leurs livraisons, **pensée pour le Cameroun** :

- montants en **FCFA** (nombres entiers, sans centimes) ;
- téléphones vérifiés et enregistrés au format **+237** (`6 90 00 00 00` devient `+237690000000`) ;
- adresses par **quartier et ville**, avec des indications pour le livreur (« portail bleu après la pharmacie »).

C'est la base du projet **A1 — Tournées de livraison** de la feuille de route (`../FEUILLE-DE-ROUTE.md`).

## Ce que fait la version 0.1

1. **Produits** : ajouter, modifier, lister par pages, retirer du catalogue (sans effacer l'historique).
2. **Commandes** : le stock est réservé dès la commande ; si un produit manque, rien n'est enregistré (erreur 409).
3. **Confirmation** : une commande confirmée crée automatiquement sa livraison, « à planifier ».
4. **Livraisons** : assigner un livreur, puis faire avancer le statut. Chaque étape est gardée dans un historique.
5. **Annulation** : possible tant que le livreur n'est pas en route ; les produits sont remis en stock.
6. **Erreurs claires** en français, au format standard *Problem Details* (RFC 9457).

### Cycle de vie d'une livraison

1. **À planifier** → on assigne un livreur → **Assignée**
2. **Assignée** → le livreur part → **En cours** (ou retour à « À planifier » pour changer de livreur)
3. **En cours** → **Livrée** (la commande passe aussi à « Livrée ») ou **Échec**
4. **Échec** → **À planifier** pour une nouvelle tentative
5. **Livrée** et **Annulée** sont définitifs.

## Lancer le projet

Il faut **Java 17 ou plus** et **Maven**.

### Option 1 — Démo rapide, sans Docker

Base de données en mémoire, avec 4 produits d'exemple :

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

Ensuite, ouvre :

- la documentation interactive (Swagger) : http://localhost:8080/swagger-ui.html
- la console de la base : http://localhost:8080/h2-console (URL JDBC : `jdbc:h2:mem:commandes`, utilisateur `sa`, sans mot de passe)

### Option 2 — Avec PostgreSQL (Docker)

Démarre Docker Desktop, puis :

```bash
docker compose up --build
```

## Lancer les tests

```bash
mvn verify
```

Les tests couvrent les règles importantes : stock réservé et rendu, total en FCFA, téléphones camerounais, passages de statut autorisés et interdits, parcours complet jusqu'à la livraison.

## Les adresses de l'API

| Méthode | Adresse | Rôle |
|---|---|---|
| GET | `/api/produits` | Lister les produits (`?actifsSeulement=false` pour tout voir) |
| POST | `/api/produits` | Ajouter un produit |
| GET / PUT / DELETE | `/api/produits/{id}` | Voir, modifier, retirer du catalogue |
| POST | `/api/commandes` | Passer une commande |
| GET | `/api/commandes` | Lister les commandes (`?statut=EN_ATTENTE`…) |
| GET | `/api/commandes/{id}` | Voir une commande |
| POST | `/api/commandes/{id}/confirmer` | Confirmer et créer la livraison |
| POST | `/api/commandes/{id}/annuler` | Annuler (stock rendu) |
| GET | `/api/livraisons` | Lister les livraisons (`?statut=A_PLANIFIER`…) |
| POST | `/api/livraisons/{id}/assigner` | Confier à un livreur |
| POST | `/api/livraisons/{id}/statut` | Passer à `EN_COURS`, `LIVREE`, `ECHEC` ou `A_PLANIFIER` |
| GET | `/api/livraisons/{id}/historique` | Historique de la livraison |

## Choix techniques

| Choix | Pourquoi |
|---|---|
| Spring Boot 3.5, Java 17 | Version stable et très répandue en entreprise |
| Règles métier dans les entités (`Produit`, `Commande`, `Livraison`) | Une règle est écrite une seule fois, à un seul endroit, et se teste facilement |
| Flyway (`src/main/resources/db/migration`) | Chaque changement de base est versionné, comme le code |
| Verrou optimiste sur `Produit` (`@Version`) | Deux commandes simultanées ne peuvent pas vendre le même stock |
| DTO en `record` (`...Requete`, `...Reponse`) | On n'expose jamais les entités de la base directement |
| H2 pour la démo et les tests, PostgreSQL en vrai | Tester sans rien installer, tourner en production sur une vraie base |

## Prochaines étapes

- [ ] Comptes et rôles avec **Spring Security + JWT** (gérant, livreur)
- [ ] Tests sur un vrai PostgreSQL avec **Testcontainers**
- [ ] Paiement **Mobile Money** à la livraison (module du socle commun)
- [ ] Notification au client (SMS / WhatsApp) quand le livreur part
- [ ] Mise en ligne de la démo
- [ ] Migration vers **Spring Boot 4** (bon exercice une fois la v1 terminée)
