# Sicherheitskonzept

## Ziel

Dieses Sicherheitskonzept beschreibt den Schutzbedarf, die Risiken, die umgesetzten Maßnahmen und die offenen produktionsnahen Erweiterungen für Pawsitters. Das Projekt ist ein Uni-Projekt; daher sind einige Maßnahmen für Demo und lokale Entwicklung bewusst pragmatisch umgesetzt, während produktive Anforderungen gesondert benannt werden.

## Schutzbedarf

Die Anwendung verarbeitet personenbezogene und fachlich sensible Daten.

| Datenart | Beispiele | Schutzbedarf |
|---|---|---|
| Accountdaten | E-Mail, Passwort-Hash, Rolle, Account-Status | hoch |
| Registrierung und Reset | Code-Hashes, Ablaufzeit, Versuche, IP-Adresse | hoch |
| Profildaten | Name, Anzeigename, Telefon, Geburtsdatum, Sprache, Bio | mittel bis hoch |
| Adressdaten | Straße, Hausnummer, PLZ, Ort, Land | hoch |
| Haustierdaten | Name, Tierart, Rasse, Beschreibung, Impfstatus | mittel |
| Bilder | Profil- und Haustierbilder | mittel bis hoch |
| Angebote | Zeitraum, Ort, Preis, Beschreibung, Status | mittel |
| Anfragen | Nachricht, Status, beteiligte Nutzer | mittel bis hoch |
| Buchungen | Owner, Sitter, Zeitraum, Preis, Haustier | hoch |
| Walletdaten | Guthaben, Treuhandzahlungen, Transaktionen | hoch |
| Bewertungen | Sterne, Kommentar, Booking-Bezug | mittel |
| Chatdaten | Nachrichten, Systemkarten, Zeitstempel, Teilnehmer | hoch |
| Benachrichtigungen | Nachrichtentyp, Empfänger, Referenz-ID | mittel |
| Sessiondaten | `JSESSIONID`, Remember-Me-Token | hoch |

## Sicherheitsarchitektur

### Spring Security

Spring Security ist zentral in `SecurityConfig` konfiguriert.

Umgesetzt:

- alle nicht öffentlichen Requests benötigen Authentifizierung,
- nicht authentifizierte Requests werden auf `/login` umgeleitet,
- Form-Login nutzt E-Mail und Passwort,
- Logout invalidiert die Session,
- Logout löscht `JSESSIONID` und `REMEMBER_ME`,
- Method Security ist mit JSR-250-Unterstützung aktiviert,
- PUMA-Asset-Routen werden explizit blockiert,
- öffentliche Routen sind begrenzt.

Öffentlich sind unter anderem:

- `/`,
- `/login`,
- `/forgot-password`,
- `/error`,
- Vaadin- und statische Assets,
- `/connect/**` für Vaadin-Verbindungsendpunkte,
- `/media/images/**` für öffentliche Bildvarianten.

Die HTTP-Filterchain verlangt für alle nicht explizit freigegebenen Requests Authentifizierung. Zusätzlich nutzen einzelne Vaadin-Views Annotationen wie `@AnonymousAllowed`, `@PermitAll` oder `@RolesAllowed`. Ein wichtiger Abgleichspunkt ist aktuell die Registrierung: `RegistrateView` ist mit `@AnonymousAllowed` markiert, `/register` steht aber nicht in der expliziten `permitAll`-Liste der `SecurityFilterChain`. Produktiv sollte das durch einen Security-Test abgesichert oder in der HTTP-Konfiguration angeglichen werden.

### Rollenmodell

Technisch existieren:

- `SIGNED_IN_USER`,
- `ADMIN`.

Reguläre Nutzer erhalten `SIGNED_IN_USER`. Fachlich werden Tierhalter und Tiersitter nicht als starre Rollen modelliert. Ein Nutzer kann je nach Aktion Haustiere besitzen, Angebote erstellen, Anfragen stellen oder Betreuung anbieten.

`ADMIN` ist aktuell ein technischer Erweiterungspunkt. Ein fachlicher Admin-Bereich ist nicht umgesetzt.

### Authentifizierung

Der Login erfolgt über E-Mail und Passwort. `DatabaseUserDetailsService` lädt Nutzer aus der Datenbank. Nur verifizierte Accounts dürfen sich anmelden. Pending- und gesperrte Accounts werden abgelehnt.

Passwörter werden mit BCrypt gespeichert und geprüft. Das Passwort wird nie im Klartext persistiert.

Für lokale Demo und Entwicklung gibt es einen konfigurierbaren Demo-User-Fallback über `petsitter.security.demo.*`. Dieser ist für produktionsnahe Umgebungen zu deaktivieren.

### Registrierungscode

Die Registrierung erzeugt zunächst einen Pending-Account. Danach wird ein Registrierungscode versendet bzw. lokal angezeigt.

Umgesetzt:

- 6-stelliger Code,
- Speicherung nur als BCrypt-Hash,
- begrenzte Gültigkeit von 20 Minuten,
- maximal 3 Fehlversuche pro Code,
- alte offene Codes werden invalidiert,
- Pending-Accounts werden mit `deleteAfter` nach 24 Stunden zur Bereinigung markiert,
- Codes werden standardmäßig maskiert geloggt.

In lokaler Entwicklung kann der Code über `petsitter.dev.show-login-code=true` vollständig im Terminal ausgegeben werden. Das ist Demo-Komfort und darf produktiv nicht aktiv sein.

### Passwort-Reset

Der Passwort-Reset nutzt einen eigenen Code-Flow mit separater Tabelle.

Umgesetzt:

- Reset-Code wird zufällig erzeugt,
- Code wird als BCrypt-Hash gespeichert,
- Code ist 20 Minuten gültig,
- maximal 3 Fehlversuche pro Code,
- unbekannte oder nicht verifizierte E-Mail-Adressen erhalten eine generische Antwort,
- neues Passwort muss die Passwort-Policy erfüllen,
- Passwort und Bestätigung müssen übereinstimmen,
- alte offene Reset-Codes werden invalidiert.

Auch Reset-Codes können lokal vollständig im Terminal ausgegeben werden. Produktiv muss ein Mailversand genutzt und die Dev-Ausgabe deaktiviert werden.

### Passwort-Policy

Die Passwort-Policy reduziert schwache Passwörter.

Umgesetzt:

- Mindestlänge,
- konkret mindestens 14 Zeichen,
- Groß- und Kleinbuchstaben,
- Ziffer,
- Sonderzeichen,
- Erkennung identischer Zeichenfolgen,
- Erkennung vierstelliger Zahlenblöcke,
- Erkennung auf- und absteigender Zahlenfolgen,
- kuratierte Liste verbotener schwacher Begriffe,
- serverseitige Prüfung in Registrierung und Passwort-Reset.

### Remember-Me

Remember-Me ist auf 7 Tage begrenzt. Die Tokenvalidierung nutzt einen DB-basierten `RememberMeUserDetailsService` und akzeptiert keine beliebigen Usernamen.

Umgesetzt:

- SHA256-Matching-Algorithmus,
- 7 Tage Gültigkeit,
- `alwaysRemember=false`,
- `useSecureCookie=true`,
- `TokenBasedRememberMeServices` liest den Key aus `PETSITTER_REMEMBER_ME_KEY`,
- Dev-Fallback-Key nur für lokale Umgebung.

Wichtig: In der `SecurityFilterChain` wird zusätzlich `.key("petsitter-remember-me-key")` gesetzt, obwohl der konkrete `RememberMeServices`-Bean bereits einen Key aus Umgebung oder Dev-Fallback nutzt. Produktiv muss sichergestellt werden, dass nur ein geheimer, externer Key wirksam ist und kein Dev-Fallback aktiv bleibt.

### CSRF

Spring-CSRF ist deaktiviert, weil Vaadin eigene Schutzmechanismen für Vaadin-Requests nutzt und Spring-CSRF Vaadin-Kommunikation stören kann.

Konsequenz:

- Vaadin-Flows bleiben kompatibel,
- manuell ergänzte Nicht-Vaadin-Endpunkte müssen separat auf CSRF-Risiken geprüft werden,
- zustandsändernde zusätzliche Endpoints benötigen eigene Schutzmaßnahmen.

## Zugriffskontrolle nach Fachbereich

| Bereich | Schutzregel |
|---|---|
| Profile | Aktuelles Profil nur für angemeldeten User; öffentliche Profile liefern reduzierte DTOs |
| Haustiere | Erstellen, Ändern, Löschen nur für den Owner |
| Bilder | Profilbild nur für aktuellen User; Haustierbild nur für Haustier des aktuellen Users |
| Angebote | Bearbeiten und Löschen nur für Creator und nur bei erlaubtem Status |
| Suche | Eigene und abgelaufene Angebote werden in öffentlichen Listen passend gefiltert |
| Anfragen | Keine Anfrage auf eigenes Offer; Abruf von Offer-Anfragen nur für Offer-Creator; eigene Anfragen werden über die aktuelle User-ID geladen |
| Buchungen | Storno, Abschluss, Pausen und Auszahlung prüfen Owner/Sitter nach Kontext |
| Wallet | Booking-Auszahlungen prüfen Owner/Sitter; Wallet-Ansicht und Demo-Aufladung binden die User-ID in der UI an `AuthenticatedUser` |
| Chat | Nutzergetriebenes Lesen und Schreiben nur für beteiligte User bzw. Owner/Sitter; Systemkarten entstehen aus internen Booking-/Review-Flows |
| Bewertungen | Nur Booking-Teilnehmer nach abgeschlossenem Booking; keine Selbst- oder Doppelbewertung |
| Notifications | Current-User-Methoden laden eigene Benachrichtigungen; Markieren als gelesen prüft den Empfänger |

Viele Prüfungen liegen bewusst im Service Layer. Dadurch reicht es bei Aktionen wie Offer-Bearbeitung, Request-Ablehnung, Booking-Statuswechsel, Chat und Review nicht aus, eine fremde ID in der UI oder URL zu verändern. Einige lesende oder demoartige Methoden akzeptieren jedoch `userId` bzw. `currentUserId` als Parameter und erwarten, dass der Caller diese aus `AuthenticatedUser` ableitet. Diese Methoden dürfen nicht ungeprüft als frei parametrisierbare Endpunkte exponiert werden.

## Bedrohungsmodell und Maßnahmen

### Unbefugter Zugriff auf geschützte Seiten

Risiko: Nicht angemeldete Nutzer könnten Profil-, Buchungs-, Wallet- oder Chatdaten einsehen.

Umgesetzt:

- Spring Security schützt nicht öffentliche Routen,
- anonyme Requests werden auf `/login` umgeleitet,
- Method Security ist aktiviert,
- Services prüfen fachliche Berechtigungen.

Offen:

- vollständige Security-Tests für alle UI-Routen,
- expliziter Test oder Konfigurationsabgleich für `/register`,
- konsistente Rollenprüfung für mögliche Admin-Funktionen.

### IDOR

IDOR bedeutet Insecure Direct Object Reference. Ein Nutzer verändert eine Objekt-ID und versucht, fremde Daten zu lesen oder zu ändern.

Umgesetzt:

- Ownership-Prüfungen in Services,
- Chat-Zugriff über Booking- und Teilnehmerbeziehung,
- Buchungsaktionen nur für Owner oder Sitter,
- öffentliche Profile mit reduzierten Feldern,
- Bildänderungen nur für eigene Ressourcen,
- Reviews nur für Booking-Teilnehmer.

Offen:

- weitere negative Zugriffstests für alle ID-basierten Operationen,
- Service-Methoden mit `userId`-Parametern bei neuen Callern konsequent an `AuthenticatedUser` binden,
- Logging verweigerter Zugriffe.

### Schwache oder kompromittierte Passwörter

Umgesetzt:

- BCrypt,
- Passwort-Policy,
- Prüfung bei Registrierung und Reset,
- keine Klartextspeicherung.

Offen:

- Prüfung gegen bekannte geleakte Passwörter,
- MFA für produktive Admin-Konten,
- bessere Auditierung von Login- und Reset-Ereignissen.

### Missbrauch von Codes

Umgesetzt:

- Codes werden gehasht,
- Codes laufen ab,
- Fehlversuche werden gezählt,
- alte Codes werden invalidiert,
- generische Antwort beim Passwort-Reset schützt vor einfacher Account-Enumeration.

Offen:

- globales Rate-Limiting nach IP und E-Mail,
- CAPTCHA oder Turnstile bei auffälligem Verhalten,
- zentrale Audit-Logs für Code-Anforderungen und Fehlversuche,
- SMTP statt Terminalausgabe.

### Session Hijacking

Umgesetzt:

- Logout invalidiert Session,
- Cookies werden beim Logout gelöscht,
- Remember-Me ist begrenzt,
- Remember-Me validiert Nutzer gegen DB.

Offen:

- HTTPS verpflichtend,
- Cookie-Flags in Zielumgebung prüfen,
- Remember-Me-Key ausschließlich über Secret konfigurieren,
- Session-Fixation-Verhalten produktionsnah prüfen.

### Chatdaten

Risiko: Nutzer könnten fremde Konversationen lesen oder Nachrichten senden.

Umgesetzt:

- `ChatAccessService` prüft Zugriff,
- `ChatService` nutzt Zugriffskontrolle vor nutzergetriebenen Nachrichtenoperationen,
- Chat ist an Booking oder Teilnehmerpaar gekoppelt,
- fremde Nutzer werden abgelehnt,
- Nachrichten erzeugen nur passende Notifications.

Offen:

- verteiltes Eventing für Multi-Instance-Betrieb,
- Audit-Logging verweigerter Chat-Zugriffe,
- produktive Datenschutz- und Aufbewahrungsregeln.

### Wallet und Zahlungen

Risiko: Nutzer könnten fremde Auszahlungen auslösen oder Walletdaten manipulieren.

Umgesetzt:

- Buchungszahlung wird über Service-Logik gehalten,
- Owner gibt Auszahlung frei,
- Sitter darf Auszahlung anfordern,
- automatische Freigabe erst nach Frist,
- unzureichendes Guthaben erzeugt fachliche Exception,
- Transaktionen werden als Ledger-Einträge gespeichert,
- `MyWalletView` lädt Guthaben und Demo-Aufladung über die aus `AuthenticatedUser` abgeleitete User-ID.

Offen:

- echte Zahlungsdienstleister-Integration,
- manipulationssichere Audit-Logs,
- Service-seitige Härtung von Wallet-Lese- und Demo-Auflade-Methoden, falls diese außerhalb der aktuellen UI genutzt werden,
- produktive Buchhaltungs- und Compliance-Prüfung.

### Bild-Upload

Risiko: Uploads könnten zu Speicherverbrauch, falschen Dateitypen oder unsicheren Medien führen.

Umgesetzt:

- maximale Dateigröße 5 MB,
- maximale Pixelzahl 20 Megapixel,
- nur JPEG und PNG,
- tatsächlicher Bildinhalt wird geprüft,
- MIME-Typ und Inhalt müssen zusammenpassen,
- Speicherung als optimierte JPEG-Varianten,
- öffentliche Auslieferung mit stabilen Varianten-URLs,
- Controller setzt `nosniff`-Header.

Offen:

- Viren- oder Malware-Scanning für produktive Uploads,
- CDN oder externer Objektspeicher für größere Produktion.

### PLZ- und externe Ortsdaten

Risiko: Externe Lookups können ausfallen oder falsche Daten liefern.

Umgesetzt:

- Formatprüfung für deutsche PLZ,
- Cache in MySQL,
- Plausibilitätsprüfung von Koordinaten,
- Fehler werden als fachliche Validierungsantworten behandelt.

Offen:

- Timeout- und Rate-Limiting-Policy für externe Dienste,
- Monitoring externer Lookup-Fehler.

### Datenbankrisiken

Umgesetzt:

- MySQL für relationale Kerndaten,
- MongoDB für Chatdaten,
- Flyway-Migrationen,
- H2 für Tests,
- Cascades für Bildvarianten bei Besitzerlöschung.

Offen:

- Backups und Restore-Prozess,
- minimale Datenbankrechte,
- Verschlüsselung ruhender Daten,
- Lösch- und Aufbewahrungsfristen,
- getrennte produktive Profile und Secrets.

### Secrets und Demo-Konfiguration

Risiko: Demo-Zugänge, Dev-Flags oder lokale Secrets werden produktiv genutzt.

Aktueller Stand:

- Demo-User ist per Default aktiv,
- Registrierungscodes und Reset-Codes können lokal vollständig ausgegeben werden,
- Datenbank- und Mongo-Credentials haben lokale Defaults,
- Remember-Me hat einen Dev-Fallback-Key.

Produktive Anforderungen:

- `PETSITTER_DEMO_ENABLED=false`,
- `petsitter.dev.show-login-code=false`,
- `petsitter.dev.show-password-reset-code=false`,
- `PETSITTER_REMEMBER_ME_KEY` als echtes Secret setzen,
- DB- und Mongo-Secrets nur über Umgebung oder Secret Store,
- keine produktiven Zugangsdaten im Repository.

## Bereits umgesetzte Sicherheitsmaßnahmen

| Maßnahme | Status |
|---|---|
| Spring Security | umgesetzt |
| Form-Login mit E-Mail und Passwort | umgesetzt |
| Authentifizierung für geschützte Routen | umgesetzt |
| Begrenzte öffentliche Routen | umgesetzt |
| BCrypt für Passwörter | umgesetzt |
| Passwort-Policy | umgesetzt |
| Registrierung mit Pending-Status und Code | umgesetzt |
| Registrierungscode als Hash | umgesetzt |
| Passwort-Reset-Code als Hash | umgesetzt |
| Versuchszählung bei Codes | umgesetzt |
| Account-Status `PENDING`, `VERIFIED`, `BLOCKED` | umgesetzt |
| Technische Rollen `ADMIN`, `SIGNED_IN_USER` | umgesetzt |
| Method Security | aktiviert |
| Session-Invalidierung bei Logout | umgesetzt |
| Cookie-Löschung bei Logout | umgesetzt |
| Remember-Me für 7 Tage | umgesetzt |
| DB-basierte Remember-Me-Uservalidierung | umgesetzt |
| Chat-Zugriffskontrolle | umgesetzt |
| Service-Level-Ownership-Prüfungen | überwiegend umgesetzt; Methoden mit `userId`-Parametern benötigen Current-User-Bindung im Caller |
| Bild-Upload-Validierung | umgesetzt |
| Öffentliche Bildroute mit `nosniff` | umgesetzt |
| Review-Berechtigungen | umgesetzt |
| Wallet-Berechtigungen für Booking-Auszahlungen | umgesetzt |
| CI-Build mit Tests | umgesetzt |

## Shift Security Left

Shift Security Left bedeutet, Sicherheitsaspekte früh in Anforderungen, Architektur, Implementierung, Tests und CI einzubeziehen.

| Phase | Umsetzung im Projekt |
|---|---|
| Anforderungen | sensible Daten, Rollen, Demo-Grenzen und Risiken wurden dokumentiert |
| Architektur | Authentifizierung, Schichtenarchitektur, Service-Prüfungen und getrennte Persistenz eingeplant |
| Implementierung | BCrypt, Passwortregeln, Code-Validierung, Accountstatus, Chat-, Wallet- und Review-Zugriffskontrollen umgesetzt |
| Testing | Tests prüfen Passwortregeln, Login, Registrierung, Reset, Zugriffsschutz, Chat, Wallet, Bilder und Edge Cases |
| CI | Maven Verify läuft automatisiert |
| Review | Trello-Review-Spalte und manuelle Prüfung unterstützen Qualität |

## Grenzen des aktuellen Ansatzes

Aktuell fehlen für eine produktionsnahe Sicherheitslage:

- Dependency Vulnerability Scan,
- Secret Scanning,
- SAST,
- DAST gegen laufende Testinstanz,
- zentralisiertes Audit-Logging,
- produktive Mail-Infrastruktur,
- produktives Rate-Limiting,
- vollständige Datenschutzprozesse,
- echte Browser-End-to-End-Security-Tests.

## Empfohlene nächste Sicherheitsmaßnahmen

| Priorität | Maßnahme |
|---|---|
| hoch | Demo-User und Dev-Code-Ausgabe deaktivieren |
| hoch | HTTPS und sichere Cookie-Konfiguration verpflichtend machen |
| hoch | Remember-Me-Key ausschließlich aus Secret Store beziehen |
| hoch | SMTP oder Mailprovider für Code-Flows integrieren |
| hoch | Rate-Limiting für Login, Registrierung und Reset ergänzen |
| hoch | Zugriffstests für alle ID-basierten Fachoperationen ausbauen |
| mittel | Dependency-Check und Secret Scanning in CI ergänzen |
| mittel | Audit-Logging für Login, Logout, Reset, Wallet und verweigerte Zugriffe |
| mittel | Upload-Malware-Scanning prüfen |
| mittel | Backup-, Restore- und Löschprozesse dokumentieren |
| niedrig | Monitoring und Alerting ergänzen |

## Fazit

Das Projekt enthält bereits mehrere relevante Sicherheitsmaßnahmen: Spring Security, BCrypt, Passwortregeln, Registrierungscodes, Passwort-Reset-Codes, Account-Status, Session-Logout, Remember-Me-Begrenzung, Service-Level-Zugriffskontrollen, Chat-Schutz, Wallet-Regeln, Review-Regeln und Upload-Härtung. Für ein reales Produkt müssten vor allem Deployment-Sicherheit, Secrets, Mailversand, Rate-Limiting, Audit-Logging, Datenschutzprozesse und automatisierte Security-Checks erweitert werden.
