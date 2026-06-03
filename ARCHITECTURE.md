# Architekturbeschreibung

## Ziel der Architektur

Pawsitters ist eine Webanwendung zur Vermittlung von Tierbetreuung. Die Architektur ist darauf ausgelegt, eine vollständige Demo-Anwendung mit klar getrennten Verantwortlichkeiten, testbarer Geschäftslogik und reproduzierbarer lokaler Infrastruktur bereitzustellen.

Wichtige Ziele:

- klare Trennung von UI, Geschäftslogik, Datenzugriff und Datenmodell,
- einfache Zusammenarbeit in einem kleinen Team,
- sichere Authentifizierung und kontrollierter Zugriff auf nutzerbezogene Daten,
- nachvollziehbare Erweiterbarkeit für Chat, Bewertungen, Wallet und Bildverwaltung,
- automatisiert testbare Kernlogik,
- lauffähige lokale Demo mit Docker Compose.

## Architekturentscheidung

Das Projekt nutzt einen Monolithen mit Schichtenarchitektur. Eine Microservice-Architektur wurde bewusst nicht gewählt, weil sie für Teamgröße, Projektzeitraum und Demo-Ziel unnötig viel Integrations- und Betriebsaufwand erzeugt hätte.

| Entscheidung                 | Begründung                                                                                                                    |
| ---------------------------- | ----------------------------------------------------------------------------------------------------------------------------- |
| Monolith statt Microservices | Eine Codebasis, weniger Deployment- und Schnittstellenaufwand, einfacher zu testen und zu demonstrieren                       |
| Spring Boot                  | Gute Integration von Security, JPA, Validation, Scheduling, Tests und Maven                                                   |
| Vaadin (ergänzt durch css)   | Java-basierte UI ohne separates JavaScript-Frontend                                                                           |
| MySQL für Kerndaten          | Relationale Beziehungen, Transaktionen und Constraints für User, Pets, Offers, Requests, Bookings, Wallet, Reviews und Bilder |
| MongoDB für Chat             | Chatnachrichten sind dokumenten- und zeitstempelorientiert und müssen append-orientiert gelesen werden                        |
| Flyway                       | Versionierte und reproduzierbare Datenbankschemata                                                                            |
| Docker Compose               | Lokale Infrastruktur für MySQL und MongoDB                                                                                    |
| H2 in Tests                  | Schnelle Tests ohne externe Datenbankcontainer                                                                                |
| GitHub Actions               | Automatischer Build und Testlauf                                                                                              |

## Systemübersicht

```text
Browser
  |
  v
Vaadin UI
  |
  v
Spring Boot Monolith
  |
  +-- UI Layer
  +-- Service Layer
  +-- Repository Layer
  +-- Domain/DTO Layer
  +-- Security/Config
  |
  +-- MySQL    Kerndaten, Wallet, Reviews, Bilder, Notifications
  +-- MongoDB  Chat-Konversationen und Chatnachrichten
```

Die Anwendung nutzt interne Java-Service-Aufrufe statt verteilter REST-Kommunikation zwischen eigenen Modulen:

```text
Vaadin View -> Service -> Repository -> Datenbank
```

Beispiele:

- `RegistrateView -> UserService -> UserRepository / LoginCodeService`
- `CreateOfferView -> OfferService -> OfferRepository / PetRepository`
- `BookingService -> WalletService / ApplicationEventPublisher`
- `ChatView -> ChatService -> ChatConversationRepository / ChatMessageRepository`
- `ImageMediaController -> ImageAssetService -> ImageAssetVariantRepository`

## Ergänzende Architekturdiagramme

Neben dieser textuellen Architekturbeschreibung gibt es weitere Diagramme, die einzelne Sichten auf das Projekt dokumentieren:

- ein Klassendiagramm, das zu Beginn der Entwicklung als fachliche und technische Strukturierung erstellt wurde,
- ein Aktivitätsdiagramm für den Ablauf beim Erstellen eines Offers,
- ein Aktivitätsdiagramm für die Account-Flows Login, Passwort vergessen und Registrierung,
- eine Docker-Visualisierung der lokalen Infrastruktur und Container-Zusammenhänge.

Das Klassendiagramm diente vor allem der frühen Abstimmung über zentrale Domänenobjekte und Beziehungen. Die Aktivitätsdiagramme beschreiben wichtige Nutzer- und Systemabläufe, während die Docker-Visualisierung zeigt, wie Anwendung, MySQL und MongoDB in der lokalen Entwicklungsumgebung zusammenspielen.

## Schichten

### UI Layer

Der UI Layer besteht aus Vaadin-Views, CSS und UI-Komponenten. Er ist für Darstellung, Navigation, Benutzerinteraktion und UI-nahe Validierung zuständig.

Beispiele:

- Start- und Suchseiten,
- Login, Registrierung und Passwort-Reset,
- Profil- und Haustierverwaltung,
- Angebots- und Filteransichten,
- Buchungsübersicht,
- Chat,
- Wallet-Ansicht,
- Bewertungsdialoge,
- Bild-Upload und Zuschnitt.

Geschäftsregeln sollen nicht zentral in Views liegen. Views rufen Services auf und zeigen deren Ergebnisse, Fehler oder DTOs an.

### Service Layer

Der Service Layer enthält die Geschäftslogik. Hier liegen Validierung, Statusübergänge, Transaktionen, Zugriffskontrollen und fachliche Fehlerbehandlung.

| Service                 | Aufgabe                                                                                  |
| ----------------------- | ---------------------------------------------------------------------------------------- |
| `UserService`           | Registrierung, Login, Profil, E-Mail-Änderung, öffentliche Profile                       |
| `LoginCodeService`      | Registrierungscodes erzeugen, hashen, prüfen und invalidieren                            |
| `PasswordResetService`  | Passwort-Reset-Codes und Passwortänderung                                                |
| `PasswordPolicyService` | Passwortregeln und schwache Muster prüfen                                                |
| `PetService`            | Haustiere erstellen, ändern, löschen und Löschfolgen analysieren                         |
| `OfferService`          | Angebote erstellen, bearbeiten, löschen, suchen und Filterlogik anwenden                 |
| `RequestService`        | Anfragen erstellen, abbrechen und berechtigt anzeigen                                    |
| `BookingService`        | Anfragen akzeptieren, Buchungen verwalten, wiederkehrende Buchungen und Abschluss-Events |
| `WalletService`         | Demo-Guthaben, Treuhandzahlungen, Erstattungen, Auszahlung und wiederkehrende Zahlungen  |
| `NotificationService`   | Benachrichtigungen erzeugen, lesen und zählen                                            |
| `ChatService`           | Konversationen, Nachrichten, Lesestatus, Review- und Request-Karten                      |
| `ChatAccessService`     | Chat-Zugriff anhand der Booking- oder Teilnehmerbeziehung prüfen                         |
| `UserReviewService`     | Bewertungen nach abgeschlossenen Buchungen, Rating-Statistiken und Review-Karten         |
| `ImageAssetService`     | Profil- und Haustierbilder validieren, optimieren, speichern und ausliefern              |
| `PostalCodeService`     | Deutsche PLZ validieren, Ortsdaten cachen und Distanzen berechnen                        |
| `FavoriteService`       | Favoriten für Angebote verwalten                                                         |

Kritische Service-Methoden sind transaktional. Ein Beispiel ist `BookingService.acceptRequest`: Die Anfrage wird akzeptiert, ein Booking wird erstellt, das Offer wird gebucht, andere offene Anfragen werden abgelehnt und anschließend wird ein `BookingCreatedEvent` veröffentlicht.

### Repository Layer

Repositories kapseln den Datenbankzugriff. Für relationale Daten wird Spring Data JPA genutzt, für Chatdaten Spring Data MongoDB.

| Repository-Typ     | Beispiele                                                                                                                                                                      |
| ------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| JPA-Repositories   | `UserRepository`, `PetRepository`, `OfferRepository`, `OfferRequestRepository`, `BookingRepository`, `WalletAccountRepository`, `UserReviewRepository`, `ImageAssetRepository` |
| Mongo-Repositories | `ChatConversationRepository`, `ChatMessageRepository`                                                                                                                          |

Die Services bleiben dadurch frei von direkter SQL- oder Mongo-Abfragelogik. Spezielle Abfragen wie Locks, Statusfilter, Suche nach offenen Offers oder Chat-Inbox werden über Repository-Methoden modelliert.

### Domain- und DTO-Layer

Der Domain Layer enthält Entities, Dokumente, Enums und fachliche Datenstrukturen. DTOs trennen die UI-Darstellung von persistierten Entities.

Wichtige Domain-Bereiche:

- User, LoginCode, PasswordResetCode,
- Pet und PetTag,
- Offer und OfferRequest,
- Booking und BookingPause,
- Notification,
- ChatConversationDocument und ChatMessageDocument,
- WalletAccount, BookingPayment, RecurringBookingPayment und WalletTransaction,
- UserReview,
- ImageAsset und ImageAssetVariant,
- PostalCodeLocation.

DTOs wie `OfferCardDto`, `BookingDto`, `WalletSummaryDto`, `PublicUserProfileDto` oder `ChatConversationDto` liefern UI-spezifische Ansichten und verhindern, dass Views direkt mit vollständigen Entities arbeiten müssen.

### Config und Infrastruktur

Konfigurationsklassen bündeln technische Querschnittsthemen:

- `SecurityConfig` für Spring Security,
- `PetsitterSecurityProperties` für Demo-Login-Konfiguration,
- `MongoDbUuidConfig` für UUID-Verarbeitung in MongoDB,
- `DockerComposeStartupGuard` für verständliches lokales Startverhalten,
- `PetsitterAppShell` für Vaadin Push.

## Zentrale Fachmodule

### Benutzer, Registrierung und Login

Benutzer registrieren sich mit E-Mail, Passwort und persönlichen Daten. Die Registrierung erzeugt zunächst einen Pending-Account. Ein 6-stelliger Registrierungscode bestätigt den Account. Der Code wird gehasht gespeichert, hat eine begrenzte Laufzeit und zählt Fehlversuche.

Login erfolgt über E-Mail und Passwort. Nur verifizierte Accounts dürfen sich anmelden. Pending- und Blocked-Accounts werden abgelehnt.

Passwort-Reset ist ein eigener Flow mit separater Code-Tabelle. Auch Reset-Codes werden gehasht gespeichert, zeitlich begrenzt und gegen Fehlversuche geschützt.

### Haustiere

Haustiere gehören genau einem User. Sie können in Tierhalter-Angeboten referenziert werden. Der Service prüft Ownership und beschreibt Löschfolgen, bevor Haustiere entfernt werden. Bei Angeboten mit mehreren Haustieren kann ein Haustier entfernt werden, ohne automatisch das gesamte Angebot zu löschen.

### Angebote und Suche

Ein Offer beschreibt entweder:

- ein Tierhalter-Angebot, bei dem Betreuung gesucht wird, oder
- ein Sitter-Angebot, bei dem Betreuung angeboten wird.

Angebote besitzen Status, Preis, Zeitraum oder wiederkehrende Wochentage, Betreuungsart, Tierart und optionale Haustierbezüge. Die Suche filtert offene und sichtbare Angebote nach Rolle, Datum, Preis, Betreuungsart, Frequenz, Tierart und Entfernung.

Die Distanzsuche nutzt deutsche PLZ-Daten. `PostalCodeService` validiert PLZ, ruft bei Bedarf Ortsdaten über einen Client ab, cached Koordinaten in MySQL und berechnet Entfernungen mit einer Haversine-Formel.

### Anfragen

Eine Anfrage bezieht sich auf ein Offer und wird als `PENDING` erstellt. Eigene Angebote können nicht angefragt werden. Doppelte Anfragen werden verhindert. Nur berechtigte Nutzer dürfen Anfragen zu einem Angebot einsehen oder abbrechen.

### Buchungen

Eine Buchung entsteht durch Annahme einer Anfrage. Dabei werden Owner, Sitter, Offer, Request, Preis, Zeitraum und Status in einer transaktionalen Operation verbunden.

Wichtige Status:

- `CREATED`,
- `CANCELLED`,
- `COMPLETED`,
- `ENDED`.

Einmalige Buchungen haben Start- und Enddatum. Regelmäßige Buchungen nutzen Wochentage und können beendet oder pausiert werden. Beim Abschluss einer Buchung veröffentlicht `BookingService` ein `BookingCompletedEvent`.

### Wallet und Zahlungen

Das Wallet-Modul bildet Demo-Guthaben und Treuhandzahlungen ab. Bei einer Buchung wird Geld vom Owner zurückgehalten. Nach Abschluss kann der Owner auszahlen oder der Sitter eine Auszahlung anfordern. Wenn eine Auszahlung angefordert wurde, kann sie nach einer Frist automatisch freigegeben werden.

Für regelmäßige Buchungen werden wöchentliche Zahlungen erzeugt. Pausen können gehaltene Zahlungen neu berechnen. Alle Bewegungen werden als Wallet-Transaktionen dokumentiert.

### Chat

Der Chat ist an Anfragen, Teilnehmerpaare und Buchungen gekoppelt. Chat-Konversationen und Nachrichten liegen in MongoDB. Konversationslisten, Nachrichten, Lesestatus und Live-Events laufen über `ChatService`.

Vaadin Push und ein In-Memory-`ChatEventBus` sorgen für UI-Aktualisierungen. In einer Multi-Instance-Produktion müsste dieser EventBus durch eine verteilte Lösung wie Redis ersetzt werden.

Zugriff ist nur für Owner und Sitter bzw. beteiligte Nutzer erlaubt. Chatnachrichten erzeugen Notifications. Systemnachrichten wie Request-Karten, Review-Karten und Review-Reminder werden ebenfalls als Chatnachrichten gespeichert.

### Bewertungen

Bewertungen sind nur nach abgeschlossenen Buchungen erlaubt. Owner und Sitter dürfen jeweils ihren Gegenpart bewerten. Selbstbewertungen, fremde Buchungen, Bewertungen vor Abschluss, ungültige Sternzahlen, zu lange Kommentare und doppelte Bewertungen werden verhindert.

Rating-Zusammenfassungen und aktuelle Bewertungen werden in öffentlichen Profilen und Angebotskarten genutzt. Nach einer Bewertung kann eine Review-Karte in den Chat geschrieben werden.

### Benachrichtigungen

Notifications informieren über Ereignisse wie Chatnachrichten, Zahlungsanforderungen, Auszahlung, Wallet-Aufladung und weitere Statusänderungen. Sie werden relational in MySQL gespeichert, weil sie direkt zu Nutzern und App-Kerndaten gehören.

### Bildverwaltung

Profil- und Haustierbilder werden nicht als externe Dateien gespeichert. Nach Upload und Zuschnitt speichert `ImageAssetService` optimierte JPEG-Varianten in MySQL:

- `AVATAR` mit 256 px,
- `DISPLAY` mit 768 px.

Uploads werden auf MIME-Typ, tatsächlichen Bildinhalt, Dateigröße und Pixelanzahl geprüft. Öffentliche Bildvarianten werden über unveränderliche URLs unter `/media/images/{assetId}/{variant}` ausgeliefert. Beim Löschen von Besitzern entfernen Datenbank-Cascade-Regeln zugehörige Bildvarianten.

### Favoriten

Favoriten erlauben Nutzern, fremde verfügbare Offers zu speichern. Eigene, abgelaufene, nicht offene oder unbekannte Angebote werden nicht als Favorit akzeptiert.

## Persistenz

### MySQL

MySQL speichert die relationalen Kerndaten:

- User, Codes und Profile,
- Pets,
- Offers und OfferRequests,
- Bookings und Pausen,
- Notifications,
- Wallet-Konten, Zahlungen und Transaktionen,
- Reviews,
- ImageAssets und Varianten,
- PLZ-Ortscache.

Flyway verwaltet das Schema über versionierte Migrationen unter `src/main/resources/db/migration`.

### MongoDB

MongoDB speichert Chat-Konversationen und Chatnachrichten. Diese Trennung passt zum append-orientierten Charakter von Chatdaten. Bildreferenzen und Userdaten werden beim Lesen aus MySQL ergänzt und nicht redundant in MongoDB kopiert.

### Tests mit H2

Tests nutzen H2 und deaktivieren Docker Compose. Dadurch laufen Unit- und Integrationstests unabhängig von lokalen MySQL- oder MongoDB-Containern.

## Interne Events

Die Anwendung nutzt Spring Events zur Entkopplung:

| Event                   | Auslöser                                      | Reaktion                                                               |
| ----------------------- | --------------------------------------------- | ---------------------------------------------------------------------- |
| `BookingCreatedEvent`   | Anfrage wurde akzeptiert und Booking erstellt | Chat-Konversation wird angelegt oder verknüpft                         |
| `BookingCompletedEvent` | Booking wird als abgeschlossen markiert       | Review-Reminder wird nach erfolgreichem Commit in den Chat geschrieben |

Der Review-Reminder läuft bewusst non-blocking: Fehler im Chat dürfen den fachlich erfolgreichen Booking-Abschluss nicht rückgängig machen.

## Sicherheitsarchitektur

Spring Security schützt alle nicht öffentlichen Routen. Öffentliche Routen sind begrenzt, unter anderem Startseite, Login, Registrierung, Passwort-Reset, statische Assets und öffentliche Bildvarianten.

Weitere Security-Punkte:

- BCrypt für Passwörter und Codes,
- Account-Status `PENDING`, `VERIFIED`, `BLOCKED`,
- technische Rollen `ADMIN` und `SIGNED_IN_USER`,
- Method Security mit JSR-250-Unterstützung,
- Session-Invalidierung und Cookie-Löschung beim Logout,
- Remember-Me mit 7 Tagen und DB-basiertem UserDetailsService,
- Service-Level-Zugriffskontrollen gegen IDOR.

Details stehen in `SECURITY_CONCEPT.md`.

## Testing-Architektur

Tests liegen unter `src/test/java`. Es gibt Unit-Tests, Integrationstests, Smoke-Tests und UI-nahe Komponententests. Die Tests decken Services, Security, Chat, Wallet, Bilder, Bewertungen, PLZ, Suche, UI-Komponenten und Infrastruktur ab.

Details stehen in `TEST_DOCUMENTATION.md`.

## CI-Architektur

GitHub Actions führt bei Pushes und Pull Requests Maven Verify aus:

```bash
./mvnw -B verify -Dspring.docker.compose.enabled=false
```

Docker Compose wird in der CI deaktiviert, damit die Pipeline ohne lokale Container startet.

## Bekannte Architekturgrenzen

| Grenze                                                | Bewertung                                                        |
| ----------------------------------------------------- | ---------------------------------------------------------------- |
| Monolith skaliert organisatorisch nur begrenzt        | Für Projektgröße und Demo sinnvoll                               |
| In-Memory-ChatEventBus ist nicht Multi-Instance-fähig | Für lokale Demo ausreichend, produktiv durch Redis o.ä. ersetzen |
| Demo-Konfiguration ist lokal aktiv                    | Für Abgabe praktisch, produktiv deaktivieren                     |
| Kein fachlicher Admin-Bereich                         | Rolle `ADMIN` bleibt nur Erweiterungspunkt                       |
| Kein produktiver Mailversand                          | Codes werden lokal angezeigt, SMTP wäre produktiv nötig          |
| Keine vollständigen E2E-Browsertests                  | Unit-, Integration- und UI-nahe Tests decken Kernlogik ab        |
| Keine automatisierten Security-Scans                  | Als produktionsnahe Erweiterung sinnvoll                         |

## Ausblick

Für eine produktionsnahe Weiterentwicklung wären sinnvoll:

- Deployment mit HTTPS und sicheren Cookie-Flags,
- Secret Store statt Dev-Fallbacks,
- SMTP-Integration,
- Rate-Limiting und CAPTCHA für Code-Flows,
- verteiltes Chat-Eventing,
- Audit-Logging,
- Backup- und Restore-Prozesse,
- Monitoring und Alerting,
- Datenschutzprozesse für Löschung und Auskunft,
- Security-Scans in CI,
- echte Browser-End-to-End-Tests.
