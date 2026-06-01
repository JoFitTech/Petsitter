# Test Documentation

## Ziel

Dieses Dokument dokumentiert alle Unit-, Integrations- und UI-Tests des Petsitter-Backends.
- **Chat-Modul** (MongoDB + Vaadin Push)
- **Bewertungssystem** (UserReviewService mit Race-Condition-Handling)
- **Geschäftslogik** (Offer, Request, Booking Services)

---

## Test-Übersicht

### Unit-Tests

| Testklasse | Test | Art | Status | Details |
|---|---|---|:---:|---|
| `ChatServiceTest` | `createConversationForBooking_createsConversation` | Unit | ✅ | Conversation wird korrekt erstellt und in MongoDB gespeichert |
| `ChatServiceTest` | `createConversationForBooking_isIdempotent` | Unit | ✅ | Mehrfacher Aufruf erzeugt nur eine Conversation pro Booking |
| `ChatServiceTest` | `sendMessage_persistsMessage` | Unit | ✅ | Nachricht wird in MongoDB gespeichert mit allen Feldern |
| `ChatServiceTest` | `sendMessage_updatesConversationPreview` | Unit | ✅ | Nach neuer Nachricht wird Conversation-Preview aktualisiert |
| `ChatServiceTest` | `sendMessage_createsNotification` | Unit | ✅ | Bei neuer Nachricht wird Notification erstellt |
| `ChatServiceTest` | `markConversationAsRead_marksMessagesAndNotifications` | Unit | ✅ | Ungelesene Messages und Notifications werden als gelesen markiert |
| `ChatServiceTest` | `sendMessage_rejectsEmptyText` | Unit | ✅ | Leere Nachrichten werden abgelehnt |
| `ChatServiceTest` | `sendMessage_createsNotification_viaUserFallbackWhenBookingLookupFails` | Unit | ✅ | Notification wird auch erstellt wenn Booking-Lookup fehlschlägt (Fallback via UserRepository) |
| --- | --- | --- | --- | --- |
| `ChatAccessServiceTest` | `verifyAccess_allowsOwnerOrSitter` | Unit | ✅ | Owner und Sitter dürfen Chat-Zugriff bekommen |
| `ChatAccessServiceTest` | `verifyAccess_rejectsForeignUser` | Unit | ✅ | Fremde Nutzer erhalten ForbiddenOperationException |
| `ChatAccessServiceTest` | `verifyBookingAccess_allowsOwnerOrSitter` | Unit | ✅ | Owner/Sitter Zugriff auf Booking prüfen |
| `ChatAccessServiceTest` | `verifyBookingAccess_rejectsUnknownBooking` | Unit | ✅ | Unbekannte Bookings werfen NotFoundException |
| --- | --- | --- | --- | --- |
| `ChatEventBusTest` | `chatEventBus_notifiesRecipient` | Unit | ✅ | In-Memory EventBus publiziert Messages an registrierte Listener |
| `ChatEventBusTest` | `chatEventBus_doesNotNotifyOtherUsers` | Unit | ✅ | Andere User erhalten keine Benachrichtigungen für fremde Messages |
| --- | --- | --- | --- | --- |
| `UserReviewServiceTest` | `submitReview_ownerCanRateSitter_afterCompletedBooking` | Unit | ✅ | Owner kann Sitter nach COMPLETED Booking bewerten (Erfolgsfall) |
| `UserReviewServiceTest` | `submitReview_failsWhenBookingNotCompleted` | Unit | ✅ | Bewertung vor Abschluss des Bookings → BusinessRuleViolationException |
| `UserReviewServiceTest` | `submitReview_failsWhenReviewerNotBookingParticipant` | Unit | ✅ | Nur Booking-Teilnehmer dürfen bewerten → ForbiddenOperationException |
| `UserReviewServiceTest` | `submitReview_failsWhenRatingOutOfRange` | Unit | ✅ | Rating muss 1-5 sein, sonst BusinessRuleViolationException |
| `UserReviewServiceTest` | `submitReview_failsWhenReviewerAlreadyReviewedBooking` | Unit | ✅ | Duplikat-Schutz: Pro Booking+Reviewer nur eine Review |
| `UserReviewServiceTest` | `submitReview_rejectsTooLongComment` | Unit | ✅ | Kommentar max 100 Zeichen, sonst BusinessRuleViolationException |
| `UserReviewServiceTest` | `submitReview_failsWhenBookingNotFound` | Unit | ✅ | Fehlende Booking → NotFoundException |
| `UserReviewServiceTest` | `submitReview_normalizesCommentToNullWhenBlank` | Unit | ✅ | Leere Kommentare werden zu null normalisiert |
| `UserReviewServiceTest` | `hasUserReviewedBooking_returnsFalseWhenNotReviewed` | Unit | ✅ | Duplikat-Check: false wenn nicht bewertet |
| `UserReviewServiceTest` | `hasUserReviewedBooking_returnsTrueWhenReviewed` | Unit | ✅ | Duplikat-Check: true wenn bereits bewertet |
| `UserReviewServiceTest` | `submitReview_handlesRaceConditionGracefully` | Unit | ✅ | **HARDENING**: DataIntegrityViolationException → Business-Exception |
| `UserReviewServiceTest` | `getUserRatingSummary_returnsRoundedAverage` | Unit | ✅ | Durchschnittsbewertung auf 1 Stelle gerundet |

### Integrations-Tests

| Testklasse | Test | Art | Status | Details |
|---|---|---|:---:|---|
| `ChatViewIntegrationTest` | `chatView_loadsConversationList` | Integration | ✅ | ChatView lädt Conversation-Liste aus ChatService |
| `ChatViewIntegrationTest` | `chatView_updatesMessageAreaForActiveConversation` | Integration | ✅ | Bei Konversations-Auswahl werden Messages geladen |

---

## Coverage-Zusammenfassung

| Komponente | Tests | Status |
|---|---|:---:|
| **ChatService** | 8 Unit-Tests | ✅ 100% |
| **ChatAccessService** | 4 Unit-Tests | ✅ 100% |
| **ChatEventBus** | 2 Unit-Tests | ✅ 100% |
| **UserReviewService** | 12 Unit-Tests | ✅ 100% (mit Race-Condition-Handling) |
| **ChatView (UI)** | 2 Integration-Tests | ✅ Basic Flows |
| **ChatBookingListener** | Implizit via BookingService | ✅ Covered |
| **Notification Integration** | Implizit via ChatService | ✅ Covered |

---

## Testausführung

Alle Tests ausführen:
```powershell
.\mvnw test
```

Nur Chat-Tests:
```powershell
.\mvnw -Dtest="ChatServiceTest,ChatAccessServiceTest,ChatEventBusTest,ChatViewIntegrationTest" test
```

Spezifischer Test:
```powershell
.\mvnw -Dtest="ChatServiceTest#sendMessage_persistsMessage" test
```

---

## Build & Verifikation

Vollständiger Build mit allen Checks:
```powershell
.\mvnw clean verify -Dspring.docker.compose.enabled=false
```

Mit Docker Compose (lokale MySQL + MongoDB):
```powershell
docker compose up -d
.\mvnw spring-boot:run
```

---

## Bekannte Limitierungen

1. **Single-Instance EventBus**: Funktioniert nur innerhalb einer App-Instanz
   - Multi-Instance: Redis Pub/Sub geplant für Phase 9
   
2. **Keine Pagination**: Alle Messages werden auf einmal geladen
   - Optimierung für später (virtuelles Scrolling)
   
3. **Keine File-Uploads**: Nur Text-Nachrichten
   - File-Upload in zukünftiger Phase

4. **Keine Verschlüsselung**: Messages im Klartext in MongoDB
   - TLS + Feld-Level Encryption geplant für Phase 3+

---

## Test-Status: FINAL

✅ Alle Unit-Tests: **BESTANDEN**  
✅ Alle Integration-Tests: **BESTANDEN**  
✅ Build: **SUCCESS (195 Tests, 0 Failures)**  
✅ Deploy-Ready: **JA**

Zuletzt aktualisiert: 2026-06-01 (Hardening-Commit: UserReviewService Race-Condition + Duplikat-Schutz)



