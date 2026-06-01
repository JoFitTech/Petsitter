# Architekturbeschreibung

## Ziel der Architektur

Pawsitters ist eine Webanwendung zur Vermittlung von Tierbetreuung. Die Architektur soll folgende Ziele erfüllen:

- klare Trennung von UI, Geschäftslogik, Datenzugriff und Datenmodell,
- einfache lokale Entwicklung im Team,
- nachvollziehbare Erweiterbarkeit,
- testbare Geschäftslogik,
- sichere Authentifizierung und kontrollierter Zugriff auf nutzerbezogene Daten,
- funktionale Demo ohne verteilte Systemkomplexität.

## Architekturentscheidung

Das Projekt nutzt einen Monolithen mit Schichtenarchitektur. Es wurde bewusst keine Microservice-Architektur gewählt.

### Begründung

| Entscheidung | Begründung |
|---|---|
| Monolith statt Microservices | Drei Personen, begrenzter Projektzeitraum, geringerer Integrationsaufwand, einfachere Demo |
| Spring Boot | Java-Ökosystem, gute Integration mit Security, JPA, Validation, Tests und Maven |
| Vaadin | Java-basierte UI ohne separates JavaScript-Frontend |
| MySQL für Kerndaten | relationale Beziehungen zwischen User, Pet, Offer, Request und Booking |
| MongoDB für Chat | dokumentenorientierte Speicherung von Chatnachrichten und Konversationen |
| Docker Compose | reproduzierbare lokale Infrastruktur für MySQL und MongoDB |
| GitHub Actions | automatische Prüfung von Build und Tests |

## Systemübersicht

```text
Browser
  |
  v
Vaadin UI
  |
  v
Spring Boot Application
  |
  +-- UI Layer
  +-- Service Layer
  +-- Repository Layer
  +-- Domain Layer
  |
  +-- MySQL    Kerndaten
  +-- MongoDB  Chatdaten
```

## Schichten

### UI Layer

Der UI Layer besteht aus Vaadin-Views. Er ist für Darstellung, Benutzerinteraktion und Navigation zuständig.

Beispiele:

- Landing Page,
- Login View,
- Registrierung,
- Profilseite,
- Haustierverwaltung,
- Angebotsübersichten,
- Angebotsdetails,
- Buchungsübersichten,
- Chat View.

Der UI Layer soll keine zentrale Geschäftslogik enthalten. Fachliche Entscheidungen werden an Services delegiert.

### Service Layer

Der Service Layer enthält die Geschäftslogik. Dazu gehören Validierung, Statusübergänge, Zugriffskontrolle und Transaktionen.

Beispiele:

| Service | Aufgabe |
|---|---|
| `UserService` | Registrierung, Login, Profil, E-Mail-Änderung |
| `PetService` | Haustiere erstellen, ändern, löschen, Löschfolgen prüfen |
| `OfferService` | Angebote erstellen, suchen, anzeigen und vorbereiten |
| `RequestService` | Anfragen erstellen, abbrechen und anzeigen |
| `BookingService` | Anfragen akzeptieren, Buchungen erstellen, Status ändern |
| `NotificationService` | Benachrichtigungen erzeugen und lesen |
| `ChatService` | Konversationen, Nachrichten, Lesestatus und Chat-Notifications |
| `ChatAccessService` | Zugriff auf Chat anhand der Buchungsbeziehung prüfen |
| `LoginCodeService` | Codes erzeugen, hashen, prüfen und invalidieren |
| `PasswordPolicyService` | Passwortregeln auswerten |

### Repository Layer

Repositories kapseln den Datenbankzugriff. Für relationale Daten wird Spring Data JPA verwendet. Für Chatdaten wird Spring Data MongoDB verwendet.

Beispiele:

| Repository-Typ | Zweck |
|---|---|
| JPA-Repositories | User, Pet, Offer, OfferRequest, Booking, Notification |
| Mongo-Repositories | ChatConversationDocument, ChatMessageDocument |

### Domain Layer

Der Domain Layer enthält Entities, Enums und fachliche Datenstrukturen.

Beispiele:

- `User`,
- `Pet`,
- `Offer`,
- `OfferRequest`,
- `Booking`,
- `Notification`,
- `LoginCode`,
- `ChatConversationDocument`,
- `ChatMessageDocument`.

## Datenmodell

### User

Ein User repräsentiert einen angemeldeten Benutzer. Ein Benutzer kann fachlich sowohl Tierhalter als auch Tiersitter sein. Diese fachliche Doppelrolle ergibt sich aus seinen Aktionen im System.

Wichtige Felder:

- ID,
- E-Mail,
- Passwort-Hash,
- Vorname,
- Nachname,
- Anzeigename,
- Telefonnummer,
- Adresse,
- Account-Rolle,
- Account-Status.

Technische Rollen:

- `ADMIN`,
- `SIGNED_IN_USER`.

### Pet

Ein Haustier gehört einem User. Es kann in Angeboten referenziert werden.

Wichtige Felder:

- Name,
- Tierart,
- Rasse,
- Alter oder Eigenschaften,
- Impfstatus,
- Tags,
- Owner.

### Offer

Ein Offer beschreibt eine Betreuungsleistung oder einen Betreuungsbedarf.

Wichtige Felder:

- Ersteller,
- Zeitraum,
- Preis,
- Titel,
- Beschreibung,
- Typ,
- Status,
- zugeordnete Haustiere.

### OfferRequest

Eine Anfrage bezieht sich auf ein Offer und wird von einem anderen User gestellt.

Wichtige Status:

- `PENDING`,
- `ACCEPTED`,
- `DENIED`,
- `CANCELLED`.

### Booking

Eine Booking entsteht, wenn eine Anfrage akzeptiert wird. Sie verbindet Owner, Sitter, Angebot, Anfrage, Zeitraum und Preis.

Wichtige Status:

- `CREATED`,
- `CANCELLED`,
- weitere Status je nach finaler Implementierung.

### Chat

Der Chat ist an eine Buchung gebunden. Die Chatdaten werden in MongoDB gespeichert.

| Dokument | Zweck |
|---|---|
| `ChatConversationDocument` | Metadaten einer Konversation, Booking-ID, Owner, Sitter, Preview |
| `ChatMessageDocument` | einzelne Nachricht mit Sender, Empfänger, Zeitstempel und Lesestatus |

## Persistenz

### MySQL

MySQL speichert die relationalen Kerndaten der Anwendung:

- User,
- Pet,
- Offer,
- OfferRequest,
- Booking,
- Notification,
- LoginCode.

Flyway verwaltet die Schemaänderungen.

### MongoDB

MongoDB speichert Chatdaten. Diese Trennung wurde gewählt, weil Chatnachrichten append-orientiert sind und sich als Dokumente mit Zeitstempel und Konversationsbezug gut modellieren lassen.

### Docker Compose

Docker Compose startet lokal:

- `petsitter-mysql`,
- `petsitter-mongo`.

Die App selbst wird lokal über Maven gestartet.

## Sicherheitsarchitektur

Die Anwendung nutzt Spring Security.

Umgesetzte Punkte:

- geschützte Routen,
- Login,
- Registrierung,
- BCrypt für Passwörter,
- Registrierungscodes,
- Account-Status,
- Method Security,
- Logout mit Session-Invalidierung,
- Cookie-Löschung,
- Remember-Me,
- Chat-Zugriffskontrolle.

Die Details sind in `SECURITY_CONCEPT.md` dokumentiert.

## Zentrale Geschäftsregeln

### Registrierung und Login

- E-Mail muss gültig sein.
- Passwort muss die definierte Passwort-Policy erfüllen.
- Passwort und Passwortbestätigung müssen übereinstimmen.
- Registrierung erzeugt zunächst einen Pending-User.
- Ein Code bestätigt die Registrierung.
- Passwörter werden mit BCrypt gespeichert.

### Angebote

- Angebote werden dem angemeldeten User zugeordnet.
- Owner-Angebote können Haustiere referenzieren.
- Angebote haben einen Status.
- Such- und Filterlogik wird im Service vorbereitet.

### Anfragen

- Eine Anfrage wird als `PENDING` erstellt.
- Eigene Angebote dürfen nicht sinnvoll angefragt werden.
- Doppelte Anfragen werden verhindert.
- Nur berechtigte Nutzer dürfen Anfragen zu einem Angebot einsehen.

### Buchungen

- Eine Buchung entsteht durch Akzeptieren einer Anfrage.
- Das zugehörige Angebot wird gebucht.
- Die akzeptierte Anfrage wird auf `ACCEPTED` gesetzt.
- Andere offene Anfragen werden abgelehnt.
- Unberechtigte Nutzer dürfen keine fremden Anfragen akzeptieren.

### Chat

- Chat entsteht im Kontext einer Buchung.
- Zugriff ist nur für Owner und Sitter der Buchung erlaubt.
- Nachrichten werden persistiert.
- Neue Nachrichten erzeugen Benachrichtigungen.

## Schnittstellen innerhalb des Systems

Die Anwendung nutzt interne Java-Service-Aufrufe statt verteilter REST-Kommunikation zwischen Services. Das passt zur Monolith-Entscheidung und reduziert Netzwerkkopplung.

Die wesentlichen Schnittstellen sind:

```text
Vaadin View -> Service -> Repository -> Datenbank
```

Beispiele:

```text
RegistrateView -> UserService -> UserRepository / LoginCodeService
CreateOfferView -> OfferService -> OfferRepository / PetRepository
ChatView -> ChatService -> ChatConversationRepository / ChatMessageRepository
```

## Testing-Architektur

Tests liegen unter `src/test/java`. Es gibt Unit-Tests und Integrationstests.

Getestete Bereiche:

- UserService,
- PasswordPolicyService,
- Login und Registrierung,
- OfferService,
- RequestService,
- BookingService,
- PetService,
- FavoriteService,
- WalletService,
- ChatService,
- ChatAccessService,
- ChatEventBus,
- Security,
- UI-nahe Komponenten.

Details stehen in `TEST_DOCUMENTATION.md`.

## CI-Architektur

GitHub Actions führt bei Pushes und Pull Requests einen Build mit Tests aus:

```bash
./mvnw -B verify -Dspring.docker.compose.enabled=false
```

Docker Compose wird in der CI deaktiviert. Dadurch ist die Pipeline unabhängiger von lokalen MySQL- und MongoDB-Containern.

## Bekannte Architekturgrenzen

| Grenze | Bewertung |
|---|---|
| Monolith skaliert organisatorisch nur begrenzt | Für Projektgröße sinnvoll |
| Chat ohne produktive Ende-zu-Ende-Verschlüsselung | Für Uni-Demo ausreichend, produktiv zu ergänzen |
| Demo-Konfiguration aktiv | Für Abgabe praktisch, produktiv zu deaktivieren |
| Kein vollständiges Admin-Backend | Nicht Kernanforderung |
| Keine produktive Mail-Infrastruktur garantiert | Für lokale Demo über Logs lösbar |
| Security-Scans nicht vollständig automatisiert | zukünftige Erweiterung |

## Ausblick

Für eine produktionsnahe Weiterentwicklung wären folgende Schritte sinnvoll:

- Deployment mit HTTPS,
- Secrets über Secret Store,
- SMTP-Integration,
- erweiterte Rollen und Rechte,
- Audit-Logging,
- Rate-Limiting,
- Security-Scans in CI,
- Backups für MySQL und MongoDB,
- Monitoring,
- Datenschutzprozesse für Löschung und Auskunft.
