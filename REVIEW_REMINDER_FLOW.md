# Auto-Bewertungs-Reminder – Ablauf & Trigger

## 🎯 Kurze Zusammenfassung

Wenn ein Booking **abgeschlossen wird** (Status → `COMPLETED`), wird automatisch eine **Review-Erinnerungskarte** in den Chat der beiden Parteien gepostet. Diese Card erinnert die Nutzer daran, dass Bewertungen nun verfügbar sind.

---

## 📊 Ablauf (Step-by-Step)

### **Schritt 1: Trigger – Booking als "abgeschlossen" markieren**

**Wo**: `BookingService.markBookingCompleted(UUID bookingId, UUID userId)`

**Wer**: Owner oder Sitter klickt in der UI auf "Termin als abgeschlossen markieren"

**Was passiert**:
```java
// Line 319-323 in BookingService.java
booking.setStatus(BookingStatus.COMPLETED);
Booking savedBooking = bookingRepository.save(booking);

// 🔔 Event wird gepublisht
eventPublisher.publishEvent(new BookingCompletedEvent(this, savedBooking.getId()));
```

**Preconditions**:
- Booking existiert und hat Status `CREATED`
- Aktueller User ist Owner oder Sitter
- Booking-Enddatum liegt in der Vergangenheit (heute oder später)

**Postconditions**:
- Booking in DB auf `COMPLETED` gesetzt ✅
- `BookingCompletedEvent` ist gepublisht 🔔

---

### **Schritt 2: Event-Listener reagiert (ChatBookingListener)**

**Wo**: `ChatBookingListener.onBookingCompleted(BookingCompletedEvent event)`

**Trigger**: Spring's `@TransactionalEventListener` mit Phase `AFTER_COMMIT`
- Nur ausgelöst, wenn die Booking-Transaktion erfolgreich committed wurde
- Verhindert Fehler bei fehlgeschlagenen Saves

**Was passiert** (Line 62-76):
```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onBookingCompleted(BookingCompletedEvent event) {
    log.info("BookingCompletedEvent received for booking {}", event.getBookingId());
    
    try {
        // 1. Finde Konversations-ID für dieses Booking
        var conversationId = chatService.getConversationIdForBooking(event.getBookingId());
        
        if (conversationId.isPresent()) {
            // 2. Poste Review-Reminder Card
            chatService.postReviewReminderCard(conversationId.get(), event.getBookingId());
            log.info("Review reminder card posted in chat for booking {}", event.getBookingId());
        } else {
            log.warn("No conversation found for booking {} – skipping review reminder", event.getBookingId());
        }
    } catch (Exception e) {
        // ⚠️ Non-blocking error handling
        log.error("Failed to post review reminder for booking {}: {}", 
                  event.getBookingId(), e.getMessage(), e);
    }
}
```

**Fehlerbehandlung**:
- Exceptions werden **nicht** geworfen (non-blocking)
- Fehler werden geloggt, aber unterbrechen nicht den Booking-Prozess
- Wenn keine Konversation gefunden wird → Warning geloggt, aber kein Error

---

### **Schritt 3: Review-Reminder-Card wird erstellt (ChatService)**

**Wo**: `ChatService.postReviewReminderCard(String conversationId, UUID bookingId)`

**Was passiert** (Line 570-594):

#### 3a) Konversation laden
```java
ChatConversationDocument conversation = conversationRepository.findById(conversationId)
    .orElseThrow(() -> new NotFoundException("Konversation nicht gefunden: " + conversationId));
```

#### 3b) Message-Dokument erstellen
```java
ChatMessageDocument card = new ChatMessageDocument();
card.setConversationId(conversationId);
card.setSenderId(null);           // 👤 System-Message (kein echten Sender)
card.setRecipientId(null);        // 👤 Für beide Parteien sichtbar
card.setType("REVIEW_REMINDER_CARD");  // 🏷️ Message-Typ
card.setBookingId(bookingId);
card.setCreatedAt(LocalDateTime.now());
card.setRead(false);
card.setMessage("⏰ Der Termin ist abgeschlossen – Bewertung ist nun verfügbar!");
```

#### 3c) Konversation-Metadaten aktualisieren
```java
LocalDateTime now = LocalDateTime.now();
conversation.setLastMessageAt(now);
conversation.setLastMessagePreview(truncatePreview("Termin abgeschlossen – Bewertung verfügbar"));
conversationRepository.save(conversation);
```
**Warum?** Damit diese Konversation oben in der Chat-Liste bleibt (meist verwendet zuerst)

#### 3d) Message speichern & publishen
```java
ChatMessageDocument saved = messageRepository.save(card);
eventBus.publish(toMessageDto(saved));
```

**Postconditions**:
- REVIEW_REMINDER_CARD in MongoDB gespeichert ✅
- Message via EventBus gepublisht 🔔

---

### **Schritt 4: Real-Time UI Update (ChatView)**

**Wo**: `ChatView` (Vaadin UI) + `ChatEventBus` (Real-Time Pub/Sub)

**Trigger**: EventBus publisht `ChatMessageDto` mit Type `REVIEW_REMINDER_CARD`

**Was passiert**:

#### 4a) Registered Listener empfängt Message
```java
// In ChatView (Line 180-181, Konstruktor)
ChatView registriert sich beim EventBus:
    var registration = eventBus.register(currentUserId, this::onMessageReceived);
```

#### 4b) onMessageReceived wird aufgerufen
```java
private void onMessageReceived(ChatMessageDto msg) {
    if (msg.conversationId().equals(selectedConversationId)) {
        ui.access(() -> {  // ⚠️ Thread-safe UI Update
            messageList.add(buildMessageBubble(msg));
            scrollToBottom();
        });
    }
}
```

#### 4c) buildMessageBubble rendert REVIEW_REMINDER_CARD
```java
// In ChatView (Line 650+)
if ("REVIEW_REMINDER_CARD".equals(msg.type())) {
    return buildReviewReminderCardBubble(msg);  // 🎨 Spezial-Rendering
}
```

#### 4d) buildReviewReminderCardBubble rendert die Karte
```java
private Component buildReviewReminderCardBubble(ChatMessageDto msg) {
    Div card = new Div();
    card.getStyle()
        .set("background", "#f0f4ff")        // 💙 Hellblau
        .set("border", "1px solid #c5d0e8")
        .set("border-radius", "8px")
        .set("padding", "16px")
        .set("text-align", "center")
        .set("margin", "12px auto")
        .set("max-width", "280px");
    
    Span title = new Span("⏰ Termin abgeschlossen");
    title.getStyle().set("font-weight", "700").set("font-size", "14px");
    
    Span subtitle = new Span("Bewertung ist nun verfügbar");
    subtitle.getStyle().set("color", "#555").set("font-size", "12px");
    
    card.add(title, new Br(), subtitle);
    return card;
}
```

**Visuelles Ergebnis** (im Chat):
```
┌─────────────────────────────────────┐
│  ⏰ Termin abgeschlossen            │
│  Bewertung ist nun verfügbar        │
└─────────────────────────────────────┘
```

**Postconditions**:
- beide Parteien sehen die Card in Echtzeit ✅
- Card ist visuell von normalen Messages unterschieden ✅

---

## 🔄 Zeitliche Abfolge (Timeline)

```
T=0s   │ User klickt "Termin abgeschlossen"
       │
T=0.1s │ BookingService.markBookingCompleted()
       │ └─ Booking.status = COMPLETED
       │ └─ bookingRepository.save()  ✅ DB commit
       │
T=0.2s │ BookingCompletedEvent gepublisht 🔔
       │ (nach erfolgreichem Transaktion-Commit)
       │
T=0.3s │ ChatBookingListener.onBookingCompleted() ausgelöst
       │ └─ getConversationIdForBooking()
       │ └─ ChatService.postReviewReminderCard()
       │ └─ messageRepository.save()  ✅ DB commit
       │
T=0.4s │ eventBus.publish(ChatMessageDto)  🔔
       │
T=0.5s │ ChatView.onMessageReceived() aufgerufen
       │ └─ buildMessageBubble() → buildReviewReminderCardBubble()
       │ └─ ui.access() → messageList.add()
       │
T=0.6s │ ✨ REVIEW_REMINDER_CARD erscheint in beiden Chats
```

---

## 🎭 Beide Perspektiven (Owner & Sitter)

Wenn **Owner** das Booking als abgeschlossen markiert:

1. Owner sieht sofort die Card im Chat
2. Alle Sitter-Browser/Clients der Konversation erhalten die Card via EventBus
3. Card erscheint für Sitter in Echtzeit

Gleich für **Sitter** wenn dieser das markiert.

---

## 🔐 Sicherheit & Constraints

| Constraint | Enforce Point | Details |
|---|---|---|
| Nur Owner/Sitter kann markieren | BookingService.markBookingCompleted() | `userId` muss Owner oder Sitter sein |
| Nur nach Enddatum | BookingService.markBookingCompleted() | `today.isAfter(booking.getEndDate())` |
| Nur aus Status CREATED | BookingService.markBookingCompleted() | `booking.getStatus() != CREATED` → Exception |
| Event-Transaction-Safety | ChatBookingListener | `@TransactionalEventListener(AFTER_COMMIT)` |
| Non-blocking Fehlerbehandlung | ChatBookingListener | Exceptions werden geloggt, nicht geworfen |

---

## 📝 Logs (bei erfolgreicher Abfolge)

```log
[INFO] BookingService - Booking 1234 marked as COMPLETED by user 5678
[INFO] ChatBookingListener - BookingCompletedEvent received for booking 1234
[INFO] ChatService - Found conversation conv-99 for booking 1234
[INFO] ChatService - Review reminder card posted in conversation conv-99 for booking 1234
[INFO] ChatView - Message received in active conversation: REVIEW_REMINDER_CARD
```

---

## 🚨 Fehlerszenarien

### Szenario 1: Keine Konversation gefunden
```log
[WARN] ChatBookingListener - No conversation found for booking 1234 – skipping review reminder
⚠️ → Card wird nicht gepostet (aber Booking ist OK)
```

### Szenario 2: DB-Fehler beim Card-Save
```log
[ERROR] ChatBookingListener - Failed to post review reminder for booking 1234: Connection timeout
⚠️ → Card wird nicht gepostet (aber Booking ist OK)
→ Admin sollte manuell die Card nachholen (noch nicht implementiert)
```

### Szenario 3: User nicht authorize
```
[ERROR] BookingService - Forbidden: User 5678 is not owner or sitter
❌ → Exception wird geworfen (Booking-Status bleibt CREATED)
→ Frontend zeigt Error-Message
```

---

## 🔗 Betroffene Dateien

| Datei | Funktion |
|---|---|
| `BookingService.java` | Trigger-Punkt: `markBookingCompleted()` publisht Event |
| `BookingCompletedEvent.java` | Event-Objekt (trägt `bookingId`) |
| `ChatBookingListener.java` | Event-Listener: reagiert auf Event |
| `ChatService.java` | Erstellt die Review-Reminder-Card |
| `ChatView.java` | Rendert die Card im UI |
| `ChatEventBus.java` | Real-Time Pub/Sub System |

---

## ✅ Verifikation (Manuelle Tests)

1. **Setup**:
   - 2 User accounts (Owner & Sitter)
   - 1 Booking mit endDate in der Vergangenheit
   - Beide Users öffnen den Chat dieser Konversation

2. **Test**:
   - Owner klickt "Termin abgeschlossen"
   - ✅ Booking-Status ändert sich zu COMPLETED
   - ✅ Review-Reminder-Card erscheint im Chat beider User (in Echtzeit)
   - ✅ Karte ist visuell unterschieden (hellblau, zentriert, Icon)

3. **Danach**:
   - Owner klickt "Bewertung abgeben"
   - ✅ Review-Form wird angezeigt
   - Owner schreibt Review und klickt "Speichern"
   - ✅ REVIEW_CARD erscheint im Chat mit Sternen + Kommentar
   - ✅ Konversation-Preview aktualisiert sich

---

## 🎓 Architektur-Pattern

```
┌─────────────────────────────────┐
│  UI Layer (ChatView)            │
│  ┌─────────────────────────────┐│
│  │ User klickt "abgeschlossen" ││
│  └──────────┬──────────────────┘│
└─────────────┼──────────────────┘
              │
         HTTP POST
              │
              ▼
┌─────────────────────────────────┐
│  Service Layer (BookingService) │
│  ┌─────────────────────────────┐│
│  │ markBookingCompleted()      ││
│  │ - Validierungen             ││
│  │ - DB Save                   ││
│  │ - publishEvent()            ││
│  └──────────┬──────────────────┘│
└─────────────┼──────────────────┘
              │
        Spring Event
              │
              ▼
┌─────────────────────────────────┐
│  Event Listener                 │
│  (ChatBookingListener)          │
│  ┌─────────────────────────────┐│
│  │ onBookingCompleted()        ││
│  │ - postReviewReminderCard()  ││
│  │ - eventBus.publish()        ││
│  └──────────┬──────────────────┘│
└─────────────┼──────────────────┘
              │
        Real-Time Event
              │
              ▼
┌─────────────────────────────────┐
│  UI-Listener (ChatView)         │
│  ┌─────────────────────────────┐│
│  │ onMessageReceived()         ││
│  │ - buildReviewReminderCard() ││
│  │ - ui.access()               ││
│  │ - messageList.add()         ││
│  └─────────────────────────────┘│
└─────────────────────────────────┘
              │
              ▼
        ✨ USER SIEHT CARD ✨
```

---

## 💡 Vorteile dieser Architektur

| Vorteil | Erklärung |
|---|---|
| **Entkopplung** | BookingService weiß nichts von Chat |
| **Skalierbarkeit** | Neue Listener können leicht hinzugefügt werden (z.B. NotificationService, WalletService) |
| **Fehlertoleranz** | Fehler im Chat beeinträchtigen nicht das Booking |
| **Transaktionssicherheit** | Event wird erst nach erfolgreichem Commit gefeuert |
| **Real-Time** | EventBus sorgt für Live-Updates ohne Polling |


