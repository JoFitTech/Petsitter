# Architecture – Petsitter Backend

## Zielbild

Das Petsitter-Backend ist als **monolithischer Schichtenaufbau** mit **Polyglot Persistence** strukturiert:

- **Präsentation**: Vaadin 25 (Web-Framework)
- **Geschäftslogik**: Spring Boot Services mit transaktionalen Rules
- **Persistenz**: MySQL (relationale Daten) + MongoDB (Chat/Events)
- **Sicherheit**: Spring Security (Form Login + passwortloses Email-Code-Auth)
- **Infrastruktur**: Docker Compose (MySQL 8.4 + MongoDB 8 + H2 für Tests)

## Schichten

### 1. **UI-Schicht** (`ui/`)

- **Komponenten**: Vaadin Views mit responsiven Layouts
- **Verantwortung**: Event-Handling, User-Feedback, Formvalidierung
- **Sicherheit**: `@RolesAllowed`, `@AnonymousAllowed`, `@PermitAll`
- **Beispiele**:
  - `OwnerStartView` / `SitterStartView` – Rollenspezifische Landingpages
  - `ChatView` – Echtzeit-Chat mit EventBus + Vaadin Push
  - `LoginView` – Passwortlos via Email+Code

### 2. **Service-Schicht** (`*/service/`)

- **Komponenten**: Spring `@Service` Beans mit Business Rules
- **Verantwortung**: Geschäftslogik, Validierungen, Transaktionen
- **Sicherheit**:
  - Pre-Condition-Checks (vor DB-Operationen)
  - Catch-Handler für Race-Conditions (z.B. `DataIntegrityViolationException` → fachliche Exceptions)
  - Logging critical Paths
- **Beispiele**:
  - `UserReviewService` – Bewertungssystem mit Duplikat-Protection
  - `ChatService` – Chat-Message-Handling + Notification-Trigger
  - `OfferService` – Offer-Management mit Matching-Logik
  - `BookingService` – 4-schrittige atomare Acceptance-Logik

### 3. **Repository-Schicht** (`*/repository/`)

- **Komponenten**: Spring Data JPA (`JpaRepository`) + MongoDB (MongoRepository)
- **Verantwortung**: Query-DSL, Pagination, Indizes
- **Beispiele**:
  - `UserRepository` – findByEmail, custom Queries
  - `OfferRepository` – findByStatusAndType, Top-N Queries
  - `ChatConversationRepository` – Mongo: findByBookingId (unique), findByOwnerOrSitter
  - `ChatMessageRepository` – Mongo: chronologische Queries per conversationId

### 4. **Domain-Schicht** (`*/domain/`)

- **Komponenten**: JPA `@Entity` oder Mongo `@Document`
- **Verantwortung**: Datenmodell, Constraints, Relationships
- **Lifecycle Hooks**: `@PrePersist`, `@PreUpdate` für Timestamps
- **Beispiele**:
  - **JPA Entities**: `User`, `Offer`, `Booking`, `OfferRequest`, `UserReview`, `Notification`
  - **Mongo Documents**: `ChatConversationDocument`, `ChatMessageDocument`

### 5. **DTO-Schicht** (`*/dto/`)

- **Komponenten**: Java Records oder Klassen für Data Transfer
- **Verantwortung**: UI-spezifische Datenansichten, Feldmapping
- **Beispiele**:
  - `BookingDto` – Für UI-Anzeige mit aggregierten Feldern
  - `ChatConversationDto` – Mit Display-Namen, Previews
  - `UserRatingSummary` – Aggregierte Bewertungsstatistiken

### 6. **Security-Schicht** (`security/`)

- **Komponenten**: `SecurityConfig`, `UserDetailsService`, Interceptors
- **Verantwortung**: Authentication, Authorization, CSRF/CORS
- **Sicherheit**:
  - Passwortlose Login: Email → 6-stelliger Code → SecurityContext
  - Remember-Me: 7-Tage Token-Persistierung
  - Method-Security: `@RolesAllowed`, `@Secured`
  - CSRF: Vaadin-integrated (XSRF-Token auto)
- **Flows**:
  - Unbekannter User → Auto-Registrierung bei erstem Code-Login
  - Bekannter User → Code-Validierung → Session-Token generiert
  - Logout → Session + Remember-Me-Token gelöscht

### 7. **Config-Schicht** (`config/`)

- **Komponenten**: Spring Configuration-Klassen
- **Verantwortung**: Bean-Management, Property-Binding, Third-Party-Setup
- **Beispiele**:
  - `MongoDbUuidConfig` – UUID-Codec für Mongo-Queries
  - `SecurityConfig` – FilterChain, AuthenticationProvider, Logout-Handler
  - Docker Compose Auto-Start

## Entscheidungen

### 1. **Monolith statt Microservices**
- **Grund**: Einfache Deployment, Single Database für transaktionale Konsistenz
- **Trade-off**: Später skalierbar, heute schneller zu entwickeln
- **Fallback**: Mono-Repo, klare Schichtengrenzen für spätere Extraktion

### 2. **MySQL + MongoDB (Polyglot)**
- **MySQL**: User, Angebote, Buchungen (relationale Integrität)
- **MongoDB**: Chat-Messages (append-only, scale-out ready)
- **Grund**: Separate Concerns, spätere Event-Sourcing möglich
- **Synchronisation**: Über Events (ApplicationEventPublisher)

### 3. **Passwortloses Email+Code Login**
- **Grund**: Weniger Phishing, keine Password-Reset-Komplexität
- **Implementierung**:
  - 6-stelliger Code (Random, gehashed with BCrypt)
  - 10 Min Gültigkeit
  - Max 3 Versuche (Rate-Limiting)
  - Auto-User-Creation bei erstem Login
- **Audit**: `requestIp`, `createdAt`, `usedAt` in `LoginCode` Entity

### 4. **Race-Condition Handling in Services**
- **Pattern**: Pre-Check + Catch-Handler
- **Beispiel** (UserReviewService):
  - 1. `existsByBooking_IdAndReviewer_Id()` – Soft-Check
  - 2. Save → Exception bei Duplicate
  - 3. Catch `DataIntegrityViolationException` → Log + Business-Exception
- **Vorteil**: Transaktionale Sicherheit, klare Fehlermeldungen

### 5. **Vaadin Push für Echtzeit-Chat**
- **Grund**: Bi-direktionale Kommunikation ohne Polling
- **Implementierung**:
  - `@Push` in `PetsitterAppShell` (Vaadin 25 AppShell-Pattern)
  - `ChatEventBus` (in-memory Pub/Sub)
  - UI-Thread-Safe via `ui.access()`
- **Fallback**: Polling-Fallback in Browser wenn Push-Transport ausfällt

### 6. **Transaktionale Atomarität**
- **Pattern**: `@Transactional` auf kritischen Service-Methoden
- **Beispiel** (BookingService.acceptRequest):
  - 1. Request → ACCEPTED (save)
  - 2. Booking erstellt (save)
  - 3. Offer → BOOKED (save)
  - 4. andere Requests → DENIED (bulk update)
  - Alles unter einer TX: Entweder alles erfolgreich oder alles rollback
- **Benefits**: Keine Zwischenzustände, konsistente Geschäftslogik

## Abhängigkeitsbaum

```
UI-Schicht
  ↓
  ├→ Service-Schicht (Business Rules)
  ├→ DTO-Schicht (Data Mapping)
  └→ Security-Schicht (Auth/Authz)
       ↓
       ├→ Domain-Schicht (Entities/Docs)
       ├→ Repository-Schicht (Queries)
       └→ Config-Schicht (Bean Setup)
            ↓
            MySQL / MongoDB (Persistent Storage)
            H2 (Test-Storage)
```

## Fehlerbehandlung

- **Custom Exceptions** (in `shared/exception/`):
  - `NotFoundException` – Resource nicht vorhanden
  - `ForbiddenOperationException` – Zugriff denied
  - `BusinessRuleViolationException` – Geschäftsregel verletzt
  - `DuplicateRequestException` – Duplikat-Check failed

- **Fallback-Patterns**:
  - **Chat-Notifications**: Primary (Booking-Lookup) → Fallback (User-Lookup)
  - **User-Creation**: Soft-Check → Catch DataIntegrityViolation → Reload

## Testing-Strategie

- **Unit-Tests** (MockRepository): Fast, Regression-Checks
- **Integration-Tests** (Spring Context + H2): Real TX, DB-Verhalten
- **UI-Tests** (Vaadin TestKit): User-Flows
- **Test-Coverage**: Ziel **90%+** für kritische Services

---

*Letztes Update: 2026-06-01 (Hardening-Commit: UserReviewService)*
