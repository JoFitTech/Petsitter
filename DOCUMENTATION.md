# Projektdokumentation

## Projekt

Pawsitters ist eine Java-basierte Webanwendung zur Vermittlung von Tierbetreuung. Die Anwendung verbindet Tierhalter, die während einer Abwesenheit eine Betreuung suchen, mit Personen, die Tiere gegen Bezahlung betreuen möchten.

Die Anwendung wurde als Gruppenprojekt im Rahmen des Moduls Software Engineering II umgesetzt.

## Team

| Person | Schwerpunkt |
|---|---|
| Kim Reger | UI, Layout, visuelle Gestaltung, Vaadin-Views |
| Luis Schirmbeck | Backend, Geschäftslogik, Datenmodell, Security, Tests, CI, Integration |
| Josef Lautner | Backend, Geschäftslogik, Datenmodell, Security, Tests, Docker, Integration |

Die Aufgaben wurden nicht starr verteilt. Die Verteilung erfolgte flexibel nach Workload, Bedarf und aktuellem Projektstand. Kim übernahm im Wesentlichen die UI-Umsetzung. Josef und Luis arbeiteten schwerpunktmäßig am Backend und an technischen Querschnittsthemen.

## Zielsetzung

Ziel war eine funktionale Plattform, über die Benutzer:

- ein Profil erstellen,
- Haustiere registrieren,
- Angebote und Betreuungsanfragen erstellen,
- passende Angebote finden,
- Anfragen stellen,
- Buchungen abschließen,
- Chatnachrichten im Kontext einer Buchung austauschen können.

Neben der funktionalen Umsetzung standen Architektur, Testing, Dokumentation, Security-Verständnis, Teamarbeit und CI im Fokus.

## Technologiestack

| Bereich | Technologie |
|---|---|
| Sprache | Java 21 |
| Backend | Spring Boot |
| Build | Maven |
| UI | Vaadin |
| Relationale Persistenz | MySQL |
| Chat-Persistenz | MongoDB |
| Migrationen | Flyway |
| Tests | JUnit, Spring Boot Test, Spring Security Test, H2 |
| Container | Docker Compose |
| CI | GitHub Actions |

## Architektur

Die Anwendung ist als Monolith mit Schichtenarchitektur umgesetzt. Der Monolith wurde gewählt, weil das Projekt einen begrenzten Umfang hat, das Team klein ist und eine gemeinsame Codebasis für Entwicklung, Debugging und Demo einfacher beherrschbar ist als mehrere Services.

Die Schichten sind:

| Schicht | Aufgabe |
|---|---|
| UI Layer | Vaadin-Views, Benutzerinteraktion, Navigation |
| Service Layer | Geschäftslogik, Validierung, Transaktionen |
| Repository Layer | Datenbankzugriff über Spring Data |
| Domain Layer | Entities, Enums und fachliche Datenmodelle |
| Persistence Layer | MySQL, MongoDB, Flyway-Migrationen |

Geschäftslogik liegt primär in Services. Entities dienen vor allem als Datenmodell. Repositories werden als Query-Layer genutzt.

## Zentrale Fachbereiche

### Benutzer und Profile

Benutzer können sich registrieren, ein Profil pflegen und öffentliche Profildaten anzeigen lassen. Das Profil enthält unter anderem Name, Anzeigename, Kontakt- und Adressdaten sowie optionale Angaben wie Sprache, Nationalität und Beschreibung.

### Haustiere

Benutzer können Haustiere anlegen. Haustiere sind dem jeweiligen Owner zugeordnet und werden für betreuungsbezogene Angebote genutzt.

### Angebote und Anfragen

Die Plattform unterstützt Angebote und Anfragen für Betreuung. Nutzer können passende Angebote suchen, Anfragen erstellen und Statusänderungen durchführen.

Wichtige Geschäftsregeln sind:

- Ein Nutzer kann nicht sinnvoll auf eigene Angebote anfragen.
- Gebuchte Angebote werden gesperrt oder anders behandelt als offene Angebote.
- Bei Annahme einer Anfrage werden andere konkurrierende Anfragen abgelehnt.
- Buchungen entstehen aus angenommenen Anfragen.

### Buchungen

Eine Buchung entsteht, wenn eine Anfrage akzeptiert wird. Sie verbindet Owner, Sitter, Zeitraum, Preis und optional ein Haustier.

### Chat

Der Chat ist als Zusatzfunktion umgesetzt. Er ist an eine Buchung gebunden. Nur Owner und Sitter einer Buchung dürfen die zugehörige Konversation sehen und Nachrichten senden.

Für den Chat wird MongoDB verwendet. Das passt besser zu Chatverläufen, weil Nachrichten als Dokumente mit Zeitstempeln gespeichert werden und nicht stark relational modelliert werden müssen. App-Kerndaten bleiben in MySQL. Dazu zählen auch optionale Profil- und Haustierbilder: Nach einem runden Zuschnitt speichert MySQL ausschließlich optimierte `AVATAR`- und `DISPLAY`-Varianten. Offer-Cover werden aus diesen Bildern dynamisch zusammengesetzt. Beim Löschen eines Users oder Haustiers entfernt die Datenbank die zugehörigen Bildvarianten per Cascade.

### Benachrichtigungen

Benachrichtigungen werden für Ereignisse wie Chatnachrichten und Statusänderungen genutzt. Dadurch bekommen Nutzer Rückmeldung über relevante Änderungen.

## Entwicklungsprozess

Das Team nutzte ein Trello-Board als Kanban-Board. Die Spalten waren:

| Spalte | Bedeutung |
|---|---|
| To Do | geplante oder noch nicht begonnene Aufgaben |
| Im Gange | aktuell bearbeitete Aufgaben |
| Review | umgesetzte Aufgaben, die geprüft werden |
| Fertig | abgeschlossene Aufgaben |

Die Arbeit erfolgte iterativ. Karten wurden je nach Bedarf erstellt, verschoben, reviewed und abgeschlossen. Die JSON-Auswertung des Trello-Boards zeigt 88 Karten und die Spalten To Do, Im Gange, Review und Fertig. Die Aufgaben waren fachlich und technisch gemischt, unter anderem UI-Karten, Backend-Karten, Security-Karten, Chat-Karten und Dokumentationskarten.

Beispiele für UI-Aufgaben:

- Logo entwerfen
- Header bauen
- Footer bauen
- Login-Seite erstellen
- Landing Page erstellen
- Profilseite erstellen
- Auftragsseiten für Tierhalter und Tiersitter
- Chat-Forum
- Design-Verbesserungen an Detailansichten, Filteroptionen, Request-Popups und Hintergründen

Beispiele für Backend-Aufgaben:

- User Registration
- Haustier hinzufügen
- Offerrequest erstellen
- Booking erstellen
- Chat-System
- Notification System
- Password-Security
- Karten- und Distanzlogik
- Filtermöglichkeiten
- Offer-Cards mit Datenbank verbinden
- Favoriten- und Like-Logik
- Tests ergänzen
- Docker-Compose-Setup

## Git und Zusammenarbeit

Die Entwicklung erfolgte über ein gemeinsames GitHub-Repository. Änderungen wurden regelmäßig committet. Die Arbeit wurde über Branches und Trello-Karten organisiert. Die Zuordnung der Aufgaben ist über Trello-Karten, Commit-Historie und Repository-Struktur nachvollziehbar.

Die Abstimmung erfolgte flexibel. Bei technischen Schnittstellen zwischen UI und Backend wurden die betroffenen Teile gemeinsam abgestimmt, zum Beispiel bei Buttons, Views, Services und Datenflüssen.

## CI

Eine GitHub-Actions-Pipeline führt bei Pushes auf main oder master sowie bei Pull Requests automatisch einen Maven-Build mit Tests aus.

Der relevante CI-Schritt ist:

```bash
./mvnw -B verify -Dspring.docker.compose.enabled=false
```

Dadurch wird geprüft, ob das Projekt baut und die Tests ohne lokal gestartete Docker-Compose-Services laufen.

## Testing

Das Projekt enthält Unit- und Integrationstests. Getestet werden unter anderem:

- Passwortregeln,
- Registrierung,
- Login,
- Profilaktualisierung,
- E-Mail-Änderung,
- Registrierungsbestätigung,
- Chat-Service,
- Chat-Event-Bus,
- UI-nahe Chat-Integration.

Die Tests decken normale Fälle und Edge Cases ab, zum Beispiel ungültige Passwörter, doppelte E-Mail-Adressen, ungültige Codes, fehlende Authentifizierung und ungültige Postleitzahlen.

## Dokumentation im Repository

Neben dieser Datei gibt es weitere technische Dokumente im Repository, unter anderem:

| Datei | Zweck |
|---|---|
| README.md | Einstieg, lokaler Start, Projektstruktur |
| BACKEND_OVERVIEW.md | Backend-Architektur und Fachmodell |
| CHAT_MODULE_SUMMARY.md | Chat-Architektur und Implementierungsstand |
| SPRING_SECURITY_FINAL_DOC.md | Security- und Login-Konzept der Implementierung |
| SECURITY_AUDIT_FIXES.md | dokumentierte Security-Fixes |
| KI_PROMPTS.md | KI-Prompts und KI-Nutzung |
| SECURITY_CONCEPT.md | Sicherheitskonzept |
| AI_USAGE_DECLARATION.md | KI-Nutzungserklärung |

## Erweiterungen über Mindestanforderungen hinaus

Das Projekt enthält mehrere Erweiterungen gegenüber den Mindestanforderungen:

| Erweiterung | Nutzen |
|---|---|
| Chat-System | Kommunikation zwischen Owner und Sitter nach Buchung |
| MongoDB für Chat | geeignete Persistenz für Nachrichtenverläufe |
| Login- und Registrierungsflow | bessere Nutzbarkeit und Security-Basis |
| Passwortregeln | geringeres Risiko schwacher Passwörter |
| Remember-Me | bessere Demo- und Nutzererfahrung |
| Benachrichtigungen | Rückmeldung bei relevanten Ereignissen |
| Distanz- und PLZ-Logik | bessere Suche und Matching |
| Docker Compose | reproduzierbare lokale Infrastruktur |
| CI-Pipeline | automatische Build- und Testprüfung |

## Bekannte Einschränkungen

Die Anwendung ist ein Uni-Projekt und kein produktionsreifes System. Folgende Punkte müssten für ein reales Produkt weiter ausgebaut werden:

- Deployment mit HTTPS,
- produktiver Mailversand über SMTP,
- vollständige Rollen- und Rechteverwaltung,
- Rate-Limiting auf Infrastruktur-Ebene,
- zentrale Audit-Logs,
- Monitoring und Alerting,
- Backup-Konzept für MySQL und MongoDB,
- Datenschutzkonzept inklusive Löschfristen und Auskunftsprozessen,
- automatisierte Security-Scans in der CI,
- ausführlichere End-to-End-Tests.

## Reflexion

Die flexible Aufgabenverteilung war für das Projekt sinnvoll, weil UI und Backend unterschiedlich viel Aufwand zu verschiedenen Zeitpunkten erzeugten. Trello half dabei, Aufgaben sichtbar zu machen und Fortschritt zu verfolgen. Die Kombination aus Monolith, Vaadin und Spring Boot reduzierte Integrationsaufwand und ermöglichte eine vollständige Demo innerhalb des Projektumfangs.

Herausfordernd waren vor allem die Integration zwischen UI und Backend, die Security-Anpassungen sowie die saubere Persistenz für Chat und Kerndaten. Durch Reviews, Tests und gemeinsame Abstimmung wurden größere Inkonsistenzen reduziert.

Die Nutzung von KI-Tools beschleunigte Konzeptarbeit, Implementierungsideen und Fehlersuche. Die Ergebnisse mussten aber geprüft, angepasst und in den bestehenden Code integriert werden. Das Verständnis des finalen Codes bleibt Aufgabe des Teams.
