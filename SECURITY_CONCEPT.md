# Sicherheitskonzept

## Ziel

Dieses Sicherheitskonzept beschreibt die sensiblen Daten, Risiken, bereits umgesetzten Maßnahmen und notwendigen Erweiterungen für Pawsitters. Das Projekt ist ein Uni-Projekt. Einige Maßnahmen sind bereits implementiert, andere werden als Anforderungen an eine produktionsnahe Version dokumentiert.

## Schutzbedarf

Die Anwendung verarbeitet personenbezogene und fachlich sensible Daten.

| Datenart | Beispiele | Schutzbedarf |
|---|---|---|
| Accountdaten | E-Mail, Passwort-Hash, Account-Rolle, Account-Status | hoch |
| Registrierungsdaten | Bestätigungscode als Hash, Ablaufzeit, Versuche, IP-Adresse | hoch |
| Profildaten | Name, Anzeigename, Telefonnummer, Geburtsdatum, Nationalität, Sprache, Bio | mittel bis hoch |
| Adressdaten | Straße, Hausnummer, PLZ, Ort, Land | hoch |
| Haustierdaten | Name, Tierart, Rasse, Alter, Beschreibung | mittel |
| Angebote | Zeitraum, Ort, Preis, Beschreibung, Status | mittel |
| Anfragen | Nachricht, Status, beteiligte Nutzer | mittel bis hoch |
| Buchungen | Owner, Sitter, Zeitraum, Preis, Haustier | hoch |
| Chatdaten | Nachrichten, Zeitstempel, Sender, Empfänger, Conversation-ID | hoch |
| Benachrichtigungen | Nachrichtentyp, Empfänger, Referenz-ID | mittel |
| Sessiondaten | JSESSIONID, Remember-Me-Token | hoch |

## Bedrohungsmodell

### Unbefugter Zugriff auf geschützte Seiten

Risiko: Nicht angemeldete Nutzer könnten Profil-, Buchungs-, Angebots- oder Chatdaten einsehen.

Gegenmaßnahmen im Projekt:

- Spring Security ist aktiviert.
- Öffentliche Routen sind begrenzt.
- Alle übrigen Requests erfordern Authentifizierung.
- Nicht authentifizierte Requests werden auf `/login` umgeleitet.
- Method Security ist aktiviert.

Weitere notwendige Maßnahmen:

- Feingranulare Rechteprüfung für alle fachlichen Operationen.
- Vollständige Tests für Zugriffsschutz auf Views und Services.
- Explizite Rollenprüfung für Admin-Funktionen.

### Schwache oder kompromittierte Passwörter

Risiko: Nutzer verwenden schwache Passwörter oder Passwörter werden aus der Datenbank gestohlen.

Gegenmaßnahmen im Projekt:

- Passwörter werden nicht im Klartext gespeichert.
- Passwort-Hashes werden mit BCrypt erzeugt.
- Es gibt eine Passwort-Policy mit Mindestlänge und Regeln gegen schwache Muster.
- Registrierung validiert die Passwortregeln.

Weitere notwendige Maßnahmen:

- Passwort-Reset-Prozess härten.
- Optional Prüfung gegen bekannte geleakte Passwörter.
- MFA für produktive Admin-Konten.

### Missbrauch von Registrierungscodes

Risiko: Ein Angreifer errät oder mehrfach verwendet Bestätigungscodes.

Gegenmaßnahmen im Projekt:

- Login- und Registrierungscodes werden gehasht gespeichert.
- Codes haben eine Ablaufzeit.
- Codes können als verwendet markiert werden.
- Die Anzahl der Versuche ist begrenzt.
- Abgelaufene Pending-Accounts können bereinigt werden.

Weitere notwendige Maßnahmen:

- Globales Rate-Limiting nach IP und E-Mail.
- CAPTCHA oder Turnstile bei auffälligen Mustern.
- Audit-Logging für fehlgeschlagene Codeversuche.

### Session Hijacking

Risiko: Ein Angreifer übernimmt Session-Cookies oder Remember-Me-Tokens.

Gegenmaßnahmen im Projekt:

- Logout invalidiert die Session.
- Logout löscht `JSESSIONID` und `REMEMBER_ME`.
- Remember-Me ist auf 7 Tage begrenzt.
- Remember-Me nutzt SHA256.
- Secure-Cookie-Verwendung ist vorgesehen.

Weitere notwendige Maßnahmen:

- HTTPS verpflichtend.
- Cookie-Flags `Secure`, `HttpOnly` und `SameSite` prüfen.
- Remember-Me-Key ausschließlich als Secret konfigurieren.
- Session-Fixation-Schutz prüfen und dokumentieren.

### Zugriff auf fremde Chatnachrichten

Risiko: Ein Nutzer könnte fremde Konversationen lesen oder Nachrichten in fremde Chats schreiben.

Gegenmaßnahmen im Projekt:

- Chat ist an Buchungen gekoppelt.
- Chat-Zugriff wird über Owner/Sitter-Beziehung geprüft.
- Chatoperationen laufen über ChatService und ChatAccessService.
- Nur beteiligte Nutzer einer Buchung dürfen Konversationen sehen und Nachrichten senden.

Weitere notwendige Maßnahmen:

- Zugriffstests für Chat-Konversationen.
- Protokollierung verweigerter Chat-Zugriffe.
- Prüfung aller UI-Routen und Query-Parameter auf IDOR-Risiken.

### IDOR bei Profilen, Angeboten und Buchungen

Definition: IDOR bedeutet Insecure Direct Object Reference. Ein Nutzer verändert eine ID in URL oder Anfrage und greift dadurch auf fremde Objekte zu.

Risiko: Zugriff auf fremde Profile, Angebote, Buchungen oder Anfragen.

Gegenmaßnahmen im Projekt:

- Services prüfen an mehreren Stellen den aktuellen Nutzer.
- Öffentliche Profile geben nur reduzierte Felder zurück.
- Fachlogik liegt im Service Layer.

Weitere notwendige Maßnahmen:

- Alle Methoden mit Objekt-ID müssen Ownership oder Berechtigung prüfen.
- Tests für fremde IDs ergänzen.
- Keine vertraulichen Felder in öffentlichen DTOs zurückgeben.

### Datenbankrisiken

Risiko: Verlust, Manipulation oder unbefugter Zugriff auf MySQL- oder MongoDB-Daten.

Gegenmaßnahmen im Projekt:

- Relationale Daten liegen in MySQL.
- Chatdaten liegen getrennt in MongoDB.
- Schemaänderungen erfolgen über Flyway.
- Testumgebung nutzt H2.

Weitere notwendige Maßnahmen:

- Datenbankzugangsdaten als Secrets verwalten.
- Backups und Restore-Prozess definieren.
- Minimale Datenbankrechte pro Anwendung.
- Verschlüsselung ruhender Daten prüfen.
- Lösch- und Aufbewahrungsfristen definieren.

### Fehlerhafte Eingaben

Risiko: Fehlerhafte oder manipulierte Eingaben führen zu ungültigem Zustand oder Angriffen.

Gegenmaßnahmen im Projekt:

- Server-seitige Validierung in Services.
- Validierung von E-Mail, Pflichtfeldern, Mindestalter, Passwortregeln und Postleitzahlen.
- UI-seitige Vorvalidierung in Vaadin-Views.

Weitere notwendige Maßnahmen:

- Einheitliche Validierung mit DTOs und Bean Validation.
- Negative Tests für alle kritischen Eingaben.
- Längenbegrenzungen und Normalisierung für Freitextfelder.

### CSRF und Webangriffe

Risiko: Angreifer lösen Aktionen im Namen eines angemeldeten Nutzers aus.

Aktueller Stand:

- Spring-CSRF ist deaktiviert, weil Vaadin eigene Mechanismen nutzt und Spring-CSRF Vaadin-Requests stören kann.
- Das muss für eine produktive Version explizit geprüft werden.

Weitere notwendige Maßnahmen:

- Vaadin-CSRF-Verhalten für alle genutzten Flows prüfen.
- Manuelle Endpoints gesondert gegen CSRF absichern.
- Security-Tests für zustandsändernde Aktionen ergänzen.

### Secrets und Demo-Konfiguration

Risiko: Demo-Zugänge, lokale Schlüssel oder Entwicklungskonfigurationen werden produktiv genutzt.

Aktueller Stand:

- Für die Abgabe bleibt ein Demo- bzw. Entwicklungsmodus aktiv, um die Prüfung und Demo zu erleichtern.
- In produktionsnahen Umgebungen muss dieser Modus deaktiviert werden.

Weitere notwendige Maßnahmen:

- Demo-User deaktivieren.
- Secrets ausschließlich über Umgebungsvariablen oder Secret Stores setzen.
- Keine produktiven Zugangsdaten im Repository speichern.
- Separate Profile für local, test und production verwenden.

## Bereits umgesetzte Sicherheitsmaßnahmen

| Maßnahme | Status |
|---|---|
| Spring Security | umgesetzt |
| Authentifizierung für geschützte Routen | umgesetzt |
| Öffentliche Route-Begrenzung | umgesetzt |
| Login und Registrierung | umgesetzt |
| BCrypt für Passwörter | umgesetzt |
| Passwort-Policy | umgesetzt |
| Registrierungscode mit Ablaufzeit | umgesetzt |
| Versuchszählung bei Codes | umgesetzt |
| Account-Status PENDING, VERIFIED, BLOCKED | umgesetzt |
| Technische Rollen ADMIN und SIGNED_IN_USER | umgesetzt |
| Method Security | aktiviert |
| Session-Invalidierung bei Logout | umgesetzt |
| Cookie-Löschung bei Logout | umgesetzt |
| Remember-Me mit 7 Tagen | umgesetzt |
| Chat-Zugriffskontrolle nach Buchungsbeziehung | umgesetzt |
| Öffentliche Profil-DTOs mit reduzierten Feldern | umgesetzt |
| CI-Build mit Tests | umgesetzt |

## Rollenmodell

Technisch gibt es die Rollen:

- `ADMIN`
- `SIGNED_IN_USER`

Fachlich können angemeldete Nutzer sowohl Tierhalter als auch Sitter sein. Diese Unterscheidung ist im Projekt eher durch fachliche Aktionen und Datenbeziehungen abgebildet, nicht als starres separates Rollenmodell. Ein Nutzer kann also je nach Kontext Haustiere besitzen, Angebote erstellen oder Betreuungsleistungen anbieten.

Für eine produktive Version sollte geprüft werden, ob zusätzliche Rollen sinnvoll sind:

- `OWNER`
- `SITTER`
- `ADMIN`
- `SUPPORT`

## Shift Security Left

Shift Security Left bedeutet, Sicherheitsaspekte möglichst früh im Entwicklungsprozess zu berücksichtigen. Security wird nicht erst am Ende geprüft, sondern bereits bei Anforderungen, Architektur, Implementierung, Tests und CI einbezogen.

## Anwendung auf das Projekt

| Phase | Umsetzung im Projekt |
|---|---|
| Anforderungen | sensible Daten und Risiken wurden identifiziert |
| Architektur | Authentifizierung, Schichtenarchitektur und Service-Prüfungen wurden eingeplant |
| Implementierung | BCrypt, Passwortregeln, Accountstatus, Code-Validierung und Chat-Zugriffskontrolle wurden umgesetzt |
| Testing | Unit-Tests prüfen unter anderem Passwortregeln, Registrierung, Login und Edge Cases |
| CI | GitHub Actions führt Build und Tests automatisch aus |
| Review | Trello-Review-Spalte und Review-Checklisten unterstützen fachliche Prüfung |

## Grenzen des aktuellen Shift-Security-Left-Ansatzes

Aktuell werden Build und Tests automatisiert ausgeführt. Es fehlen noch automatisierte Security-Checks wie:

- Dependency Vulnerability Scan,
- Secret Scanning,
- SAST,
- Testabdeckung für alle Zugriffsschutzregeln,
- DAST gegen eine laufende Testinstanz.

## Empfohlene nächste Sicherheitsmaßnahmen

| Priorität | Maßnahme |
|---|---|
| hoch | Demo-User und Dev-Fallbacks für produktionsnahe Umgebungen deaktivieren |
| hoch | HTTPS und sichere Cookie-Konfiguration verpflichtend machen |
| hoch | Zugriffstests für Chat, Buchungen, Angebote und Profile ergänzen |
| hoch | Secrets aus Code und Konfigurationsdateien entfernen |
| mittel | Dependency-Check in CI ergänzen |
| mittel | Secret Scanning in CI ergänzen |
| mittel | Audit-Logging für Login, Logout und verweigerte Zugriffe |
| mittel | Rate-Limiting auf IP- und Account-Ebene |
| mittel | vollständiges Rollen- und Rechtekonzept für Admin-Funktionen |
| niedrig | Monitoring und Alerting |
| niedrig | Datenschutzprozesse für Export und Löschung |

## Fazit

Das Projekt enthält bereits mehrere sinnvolle Sicherheitsmaßnahmen. Besonders relevant sind Spring Security, BCrypt, Passwortregeln, Registrierungscodes, Session-Logout, Remember-Me-Begrenzung und die Zugriffskontrolle im Chat. Für ein reales Produkt müssten vor allem Deployment-Sicherheit, Secrets, HTTPS, Audit-Logging, Rate-Limiting, Security-Tests und automatisierte Security-Checks erweitert werden.
