# KI-Änderungen Log

Dieses Dokument dokumentiert alle Änderungen, die vom KI-Assistant durchgeführt wurden, mit Zeitstempel, Begründung und Kontext.

---

## 2026-05-08 | Phase 0: Grundstruktur & Dokumentation

### 10:00 — Domain Entities angelegt

**Was:** 
- `User.java` (mit Email, PasswordHash, Kontaktdaten)
- `Pet.java` mit `PetSpecies.java` Enum
- `Offer.java` mit `OfferType.java` und `OfferStatus.java` Enums
- `OfferRequest.java` mit `RequestStatus.java` Enum
- `Booking.java` mit `BookingStatus.java` Enum
- `Notification.java` mit `NotificationType.java` Enum

**Warum:** 
Entities sind die Foundation. Ohne sie keine Datenbank-Struktur. JPA/Hibernate generiert Tabellen automatisch aus diesen Klassen.

**Kontext:** 
Fachliches Modell aus INFOFORME.md umgesetzt. Jede Entity hat eindeutige Verantwortung:
- User: Authentifizierung + Kontaktdaten
- Pet: Haustier-Verwaltung
- Offer: Such-/Angebots-Verwaltung (2 Typen: OWNER_OFFER, SITTER_OFFER)
- OfferRequest: Anfragen von Usern auf Offers
- Booking: Bestätigte Buchungen nach Request-Acceptance
- Notification: In-App Benachrichtigungen

---

### 11:30 — Repositories angelegt

**Was:**
- `UserRepository.java` (extends JpaRepository)
- `PetRepository.java`
- `OfferRepository.java`
- `OfferRequestRepository.java`
- `BookingRepository.java`
- `NotificationRepository.java`

**Warum:**
Repositories sind die Query-Layer. Spring Data JPA generiert automatisch CRUD-Operationen aus diesen Interfaces (findById, save, delete, etc.).

**Kontext:**
Repositories sind "thin" — nur Interfaces, keine Implementierung nötig. JPA macht die Arbeit.

---

### 12:00 — Service-Skelette angelegt

**Was:**
- `OfferService.java` (Struktur für Offer-Management)
- `RequestService.java` (Struktur für Request-Management)
- `BookingService.java` (Struktur für Booking-Management)
- `PetService.java`
- `UserService.java`
- `NotificationService.java`

**Warum:**
Services enthalten die Geschäftslogik. Skelette werfen gerade `UnsupportedOperationException`, aber die Struktur ist klar. Wird in Phase 2 implementiert.

**Kontext:**
Services haben eine Transaktion mit `@Transactional` (später). BookingService.acceptRequest() ist die zentrale Methode mit 4 Schritten.

---

### 12:30 — Exception-Klassen angelegt

**Was:**
- `NotFoundException.java`
- `ForbiddenOperationException.java`
- `BusinessRuleViolationException.java`
- `DuplicateRequestException.java`

**Warum:**
Fachliche Fehlerbehandlung. Statt generischer `RuntimeException` haben wir klare, dokumentierte Exceptions. Die UI kann damit umgehen (z.B. "Request already exists" → DuplicateRequestException).

**Kontext:**
Diese Exceptions werden in Phase 2 in Services geworfen, wenn Businessregeln verletzt werden.

---

### 13:00 — Security-Config gehärtet

**Was:**
- CSRF-Deaktivierung entfernt (Default-Schutz aktiv)
- Passwort-Hashing auf BCrypt umgestellt
- Demo-User per Env-Var konfigurierbar
- PrePersist/PreUpdate für automatische Timestamps

**Warum:**
Security ist nicht optional. BCrypt ist der Standard für Passwort-Hashing. Timestamps sind für Audit-Trails wichtig.

**Kontext:**
`application.properties` kann `PETSITTER_DEMO_USERNAME` und `PETSITTER_DEMO_PASSWORD` Env-Vars nutzen. Defaultfallback zu `localuser/localpass`.

---

## 2026-05-09 | Phase 0: Dokumentation & JavaDoc

### 09:00 — BACKEND_OVERVIEW.md angelegt

**Was:**
- Projekt-Übersicht für Kollegen (Architektur, Status, Entity-Modell, Geschäftsregeln)
- Tabelle: Technologie + Stand
- Schichten-Diagramm
- Entity-Modell mit allen Relationships
- Businessregeln als Tabelle
- Nächste Phasen (1-5) mit Checklisten
- FAQ (Warum Monolith? Warum Flyway?)

**Warum:**
Kollegen verstehen das Projekt schneller. Dokumentation ist Anforderung, nicht Optional.

**Kontext:**
206 Zeilen, covering alles. Kollegenmaterial.

---

### 10:00 — TECHNOLOGY_GUIDE.md angelegt

**Was:**
- Spring Boot erklärt (vs. reines Spring)
- Java 21 Features (Records, Virtual Threads, Pattern Matching)
- Spring Data JPA + Repositories (wie generiert Spring SQL?)
- MySQL vs. H2 (wann welche?)
- Flyway: Warum NOT `ddl-auto: update`?
- Spring Security: Form Login + BCrypt
- @Transactional: Atomare Transaktionen
- JSR-380 Validierung
- Test-Pyramide mit Beispielen
- Common Fehler (LazyInitialization, N+1 Queries)
- Kurzreferenz aller Annotations

**Warum:**
Tech-Stack ist komplex. Kollegen verstehen Framework-Entscheidungen besser.

**Kontext:**
350+ Zeilen, sehr detailliert.

---

### 10:30 — JavaDoc in Domain-Entities

**Was:**
- `User.java`: Klassenlevel-JavaDoc + Feld-Kommentare
  - Erklärt: "User kann Owner UND Sitter sein"
  - Warnt: "Passwörter NIEMALS plaintext"
  - Erklärt: Email ist unique (Login-ID)

- `Offer.java`: Umfassendes JavaDoc
  - Erklärt: OWNER_OFFER vs. SITTER_OFFER
  - Zustandsübergänge (OPEN → BOOKED → COMPLETED)
  - Optimistic Locking mit `@Version`
  - Businessregeln (Creator-only Edit, Booked-Lock, etc.)

- `Pet.java`: Ownership + Verwendung
  - Pet gehört genau einem Owner
  - Wird in OWNER_OFFERs referenziert

- `OfferRequest.java`: Request-Lifecycle
  - PENDING → ACCEPTED oder DENIED
  - Unique Constraint (anvis Duplikate)
  - Workflow (erstellen → sehen → accept/deny)

- `Booking.java`: Booking-Lifecycle
  - Entsteht durch acceptRequest()
  - 1:1 zu Offer und OfferRequest
  - Speichert Owner, Sitter, Pet, Zeitraum, Preis

**Warum:**
JavaDoc ist Anforderung. Jede Klasse braucht Erklärung: Was? Warum? Wie?

**Kontext:**
Detailliert, mit Beispielen und Referenzen zu Services.

---

### 11:00 — JavaDoc in Services

**Was:**
- `OfferService.java`: 
  - Klassenlevel: Was macht der Service?
  - createOwnerOffer(): Validierungen, Exceptions
  - createSitterOffer(): Pet-Handling erklärt
  - updateOffer(): "PENDING Requests → DENIED" Regel erklärt
  - cancelOffer(): Creator-only
  - findMatchingOffersForUser(): Matching-Logik dokumentiert

- `RequestService.java`:
  - Klassenlevel: "RequestService ist NICHT für Accept zuständig"
  - createRequest(): Validierungen, Self-Request Forbidden
  - cancelRequest(): Requester-only
  - findRequestsForOffer(): Zugriffskontrolle erklärt

- `BookingService.java`:
  - Klassenlevel: "ZENTRALE Methode!"
  - acceptRequest(): **4-Schritt Transaktion dokumentiert**
    1. Request → ACCEPTED
    2. Booking erzeugt
    3. Offer → BOOKED
    4. andere Requests → DENIED
  - cancelBooking(): Requester-only
  - getBookings(): User-filtering

**Warum:**
Services sind Geschäftslogik. Jede Methode braucht Erklärung: Input-Validierungen, Output, Exceptions, Businessregeln.

**Kontext:**
acceptRequest() ist besonders wichtig — Transaktion mit 4 Schritten, wird in Phase 2 implementiert.

---

### 11:30 — DOCUMENTATION_START_HERE.md angelegt

**Was:**
- Einstiegspunkt für Kollegenmaterial
- Lesreihenfolge (Overview → Tech → Klassen)
- Code-Struktur erklärt
- Geschäftslogik mit Flowchart (Request → Booking)
- Run & Test Commands
- FAQ Pointer

**Warum:**
Kollegen wissen nicht, wo sie anfangen. Diese Datei ist Roadmap.

**Kontext:**
Minimal, aber fokussiert.

---

### 12:00 — Build-Test erfolgreich

**Was:**
`./mvnw clean test` ausgeführt

**Ergebnis:**
```
Tests run: 1, Failures: 0, Errors: 0
BUILD SUCCESS
```

**Warum:**
Sicherstellen, dass neue JavaDoc-Änderungen keine Compile-Fehler einführen.

**Kontext:**
Alles läuft. Phase 0 abgeschlossen.

---

### 13:00 — .gitignore aktualisiert

**Was:**
- `INFOFORME.md` hinzugefügt (aus `.gitignore` entfernt — soll versioniert werden)

**Warum:**
Dokumentation sollte im Repo sein (für Transparenz).

**Kontext:**
Bestätigt, dass INFOFORME.md sichtbar ist.

---

## 2026-05-09 | Phase 1-5 Roadmap dokumentiert

### 14:00 — Phasen in INFOFORME.md dokumentiert

**Was:**
- Phase 1 (2-3 Tage): Flyway + SQL Migrations
- Phase 2 (3-4 Tage): Services implementieren + 14-18 Unit Tests
- Phase 3 (1-2 Tage): Security (Login, BCrypt, Session)
- Phase 4 (3-4 Tage): Vaadin UI (Views, Forms)
- Phase 5 (1-2 Tage): Dokumentation + Demo

**Warum:**
Klare Roadmap für nächste Schritte.

**Kontext:**
Gesamtprojekt: ~10-15 Tage bis Abgabe bereit.

---

## Nächste Schritte (Phase 1)

Diese Items sind für den nächsten Run geplant:

- [ ] Flyway zu `pom.xml` hinzufügen
- [ ] `V1__create_schema.sql` schreiben (alle Tabellen + Constraints)
- [ ] `V2__insert_demo_data.sql` schreiben (4 User, 3 Pets, 5 Offers, 2 Requests)
- [ ] `src/main/resources/db/migration/` Ordner erstellen
- [ ] Docker Compose Start testen (`docker compose up -d`)
- [ ] App starten (`./mvnw spring-boot:run -Dspring-boot.run.profiles=local`)
- [ ] Smoke Test: Demo-Daten sichtbar in DB

---

## Zusammenfassung Phase 0

✅ **5 Domain Entities** mit vollständiger Struktur
✅ **6 Repositories** (Spring Data JPA)
✅ **6 Services** als Skelette (Struktur klar)
✅ **4 Custom Exceptions** für Fehlerbehandlung
✅ **Security Config** (BCrypt, CSRF aktiv)
✅ **3 Dokumentations-Dateien** (Overview, Tech, Start Here)
✅ **Umfassende JavaDoc** in allen Klassen
✅ **Build erfolgreich** (1 Test grün, 0 Failures)

**Gesamtzeit:** ~4 Stunden
**Nächste Phase:** Flyway + Migrationen

---

## 2026-05-09 | UUID-Umstellung (statt Long IDs)

### 10:35 — IDs von Long auf UUID migriert

**Was:**
- Alle primären Entity-IDs von `Long` auf `UUID` umgestellt
  - `User`, `Pet`, `Offer`, `OfferRequest`, `Booking`, `Notification`
- `@GeneratedValue(strategy = GenerationType.IDENTITY)` auf `GenerationType.UUID` umgestellt
- Repositories auf `JpaRepository<..., UUID>` umgestellt
- Repository-Methoden mit `...Id(...)` Parametern auf `UUID` aktualisiert
- Service-Signaturen mit ID-Parametern auf `UUID` aktualisiert

**Warum:**
- Vorgabe: IDs sollen UUID statt Long sein
- UUIDs sind global eindeutig, besser für verteilte Systeme und Datenimporte
- Keine Vorhersagbarkeit wie bei fortlaufenden Long IDs

**Kontext:**
- Keine zusätzliche Dependency nötig (`java.util.UUID` ist Standard in Java)
- `Offer.version` bleibt bewusst `Long`, da es für Optimistic Locking gedacht ist und keine fachliche ID darstellt

### 10:46 — Build/Test verifiziert

**Was:**
- `./mvnw test` ausgeführt

**Ergebnis:**
```
Tests run: 1, Failures: 0, Errors: 0
BUILD SUCCESS
```

**Warum:**
- Sicherstellen, dass die UUID-Umstellung vollständig konsistent ist und keine Compile-/Runtime-Probleme erzeugt

---

## 2026-05-09 | Config + Spring Security Ausbau

### 11:00 — Security auf DB-Auth + konfigurierbaren Demo-Fallback umgestellt

**Was:**
- `SecurityConfig` umgebaut:
  - `@EnableWebSecurity`, `@EnableMethodSecurity(jsr250Enabled = true)` aktiviert
  - explizite Login-Seite `/login` konfiguriert
  - zentrale Permit-Whitelist fuer Vaadin-Assets ergaenzt
  - Logout-Flow mit Session-Invalidierung gesetzt
- Neue Properties-Klasse angelegt: `PetsitterSecurityProperties`
  - Prefix: `petsitter.security.demo.*`
  - steuerbar ueber Env-Variablen
- Neuen `DatabaseUserDetailsService` eingebaut:
  - Login zunaechst ueber DB-User (`UserRepository.findByEmail(...)`)
  - optionaler Demo-User-Fallback (konfigurierbar)
- Neue Vaadin `LoginView` unter Route `/login` erstellt
- `AuthenticatedUser` Helper-Komponente erstellt (aktuell eingeloggten Domain-User laden)

**Warum:**
- Security sollte nicht auf dauerhaftem In-Memory-Only-Setup bleiben
- Konfigurierbarkeit ueber Properties ist sauberer als harte Werte im Code
- Einheitlicher Login-Flow fuer Vaadin + Spring Security

**Kontext:**
- Demo-Zugang ist weiterhin moeglich, aber zentral konfigurierbar
- DB-basierte Auth ist damit vorbereitet fuer echte Registrierung/Seed-User

### 11:03 — Build/Test verifiziert

**Was:**
- `./mvnw test` ausgefuehrt

**Ergebnis:**
```
Tests run: 1, Failures: 0, Errors: 0
BUILD SUCCESS
```

**Warum:**
- Sicherstellen, dass der neue Security-Stack den Spring-Kontext sauber startet

---

## 2026-05-09 | Konfiguration von YAML auf .properties umgestellt

### 11:05 — Config-Dateien migriert

**Was:**
- `src/main/resources/application.yml` nach `src/main/resources/application.properties` migriert
- `src/main/resources/application-compose.yml` nach `src/main/resources/application-local.properties` migriert
- `src/test/resources/application.yml` nach `src/test/resources/application.properties` migriert
- alte YAML-Dateien entfernt, um Konflikte durch doppelte Quellen zu vermeiden

**Warum:**
- Vorgabe: Konfiguration soll nicht in YAML, sondern in `.properties` gepflegt werden
- Eine einzige Konfigurationsart vermeidet Missverstaendnisse im Team

**Kontext:**
- Inhalte wurden 1:1 in Dot-Notation uebernommen (inkl. Security- und Compose-Profileinstellungen)

### 11:07 — Build/Test verifiziert

**Was:**
- `./mvnw test` ausgefuehrt

**Ergebnis:**
```
Tests run: 1, Failures: 0, Errors: 0
BUILD SUCCESS
```

**Warum:**
- Sicherstellen, dass Spring die neuen `.properties` korrekt laedt (Main + Local-Profil + Tests)

---

## 2026-05-09 | Aufraeumen: Restliche .yml-Dateien entfernt + Doku aktualisiert

### 11:09 — Letzte `.yml` im Repo entfernt

**Was:**
- `docker-compose.yml` geloescht
- Local-Profil auf `compose.yaml` umgestellt:
  - `src/main/resources/application-local.properties`
  - `spring.docker.compose.file=compose.yaml`

**Warum:**
- Wunsch: keine `.yml`-Dateien mehr im Repo
- Konfigurationsstand konsistent halten (Properties + compose.yaml)

### 11:10 — Doku-Verweise auf YAML korrigiert

**Was:**
- `BACKEND_OVERVIEW.md` aktualisiert:
  - Env-Variablen auf `PETSITTER_DEMO_USERNAME` / `PETSITTER_DEMO_PASSWORD`
  - Verweise von `application.yml` auf `application.properties`
  - Test-Config-Verweis auf `src/test/resources/application.properties`
- `TECHNOLOGY_GUIDE.md` Profil-Dateinamen auf `.properties` umgestellt
- `INFOFORME.md` Referenz auf `application-local.properties` + `compose.yaml` aktualisiert

### 11:12 — Profilname von `compose` auf `local` umbenannt

**Was:**
- `application-compose.properties` in `application-local.properties` umbenannt
- Aktivierungsprofil auf `spring.config.activate.on-profile=local` gesetzt
- alten `application-compose.properties` entfernt

**Warum:**
- Wunsch aus Team: Profil soll fachlich als lokales Entwicklungsprofil benannt sein

**Verifikation:**
- `./mvnw test` ausgefuehrt
- Ergebnis: `BUILD SUCCESS` (Tests run: 1, Failures: 0, Errors: 0)

---

## 2026-05-09 | Docker-Topologie erweitert (MySQL + MongoDB)

### 11:15 — Zwei-DB-Containerstruktur umgesetzt

**Was:**
- `compose.yaml` erweitert um `petsitter-mongo` Service:
  - Image: `mongo:8`
  - Port: `27017`
  - Persistentes Volume: `petsitter-mongo-data`
  - Healthcheck via `mongosh` Ping
- bestehender MySQL-Container bleibt unveraendert fuer den relationalen App-Kern
- `application-local.properties` um Mongo-Properties ergaenzt:
  - `spring.data.mongodb.host/port/database/username/password/authentication-database`
- `README.md` Docker-Abschnitt aktualisiert (MySQL + Mongo klar dokumentiert)

**Warum:**
- Polyglot-Persistence macht fuer euren Scope Sinn:
  - Relationales Domänenmodell (User, Offer, Booking, Request) in MySQL
  - Chat-Messages dokumentenorientiert in MongoDB
- Damit ist die Chat-Infrastruktur vorbereitet, ohne den bestehenden Kern umzubauen

### 11:15 — Build/Test verifiziert

**Was:**
- `./mvnw test` ausgefuehrt

**Ergebnis:**
```
Tests run: 1, Failures: 0, Errors: 0
BUILD SUCCESS
```

**Zusatzcheck Docker:**
- `docker compose -f compose.yaml config` ausgefuehrt
- Ergebnis: Compose-Datei ist valide (MySQL + Mongo Services werden korrekt aufgeloest)

---

## 2026-05-09 | Rueckumstellung Config auf YAML

### 11:17 — `application*.properties` wieder auf `.yml` migriert

**Was:**
- `src/main/resources/application.properties` -> `src/main/resources/application.yml`
- `src/main/resources/application-local.properties` -> `src/main/resources/application-local.yml`
- `src/test/resources/application.properties` -> `src/test/resources/application.yml`
- alte `application*.properties`-Dateien entfernt, um Doppelkonfiguration zu vermeiden

**Warum:**
- Vorgabe: Konfiguration soll wieder in YAML gepflegt werden

**Kontext:**
- Inhalte 1:1 uebernommen (DB, Security-Demo, local-Profil, Mongo-Settings, Test-H2)

### 11:17 — Build/Test verifiziert

**Was:**
- `./mvnw test` ausgefuehrt

**Ergebnis:**
```
Tests run: 1, Failures: 0, Errors: 0
BUILD SUCCESS
```

---

## 2026-05-09 | Security-Dokumentation erweitert

### 11:25 — `SECREADME.md` erstellt

**Was:**
- Neue Datei `SECREADME.md` im Projekt-Root angelegt
- Inhalt dokumentiert:
  - aktuellen Security-Status
  - Aufbau von `SecurityConfig` / `SecurityFilterChain`
  - DB-basierten `UserDetailsService` inkl. Demo-Fallback
  - Login-Flow mit Vaadin `LoginView`
  - BCrypt / Method Security / Logout-Verhalten
  - naechste Security-Schritte (Rollenmodell, Tests, Registrierung)

**Warum:**
- Wunsch: zentrale, verstaendliche Security-Doku fuer Teamabstimmung und Erklaerung
- reduziert Einarbeitungsaufwand fuer Kollegen
- `KICHANGES.md` veraltete Env-/Datei-Referenz korrigiert

**Warum:**
- Dokumentation muss den aktuellen technischen Stand 1:1 widerspiegeln

### 11:10 — Build/Test verifiziert

**Was:**
- `./mvnw test` ausgefuehrt

**Ergebnis:**
```
Tests run: 1, Failures: 0, Errors: 0
BUILD SUCCESS
```


