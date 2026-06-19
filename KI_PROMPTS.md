# KI-Nutzung und Prompts

## Zweck

Diese Datei dokumentiert den KI-Einsatz im Projekt Pawsitters. Die Prompts bilden konkrete Arbeitsaufträge aus Konzept, Implementierung, Debugging, Tests, Security, Dokumentation und Präsentation ab. Sie sind bewusst direkt formuliert und benennen das erwartete Ergebnis, die betroffenen Schichten sowie die erforderlichen Verknüpfungen.

## Verwendete Tools

| Tool | Einsatzbereich |
|---|---|
| ChatGPT | Konzept, Architektur, Präsentation, Dokumentation, technische Erklärungen |
| Claude | Implementierungsideen, Refactoring, Debugging und Alternativvorschläge |
| Codex | Repository-nahe Implementierungsunterstützung, Tests, Dokumentation und Codeanpassungen |
| Gemini/Antigravity | Implementierungsunterstützung, Code- und Testideen |

Die genaue Modellversion wurde nicht in jeder Interaktion separat protokolliert. KI wurde als unterstützendes Werkzeug genutzt; fachliche Entscheidungen, Integration, Tests und Verantwortung blieben beim Team.

## Qualitätsregeln

KI-Ergebnisse wurden nach folgenden Regeln behandelt:

- Keine ungeprüfte Übernahme von Code oder Security-Aussagen.
- Vorschläge mussten zur bestehenden Paket- und Schichtenstruktur passen.
- UI, Geschäftslogik und Datenzugriff mussten klar getrennt bleiben. Vaadin-Views greifen über definierte Service-Methoden und DTOs auf das Backend zu und nicht direkt auf Repositories oder Entities.
- Neue UI-Aktionen mussten mit konkreten Buttons, Navigationen und Click-Listenern umgesetzt werden.
- War die zugehörige Backend-Funktion noch nicht vorhanden, blieb der Button anklickbar und gab über einen eindeutig bezeichneten `System.out.println`-Platzhalter aus, welche Backend-Aktion später anzubinden ist. Die UI durfte dabei keinen erfolgreichen Datenbankvorgang vortäuschen.
- Für solche Platzhalter musste der benötigte Service-Vertrag mit Methodennamen, Parametern, Rückgabewert und Fehlerfällen vorbereitet oder dokumentiert werden.
- Code musste kompilieren und lokal nachvollziehbar sein.
- Tests mussten angepasst oder ergänzt werden, wenn Geschäftslogik betroffen war.
- Security-Aussagen mussten zwischen umgesetztem Stand und Zukunftsmaßnahmen unterscheiden.
- Vorschläge mit zu großem Umfang wurden reduziert oder verworfen.
- Der finale Code musste im Team erklärbar bleiben.

## Nutzung nach Teammitgliedern

| Person | Typische KI-Nutzung |
|---|---|
| Kim Reger | UI-Ideen, Layoutvarianten, Vaadin-Views, Formulierungen für Nutzerführung |
| Luis Schirmbeck | Backend, Security, CI, Tests, Dokumentation, Präsentationsstruktur, Debugging |
| Josef Lautner | Backend, Datenmodell, Security, Docker, Tests, technische Dokumentation |

## Prompt-Sammlung

### Konzept und Projektplanung

**Grundidee der Plattform**

```text
Strukturiere das Grundkonzept für die Webanwendung Pawsitters im Modul Software
Engineering II. Verbinde Tierhalter und Tiersitter über Benutzerprofile,
Haustiere, Betreuungsangebote, Anfragen, Buchungen, Chat und Bewertungen.
Definiere für jeden Bereich den Nutzerfluss, die benötigten UI-Aktionen und die
zuständige Backend-Funktion. Begrenze den Umfang auf ein realistisch umsetzbares
Projekt für drei Personen und lege einen durchgängigen Demo-Ablauf fest.
```

**Stakeholder und Anforderungen**

```text
Ermittle die Stakeholder der Petsitter-Plattform. Trenne Tierhalter, Tiersitter,
Auftraggeberin bzw. Dozentin, Entwicklerteam und mögliche spätere Betreiber.
Leite daraus funktionale und nicht-funktionale Anforderungen ab. Ordne jede
funktionale Anforderung einem sichtbaren UI-Einstiegspunkt und einer fachlichen
Backend-Verantwortung zu.
```

**Umfang für die Demo**

```text
Priorisiere Registrierung, Login, Profile, Haustiere, Angebote, Anfragen,
Buchungen, Chat, Bewertungen, Favoriten, Wallet, Admin-Bereich und echtes
Payment für die Pawsitters-Demo. Teile die Funktionen in Muss, Soll und Ausblick
ein. Lege für noch nicht umgesetzte Backend-Funktionen fest, welche Buttons und
Navigationen im Frontend bereits angelegt werden und welcher eindeutige
Konsolen-Platzhalter beim Anklicken ausgeführt wird. Begründe jede Abgrenzung
bezogen auf Teamgröße, Demo-Nutzen und Implementierungsaufwand.
```

**Präsentationsstruktur**

```text
Erstelle die Struktur für eine 25-minütige Präsentation der
Java-/Spring-Boot-/Vaadin-Anwendung Pawsitters. Decke Konzept, Teamarbeit,
Trello, Git, Architektur, Techstack, Security, Tests, CI, Demo und KI-Nutzung
ab. Plane Zeit pro Abschnitt ein und ordne die Demo so, dass UI-Aktion,
Service-Aufruf und sichtbares Ergebnis jeweils nachvollziehbar aufeinanderfolgen.
Kennzeichne Funktionen mit Frontend-Platzhaltern klar als noch nicht ans Backend
angebunden.
```

### Architektur

**Monolith oder Microservices**

```text
Bewerte Monolith und Microservices für Pawsitters unter den Rahmenbedingungen
drei Entwickler, begrenzter Uni-Projektzeitraum, Java 21, Spring Boot, Vaadin,
MySQL, MongoDB für Chat und lokale Demo. Lege die Entscheidung für einen
Monolithen mit Schichtenarchitektur nachvollziehbar fest. Zeige, wie UI und
Backend innerhalb des Monolithen über DTOs und Service-Methoden getrennt
bleiben, und benenne die Grenzen dieser Entscheidung.
```

**Schichtenarchitektur**

```text
Lege die Schichtenarchitektur für Pawsitters fest. Verwende Vaadin für den
UI-Layer, Spring-Boot-Services für die Geschäftslogik, Spring Data JPA für
MySQL, Spring Data MongoDB für Chatdaten und DTOs als Übergabeobjekte zur UI.
Definiere die zulässigen Abhängigkeiten zwischen UI, Service, Repository,
Domain, DTO, Security und Config. Zeige den vollständigen Aufrufweg an
UserService, OfferService, BookingService, ChatService und WalletService.
Verhindere direkte Repository- und Entity-Zugriffe aus den Views.
```

**MySQL und MongoDB**

```text
Dokumentiere die Persistenzaufteilung von Pawsitters. Speichere User, Pets,
Offers, Requests, Bookings, Wallet, Reviews, Notifications, Bilder und
PLZ-Cache in MySQL. Speichere Chat-Konversationen und Chatnachrichten in
MongoDB. Begründe die Aufteilung fachlich und technisch, definiere die IDs an
den Modulgrenzen und benenne Konsistenz-, Transaktions- und Betriebsnachteile.
```

**Events zwischen Booking und Chat**

```text
Entkopple Booking und Chat mit Spring Events. Veröffentliche nach dem
Akzeptieren einer Anfrage ein BookingCreatedEvent und lasse einen Listener die
Chat-Konversation anlegen oder verknüpfen. Veröffentliche beim Abschluss ein
BookingCompletedEvent und lasse einen Listener eine Review-Erinnerungskarte im
Chat erzeugen. Führe Chat-Reaktionen erst nach erfolgreichem Commit aus und
verhindere, dass ein Chat-Fehler die Booking-Transaktion zurückrollt.
Dokumentiere Event-Felder, Listener-Verhalten und Fehlerbehandlung.
```

**Architekturdokument aktualisieren**

```text
Prüfe ARCHITECTURE.md vollständig gegen den aktuellen Repository-Stand.
Gleiche Chat, MongoDB, Wallet, Reviews, Bildverwaltung, PLZ-Distanzsuche,
Favoriten, Notifications, wiederkehrende Buchungen, Spring Events und Tests
mit den vorhandenen Klassen ab. Ergänze fehlende Aufrufwege und Schnittstellen
zwischen Vaadin-Views und Services. Entferne veraltete oder nicht belegbare
Aussagen und kennzeichne noch nicht angebundene UI-Funktionen als Platzhalter.
```

### Backend und Geschäftslogik

**Domainmodell**

```text
Setze das Domainmodell für Pawsitters mit User, Pet, Offer, OfferRequest,
Booking und Notification als Kern um. Bilde ab, dass ein User sowohl Tierhalter
als auch Tiersitter sein kann, Offers von beiden Seiten stammen, Requests auf
Offers verweisen und aus akzeptierten Requests Bookings entstehen. Definiere
Felder, Beziehungen, Status-Enums, Datenbank-Constraints und fachliche
Invarianten. Stelle UI-Daten ausschließlich über geeignete DTOs bereit.
```

**Service-Struktur**

```text
Strukturiere die gesamte Geschäftslogik in UserService, PetService,
OfferService, RequestService, BookingService, NotificationService, ChatService,
ChatAccessService, WalletService, UserReviewService, ImageAssetService und
PostalCodeService. Definiere pro Service Verantwortung, öffentliche Methoden,
Parameter, DTO-Rückgaben, Berechtigungsprüfungen und Fehlerfälle. Halte
Vaadin-Views und Entities frei von Geschäftslogik und lasse Repositories nur
durch Services verwenden.
```

**Anfrage akzeptieren**

```text
Implementiere BookingService.acceptRequest(requestId, offerCreatorId)
transaktional. Prüfe, dass der Request existiert und PENDING ist, das Offer OPEN
ist und offerCreatorId dem Creator des Offers entspricht. Setze den Request auf
ACCEPTED, erzeuge das Booking, setze das Offer auf BOOKED, lehne alle anderen
PENDING Requests mit DENIED ab und veröffentliche ein BookingCreatedEvent.
Gib der UI ein DTO mit dem aktualisierten Zustand zurück und bilde fachliche
Fehler als verständliche Exceptions ab. Ergänze Tests für Erfolg, unberechtigten
Zugriff, falsche Status und konkurrierende Anfragen.
```

**Regelmäßige Buchungen**

```text
Erweitere BookingService um einmalige und regelmäßige Buchungen. Verwende für
regelmäßige Offers die Frequenz REGULAR und mehrere Wochentage. Implementiere
klar getrennte Service-Methoden zum Anlegen einer Pause und zum Beenden einer
regelmäßigen Buchung. Lege in MyBookings die Buttons „Pause eintragen“ und
„Buchung beenden“ an und verbinde sie mit diesen Methoden. Ist eine Methode
noch nicht implementiert, halte den Button anklickbar und gib beispielsweise
`BACKEND_TODO BookingService.addRecurringPause bookingId=<id>` aus. Prüfe überlappende
Pausen, ungültige Zeiträume, Berechtigungen und bereits beendete Buchungen.
```

**Wallet und Treuhandzahlung**

```text
Implementiere die Demo-Wallet-Logik für Pawsitters. Lege Aufladebuttons für 25,
50 und 100 Euro an und verbinde sie mit WalletService.devTopUp. Halte
den Buchungsbetrag beim Owner zurück, erstatte ihn bei einem Storno vor Beginn,
erlaube dem Owner nach dem Enddatum die Auszahlung und dem Sitter die
Auszahlungsanforderung. Gib angeforderte Auszahlungen nach sieben Tagen
automatisch frei. Definiere Entities, Statuswerte, Service-Methoden, DTOs und
Tests. Verwende für noch fehlende Backend-Methoden anklickbare Buttons mit
einem eindeutigen `BACKEND_TODO`-Konsolenaufruf.
```

**PLZ- und Distanzsuche**

```text
Implementiere die Angebotssuche nach Entfernung zu einer deutschen PLZ.
Kapsle Lookup, MySQL-Cache, Plausibilitätsprüfung deutscher Koordinaten und
Haversine-Distanzberechnung im PostalCodeService. Übergib der UI nur ein
Suchkriterien-DTO und sortierte OfferCardDtos. Verbinde PLZ-Feld,
Entfernungsregler und Suchbutton mit der Service-Methode. Zeige bei ungültiger
PLZ eine verständliche Fehlermeldung und definiere das Verhalten bei nicht
erreichbarem externem Lookup-Client.
```

**Bildverarbeitung**

```text
Implementiere ImageAssetService für Profil- und Haustierbilder. Akzeptiere nur
tatsächliche JPEG- und PNG-Inhalte bis 5 MB und 20 Megapixel. Erzeuge nach dem
Zuschnitt optimierte JPEG-Varianten für AVATAR und DISPLAY und speichere das
Original nicht dauerhaft. Definiere Upload-, Ersetzen- und Löschen-Methoden als
Backend-Schnittstelle der UI. Prüfe bei Haustierbildern die Ownership im Service
und liefere der View ausschließlich ImageRefDtos zurück.
```

**Debugging einer Spring-Boot-Fehlermeldung**

```text
Analysiere den folgenden Fehler aus der Spring-Boot-/Vaadin-Anwendung. Ordne
ihn zuerst dem UI-, Service-, Repository-, Datenbank- oder Config-Layer zu.
Bestimme die wahrscheinlichste Ursache, prüfe den Aufrufweg über die
Schichtengrenzen und nenne die konkret zu ändernden Dateien. Setze anschließend
den kleinsten passenden Fix um und ergänze einen Test, der den Fehler vorher
reproduziert und danach verhindert:

[Hier wurde die konkrete Fehlermeldung oder der Stacktrace eingefügt.]
```

### UI und Vaadin

**Logo-Entwürfe**

```text
Erstelle fünf deutlich unterschiedliche Logo-Vorschläge für Pawsitters. Das
Logo soll freundlich, modern und vertrauenswürdig wirken und die Vermittlung
zwischen Tierhaltern und Tiersittern darstellen. Kombiniere dafür eine klare
Wortmarke „Pawsitters“ mit Elementen wie Pfote, Herz, Tier-Silhouette oder Haus,
ohne das Logo mit Details zu überladen. Verwende die warme Farbwelt der
Anwendung mit Dunkelbraun, Creme und einem zurückhaltenden Mintton. Zeige jeden
Vorschlag als horizontales Logo für den Seitenkopf und als kompakte Bildmarke
für App-Icon oder Favicon. Achte auf gute Erkennbarkeit in kleinen Größen, einen
transparenten Hintergrund und eine Gestaltung, die sich anschließend als SVG
und PNG in die Vaadin-Anwendung einbinden lässt. Erläutere pro Vorschlag kurz
die Grundidee und die verwendeten Gestaltungselemente.
```

**Vaadin-View-Struktur**

```text
Strukturiere die Vaadin-Views und wiederverwendbaren Komponenten von Pawsitters.
Ordne Startseite, LoginView, RegistrateView, ForgotPasswordView, UserView,
PersonalDetailView, MyPetView, MyOffers, MyBookings, MyWalletView, OfferView,
PetsitterFilterView, CreateOfferView, BookingView und ChatView öffentlichen oder
geschützten Routen zu. Lege pro View die sichtbaren Buttons, Navigationen und
verwendeten Service-Methoden fest. Übergib Daten über DTOs und verhindere
direkte Repository-Zugriffe. Lege für noch fehlende Backend-Funktionen bereits
anklickbare Buttons mit eindeutigem `BACKEND_TODO`-Konsolenplatzhalter an.
```

**Registrierungsformular**

```text
Setze das Vaadin-Registrierungsformular mit E-Mail, Passwort,
Passwortbestätigung, Vorname, Nachname, Geburtsdatum, Telefon, Straße,
Hausnummer, PLZ, Ort und Land um. Ergänze „Registrieren“ und „Abbrechen“ sowie
eine Navigation zum Login. Validiere Pflichtfelder unmittelbar in der UI,
prüfe Passwort-Policy und PLZ-Ort-Zuordnung aber verbindlich im Backend über
UserService und PostalCodeService. Übertrage die Eingaben in einem
UserRegistrationRequest-DTO, zeige Service-Fehler feldbezogen an und starte nach
Erfolg den Registrierungscode-Flow.
```

**Such- und Filter-UI**

```text
Setze die Such- und Filteroberfläche für Pawsitters-Angebote in Vaadin um.
Ergänze Felder für Rolle bzw. Suchmodus, Zeitraum, flexible Datumssuche, Preis,
Betreuungsart, Frequenz, Tierart, Ausgangs-PLZ und Entfernung. Lege die Buttons
„Suchen“, „Filter“, „Zurücksetzen“ und „Angebot anzeigen“ an. Überführe alle
Eingaben in ein OfferSearchCriteria-DTO und rufe genau eine Suchmethode im
OfferService auf. Interpretiere Sliderwerte über 100 km als unbegrenzt und
zeige Lade-, Leer- und Fehlerzustand getrennt an.
```

**ChatView**

```text
Setze die ChatView mit Konversationsliste links und Nachrichtenbereich rechts
um. Ergänze Eingabefeld, Senden-Button, Zurück-Button und Kamera-Button. Verbinde
Senden, Auswahl einer Konversation und Lesestatus mit ChatService; aktualisiere
neue Nachrichten über EventBus und Vaadin Push. Unterstütze den Query-Parameter
der ausgewählten Konversation und entferne Listener beim Detach. Solange der
Bildversand im Backend fehlt, bleibt der Kamera-Button anklickbar und führt
`System.out.println("BACKEND_TODO ChatService.sendImage conversationId=" + id)`
aus. Zeige dabei keinen erfolgreichen Upload an.
```

**Review-UI**

```text
Erweitere MyBookings um Bewertungen nach abgeschlossenen Buchungen. Zeige den
Button „Bewerten“ nur bei COMPLETED und nur dann, wenn der aktuelle User noch
nicht bewertet hat. Öffne einen Dialog mit 1 bis 5 Sternen, Kommentar,
„Bewertung speichern“ und „Abbrechen“. Übergib bookingId, rating und comment an
UserReviewService und aktualisiere die Ansicht erst nach erfolgreicher
Bestätigung. Erzeuge die Review-Karte über den Backend-Service und nicht direkt
in der View. Halte den Button bei noch fehlendem Service anklickbar und gib den
vorgesehenen Methodenaufruf als `BACKEND_TODO` auf der Konsole aus.
```

**Bild-Upload und Zuschnitt**

```text
Setze den Vaadin-Upload für Profil- und Haustierbilder um. Ergänze die Buttons
„Bild auswählen“, „Zuschneiden“, „Speichern“, „Bild entfernen“ und „Abbrechen“.
Halte temporäre Uploaddaten nur bis zur Übergabe an ImageAssetService in der UI.
Lasse MIME-Typ, tatsächlichen Inhalt, Größe, Megapixel und Ownership verbindlich
im Backend prüfen. Aktualisiere Avatar oder Display-Variante erst mit der
zurückgegebenen ImageRefDto. Lege für noch nicht angebundene Löschen- oder
Speichern-Aktionen Click-Listener mit konkretem `BACKEND_TODO`-Aufruf an.
```

### Security

**Sicherheitskonzept**

```text
Erstelle das Sicherheitskonzept für Pawsitters auf Basis des aktuellen Codes.
Bewerte personenbezogene Daten, Adressdaten, Haustierdaten, Angebote, Anfragen,
Buchungen, Chatnachrichten, Walletdaten, Bewertungen, Bilder und Sessiondaten.
Dokumentiere Schutzbedarf, Bedrohungsmodell, tatsächlich umgesetzte Maßnahmen
und offene produktive Maßnahmen. Trenne Uni-Demo und Produktion eindeutig und
behaupte keine Absicherung, die im Repository nicht nachweisbar ist.
```

**Shift Security Left**

```text
Wende Shift Security Left konkret auf Pawsitters an. Ordne Anforderungen,
Architektur, Implementierung, Tests, CI und Review jeweils überprüfbare
Security-Maßnahmen zu. Beziehe Passwortregeln, BCrypt, Account-Status,
Code-Flows, Service-Level-Zugriffskontrollen, SecurityIntegrationTest und CI
ein. Benenne fehlende Maßnahmen separat und leite konkrete nächste Aufgaben
mit betroffenen Klassen oder Konfigurationen ab.
```

**Passwortregeln**

```text
Implementiere PasswordPolicyService für Pawsitters. Fordere mindestens 14
Zeichen sowie Großbuchstaben, Kleinbuchstaben, Zahl und Sonderzeichen. Lehne
gleiche Zeichen, Zahlenfolgen und verbotene Begriffe ab. Gib strukturierte
Validierungsfehler an Registrierung und Passwort-Reset zurück, ohne die Regeln
in den Views zu duplizieren. Ergänze Unit-Tests für jede einzelne Regel,
Kombinationen und gültige Grenzfälle.
```

**Registrierungscode**

```text
Implementiere den Registrierungscode-Flow. Erzeuge beim Start einen
Pending-User, generiere einen sechsstelligen Code und übergib ihn an einen
abstrakten Mail-Service. Speichere nur den BCrypt-Hash, Ablaufzeit,
Fehlversuchszähler und Verwendungsstatus. Ergänze in der UI „Code bestätigen“,
„Code erneut senden“ und „E-Mail ändern“ und verbinde die Aktionen ausschließlich
mit LoginCodeService. Gib Codes nur im aktivierten lokalen Dev-Modus auf der
Konsole aus. Ergänze Tests für Ablauf, Fehlversuche, Wiederverwendung und Erfolg.
```

**Passwort-Reset**

```text
Implementiere den sicheren Passwort-Reset-Flow. Lege die UI-Aktionen „Anfrage
senden“, „Code bestätigen“, „Passwort zurücksetzen“ und „Zurück zum Login“ an.
Erzeuge für verifizierte Accounts einen sechsstelligen Code, speichere nur den
Hash und versende ihn über PasswordResetMailService. Gib bei unbekannter E-Mail
dieselbe Erfolgsmeldung zurück. Prüfe beim Abschluss Code, Ablaufzeit,
Versuchszähler, Passwort-Policy und Passwortbestätigung im Backend. Gib den
Klartextcode ausschließlich im expliziten Dev-Modus auf der Konsole aus.
```

**Remember-Me-Audit**

```text
Prüfe die Spring-Security-Remember-Me-Konfiguration gegen den aktuellen Code.
Kontrolliere hardcodierte Schlüssel, permissive UserDetailsService-Logik,
Algorithmus, Token-Laufzeit, Cookie-Flags und Demo-Fallbacks. Setze sichere,
projektverträgliche Korrekturen um und ergänze Tests. Dokumentiere exakt, welche
Maßnahmen umgesetzt sind und welche nur als Produktionsanforderung bestehen.
```

**IDOR und Service-Level-Zugriff**

```text
Analysiere und schließe IDOR-Risiken durch manipulierbare Offer-, Request-,
Booking-, Conversation-, Pet- und ImageAsset-IDs. Definiere für Profile,
Haustiere, Angebote, Anfragen, Buchungen, Chat, Wallet, Reviews und Bilder die
erforderliche Service-Level-Autorisierung. Verlasse dich nicht auf ausgeblendete
Buttons oder geschützte Routen. Ergänze pro Bereich mindestens einen Test mit
einer fremden Benutzer-ID.
```

**Demo-Flags**

```text
Bewerte die Demo-Konfiguration mit Demo-User, Dev-Ausgabe für Registrierungs-
und Passwort-Reset-Codes, lokalen DB-Credentials und Dev-Fallback für
Remember-Me. Binde jeden Demo-Mechanismus an ein explizites lokales Profil und
verhindere seine Aktivierung in Produktion. Trenne im Security-Konzept klar
zwischen akzeptierter Uni-Demo-Konfiguration, verbleibendem Risiko und
zwingender Produktionsmaßnahme.
```

### Chat-Modul

**Chat-Architektur**

```text
Setze das Chat-Modul mit MongoDB um. Speichere Konversationen und Nachrichten
als Documents und bilde Owner und Sitter als Teilnehmer ab. Lade Nachrichten
chronologisch, verwalte Lesestatus, erzeuge Notifications und aktualisiere die
ChatView über Vaadin Push. Definiere Documents, DTOs, Repositories und die
öffentlichen Methoden von ChatService und ChatAccessService. Prüfe jede
Conversation-ID im Backend und halte Mongo-Documents aus der UI heraus.
```

**Konversation beim Request**

```text
Implementiere das Absenden einer Anfrage als abgestimmten Request- und
Chat-Ablauf. Lege am Offer den Button „Anfrage senden“ an und übergib offerId,
requesterId und Nachricht an RequestService. Erzeuge oder verwende anschließend
über ChatService eine Konversation zwischen Offer-Creator und Requester,
speichere eine Request-Systemkarte und danach die erste Nachricht. Definiere
die erforderlichen Offer-, Request-, Booking- und Teilnehmer-IDs in der
Conversation. Zeige Erfolg erst an, wenn der Backend-Aufruf abgeschlossen ist.
```

**Konversation beim Booking**

```text
Veröffentliche nach erfolgreichem BookingService.acceptRequest ein
BookingCreatedEvent. Lasse ChatBookingListener nach dem Commit eine Conversation
für das Booking anlegen oder eine vorhandene Conversation mit der bookingId
verknüpfen. Fange Chat-Fehler im Listener ab, protokolliere sie nachvollziehbar
und lasse die abgeschlossene Booking-Transaktion unverändert. Ergänze Tests für
neue, vorhandene und fehlgeschlagene Chat-Verknüpfung.
```

**Review-Reminder im Chat**

```text
Veröffentliche beim Wechsel eines Bookings auf COMPLETED ein
BookingCompletedEvent. Prüfe nach erfolgreichem Commit, ob eine Conversation
für die bookingId existiert. Speichere dort über ChatService eine
REVIEW_REMINDER_CARD und publiziere sie über den EventBus. Protokolliere eine
fehlende Conversation oder einen Chat-Fehler, ohne den Booking-Abschluss
zurückzurollen. Ergänze Tests für alle drei Pfade.
```

**Review-Karte nach Bewertung**

```text
Speichere eine Bewertung nach abgeschlossenem Booking über UserReviewService.
Erzeuge bei vorhandener Chat-Konversation zusätzlich eine REVIEW_CARD mit
Sternen und Kommentar und publiziere sie für die Live-Anzeige. Halte die
Review-Persistenz unabhängig vom optionalen Chat-Schritt: Eine fehlende
Conversation oder ein Chat-Fehler darf die gespeicherte Bewertung nicht
verwerfen. Übergib der UI ein UserReviewDto mit dem verbindlichen Ergebnis.
```

### Reviews und Ratings

**Bewertungsregeln**

```text
Implementiere die Geschäftsregeln in UserReviewService. Erlaube Bewertungen
nur für Bookings mit COMPLETED und nur durch Owner oder Sitter der Buchung.
Bestimme den bewerteten Gegenpart im Backend, verhindere Selbstbewertungen und
erlaube pro Person und Booking genau eine Bewertung. Validiere 1 bis 5 Sterne
und maximal 100 Zeichen Kommentar. Gib der UI ein UserReviewDto zurück und liefere
für jeden Regelverstoß eine eindeutige fachliche Exception.
```

**Dynamische Profilbewertungen**

```text
Ersetze statische Sterne in Profilen und OfferCards durch dynamische Bewertungen.
Erweitere PublicUserProfileDto um UserRatingSummary und aktuelle UserReviewDtos und
lasse UserService die Daten über UserReviewService zusammenstellen. Zeige in
ProfilePopUp und OfferCards Durchschnitt, Anzahl und letzte Kommentare an.
Lege eine anklickbare Aktion „Alle Bewertungen anzeigen“ an. Ist die zugehörige
Backend-Abfrage noch nicht vorhanden, gib ihren vorgesehenen Service-Aufruf als
`BACKEND_TODO` aus und zeige den Leerzustand „Noch keine Bewertungen“ korrekt an.
```

**Tests für Reviews**

```text
Implementiere Unit-Tests für UserReviewService. Decke ab: Owner bewertet Sitter
nach abgeschlossenem Booking, Review-Karte wird in den Chat geschrieben,
Review bleibt ohne Conversation erfolgreich, Bewertung vor Abschluss und durch
fremden User wird abgelehnt, Rating außerhalb 1 bis 5 wird abgelehnt, doppelte
Bewertung und zu langer Kommentar werden abgelehnt und der Durchschnitt wird
korrekt gerundet. Prüfe neben Rückgaben auch Persistenz und Interaktionen mit
dem Chat-Service.
```

### Wallet und Zahlungen

**Demo-Wallet**

```text
Setze MyWalletView als Demo-Wallet ohne echtes Payment um. Zeige verfügbares
Guthaben, gehaltene ausgehende Beträge, erwartete eingehende Beträge und letzte
Transaktionen aus einem WalletSummaryDto. Lege die Buttons „25 € aufladen“,
„50 € aufladen“ und „100 € aufladen“ an und verbinde sie mit
WalletService.devTopUp. Reserviere den Gesamtbetrag einer Buchung als
Treuhandzahlung im Backend. Aktualisiere die UI nur mit der Antwort des Service.
```

**Auszahlung und automatische Freigabe**

```text
Implementiere die Auszahlungslogik für BookingPayment. Lege in MyBookings für
den Owner „Auszahlung freigeben“ und für den Sitter „Auszahlung anfordern“ an.
Verbinde beide Buttons mit getrennten WalletService-Methoden und prüfe Rolle,
Booking-Status, Payment-Status und Enddatum im Backend. Zahle vor oder am
Enddatum nicht aus und gib angeforderte Zahlungen nach sieben Tagen automatisch
frei. Definiere Statuswerte, DTOs und Tests. Falls eine Backend-Methode fehlt,
lasse den Button anklickbar und gib den vollständigen geplanten Methodenaufruf
als `BACKEND_TODO` aus.
```

**Regelmäßige Zahlungen**

```text
Implementiere wöchentliche Zahlungen für regelmäßige Buchungen. Berechne anhand
der gebuchten Wochentage die zahlbaren Termine, berücksichtige Pausen und
erzeuge jede Wochenzahlung idempotent. Berechne reservierte Beträge nach einer
Pause neu oder überspringe betroffene Termine. Erzeuge bei unzureichendem
Guthaben eine Top-Up-Notification mit Navigation zur Wallet. Kapsle die Logik
im WalletService und ergänze Tests für Teilwochen, Pausen, Wiederholung und zu
wenig Guthaben.
```

### Tests

**Testfälle für Kernlogik**

```text
Erstelle und implementiere die fehlenden Unit-Tests für Registrierung, Login,
Passwortregeln, Passwort-Reset, Profil, Haustiere, Angebote, Anfragen,
Buchungen, Chat, Wallet, Reviews, Bilder, PLZ-Validierung und Favoriten.
Ordne jeden Test als Normalfall oder Edge Case ein. Prüfe nicht nur Methoden-
rückgaben, sondern auch Statusänderungen, Persistenz, Berechtigungen und
Interaktionen an Service-Grenzen.
```

**SecurityIntegrationTest**

```text
Implementiere SecurityIntegrationTest mit MockHttpServletRequest,
MockHttpServletResponse und FilterChainProxy. Prüfe, dass die Startseite und
öffentliche Medienroute anonym erreichbar sind, geschützte Routen auf /login
umleiten, der Demo-Login eine authentifizierte Session erzeugt und Logout die
Session invalidiert. Ergänze Negativtests, damit UI-Navigation keinen fehlenden
Service-Level-Zugriffsschutz verdeckt.
```

**Bildtests**

```text
Implementiere Tests für ImageAssetService und ImageMediaController. Prüfe, dass
PNG-Uploads als JPEG-Varianten gespeichert werden, falsche MIME-Typen, zu große
Dateien und beschädigte Inhalte abgelehnt werden, beim Ersetzen eines Profilbilds
das alte Asset gelöscht wird, fremde Haustierbilder nicht verändert werden und
die öffentliche Medienroute den `X-Content-Type-Options: nosniff`-Header setzt.
```

**Wallettests**

```text
Implementiere Tests für WalletService. Decke Demo-Aufladung,
Treuhandreservierung, zu wenig Guthaben ohne Mutation, doppelte Reservierung,
Erstattung bei Storno, Auszahlung an den Sitter, Ablehnung vor und am Enddatum,
Auszahlungsanforderung und automatische Freigabe nach sieben Tagen ab. Prüfe
Kontostände, Payment-Status und WalletTransaction-Einträge gemeinsam.
```

**Testdokumentation**

```text
Aktualisiere TEST_DOCUMENTATION.md anhand der tatsächlich vorhandenen Tests.
Belege, dass mindestens zehn Unit-Tests vorhanden sind, und liste jede
Testklasse mit Bereich und Testart auf. Dokumentiere die wichtigen Einzeltests
mit Testklasse, Testname bzw. Testbereich, geprüftem Verhalten, erwartetem
Ergebnis und Falltyp Normalfall oder Edge Case. Entferne Einträge, die im Code
nicht nachweisbar sind.
```

### CI und Infrastruktur

**GitHub Actions**

```text
Setze eine GitHub-Actions-Pipeline für das Maven-/Spring-Boot-Projekt mit Java
21 um. Starte sie bei Pushes und Pull Requests, checke das Repository aus,
richte JDK 21 und Maven-Cache ein und führe
`./mvnw -B verify -Dspring.docker.compose.enabled=false` aus. Sorge dafür, dass
ein fehlgeschlagener Build oder Test den Workflow zuverlässig fehlschlagen lässt.
```

**Docker Compose in Tests deaktivieren**

```text
Deaktiviere Spring Boot Docker Compose in Tests und CI mit
`spring.docker.compose.enabled=false`. Konfiguriere H2 für relationale Tests und
isolierte Testalternativen für MongoDB-Abhängigkeiten. Halte die lokale
MySQL-/MongoDB-Konfiguration unverändert. Dokumentiere Profilauflösung,
Unterschiede zwischen H2 und MySQL und typische Fehler durch versehentlich
geladene Produktionskonfiguration.
```

**Docker-Startguard**

```text
Implementiere DockerComposeStartupGuard. Prüfe vor dem lokalen Start, ob Docker
erreichbar ist, und gib bei fehlender Verbindung eine konkrete Anleitung aus.
Blockiere den Start nur, wenn Spring Docker Compose aktiviert ist. Deaktiviere
den Guard in Tests und CI und ergänze Tests für aktivierte, deaktivierte und
nicht erreichbare Docker-Konfiguration.
```

### Dokumentation

**Architekturdokumentation**

```text
Aktualisiere ARCHITECTURE.md anhand des aktuellen Codes. Dokumentiere Ziel,
Architekturentscheidung, Systemübersicht, Schichten, zentrale Fachmodule,
Persistenz mit MySQL und MongoDB, interne Events, Security, Testing, CI,
bekannte Grenzen und Ausblick. Zeige die Schnittstellen von Vaadin-Views über
DTOs und Services zum Backend und kennzeichne reine UI-Platzhalter eindeutig.
```

**Security-Dokumentation**

```text
Aktualisiere SECURITY_CONCEPT.md anhand der implementierten Maßnahmen.
Dokumentiere Schutzbedarf, Spring Security, Rollenmodell, Authentifizierung,
Registrierungscode, Passwort-Reset, Passwort-Policy, Remember-Me, CSRF,
Service-Level-Zugriffskontrollen, Bedrohungsmodell, Shift Security Left,
Grenzen und nächste Sicherheitsmaßnahmen. Trenne nachweisbar umgesetzt,
Demo-only und für Produktion erforderlich.
```

**Teamdokumentation**

```text
Aktualisiere DOCUMENTATION.md zur Zusammenarbeit im Team. Dokumentiere die
gemeinsame Planung zu Projektbeginn, das Trello-Board mit To Do, Im Gange,
Review und Fertig, Git und GitHub, flexible Aufgabenverteilung, Abstimmung
zwischen UI und Backend, CI, Testing, KI-Nutzung und Reflexion. Beschreibe
konkret, wie UI-Buttons zunächst mit Click-Listenern und Konsolenausgaben
vorbereitet und später über definierte Service-Methoden ans Backend angebunden
wurden.
```

**Markdown-Konsolidierung**

```text
Konsolidiere die Projektdokumentation in ARCHITECTURE.md,
SECURITY_CONCEPT.md, KI_PROMPTS.md, DOCUMENTATION.md und
TEST_DOCUMENTATION.md. Übernimm relevante Inhalte aus älteren Dateien,
entferne doppelte oder veraltete Aussagen und korrigiere alle internen Verweise.
Lösche anschließend nur die nachweislich ersetzten Markdown-Dateien; README.md
und technisch erforderliche Dateien bleiben erhalten.
```

## Konkrete KI-unterstützte Funktionsbereiche

| Bereich | Nutzung |
|---|---|
| Grundstruktur | Vorschläge für Entities, Repositories, Services und Exceptions |
| Security | Analyse und Härtung von BCrypt, Codes, Demo-User, Remember-Me, Login/Logout und Reset |
| Chat | Architektur mit MongoDB, EventBus, Vaadin Push, Notifications und Booking-Events |
| Reviews | Regeln für abgeschlossene Buchungen, Rating-Summary, Duplikatschutz und Chat-Karten |
| Wallet | Demo-Guthaben, Treuhandzahlung, Storno-Erstattung, Auszahlung, automatische Freigabe und wiederkehrende Zahlungen |
| Bilder | Upload-Validierung, JPEG-Varianten, öffentliche Medienroute und Tests |
| PLZ/Map | Ortsdaten-Cache, Distanzrechnung, Filterlogik und UI-Anbindung |
| Tests | Testideen, Edge Cases, Mock-Strukturen, Integrationstests und Testdokumentation |
| Dokumentation | Architektur-, Security-, KI-, Prozess- und Testdokumentation |
| UI-Backend-Anbindung | Konkrete Buttons, Click-Listener, DTO-/Service-Verträge und anklickbare Konsolen-Platzhalter für noch fehlende Backend-Funktionen |

## Anpassungen an KI-Ergebnissen

Viele Antworten wurden nicht direkt übernommen. Typische Anpassungen waren:

- Umbenennung auf konkrete Klassen und Packages,
- Reduktion auf Uni-Projektumfang,
- Anpassung an Vaadin statt generischem Frontend,
- Trennung von Vaadin-Views, DTOs, Services und Repositories,
- Ergänzung konkreter Buttons, Navigationen und Click-Listener,
- Vorbereitung fehlender Backend-Anbindungen durch eindeutig bezeichnete Konsolenausgaben,
- Anpassung an MySQL für Kerndaten und MongoDB für Chat,
- Anpassung an bestehende Flyway-Migrationen,
- Ergänzung projektspezifischer Geschäftsregeln,
- Entfernung zu komplexer Vorschläge,
- Korrektur nicht existierender APIs,
- Ergänzung von Tests,
- manuelle Prüfung der Demo-Flows.

## Verworfene oder reduzierte Vorschläge

| Vorschlag | Grund |
|---|---|
| vollständige Microservice-Architektur | zu hoher Integrations- und Betriebsaufwand |
| separates REST-Frontend | Vaadin deckt die UI im Projekt ausreichend ab |
| produktiver Mailversand über SMTP | für lokale Demo nicht zwingend, aber produktiv nötig |
| vollständiges Admin-Backend | nicht Kernanforderung |
| echtes Payment-System mit Provider | zu groß für Projektumfang |
| produktive Monitoring-Infrastruktur | für Uni-Demo nicht erforderlich |
| externe Security-Scanner in CI | sinnvoll, aber nicht vollständig umgesetzt |
| Ende-zu-Ende-Verschlüsselung im Chat | zu umfangreich für Projektstand |

## Verantwortlichkeit

Die KI erzeugte Vorschläge, Codeideen und Textentwürfe. Das Team entschied, welche Vorschläge verwendet werden. Finaler Code, Architekturentscheidungen, Tests, Demo, Security-Verständnis und Dokumentation wurden durch das Team geprüft und verantwortet.
