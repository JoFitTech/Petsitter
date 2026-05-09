# Backend-Übersicht für Kollegen

## 🎯 Was ist schon gemacht?

Das Backend für **Petsitter** ist als **monolithischer Spring Boot Microservice** mit einer sauberen Schichtenarchitektur aufgebaut.

### Aktuelle Status

| Komponente | Status | Details |
|------------|--------|---------|
| **Entities & Domain** | ✅ Vollständig | User, Pet, Offer, Request, Booking, Notification |
| **Repositories (JPA)** | ✅ Vollständig | Spring Data JPA Interfaces für alle Entities |
| **Services (Geschäftslogik)** | 🔄 Skelette | Struktur da, Implementierung in Phase 2 |
| **Security** | ✅ Basis | Spring Security, BCrypt, Form Login vorbereitet |
| **Persistenz** | ⏳ Kommt | Flyway Migrations (`V1__create_schema.sql`) in Phase 1 |
| **Tests** | 🔄 Minimal | Nur Smoke-Test, Unit-/Integration-Tests in Phase 2 |
| **UI (Vaadin)** | ⏳ Kommt | Phase 4 |

---

## 🏗️ Architektur-Ansatz

### Schichten (von oben nach unten)

```
┌─────────────────────────────────────────┐
│  UI Layer (Vaadin Views)                │ Phase 4
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│  Service Layer (Geschäftslogik)         │ Service Classes
│  - OfferService                         │ mit Transaktionen,
│  - RequestService                       │ Validierung,
│  - BookingService                       │ Businessregeln
│  - PetService, UserService, etc.        │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│  Repository Layer (JPA)                 │ Spring Data JPA
│  - UserRepository                       │ Interfaces
│  - PetRepository                        │ (automatisch
│  - OfferRepository                      │  implementiert)
│  - etc.                                 │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│  Domain/Entity Layer                    │ JPA Entities
│  - User, Pet, Offer, Request            │ mit JSR-380
│  - Booking, Notification                │ Validierung
│  - Enums: PetSpecies, OfferType, etc.   │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│  Persistenz (MySQL 8.4)                 │ Docker Compose
│  - Flyway Migrations                    │ + H2 für Tests
└─────────────────────────────────────────┘
```

### Kern-Prinzipien

1. **Keine Geschäftslogik in Entities** – Entities sind reine Datenmodelle.
2. **Alle Geschäftsregeln gehören in Services** – mit Transaktionen und Validierung.
3. **Repositories sind nur Query-Layer** – nutzen Spring Data JPA.
4. **Exceptions für Fehlerbehandlung** – `NotFoundException`, `ForbiddenOperationException`, `BusinessRuleViolationException`.

---

## 🔑 Wichtigste Geschäftsregeln (in Services implementiert)

### Offer-Management

| Regel | Ort | Kurz |
|-------|-----|------|
| OWNER_OFFER braucht Pet | `OfferService.createOwnerOffer()` | Validierung im Service |
| SITTER_OFFER braucht kein Pet | `OfferService.createSitterOffer()` | `pet` ist nullable |
| Nur Creator darf bearbeiten | `OfferService.updateOffer()` | Guard-Clause |
| Booked Offer nicht bearbeitbar | `OfferService.updateOffer()` | Status-Check |
| Bei Edit: PENDING Requests → DENIED | `OfferService.updateOffer()` | Service-Transaktion |

### Request-Management

| Regel | Ort | Kurz |
|-------|-----|------|
| Kein Request auf eigenes Offer | `RequestService.createRequest()` | `requester.id != offer.creator.id` |
| Ein User pro Offer | `RequestService.createRequest()` | Unique DB-Constraint |
| Nur auf OPEN Offers | `RequestService.createRequest()` | Status-Check |

### Booking-Management

| Regel | Ort | Kurz |
|-------|-----|------|
| Accept erzeugt Booking | `BookingService.acceptRequest()` | neue Booking-Entity |
| Accept → Offer BOOKED | `BookingService.acceptRequest()` | Offer.status = BOOKED |
| Accept → andere Requests DENIED | `BookingService.acceptRequest()` | Batch Update |
| Nur Creator darf akzeptieren | `BookingService.acceptRequest()` | Guard-Clause |

---

## 📦 Entity-Modell (was ist was?)

### User

```
User
├─ id: Long (PK)
├─ email: String (UNIQUE, für Login)
├─ passwordHash: String (BCrypt)
├─ firstName, lastName
├─ phone, city
├─ createdAt, updatedAt (auto)
├─ Relation: Pets (1:N, Owner)
├─ Relation: Offers (1:N, Creator)
└─ Relation: Requests (1:N, Requester)
```

**Fachlich:** Kann Owner UND Sitter zugleich sein (Dual-Role).

---

### Pet

```
Pet
├─ id: Long (PK)
├─ name: String
├─ species: PetSpecies (Enum: DOG, CAT, BIRD, RABBIT, OTHER)
├─ breed: String
├─ age: Integer
├─ notes: Text
├─ owner: User (N:1, NOT NULL)
├─ Relation: Offers (1:N, pet)
└─ createdAt (auto)
```

**Fachlich:** Haustier gehört genau einem Owner. Wird von Owner in OWNER_OFFER referenziert.

---

### Offer

```
Offer
├─ id: Long (PK)
├─ creator: User (N:1, NOT NULL)
├─ type: OfferType (Enum: OWNER_OFFER, SITTER_OFFER)
├─ pet: Pet (N:1, nullable) – nur bei OWNER_OFFER gefüllt
├─ startDate, endDate: LocalDate
├─ city: String
├─ pricePerWeek: BigDecimal
├─ description: String
├─ status: OfferStatus (OPEN, BOOKED, CANCELLED)
├─ version: Long (@Version für Optimistic Locking)
├─ Relation: Requests (1:N, offer)
├─ Relation: Booking (1:1, offer)
└─ createdAt, updatedAt (auto)
```

**Fachlich:** Zentrale Entity. Owner/Sitter suchen/bieten Betreuung an.

---

### OfferRequest

```
OfferRequest
├─ id: Long (PK)
├─ offer: Offer (N:1, NOT NULL)
├─ requester: User (N:1, NOT NULL)
├─ status: RequestStatus (PENDING, ACCEPTED, DENIED, CANCELLED)
├─ message: String (Nachricht des Requesters)
├─ createdAt, updatedAt (auto)
└─ Unique: (offer_id, requester_id) – ein Request pro User/Offer
```

**Fachlich:** Request zum Annehmen eines Offers. Von Sitter für OWNER_OFFER, von Owner für SITTER_OFFER.

---

### Booking

```
Booking
├─ id: Long (PK)
├─ offer: Offer (1:1, NOT NULL)
├─ acceptedRequest: OfferRequest (1:1, NOT NULL)
├─ owner: User (N:1, NOT NULL)
├─ sitter: User (N:1, NOT NULL)
├─ pet: Pet (N:1, nullable)
├─ startDate, endDate: LocalDate
├─ pricePerWeek: BigDecimal
├─ status: BookingStatus (CREATED, CANCELLED, COMPLETED)
└─ createdAt (auto)
```

**Fachlich:** Entsteht, wenn ein Request akzeptiert wird. Definiert die tatsächliche Betreuung.

---

### Notification

```
Notification
├─ id: Long (PK)
├─ recipient: User (N:1, NOT NULL)
├─ type: NotificationType (REQUEST_DENIED, REQUEST_ACCEPTED, OFFER_CHANGED, BOOKING_CREATED)
├─ message: String
├─ isRead: Boolean
└─ createdAt (auto)
```

**Fachlich:** Placeholder für Benachrichtigungen. In-App, keine E-Mail (noch nicht).

---

## 🔐 Security-Modell

### Aktuelle Implementierung

| Komponente | Was | Wo |
|------------|-----|-----|
| **PasswordEncoder** | BCrypt Hashing | `SecurityConfig.java:50-53` |
| **UserDetailsService** | Automatisch aus DB (Spring Security Integration) | `SecurityConfig.java:55+` |
| **Security Filter Chain** | Form Login, CSRF aktiv, Auth required | `SecurityConfig.java:34-37` |
| **Demo-Credentials** | env vars `DEMO_USERNAME`, `DEMO_PASSWORD` | `SecurityConfig.java:41-47` |

### Für Demo (lokale Entwicklung)

```properties
# In application.yml oder als Env-Var
DEMO_USERNAME=localuser
DEMO_PASSWORD=localpass
# Passwort wird mit BCrypt gehasht
```

### Was noch fehlt

- [ ] E-Mail-Verifikation
- [ ] Rate Limiting
- [ ] Audit Logs
- [ ] RBAC (Rollen-Modell)

---

## 🧪 Test-Strategie

### Phase 1: Unit Tests (in Arbeit)

Ziel: 14-18 Tests für Kernregeln.

```
OfferService Tests
├─ createOwnerOffer_withPet_success
├─ createOwnerOffer_withoutPet_fails
├─ updateBookedOffer_fails
└─ updateOfferWithPendingRequests_deniesRequests

RequestService Tests
├─ createRequest_success
├─ createRequestOnOwnOffer_fails
├─ createDuplicateRequest_fails
└─ createRequestOnBookedOffer_fails

BookingService Tests
├─ acceptRequest_createsBooking
├─ acceptRequest_setsOfferBooked
├─ acceptRequest_deniesOtherRequests
└─ acceptRequestByNonCreator_fails
```

### Phase 2: Integration Tests

Mit H2 in-Memory DB.

### Phase 3: CI/CD

GitHub Actions + Maven Verify.

---

## 📂 Dateistruktur (wichtig zu verstehen)

```
src/main/java/com/softwareengineering/petsitter/
├─ user/
│  ├─ domain/User.java               Entity + Validation
│  ├─ repository/UserRepository.java  JPA Interface
│  └─ service/UserService.java        Business Logic
├─ pet/
│  ├─ domain/Pet.java
│  ├─ domain/PetSpecies.java          Enum
│  ├─ repository/PetRepository.java
│  └─ service/PetService.java
├─ offer/
│  ├─ domain/Offer.java
│  ├─ domain/OfferType.java           Enum
│  ├─ domain/OfferStatus.java          Enum
│  ├─ repository/OfferRepository.java
│  └─ service/OfferService.java        ⭐ Kernlogik
├─ offerrequest/
│  ├─ domain/OfferRequest.java
│  ├─ domain/RequestStatus.java        Enum
│  ├─ repository/OfferRequestRepository.java
│  └─ service/RequestService.java      ⭐ Kernlogik
├─ booking/
│  ├─ domain/Booking.java
│  ├─ domain/BookingStatus.java        Enum
│  ├─ repository/BookingRepository.java
│  └─ service/BookingService.java      ⭐ Kernlogik
├─ notification/
│  ├─ domain/Notification.java
│  ├─ domain/NotificationType.java     Enum
│  ├─ repository/NotificationRepository.java
│  └─ service/NotificationService.java
├─ shared/
│  └─ exception/
│     ├─ NotFoundException.java
│     ├─ ForbiddenOperationException.java
│     ├─ BusinessRuleViolationException.java
│     └─ DuplicateRequestException.java
├─ security/
│  └─ SecurityConfig.java              Spring Security
└─ PetsitterApplication.java            Main Class
```

---

## 🚀 Nächste Schritte (Phasen)

### Phase 1: Persistenzbasis (2-3 Tage)

- [ ] Flyway Migration `V1__create_schema.sql` (Tabellen + Constraints)
- [ ] Flyway Migration `V2__insert_demo_data.sql` (Test-Daten: 4 User, 3 Pets, 5 Offers)
- [ ] `docker compose up -d` + `./mvnw spring-boot:run -Dspring-boot.run.profiles=compose`
- [ ] Smoke Test: DB lädt, Demo-Daten sichtbar

### Phase 2: Geschäftslogik (3-4 Tage)

- [ ] `OfferService` implementieren (createOwnerOffer, createSitterOffer, updateOffer, cancelOffer)
- [ ] `RequestService` implementieren (createRequest, cancelRequest)
- [ ] `BookingService` implementieren (acceptRequest mit Transaktion!)
- [ ] 14-18 Unit Tests schreiben
- [ ] `./mvnw test` grün

### Phase 3: Security (1-2 Tage)

- [ ] UserDetailsService vollständig
- [ ] Login/Logout UI (minimal)
- [ ] Spring Security Config härtung
- [ ] Demo-Login testen

### Phase 4: Vaadin UI (3-4 Tage)

- [ ] Views vor die Services binden
- [ ] Kernflows: Pet anlegen, Offer suchen, Request erstellen, akzeptieren

### Phase 5: Doku & Test (1-2 Tage)

- [ ] `TEST_DOCUMENTATION.md` (alle Tests dokumentieren)
- [ ] `ARCHITECTURE.md` (diese Übersicht + Entscheidungen)
- [ ] `SECURITY.md` (Risiken, Maßnahmen, Gaps)
- [ ] `KI_PROMPTS.md` (wie KI eingesetzt wurde)

---

## 🛠️ Tech-Stack (kurz)

| Layer | Technologie | Grund |
|-------|-------------|-------|
| Backend | Spring Boot 3.x, Java 21 | Standard für Enterprise-Entwicklung |
| ORM | Spring Data JPA | Standard für relationale DBs in Spring |
| DB | MySQL 8.4 (Prod), H2 (Tests) | MySQL stabil, H2 einfach für Tests |
| Migration | Flyway | Gold Standard für versionsgesteuertes Schema |
| Security | Spring Security + BCrypt | Built-in, sicher, ausgereift |
| Validation | JSR-380 (Hibernate Validator) | Standard in Spring |
| Testing | JUnit 5, Mockito, H2 | Standard für Spring Boot |
| Build | Maven 3.x | Projekt-Standard |
| CI | GitHub Actions (kommt) | kostenlos, einfach |
| UI | Vaadin 24 LTS | Komponentenbasiert, Java-native |
| Logging | SLF4J + Logback | Default in Spring Boot |
| Serialization | Jackson | Default in Spring Boot |

---

## 💡 Häufige Fragen an einen Kollegen

**F: Warum keine Microservices?**  
A: Projektgröße klein, Team klein, Microservices bringen unötige Komplexität (Service-Discovery, Datenkonsistenz, Deployment). Monolith ist schneller, stabiler, leichter zu verstehen.

**F: Warum Flyway und nicht `ddl-auto: update`?**  
A: `ddl-auto` ist unvorhersehbar und nicht versioniert. Flyway garantiert, dass jede DB-Migration getestet und reproduzierbar ist.

**F: Warum keine REST-API?**  
A: Vaadin spricht direkt mit Services. REST nicht nötig, wenn nur eine UI. Würde unnötige Layer erzeugen.

**F: Warum ist `pet` bei SITTER_OFFER nullable?**  
A: Sitter bietet generelle Betreuung an, nicht für ein spezifisches Haustier. Pet wird bei Booking bekannt.

**F: Wie wird das Booking-Accept transaktional sauber?**  
A: `@Transactional` in `BookingService.acceptRequest()`. Entweder alle Schritte erfolgreich oder Rollback.

**F: Wie testen wir ohne echte MySQL während CI?**  
A: H2 in-Memory DB in Tests. `application-test.yml` mit H2-Datasource. Flyway-Migrations laufen auch auf H2.

---

## 📋 Checkliste für Onboarding

- [ ] Repo clonen
- [ ] `./mvnw clean install` ausführen
- [ ] `docker compose up -d` starten (MySQL)
- [ ] `./mvnw spring-boot:run -Dspring-boot.run.profiles=compose` starten
- [ ] `http://localhost:8080` im Browser testen
- [ ] `./mvnw test` ausführen (sollte grün sein)
- [ ] Diese Datei durchlesen
- [ ] Je ein Service-Test selbst schreiben

---

**Fragen? Einfach die Service-Docu oder JavaDoc in den Klassen lesen.**

