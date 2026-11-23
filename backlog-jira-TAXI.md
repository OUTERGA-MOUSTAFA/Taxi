# Backlog JIRA — Projet TAXI
## Plateforme de covoiturage multi-étapes (Spring Boot)

### Comment utiliser ce fichier

1. Dans JIRA, créer d'abord les 8 **Epics** (bouton « Créer », type *Epic*). Copier le titre et la description.
2. Créer ensuite chaque **Tâche** en choisissant son Epic comme *Parent*. Copier la description et les critères d'acceptation.
3. Ouvrir chaque tâche et ajouter ses **Sous-tâches** (« Ajouter une sous-tâche »). La phrase après le tiret est la description.
4. Glisser les tâches de E1 à E4 dans **Sprint 1** (16 → 17/11) et celles de E5 à E8 dans **Sprint 2** (18 → 20/11), puis « Démarrer un sprint ».
5. Travailler dans l'ordre des numéros. Une tâche passe à « Terminé » seulement si la *Definition of Done* est respectée.

**Definition of Done (valable pour toutes les tâches)**
- Le code compile (`mvn clean install` passe).
- L'endpoint est testé dans Postman : un cas qui marche et un cas refusé.
- Les règles refusées sont loguées en WARN, les actions réussies en INFO.
- Le commit est poussé sur GitHub avec un message clair.

**Estimation** : points de story (1 = 1 h, 2 = 2 h, 3 = demi-journée, 5 = journée).

---

## E1 — Socle technique du projet

**Description :** Mettre en place le projet Spring Boot, la base PostgreSQL sous Docker et la structure en couches, pour que l'application démarre sans erreur et se connecte à la base.

### T1.1 — Initialiser le projet Spring Boot (2 pts)

**Description :** Générer le squelette du projet avec Spring Initializr : Java 17, Maven, artefact `covoiturage`, groupe `com.youcode`.

**Critères d'acceptation :**
- Dépendances présentes : Spring Web, Spring Data JPA, PostgreSQL Driver, Validation, MapStruct, springdoc-openapi (Swagger), Lombok (optionnel).
- `mvn clean install` réussit.
- Le dépôt GitHub existe avec un `.gitignore` Java et un premier commit.

**Sous-tâches :**
- **S1.1.1 — Générer le projet sur start.spring.io** — Maven, Java 17, Spring Boot 3.x, ajouter les dépendances citées, télécharger et dézipper.
- **S1.1.2 — Configurer MapStruct dans le pom.xml** — ajouter `mapstruct` et `mapstruct-processor` dans `annotationProcessorPaths` du plugin compiler (plus `lombok-mapstruct-binding` si Lombok est utilisé).
- **S1.1.3 — Créer le dépôt GitHub** — `git init`, `.gitignore`, premier commit « chore: init projet Spring Boot », push.

### T1.2 — PostgreSQL avec Docker Compose (2 pts)

**Description :** Lancer la base de données avec un seul `docker compose up -d` et connecter l'application dessus.

**Critères d'acceptation :**
- Fichier `docker-compose.yml` à la racine : image `postgres:16`, port 5432, volume persistant, base `covoiturage`.
- `application.yml` contient l'URL JDBC, l'utilisateur, le mot de passe, `ddl-auto: update` et `show-sql: true`.
- Au démarrage, les logs montrent « HikariPool-1 - Start completed » sans erreur.

**Sous-tâches :**
- **S1.2.1 — Écrire docker-compose.yml** — service `db`, variables `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, volume `pgdata`.
- **S1.2.2 — Configurer application.yml** — datasource, JPA (dialecte PostgreSQL, `open-in-view: false`), port serveur 8080.
- **S1.2.3 — Vérifier le démarrage** — `docker compose up -d` puis `mvn spring-boot:run` ; garder une capture du log de connexion pour le README.

### T1.3 — Structure en couches et outils transverses (2 pts)

**Description :** Créer l'arborescence des packages et les outils communs (logs, enums) avant d'écrire la première entité.

**Critères d'acceptation :**
- Packages : `controller`, `service` (interfaces) + `service.impl`, `repository`, `entity`, `dto`, `mapper`, `exception`, `enums`.
- Les deux enums `StatutTrajet` et `StatutReservation` existent.
- Un logger SLF4J est utilisable dans chaque service (`LoggerFactory.getLogger`).

**Sous-tâches :**
- **S1.3.1 — Créer les packages** — un package par couche, plus 3 lignes dans le README expliquant le rôle de chaque couche.
- **S1.3.2 — Créer les enums** — `StatutTrajet { PUBLIE, COMPLET, TERMINE, ANNULE }` et `StatutReservation { CONFIRMEE, ANNULEE }`.
- **S1.3.3 — Configurer les logs** — niveau INFO par défaut dans `application.yml`, format lisible ; vérifier qu'un log INFO apparaît au démarrage.

### T1.4 — Diagramme de classes UML (3 pts)

**Description :** Dessiner le modèle de données avant de coder les entités, pour fixer les relations et les attributs.

**Critères d'acceptation :**
- 6 classes : `Conducteur`, `Passager`, `Vehicule`, `Trajet`, `PointArret`, `Reservation` + 2 enums.
- Relations : Conducteur 1—* Vehicule ; Conducteur 1—* Trajet ; Vehicule 1—* Trajet ; Trajet 1—* PointArret (composition) ; Passager 1—* Reservation ; Trajet 1—* Reservation ; Reservation → PointArret (montée et descente).
- Export en PNG dans le dossier `docs/` du dépôt.

**Sous-tâches :**
- **S1.4.1 — Dessiner le diagramme** — avec draw.io ou PlantUML, attributs et types (BigDecimal pour les prix, LocalDateTime pour les heures).
- **S1.4.2 — Exporter et versionner** — `docs/diagramme-classes.png` + lien dans le README.

---

## E2 — Conducteurs, passagers et gestion des erreurs

**Description :** Premier CRUD complet (conducteur), puis le second (passager), et la gestion centralisée des erreurs qui servira à toutes les autres parties.

### T2.1 — CRUD Conducteur (3 pts)

**Description :** Créer, consulter, lister, modifier et rechercher un conducteur.

**Critères d'acceptation :**
- Endpoints : `POST /api/conducteurs`, `GET /api/conducteurs`, `GET /api/conducteurs/{id}`, `PUT /api/conducteurs/{id}`, `GET /api/conducteurs/recherche?nom=&email=`.
- Email unique et numéro de permis unique : contrainte en base + vérification dans le service → 409.
- Champs obligatoires validés avec Bean Validation → 400.

**Sous-tâches :**
- **S2.1.1 — Entité Conducteur** — `id`, `nom`, `prenom`, `email` (unique), `telephone`, `numeroPermis` (unique) ; `@Entity`, `@Column(unique = true)`.
- **S2.1.2 — DTOs records + validation** — `ConducteurRequest` (`@NotBlank`, `@Email`) et `ConducteurResponse`.
- **S2.1.3 — Mapper MapStruct** — interface `ConducteurMapper` : `toEntity`, `toResponse`, `updateEntity`.
- **S2.1.4 — Repository** — `existsByEmail`, `existsByNumeroPermis`, `findByNomContainingIgnoreCaseOrEmailContainingIgnoreCase`.
- **S2.1.5 — Service (interface + impl)** — règles d'unicité, log INFO « Conducteur créé id=… », WARN « Email déjà utilisé ».
- **S2.1.6 — Controller + test Postman** — les 5 endpoints testés, requêtes sauvegardées dans la collection.

### T2.2 — CRUD Passager (2 pts)

**Description :** Même travail que le conducteur, sans numéro de permis, avec une recherche par email.

**Critères d'acceptation :**
- Endpoints : `POST /api/passagers`, `GET /api/passagers`, `GET /api/passagers/{id}`, `PUT /api/passagers/{id}`, `GET /api/passagers/recherche?email=`.
- Email unique → 409 si doublon.
- L'historique des réservations (`GET /api/passagers/{id}/reservations`) sera fait dans E6.

**Sous-tâches :**
- **S2.2.1 — Entité Passager** — `id`, `nom`, `prenom`, `email` (unique), `telephone`.
- **S2.2.2 — DTOs + mapper** — `PassagerRequest`, `PassagerResponse`, `PassagerMapper`.
- **S2.2.3 — Repository + service + controller** — CRUD et recherche par email, logs INFO/WARN.
- **S2.2.4 — Test Postman** — création, doublon d'email (409), champ vide (400).

### T2.3 — Gestion centralisée des erreurs avec ProblemDetail (3 pts)

**Description :** Toutes les erreurs doivent sortir dans le même format JSON, avec le bon code HTTP.

**Critères d'acceptation :**
- Une classe annotée `@RestControllerAdvice`.
- Réponse `ProblemDetail` contenant : `timestamp`, `status`, `type` (nom de l'erreur), `detail` (message clair), `instance` (chemin de la requête).
- Mapping : validation → 400 ; ressource d'un autre utilisateur → 403 ; introuvable → 404 ; règle métier → 409 ; autre → 500.

**Sous-tâches :**
- **S2.3.1 — Créer les exceptions métier** — `ResourceNotFoundException` (404), `BusinessRuleException` (409), `ForbiddenActionException` (403).
- **S2.3.2 — Écrire GlobalExceptionHandler** — une méthode `@ExceptionHandler` par exception, plus `MethodArgumentNotValidException` (400 avec la liste des champs en erreur) et `Exception` (500).
- **S2.3.3 — Tester dans Postman** — un 404 (`GET /api/conducteurs/999`), un 400 (email vide), un 409 (email en double) ; vérifier le JSON.

---

## E3 — Véhicules

**Description :** Un conducteur possède plusieurs véhicules ; il ne peut publier qu'avec les siens.

### T3.1 — Entité Vehicule et relation avec Conducteur (2 pts)

**Description :** Modéliser le véhicule et son propriétaire.

**Critères d'acceptation :**
- Attributs : `id`, `marque`, `modele`, `immatriculation` (unique), `couleur`, `capacite` (1 à 8), `conducteur` (`@ManyToOne`).
- `capacite` = places passagers sans le conducteur, validée avec `@Min(1) @Max(8)` → 400.

**Sous-tâches :**
- **S3.1.1 — Entité Vehicule** — colonnes, contrainte unique sur `immatriculation`, `@ManyToOne(fetch = LAZY) Conducteur`.
- **S3.1.2 — DTOs + mapper** — 
- **S3.1.3 — Repository** — `findByConducteurId`, `existsByImmatriculation`.

### T3.2 — Endpoints véhicules (3 pts)

**Description :** Ajouter, consulter, modifier, lister et supprimer un véhicule.

**Critères d'acceptation :**
- `POST /api/vehicules`, `GET /api/vehicules/{id}`, `PUT /api/vehicules/{id}`, `GET /api/conducteurs/{id}/vehicules`, `DELETE /api/vehicules/{id}`.
- Immatriculation en double → 409.
- Suppression refusée (409) si le véhicule est lié à au moins un trajet (règle activée dans E4 quand `Trajet` existera).

**Sous-tâches :**
- **S3.2.1 — Service véhicule** — création liée à un conducteur existant (404 sinon), modification, listing par conducteur.
- **S3.2.2 — Suppression conditionnelle** — prévoir l'appel à `trajetRepository.existsByVehiculeId` (à brancher dans E4) ; en attendant, supprimer librement.
- **S3.2.3 — Controller + Postman** — les 5 e`VehiculeRequest` (avec `conducteurId`), `VehiculeResponse` (avec le nom du conducteur), `VehiculeMapper`.ndpoints + cas 400 (capacité 9) et 409 (immatriculation en double).

---

## E4 — Trajets et points d'arrêt

**Description :** Cœur du projet : publier un trajet avec des points d'arrêt ordonnés et un prix cumulé depuis le départ.

### T4.1 — Entités Trajet et PointArret (3 pts)

**Description :** Modéliser le trajet et ses points d'arrêt en composition (les points n'existent pas sans leur trajet).

**Critères d'acceptation :**
- `PointArret` : `id`, `trajet`, `ville`, `ordre` (0 = départ), `heurePassage` (LocalDateTime), `prixDepuisDepart` (BigDecimal, 2 décimales).
- `Trajet` : `id`, `conducteur`, `vehicule`, `villeDepart`, `villeArrivee`, `dateDepart`, `placesProposees`, `statut`, `pointsArret` (`@OneToMany(cascade = ALL, orphanRemoval = true)`, `@OrderBy("ordre")`).
- `villeDepart`, `villeArrivee`, `dateDepart` sont remplis à partir du premier et du dernier point.

**Sous-tâches :**
- **S4.1.1 — Entité PointArret** — colonnes + `@ManyToOne Trajet` ; `@Column(precision = 10, scale = 2)` pour le prix.
- **S4.1.2 — Entité Trajet** — relations vers Conducteur, Vehicule, liste de points, `@Enumerated(STRING) StatutTrajet`.
- **S4.1.3 — Repository Trajet** — `findByConducteurId`, `existsByVehiculeId` ; activer la règle de suppression de E3.

### T4.2 — Publier un trajet : POST /api/trajets (5 pts)

**Description :** Un conducteur publie un trajet avec un de ses véhicules, un nombre de places et une liste ordonnée de points d'arrêt. Toutes les règles du brief sont vérifiées dans le service.

**Critères d'acceptation :**
- Corps : `conducteurId`, `vehiculeId`, `placesProposees`, `pointsArret[] { ville, heurePassage, prixDepuisDepart }`.
- Règles (sinon 400) : au moins 2 points ; départ dans le futur ; heures strictement croissantes ; chaque ville une seule fois ; prix = 0 au premier point puis strictement croissant ; places entre 1 et la capacité du véhicule.
- Règle (sinon 403) : le véhicule appartient au conducteur.
- Le trajet est créé en statut `PUBLIE`, réponse 201 avec le détail.
- Log INFO « Trajet publié id=… », WARN pour chaque règle refusée.

**Sous-tâches :**
- **S4.2.1 — DTOs** — `TrajetRequest`, `PointArretRequest`, `TrajetResponse`, `PointArretResponse` (records + validation de base).
- **S4.2.2 — Mapper** — `TrajetMapper` avec mapping des points ; ignorer le champ `trajet` des points pour éviter la boucle.
- **S4.2.3 — Validation métier dans TrajetServiceImpl** — une méthode privée par règle : `verifierPoints`, `verifierHeures`, `verifierPrix`, `verifierPlaces`, `verifierProprietaireVehicule`.
- **S4.2.4 — Attribution de l'ordre** — numéroter les points de 0 à n dans l'ordre reçu, remplir `villeDepart`, `villeArrivee`, `dateDepart`.
- **S4.2.5 — Controller + Postman** — trajet de Hamid (Rabat 0 → Casablanca 40 → Settat 70 → Marrakech 120, 3 places), cas 400 (5 places pour une capacité de 4), cas 403 (véhicule d'un autre conducteur).

### T4.3 — Consulter un trajet sans problème N+1 (3 pts)

**Description :** Détail d'un trajet (parcours, places restantes, conducteur) en une seule requête SQL.

**Critères d'acceptation :**
- `GET /api/trajets/{id}` renvoie le parcours ordonné, le conducteur, le véhicule et `placesRestantes` (calculé, jamais stocké).
- `GET /api/conducteurs/{id}/trajets` liste les trajets d'un conducteur.
- Avec `show-sql: true`, une seule requête principale (JOIN FETCH) pour le détail.

**Sous-tâches :**
- **S4.3.1 — Requête JOIN FETCH** — `@Query("select t from Trajet t join fetch t.pointsArret join fetch t.conducteur join fetch t.vehicule where t.id = :id")`.
- **S4.3.2 — Calcul des places restantes** — `placesProposees − somme(places des réservations CONFIRMEE)` dans le service ; tant que E6 n'existe pas, retourner `placesProposees`.
- **S4.3.3 — Endpoints + Postman** — détail, liste, cas 404.

### T4.4 — Terminer un trajet (2 pts)

**Description :** Le conducteur propriétaire marque son trajet comme terminé, uniquement après l'heure de départ.

**Critères d'acceptation :**
- `PATCH /api/trajets/{id}/terminer?conducteurId=` → 204.
- 403 si ce n'est pas le conducteur du trajet ; 409 si l'heure de départ n'est pas passée ; 409 si le statut est déjà `TERMINE` ou `ANNULE`.

**Sous-tâches :**
- **S4.4.1 — Méthode terminer dans le service** — vérifier propriétaire, statut, `LocalDateTime.now().isAfter(dateDepart)`.
- **S4.4.2 — Endpoint + Postman** — scénario 9 : terminer avant 17:00 → 409.

---

## E5 — Recherche de trajets

**Description :** Trouver les trajets entre deux villes à une date, même si les villes sont des étapes intermédiaires, en une seule requête JPQL paginée.

### T5.1 — Requête JPQL de recherche (5 pts)

**Description :** Une seule requête qui joint deux points d'arrêt du même trajet, vérifie l'ordre, calcule le prix du segment et les places restantes.

**Critères d'acceptation :**
- Paramètres : `villeDepart`, `villeArrivee`, `date`, `places` (défaut 1), `prixMax` (optionnel), pagination.
- Conditions : même trajet, `pm.ordre < pd.ordre`, villes insensibles à la casse, date du départ = date demandée, statut `PUBLIE`, places restantes ≥ places demandées, prix du segment ≤ `prixMax` s'il est fourni.
- Tri par heure de départ du trajet.
- Résultat : projection `TrajetRechercheResponse` (id du trajet, ville et heure de montée, ville et heure de descente, prix du segment, places restantes, nom du conducteur).

**Sous-tâches :**
- **S5.1.1 — Écrire la requête @Query** — `select new ...TrajetRechercheResponse(t.id, pm.ville, pm.heurePassage, pd.ville, pd.heurePassage, pd.prixDepuisDepart - pm.prixDepuisDepart, t.placesProposees - coalesce((select sum(r.nombrePlaces) from Reservation r where r.trajet = t and r.statut = 'CONFIRMEE'), 0), c.nom) from Trajet t join t.pointsArret pm join t.pointsArret pd join t.conducteur c where ...`.
- **S5.1.2 — Pagination** — paramètre `Pageable`, `countQuery` séparé, `order by t.dateDepart`.
- **S5.1.3 — Tester la requête** — Settat → Casablanca ne renvoie rien ; Casablanca → Settat renvoie 30 DH.

### T5.2 — Endpoint GET /api/trajets/recherche (2 pts)

**Description :** Exposer la recherche avec des paramètres de requête et une réponse paginée.

**Critères d'acceptation :**
- `GET /api/trajets/recherche?villeDepart=Settat&villeArrivee=Marrakech&date=2026-11-25&places=1&page=0&size=10`.
- Réponse `Page` : contenu + `totalElements` + `totalPages`.
- Scénario 2 validé : Settat → Casablanca ne renvoie pas le trajet de Hamid (200, liste vide).

**Sous-tâches :**
- **S5.2.1 — Service + controller** — validation des paramètres : `date` au format ISO, `places ≥ 1`.
- **S5.2.2 — Postman** — 3 requêtes : segment complet, segment intermédiaire, mauvais sens.

---

## E6 — Réservations et cycle de vie

**Description :** Réserver des places sur un segment, calculer le montant, mettre à jour le statut du trajet, annuler une réservation ou un trajet entier de façon transactionnelle.

### T6.1 — Entité Reservation (2 pts)

**Description :** Modéliser la réservation d'un passager sur un segment d'un trajet.

**Critères d'acceptation :**
- Attributs : `id`, `passager`, `trajet`, `pointMontee`, `pointDescente`, `nombrePlaces`, `montantTotal` (BigDecimal 2 décimales), `dateReservation`, `statut` (`StatutReservation`).
- Repository : `sumPlacesConfirmees(trajetId)`, `existsByPassagerIdAndTrajetIdAndStatut`, `findByPassagerId`, `findByTrajetId`.

**Sous-tâches :**
- **S6.1.1 — Entité** — relations `@ManyToOne` vers Passager, Trajet et PointArret (montée, descente).
- **S6.1.2 — Repository** — `@Query("select coalesce(sum(r.nombrePlaces), 0) from Reservation r where r.trajet.id = :id and r.statut = 'CONFIRMEE'")`.
- **S6.1.3 — DTOs + mapper** — `ReservationRequest` (`passagerId`, `trajetId`, `pointMonteeId`, `pointDescenteId`, `nombrePlaces`), `ReservationResponse`.

### T6.2 — Réserver des places : POST /api/reservations (5 pts)

**Description :** Un passager réserve une ou plusieurs places entre un point de montée et un point de descente. Le montant est calculé automatiquement.

**Critères d'acceptation :**
- 404 si passager, trajet ou point introuvable ; 400 si les points n'appartiennent pas au trajet ou si montée ≥ descente.
- 409 si trajet non `PUBLIE` ; 409 si places restantes insuffisantes ; 409 si le passager a déjà une réservation `CONFIRMEE` sur ce trajet.
- `montantTotal = (prixDescente − prixMontee) × nombrePlaces`, arrondi à 2 décimales (`RoundingMode.HALF_UP`).
- Si places restantes = 0 après la réservation → trajet passe à `COMPLET`.
- Méthode annotée `@Transactional`, réponse 201.
- Scénario 1 (Salma, Settat → Marrakech, 50 DH, 2 places restantes) et scénario 3 (Karim, 2 places, 160 DH, COMPLET) passent.

**Sous-tâches :**
- **S6.2.1 — Vérifications d'existence et d'appartenance** — charger passager, trajet, points ; vérifier `point.getTrajet().getId()`.
- **S6.2.2 — Règles métier** — statut PUBLIE, ordre montée < descente, places disponibles, pas de doublon ; une exception claire par règle + log WARN.
- **S6.2.3 — Calcul du montant** — méthode `calculerMontant(pointMontee, pointDescente, places)` avec BigDecimal.
- **S6.2.4 — Mise à jour du statut du trajet** — méthode `mettreAJourStatut(trajet)` : COMPLET si 0 place, PUBLIE sinon (réutilisée à l'annulation).
- **S6.2.5 — Controller + Postman** — scénarios 1, 3, 4 (409 trajet complet) et 6 (400 montée après descente).

### T6.3 — Consulter et annuler sa réservation (3 pts)

**Description :** Le passager voit ses réservations et peut annuler jusqu'à 24 h avant le départ.

**Critères d'acceptation :**
- `GET /api/passagers/{id}/reservations` (historique).
- `PATCH /api/reservations/{id}/annuler?passagerId=` → 204.
- 403 si la réservation n'est pas au passager ; 409 si déjà annulée ; 409 si moins de 24 h avant le départ.
- Après annulation : statut `ANNULEE`, places libérées, trajet `COMPLET` → `PUBLIE` si une place se libère.
- Scénario 5 (Karim annule 3 jours avant → 204, trajet PUBLIE) et scénario 10 (Salma annule 2 h avant → 409) passent.

**Sous-tâches :**
- **S6.3.1 — Historique passager** — service + controller, DTO avec segment et montant.
- **S6.3.2 — Annulation** — vérifier propriétaire, statut, `Duration.between(now, dateDepart).toHours() >= 24`, puis `mettreAJourStatut(trajet)`.
- **S6.3.3 — Postman** — scénarios 5 et 10.

### T6.4 — Annuler un trajet et ses réservations en bloc (3 pts)

**Description :** Le conducteur annule un trajet `PUBLIE` ou `COMPLET` ; toutes ses réservations `CONFIRMEE` passent à `ANNULEE` dans la même transaction.

**Critères d'acceptation :**
- `PATCH /api/trajets/{id}/annuler?conducteurId=` → 204.
- 403 si ce n'est pas le propriétaire ; 409 si le trajet est `TERMINE` ou `ANNULE`.
- Tout réussit ou tout échoue (`@Transactional`).
- Scénario 11 (trajet avec 2 réservations → 204, tout annulé) passe.

**Sous-tâches :**
- **S6.4.1 — Méthode annulerTrajet** — charger le trajet et ses réservations, passer chaque réservation CONFIRMEE à ANNULEE avec un stream, statut trajet ANNULE.
- **S6.4.2 — Vérifier la transaction** — provoquer une exception au milieu et constater le rollback (test manuel ou unitaire).
- **S6.4.3 — Postman** — scénario 11.

### T6.5 — Réservations de ses trajets, côté conducteur (1 pt)

**Description :** Le conducteur consulte les réservations faites sur ses propres trajets.

**Critères d'acceptation :**
- `GET /api/trajets/{id}/reservations?conducteurId=` → liste (passager, segment, places, montant, statut).
- 403 si le conducteur n'est pas le propriétaire du trajet.

**Sous-tâches :**
- **S6.5.1 — Service + controller + Postman** — un seul endpoint, un cas 200 et un cas 403.

---

## E7 — Tests unitaires

**Description :** Tester les services de trajet et de réservation avec JUnit 5, Mockito et AssertJ : cas nominaux et règles refusées.

### T7.1 — Tests de TrajetService (3 pts)

**Description :** Vérifier la publication, les règles refusées et la fin de trajet sans base de données (repositories mockés).

**Critères d'acceptation :**
- Classe `TrajetServiceImplTest` avec `@ExtendWith(MockitoExtension.class)`.
- Au moins 6 tests verts : publication OK ; 5 places pour une capacité de 4 → exception ; véhicule d'un autre → `ForbiddenActionException` ; heures non croissantes → exception ; prix non croissant → exception ; terminer avant le départ → `BusinessRuleException`.

**Sous-tâches :**
- **S7.1.1 — Fixture du trajet de Hamid** — méthode utilitaire qui construit conducteur, véhicule (capacité 4) et les 4 points.
- **S7.1.2 — Tests des cas nominaux** — `assertThat(response.statut()).isEqualTo(PUBLIE)`, `verify(trajetRepository).save(any())`.
- **S7.1.3 — Tests des règles refusées** — `assertThatThrownBy(...).isInstanceOf(...)` + `verify(repository, never()).save(any())`.

### T7.2 — Tests de ReservationService (3 pts)

**Description :** Vérifier le calcul du montant, le passage à COMPLET et les refus.

**Critères d'acceptation :**
- Au moins 6 tests verts : montant 50 DH (Settat → Marrakech, 1 place) ; montant 160 DH et trajet COMPLET (2 places Casablanca → Marrakech) ; réservation sur COMPLET → 409 ; montée après descente → 400 ; annulation à 2 h → 409 ; annulation du trajet annule les réservations.

**Sous-tâches :**
- **S7.2.1 — Mocks** — `ReservationRepository`, `TrajetRepository`, `PassagerRepository`, `PointArretRepository`.
- **S7.2.2 — Tests du montant** — `assertThat(montant).isEqualByComparingTo("50.00")`.
- **S7.2.3 — Tests des refus et de l'annulation** — vérifier le statut du trajet après chaque opération.

---

## E8 — Livrables et soutenance

**Description :** Tout ce qui est demandé le jour de la soutenance : collection Postman, données de démo, README, Swagger, slides et JIRA à jour.

### T8.1 — Collection Postman des 11 scénarios (3 pts)

**Description :** Une collection organisée par dossiers : Conducteurs, Passagers, Véhicules, Trajets, Recherche, Réservations, Scénarios 1 à 11.

**Critères d'acceptation :**
- Variables de collection : `baseUrl`, `hamidId`, `trajetId`, `salmaId`, `karimId`, `imaneId`.
- Chaque scénario contient un test sur le code HTTP attendu : `pm.response.to.have.status(201)`.
- Export JSON dans `docs/postman/`.

**Sous-tâches :**
- **S8.1.1 — Dossiers CRUD** — toutes les requêtes des epics E2 à E6.
- **S8.1.2 — Dossier Scénarios** — les 11 requêtes dans l'ordre du brief, avec les tests de statut.
- **S8.1.3 — Export + commit** — fichier JSON versionné, lien dans le README.

### T8.2 — Jeu de données de démonstration (2 pts)

**Description :** Au moins 5 enregistrements par table avant la soutenance.

**Critères d'acceptation :**
- 5 conducteurs, 5 passagers, 5 véhicules, 5 trajets (dont celui de Hamid), au moins 20 points d'arrêt, 5 réservations.
- Chargement reproductible : fichier `data.sql` ou dossier Postman « Seed » à lancer avec le Runner.

### T8.3 — README (2 pts)

**Description :** Un README qui permet à l'évaluateur de lancer et de comprendre le projet en 5 minutes.

**Critères d'acceptation :**
- Sections : présentation, stack, lancement (`docker compose up -d`, `mvn spring-boot:run`), diagramme de classes, tableau des endpoints, règles métier principales, codes d'erreur, lien Postman, lien JIRA.


### T8.4 — Swagger / OpenAPI (1 pt)

**Description :** Documentation automatique accessible sur `/swagger-ui.html` grâce à springdoc.

**Critères d'acceptation :**
- Tous les endpoints apparaissent avec leurs DTOs ; titre et description de l'API renseignés.

**Sous-tâches :**
- **S8.4.1 — Dépendance springdoc + configuration** — bean `OpenAPI` avec titre « API Covoiturage » et version.
