# Quick Start für Kollegen: Dokumentation lesen

Wenn du das Projekt übernimmst oder mitwirkst, hier ist die Reihenfolge zum Verstehen:

## 1. Projekt-Übersicht (Start hier!)
Datei: `BACKEND_OVERVIEW.md`

- Was ist das Projekt (Petsitter-Plattform)?
- Aktuelle Phase (Grundstruktur up, Services in Zukunft)
- Architektur-Schichten
- Geschäftsregeln zusammengefasst
- Nächste Schritte pro Phase

## 2. Tech-Stack verstehen
Datei: `TECHNOLOGY_GUIDE.md`

- Spring Boot, Java 21, JPA, Flyway, MySQL, H2
- Warum jede Technologie?
- Wie funktioniert Dependency Injection?
- Wie funktioniert @Transactional?
- Common Pitfalls (N+1 Queries, LazyInitializationException)

## 3. Einzelne Klassen lesen
Alle Domain-Entities und Services haben ausführliche JavaDoc:
- `src/main/java/com/softwareengineering/petsitter/user/domain/User.java`
- `src/main/java/com/softwareengineering/petsitter/offer/domain/Offer.java`
- `src/main/java/com/softwareengineering/petsitter/offerrequest/domain/OfferRequest.java`
- `src/main/java/com/softwareengineering/petsitter/booking/domain/Booking.java`
- `src/main/java/com/softwareengineering/petsitter/offer/service/OfferService.java`
- `src/main/java/com/softwareengineering/petsitter/offerrequest/service/RequestService.java`
- `src/main/java/com/softwareengineering/petsitter/booking/service/BookingService.java`

Die JavaDocs erklären:
- **Was**: Zweck der Klasse
- **Warum**: Fachliche Bedeutung
- **Wie**: Wichtige Businessregeln und Constraints

## 4. Code-Struktur
```
src/main/java/com/softwareengineering/petsitter/
├─ user/domain/User.java          ← Entity: "Wer sind die Benutzer?"
├─ pet/domain/Pet.java            ← Entity: "Welche Haustiere?"
├─ offer/domain/Offer.java        ← Entity: "Wer sucht/bietet was an?"
├─ offerrequest/domain/OfferRequest.java  ← Entity: "Wer interessiert sich für was?"
├─ booking/domain/Booking.java    ← Entity: "Was wurde bestätigt?"
├─ offer/service/OfferService.java      ← Service: Business-Logik für Offers
├─ offerrequest/service/RequestService.java  ← Service: Business-Logik für Requests
├─ booking/service/BookingService.java       ← Service: ZENTRALE Logik (Accept → Booking)
└─ security/SecurityConfig.java   ← Spring Security Setup
```

## 5. Geschäftslogik verstehen

Die Kernfunktion ist **Request akzeptieren**:

```
User A erstellt OWNER_OFFER (sucht Sitter für Pet)
  ↓
User B erstellt REQUEST (interessiert sich dafür)
  ↓
User A sieht REQUEST und klickt "Akzeptieren"
  ↓
BookingService.acceptRequest() wird aufgerufen:
  1. Request → ACCEPTED
  2. Neues Booking wird erstellt
  3. Offer → BOOKED  (locked, kein Edit mehr)
  4. Alle anderen Requests auf diesem Offer → DENIED
  5. (Optional: Notification an andere User)
```

Das ist eine **atomare Transaktion** → entweder alles erfolgreich oder Rollback!

## 6. Run & Test

```powershell
# Build
./mvnw clean install

# Unit Test
./mvnw test

# Starten (MySQL via Docker)
docker compose up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## 7. Fragen?

Schau diese Dateien an:
- `BACKEND_OVERVIEW.md` → Projekt-FAQ
- `TECHNOLOGY_GUIDE.md` → Tech FAQ
- JavaDoc in den Klassen → detaillierte Regeln

Nicht gefunden? Frag einen Senior oder schau in die `INFOFORME.md` → Meine detaillierten Ausführungen.

---

**WICHTIG**: Die Services sind noch nicht implementiert (werfen `UnsupportedOperationException`). Das ist normal — Phase 1 konzentriert sich auf Domain-Modell und Persistierung (Flyway). Phase 2 implementiert die Services.

---

Letzter Check: `./mvnw test` sollte grün sein. Falls nicht → Bug in den JavaDocs, meld das!

