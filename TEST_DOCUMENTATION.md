# Test Documentation

## Ziel

Dieses Dokument steuert die Qualitätskontrolle für das Chat-Zusatzmodul.

## Statuskontrolle (vor Phase 7)

- [ ] Build-Baseline: Compile ohne Fehler (`chat`, `notification`, `ui`)
- [ ] Zugriffsschutz validiert (nur Owner/Sitter)
- [ ] EventBus-Flow validiert (publish/register/remove)
- [ ] Header-Badge validiert (Unread aus Notification-Service)

## Phase 7: Tests (1-2 Tage)

### Unit-Tests

- [x] `ChatServiceTest`
  - [x] `createConversationForBooking_createsConversation`
  - [x] `createConversationForBooking_isIdempotent`
  - [x] `sendMessage_persistsMessage`
  - [x] `sendMessage_updatesConversationPreview`
  - [x] `markConversationAsRead_marksMessagesAndNotifications`
  - [x] `sendMessage_rejectsEmptyText`

- [x] `ChatAccessServiceTest`
  - [x] `verifyAccess_allowsOwnerOrSitter`
  - [x] `verifyAccess_rejectsForeignUser`
  - [x] `verifyBookingAccess_allowsOwnerOrSitter`
  - [x] `verifyBookingAccess_rejectsUnknownBooking`

- [x] `ChatEventBusTest`
  - [x] `chatEventBus_notifiesRecipient`
  - [x] `chatEventBus_doesNotNotifyOtherUsers`

### Integration-Tests

- [x] `ChatViewIntegrationTest`
  - [x] lädt Conversation-Liste aus `ChatService`
  - [x] Message-Bereich aktualisierbar für aktive Conversation

## Phase 8: UI-Features (1-2 Tage)

- [ ] Benachrichtigungsbox im Header
- [ ] Avatar in Messages
- [ ] Badge mit EventBus-Integration
- [ ] Typing-Indicator (mit Debounce)

## Phase 9: Multi-Instance (optional)

- [ ] Redis Pub/Sub Adapter für `ChatEventBus`
- [ ] Fehlerbehandlung (Retry, Timeout, Idempotenz)
- [ ] Feature-Flag zwischen In-Memory und Redis

## Testausführung

```powershell
.\mvnw -Dtest="ChatServiceTest,ChatAccessServiceTest,ChatEventBusTest,ChatViewIntegrationTest" test
```

```powershell
.\mvnw test
```
