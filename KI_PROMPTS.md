# KI-Nutzung und Prompts

## Zweck

Diese Datei dokumentiert, wie KI im Projekt Pawsitters genutzt wurde. Die Prompts sind als konkrete Arbeits-Prompts formuliert, wie sie im Projektverlauf für Konzept, Implementierung, Debugging, Tests, Security, Dokumentation und Präsentation verwendet wurden. Einige Formulierungen wurden für diese Dokumentation geglättet, damit sie vollständig und verständlich lesbar sind.

## Verwendete Tools

| Tool | Einsatzbereich |
|---|---|
| ChatGPT | Konzept, Architektur, Präsentation, Dokumentation, technische Erklärungen |
| Claude | Implementierungsideen, Refactoring, Debugging und Alternativvorschläge |
| Codex | Repository-nahe Implementierungsunterstützung, Tests, Dokumentation und Codeanpassungen |
| Gemini/Antigravity | Implementierungsunterstützung, Code- und Testideen |

Die genaue Modellversion wurde nicht in jeder Interaktion separat protokolliert. KI wurde als unterstützendes Werkzeug genutzt; fachliche Entscheidung, Integration, Tests und Verantwortung blieben beim Team.

## Qualitätsregeln

KI-Ergebnisse wurden nach folgenden Regeln behandelt:

- Keine ungeprüfte Übernahme von Code oder Security-Aussagen.
- Vorschläge mussten zur bestehenden Paket- und Schichtenstruktur passen.
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
Wir entwickeln im Modul Software Engineering II eine Webanwendung namens Pawsitters.
Die Anwendung soll Tierhalter und Tiersitter verbinden. Bitte hilf uns, das
Grundkonzept zu strukturieren. Berücksichtige Benutzerprofile, Haustiere,
Betreuungsangebote, Anfragen, Buchungen, Chat, Bewertungen und einen sinnvollen
Demo-Ablauf. Das Projekt wird von drei Personen umgesetzt und soll als Uni-Projekt
realistisch, aber nicht zu groß werden.
```

**Stakeholder und Anforderungen**

```text
Welche Stakeholder hat eine Petsitter-Plattform in einem Uni-Projekt?
Bitte unterscheide zwischen fachlichen Stakeholdern wie Tierhaltern und Tiersittern,
der Auftraggeberin bzw. Dozentin, dem Entwicklerteam und möglichen späteren
Betreibern. Leite daraus funktionale und nicht-funktionale Anforderungen ab.
```

**Umfang für die Demo**

```text
Wir müssen entscheiden, welche Features für unsere Pawsitters-Demo wichtig sind
und welche zu groß für den Projektumfang wären. Bitte priorisiere Features wie
Registrierung, Login, Profile, Haustiere, Angebote, Anfragen, Buchungen, Chat,
Bewertungen, Favoriten, Wallet, Admin-Bereich und echtes Payment. Begründe,
was in die Demo gehört und was wir als Ausblick dokumentieren sollten.
```

**Präsentationsstruktur**

```text
Erstelle eine Präsentationsstruktur für ein 25-minütiges Software-Engineering-Projekt.
Das Projekt heißt Pawsitters und ist eine Java/Spring-Boot/Vaadin-Anwendung.
Die Präsentation soll Konzept, Teamarbeit, Trello, Git, Architektur, Techstack,
Security, Tests, CI, Demo und KI-Nutzung enthalten. Bitte gib auch eine sinnvolle
Reihenfolge für die Demo-Flows an.
```

### Architektur

**Monolith oder Microservices**

```text
Vergleiche Monolith und Microservices für unser Projekt Pawsitters.
Rahmenbedingungen: drei Entwickler, begrenzter Uni-Projektzeitraum, Java 21,
Spring Boot, Vaadin, MySQL, MongoDB für Chat und eine lokale Demo. Begründe,
warum ein Monolith mit Schichtenarchitektur für uns sinnvoller ist als Microservices.
Nenne auch die Grenzen dieser Entscheidung.
```

**Schichtenarchitektur**

```text
Beschreibe eine passende Schichtenarchitektur für Pawsitters.
Die Anwendung nutzt Vaadin als UI, Spring Boot Services für Geschäftslogik,
Spring Data JPA für MySQL, Spring Data MongoDB für Chatdaten und DTOs für die
UI-Darstellung. Bitte erkläre UI Layer, Service Layer, Repository Layer,
Domain Layer, DTOs, Security und Config. Gib konkrete Beispiele mit Klassen wie
UserService, OfferService, BookingService, ChatService und WalletService.
```

**MySQL und MongoDB**

```text
Erkläre, warum wir in Pawsitters MySQL für Kerndaten und MongoDB für Chatdaten
verwenden können. MySQL speichert User, Pets, Offers, Requests, Bookings,
Wallet, Reviews, Notifications, Bilder und PLZ-Cache. MongoDB speichert
Chat-Konversationen und Chatnachrichten. Bitte begründe diese Aufteilung
fachlich und technisch und nenne mögliche Nachteile.
```

**Events zwischen Booking und Chat**

```text
Wir wollen den BookingService nicht direkt mit Chat-Details überladen.
Wenn eine Anfrage akzeptiert wird, soll ein BookingCreatedEvent veröffentlicht
werden, damit ein Listener eine Chat-Konversation anlegt. Wenn ein Booking
abgeschlossen wird, soll ein BookingCompletedEvent veröffentlicht werden, damit
im Chat eine Review-Erinnerungskarte erscheint. Bitte beschreibe dieses Pattern
und erkläre, warum es die Module entkoppelt.
```

**Architekturdokument aktualisieren**

```text
Bitte prüfe unsere Architekturdokumentation gegen den aktuellen Code.
Achte besonders darauf, ob Chat, MongoDB, Wallet, Reviews, Bildverwaltung,
PLZ-Distanzsuche, Favoriten, Notifications, wiederkehrende Buchungen,
Spring Events und Tests vollständig beschrieben sind. Ergänze fehlende Punkte
und entferne Aussagen, die noch aus einem älteren Projektstand stammen.
```

### Backend und Geschäftslogik

**Domainmodell**

```text
Erstelle ein Domainmodell für Pawsitters. Wir brauchen User, Pet, Offer,
OfferRequest, Booking und Notification als Kernmodell. Ein User kann fachlich
sowohl Tierhalter als auch Tiersitter sein. Offers können von Tierhaltern oder
Sittern erstellt werden. Requests beziehen sich auf Offers. Aus akzeptierten
Requests entstehen Bookings. Bitte schlage passende Felder, Beziehungen,
Status-Enums und wichtige Constraints vor.
```

**Service-Struktur**

```text
Wir wollen die Geschäftslogik nicht in Vaadin-Views oder Entities schreiben.
Bitte schlage eine Service-Struktur für Pawsitters vor. Berücksichtige
UserService, PetService, OfferService, RequestService, BookingService,
NotificationService, ChatService, ChatAccessService, WalletService,
UserReviewService, ImageAssetService und PostalCodeService. Erkläre jeweils,
welche Verantwortung der Service haben sollte.
```

**Anfrage akzeptieren**

```text
Implementiere bzw. beschreibe die Geschäftslogik für BookingService.acceptRequest.
Die Methode bekommt eine Request-ID und die ID des Offer-Creators. Sie soll prüfen:
Request existiert, Request ist PENDING, Offer ist OPEN und der aktuelle User ist
der Creator des Offers. Danach soll die Anfrage auf ACCEPTED gesetzt werden,
ein Booking erstellt werden, das Offer auf BOOKED gesetzt werden, alle anderen
PENDING Requests auf DENIED gesetzt werden und ein BookingCreatedEvent
veröffentlicht werden. Alles soll transaktional sein.
```

**Regelmäßige Buchungen**

```text
Wir wollen neben einmaligen Buchungen auch regelmäßige Buchungen unterstützen.
Ein Offer kann die Frequenz REGULAR haben und mehrere Wochentage enthalten.
Bitte hilf bei der Modellierung im BookingService: Wie unterscheiden wir
einmalige Buchungen und regelmäßige Buchungen? Wie können Pausen eingetragen
werden? Wie kann eine regelmäßige Buchung beendet werden? Welche Fehlerfälle
sollten wir prüfen?
```

**Wallet und Treuhandzahlung**

```text
Entwirf eine Demo-Wallet-Logik für Pawsitters. Nutzer sollen Demo-Guthaben
aufladen können. Beim Akzeptieren einer Buchung soll der Betrag beim Owner
zurückgehalten werden. Bei Storno vor Beginn soll das Geld zurückerstattet
werden. Nach dem Enddatum kann der Owner die Auszahlung an den Sitter freigeben
oder der Sitter kann eine Auszahlung anfordern. Nach sieben Tagen soll eine
angeforderte Auszahlung automatisch freigegeben werden. Bitte beschreibe die
benötigten Entities, Statuswerte, Service-Methoden und Tests.
```

**PLZ- und Distanzsuche**

```text
Wir brauchen eine Suche, bei der Angebote nach Entfernung zur eingegebenen
deutschen PLZ gefiltert werden können. Bitte entwirf eine Lösung mit
PostalCodeService, einem Cache in MySQL, einem externen Lookup-Client,
Plausibilitätsprüfung für deutsche Koordinaten und Haversine-Distanzberechnung.
Die Suche soll Offers nach Entfernung sortieren und bei ungültiger PLZ
eine verständliche Fehlermeldung liefern.
```

**Bildverarbeitung**

```text
Wir wollen Profil- und Haustierbilder in Pawsitters unterstützen. Bitte entwirf
einen ImageAssetService. Uploads sollen nur JPEG und PNG akzeptieren, maximal
5 MB groß sein, maximal 20 Megapixel haben und anhand des tatsächlichen Inhalts
geprüft werden. Nach dem Zuschnitt sollen optimierte JPEG-Varianten für AVATAR
und DISPLAY gespeichert werden. Die Originaldatei soll nicht dauerhaft gespeichert
werden. Erkläre auch, wie Ownership-Prüfungen für Haustierbilder funktionieren.
```

**Debugging einer Spring-Boot-Fehlermeldung**

```text
Analysiere diesen Fehler aus unserer Spring-Boot/Vaadin-Anwendung und schlage
konkrete Debugging-Schritte vor. Bitte erkläre zuerst, welche Komponente
wahrscheinlich betroffen ist, dann welche Ursache am wahrscheinlichsten ist,
und danach, welche Dateien oder Konfigurationen wir prüfen sollten:

[Hier wurde die konkrete Fehlermeldung oder der Stacktrace eingefügt.]
```

### UI und Vaadin

**Vaadin-View-Struktur**

```text
Wir bauen Pawsitters mit Vaadin. Bitte schlage eine sinnvolle Struktur für
Views und Komponenten vor. Es gibt Startseite, LoginView, RegistrateView,
ForgotPasswordView, UserView, PersonalDetailView, MyPetView, MyOffers,
MyBookings, MyWalletView, OfferView, PetsitterFilterView, CreateOfferView,
BookingView und ChatView. Welche Views sollten öffentlich sein und welche
brauchen Login? Welche Services sollten die Views jeweils verwenden?
```

**Registrierungsformular**

```text
Hilf uns bei der Validierung einer Vaadin-Registrierungsseite.
Benötigt werden E-Mail, Passwort, Passwortbestätigung, Vorname, Nachname,
Geburtsdatum, Telefon, Straße, Hausnummer, PLZ, Ort und Land. Bitte nenne
serverseitige und UI-seitige Validierungen. Das Passwort muss die Policy erfüllen
und die PLZ soll über PostalCodeService gegen den Ort geprüft werden.
```

**Such- und Filter-UI**

```text
Entwirf eine Such- und Filteroberfläche für Pawsitters-Angebote in Vaadin.
Filter sollen Rolle bzw. Suchmodus, Zeitraum, flexible Datumssuche, Preis,
Betreuungsart, Frequenz, Tierart, Ausgangs-PLZ und Entfernung enthalten.
Die Entfernung soll über einen Slider steuerbar sein und bei mehr als 100 km
als unbegrenzte Distanz interpretiert werden.
```

**ChatView**

```text
Bitte entwirf eine ChatView für Pawsitters in Vaadin. Links soll eine Liste
der Konversationen stehen, rechts der Nachrichtenbereich mit Eingabefeld.
Neue Nachrichten sollen über einen EventBus und Vaadin Push live erscheinen.
Die View soll Query-Parameter für eine ausgewählte Konversation unterstützen,
beim Lesen Nachrichten als gelesen markieren und beim Detach Listener entfernen.
```

**Review-UI**

```text
Wir wollen nach abgeschlossenen Buchungen Bewertungen ermöglichen.
Bitte entwirf eine UI-Logik für MyBookings: Wenn ein Booking COMPLETED ist
und der aktuelle User noch nicht bewertet hat, soll ein Bewertungsdialog
angezeigt werden. Der Dialog soll Sterne und Kommentar enthalten. Nach dem
Speichern soll die Bewertung sichtbar sein und optional eine Review-Karte im
Chat entstehen.
```

**Bild-Upload und Zuschnitt**

```text
Bitte hilf bei einem Vaadin-Upload für Profil- und Haustierbilder.
Der User soll ein Bild auswählen, zuschneiden und speichern können. Danach
soll die UI die neue Avatar- oder Display-Variante anzeigen. Der Backend-Service
prüft MIME-Typ, Größe und Ownership. Bitte beschreibe die Komponenten und
Fehlerfälle.
```

### Security

**Sicherheitskonzept**

```text
Erstelle ein Sicherheitskonzept für Pawsitters. Die Anwendung verarbeitet
personenbezogene Daten, Adressdaten, Haustierdaten, Angebote, Anfragen,
Buchungen, Chatnachrichten, Walletdaten, Bewertungen, Bilder und Sessiondaten.
Bitte beschreibe Schutzbedarf, Bedrohungsmodell, umgesetzte Maßnahmen und
offene produktive Maßnahmen. Unterscheide klar zwischen Uni-Demo und Produktion.
```

**Shift Security Left**

```text
Erkläre Shift Security Left für unser Projekt Pawsitters. Bitte beschreibe,
wie Security schon bei Anforderungen, Architektur, Implementierung, Tests,
CI und Review berücksichtigt wurde. Nenne Beispiele wie Passwortregeln,
BCrypt, Account-Status, Code-Flows, Service-Level-Zugriffskontrollen,
SecurityIntegrationTest und CI. Nenne auch, was noch fehlt.
```

**Passwortregeln**

```text
Welche Passwortregeln sollten wir für Pawsitters umsetzen?
Das Passwort soll mindestens 14 Zeichen haben, Groß- und Kleinbuchstaben,
Zahl und Sonderzeichen enthalten und schwache Muster wie gleiche Zeichen,
Zahlenfolgen oder verbotene Begriffe ablehnen. Bitte schlage eine Java-Service-
Struktur und passende Unit-Tests vor.
```

**Registrierungscode**

```text
Wir brauchen einen Registrierungscode-Flow. Beim Start der Registrierung soll
ein Pending-User entstehen und ein 6-stelliger Code verschickt werden.
Der Code soll nicht im Klartext in der Datenbank liegen, sondern mit BCrypt
gehasht werden. Er soll ablaufen, Fehlversuche zählen und nach erfolgreicher
Nutzung als verwendet markiert werden. Bitte beschreibe Service, Entity,
Repository und Tests.
```

**Passwort-Reset**

```text
Entwirf einen sicheren Passwort-Reset-Flow für Pawsitters.
Der User gibt seine E-Mail ein. Wenn ein verifizierter Account existiert,
wird ein 6-stelliger Reset-Code erzeugt, gehasht gespeichert und versendet.
Wenn kein Account existiert, soll trotzdem eine generische Erfolgsmeldung
zurückgegeben werden. Beim Abschluss müssen Code, Ablaufzeit, Versuchszähler,
Passwort-Policy und Passwortbestätigung geprüft werden.
```

**Remember-Me-Audit**

```text
Bitte prüfe unsere Spring-Security-Remember-Me-Konfiguration.
Achte auf Risiken wie hardcoded Remember-Me-Key, permissive UserDetailsService,
unsicheren Algorithmus, zu lange Token-Laufzeit, fehlende Cookie-Flags und
Demo-Fallbacks. Schlage konkrete Fixes vor und formuliere, was wir in der
Security-Dokumentation als umgesetzt oder als produktive Anforderung nennen
sollten.
```

**IDOR und Service-Level-Zugriff**

```text
Welche IDOR-Risiken gibt es in Pawsitters? Nutzer könnten IDs in URLs oder
Requests verändern, z. B. Offer-ID, Request-ID, Booking-ID, Conversation-ID,
Pet-ID oder ImageAsset-ID. Bitte nenne für Profile, Haustiere, Angebote,
Anfragen, Buchungen, Chat, Wallet, Reviews und Bilder die nötigen
Service-Level-Prüfungen.
```

**Demo-Flags**

```text
Bewerte die Risiken unserer Demo-Konfiguration. In der lokalen Umgebung gibt
es einen Demo-User, Dev-Ausgabe für Registrierungs- und Passwort-Reset-Codes,
lokale DB-Credentials und einen Dev-Fallback für Remember-Me. Was ist für die
Uni-Demo akzeptabel, was muss für Produktion deaktiviert werden und wie sollte
das im Security-Konzept formuliert werden?
```

### Chat-Modul

**Chat-Architektur**

```text
Plane ein Chat-Modul für Pawsitters. Der Chat soll MongoDB verwenden und
Konversationen sowie Nachrichten speichern. Es gibt Owner und Sitter als
Teilnehmer. Nachrichten sollen chronologisch geladen werden, Lesestatus haben,
Notifications erzeugen und über Vaadin Push live aktualisiert werden. Bitte
schlage Documents, DTOs, Repositories, Services und Zugriffskontrollen vor.
```

**Konversation beim Request**

```text
Wenn ein User eine Anfrage auf ein Offer stellt, soll eine Chat-Konversation
zwischen Offer-Creator und Requester entstehen oder wiederverwendet werden.
Zusätzlich soll eine Request-Systemkarte im Chat gespeichert werden und danach
eine erste Nachricht des Requesters. Bitte beschreibe die Logik in ChatService
und welche IDs in der Conversation gespeichert werden sollten.
```

**Konversation beim Booking**

```text
Wenn BookingService.acceptRequest erfolgreich war, soll ein BookingCreatedEvent
veröffentlicht werden. ChatBookingListener soll nach dem Commit eine Conversation
für das Booking erstellen oder eine vorhandene Konversation mit dem Booking
verknüpfen. Bitte achte darauf, dass die Booking-Transaktion nicht durch Chat-
Fehler kaputtgeht.
```

**Review-Reminder im Chat**

```text
Wenn ein Booking als COMPLETED markiert wird, soll ein BookingCompletedEvent
veröffentlicht werden. Ein Listener soll nach erfolgreichem Commit prüfen, ob
es eine Conversation für das Booking gibt. Falls ja, soll ChatService eine
REVIEW_REMINDER_CARD als Systemnachricht speichern und per EventBus publizieren.
Falls nicht, soll nur geloggt werden. Fehler im Chat sollen den Booking-Abschluss
nicht rückgängig machen.
```

**Review-Karte nach Bewertung**

```text
Wenn ein User nach einem abgeschlossenen Booking eine Bewertung abgibt, soll
die Bewertung gespeichert werden. Falls es eine Chat-Konversation gibt, soll
zusätzlich eine REVIEW_CARD mit Sternen und Kommentar als Systemnachricht
gespeichert und live angezeigt werden. Falls keine Konversation existiert,
soll die Bewertung trotzdem erfolgreich gespeichert bleiben.
```

### Reviews und Ratings

**Bewertungsregeln**

```text
Bitte entwirf die Geschäftsregeln für UserReviewService.
Eine Bewertung darf nur für ein Booking im Status COMPLETED erstellt werden.
Nur Owner oder Sitter der Buchung dürfen bewerten. Bewertet wird immer der
jeweilige Gegenpart. Selbstbewertungen sind nicht erlaubt. Eine Person darf
pro Booking nur einmal bewerten. Das Rating muss zwischen 1 und 5 liegen und
der Kommentar darf maximal 100 Zeichen haben.
```

**Dynamische Profilbewertungen**

```text
Unsere Profile und OfferCards sollen keine statischen Sterne mehr anzeigen.
Bitte hilf, dynamische Bewertungen einzubauen. PublicUserProfileDto soll eine
UserRatingSummary und aktuelle Reviews enthalten. UserService soll dafür
UserReviewService aufrufen. ProfilePopUp und OfferCards sollen Durchschnitt,
Anzahl und letzte Kommentare anzeigen. Wenn es keine Bewertungen gibt, soll
ein sinnvoller leerer Zustand angezeigt werden.
```

**Tests für Reviews**

```text
Schlage Unit-Tests für UserReviewService vor. Bitte decke ab:
Owner bewertet Sitter nach abgeschlossenem Booking, Review-Karte wird in den
Chat geschrieben, Review funktioniert auch ohne Conversation, Bewertung vor
Abschluss wird abgelehnt, fremder User wird abgelehnt, Rating außerhalb 1 bis 5
wird abgelehnt, doppelte Bewertung wird abgelehnt, zu langer Kommentar wird
abgelehnt und Durchschnittsbewertung wird korrekt gerundet.
```

### Wallet und Zahlungen

**Demo-Wallet**

```text
Wir wollen eine Wallet-Ansicht für Pawsitters, aber kein echtes Payment.
Bitte entwirf eine Demo-Lösung: Nutzer können 25, 50 oder 100 Euro Demo-Guthaben
aufladen. Bei einer Buchung wird der Gesamtbetrag als Treuhandzahlung gehalten.
Das Wallet soll verfügbares Guthaben, gehaltene ausgehende Beträge, erwartete
eingehende Beträge und die letzten Transaktionen anzeigen.
```

**Auszahlung und automatische Freigabe**

```text
Bitte beschreibe die Auszahlungslogik für BookingPayment.
Der Owner darf nach Ende der Betreuung die Auszahlung freigeben. Der Sitter
darf nach Ende der Betreuung eine Auszahlung anfordern. Wenn der Owner nicht
reagiert, soll nach sieben Tagen automatisch ausgezahlt werden. Vor oder am
Enddatum darf keine Auszahlung stattfinden. Bitte nenne Statuswerte, Methoden
und Tests.
```

**Regelmäßige Zahlungen**

```text
Für regelmäßige Buchungen sollen wöchentliche Zahlungen erzeugt werden.
Jede Woche wird anhand der gebuchten Wochentage berechnet, wie viele Termine
zahlbar sind. Bei Pausen sollen gehaltene Zahlungen neu berechnet oder übersprungen
werden. Wenn das Guthaben nicht reicht, soll eine Top-Up-Notification entstehen.
Bitte schlage eine Service-Logik und Testfälle vor.
```

### Tests

**Testfälle für Kernlogik**

```text
Bitte sammle Unit-Tests für die Kernlogik von Pawsitters.
Berücksichtige Registrierung, Login, Passwortregeln, Passwort-Reset, Profil,
Haustiere, Angebote, Anfragen, Buchungen, Chat, Wallet, Reviews, Bilder,
PLZ-Validierung und Favoriten. Gib pro Test kurz an, ob es ein Normalfall oder
Edge Case ist.
```

**SecurityIntegrationTest**

```text
Welche Integrationstests sollten wir für Spring Security schreiben?
Bitte prüfe: Startseite ist öffentlich, öffentliche Medienroute ist erreichbar,
geschützte Route leitet anonym auf /login um, Demo-Login erzeugt eine
authentifizierte Session und Logout invalidiert die Session. Schreibe die Tests
mit MockHttpServletRequest, MockHttpServletResponse und FilterChainProxy.
```

**Bildtests**

```text
Schlage Tests für ImageAssetService und ImageMediaController vor.
Wir wollen prüfen, dass PNG-Uploads als JPEG-Varianten gespeichert werden,
falsche MIME-Typen, zu große Dateien und kaputte Inhalte abgelehnt werden,
beim Ersetzen eines Profilbilds das alte Asset gelöscht wird, fremde Haustierbilder
nicht verändert werden dürfen und die öffentliche Medienroute den nosniff-Header
setzt.
```

**Wallettests**

```text
Bitte schlage Tests für WalletService vor.
Abdecken sollen wir Demo-Aufladung, Treuhandreservierung, zu wenig Guthaben
ohne Mutation, doppelte Reservierung, Erstattung bei Storno, Auszahlung an
Sitter, Ablehnung vor und am Enddatum, Auszahlung anfordern und automatische
Freigabe nach sieben Tagen.
```

**Testdokumentation**

```text
Erstelle eine Testdokumentation für unser Projekt. Sie soll erklären, dass die
Vorgabe von mindestens 10 Unit-Tests erfüllt ist. Bitte liste alle Testklassen
mit Bereich und Testart auf. Danach dokumentiere wichtige Einzeltests mit:
Testklasse, Testname oder Testbereich, was getestet wird, erwartetes Ergebnis
und Falltyp Normalfall oder Edge Case.
```

### CI und Infrastruktur

**GitHub Actions**

```text
Wir brauchen eine einfache CI für ein Maven-Spring-Boot-Projekt mit Java 21.
Bitte erstelle oder erkläre eine GitHub-Actions-Pipeline, die bei Pushes und
Pull Requests läuft, das Repository auscheckt, JDK 21 einrichtet, Maven-Cache
nutzt und ./mvnw -B verify -Dspring.docker.compose.enabled=false ausführt.
```

**Docker Compose in Tests deaktivieren**

```text
In der lokalen Entwicklung nutzt Pawsitters Docker Compose für MySQL und MongoDB.
In Tests und CI soll Spring Boot Docker Compose aber nicht starten. Bitte erkläre,
wie wir spring.docker.compose.enabled=false setzen und warum H2 in Tests sinnvoll
ist. Nenne auch mögliche Stolperfallen.
```

**Docker-Startguard**

```text
Wir wollen lokal eine verständliche Meldung, wenn Docker bzw. Docker Desktop
nicht läuft, weil unsere App sonst beim Start unklar fehlschlägt. Bitte entwirf
eine kleine Guard-Klasse, die prüft, ob Docker erreichbar ist, und nur blockiert,
wenn Spring Docker Compose aktiviert ist. In Tests soll diese Prüfung deaktiviert
sein.
```

### Dokumentation

**Architekturdokumentation**

```text
Schreibe eine Architekturbeschreibung für Pawsitters. Bitte beschreibe Ziel,
Architekturentscheidung, Systemübersicht, Schichten, zentrale Fachmodule,
Persistenz mit MySQL und MongoDB, interne Events, Security, Testing, CI,
bekannte Grenzen und Ausblick. Die Doku soll zum aktuellen Code passen.
```

**Security-Dokumentation**

```text
Schreibe ein Sicherheitskonzept für Pawsitters. Bitte dokumentiere Schutzbedarf,
Spring Security, Rollenmodell, Authentifizierung, Registrierungscode,
Passwort-Reset, Passwort-Policy, Remember-Me, CSRF, Zugriffskontrollen nach
Fachbereich, Bedrohungsmodell, umgesetzte Maßnahmen, Shift Security Left,
Grenzen und nächste Sicherheitsmaßnahmen.
```

**Teamdokumentation**

```text
Schreibe eine Projektdokumentation über unsere Zusammenarbeit im Team.
Erwähne, dass wir uns besonders am Anfang persönlich zusammengesetzt und das
Projekt geplant haben. Beschreibe unser Trello-Board mit To Do, Im Gange,
Review und Fertig, Git und GitHub für Versionierung und Zusammenarbeit,
flexible Aufgabenverteilung, UI-Backend-Abstimmung, CI, Testing, KI-Nutzung
und eine kurze Reflexion.
```

**Markdown-Konsolidierung**

```text
Wir haben viele Markdown-Dateien im Projekt, wollen aber nur noch
ARCHITECTURE.md, SECURITY_CONCEPT.md, KI_PROMPTS.md, DOCUMENTATION.md und
TEST_DOCUMENTATION.md behalten. Bitte konsolidiere die Inhalte der alten
Dateien in diese fünf Dokumente, entferne tote Verweise und lösche alle übrigen
Markdown-Dateien im gesamten Repository.
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

## Anpassungen an KI-Ergebnissen

Viele Antworten wurden nicht direkt übernommen. Typische Anpassungen waren:

- Umbenennung auf konkrete Klassen und Packages,
- Reduktion auf Uni-Projektumfang,
- Anpassung an Vaadin statt generischem Frontend,
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
