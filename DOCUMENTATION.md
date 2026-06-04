# Projektdokumentation und Zusammenarbeit

## Projekt

Pawsitters ist eine Java-basierte Webanwendung zur Vermittlung von Tierbetreuung. Die Anwendung verbindet Tierhalter, die Betreuung für ihre Haustiere suchen, mit Personen, die Tiere betreuen möchten.

Das Projekt wurde als Gruppenprojekt im Modul Software Engineering II umgesetzt. Neben der funktionalen Anwendung standen Teamarbeit, Architektur, Security, Testing, CI, Dokumentation und Reflexion des Entwicklungsprozesses im Fokus.

## Team

| Person          | Schwerpunkt                                                                |
| --------------- | -------------------------------------------------------------------------- |
| Kim Reger       | UI, Layout, visuelle Gestaltung, Vaadin-Views, Nutzerführung               |
| Luis Schirmbeck | Backend, Geschäftslogik, Security, Tests, CI, Dokumentation, Integration   |
| Josef Lautner   | Backend, Geschäftslogik, Datenmodell, Security, Tests, Docker, Integration |

Die Aufgaben wurden nicht strikt getrennt. Die Schwerpunkte halfen bei Orientierung und Verantwortung, aber viele Schnittstellen wurden gemeinsam abgestimmt. Besonders bei UI-Backend-Integration, Security, Demo-Flows und Fehlerbehebung gab es Überschneidungen.

## Zielsetzung

Ziel war eine funktionale Plattform, über die Benutzer:

- sich registrieren und anmelden,
- ein Profil pflegen,
- Haustiere verwalten,
- Betreuungsangebote erstellen und suchen,
- Anfragen stellen,
- Buchungen abschließen und verwalten,
- im Kontext von Anfragen und Buchungen chatten,
- Bewertungen nach abgeschlossenen Buchungen abgeben,
- Favoriten speichern,
- Demo-Guthaben und Treuhandzahlungen im Wallet nachvollziehen können.

## Technologiestack

| Bereich                | Technologie                                                      |
| ---------------------- | ---------------------------------------------------------------- |
| Sprache                | Java 21                                                          |
| Backend                | Spring Boot                                                      |
| UI                     | Vaadin (ergänzt durch css)                                       |
| Build                  | Maven                                                            |
| Relationale Persistenz | MySQL                                                            |
| Chat-Persistenz        | MongoDB                                                          |
| Migrationen            | Flyway                                                           |
| Tests                  | JUnit, Spring Boot Test, Spring Security Test, Mockito/Fakes, H2 |
| Container              | Docker Compose                                                   |
| CI                     | GitHub Actions                                                   |

## Zusammenarbeit im Team

### Persönliche Planung

Gerade am Anfang hat sich das Team persönlich zusammengesetzt, um das Projekt zu planen. In dieser Phase wurden die Grundidee, Kernfunktionen und wichtigsten Objekte besprochen:

- User,
- Pet,
- Offer,
- OfferRequest,
- Booking,
- Chat,
- Security,
- Demo-Ablauf.

Diese frühen Treffen waren wichtig, weil dadurch ein gemeinsames Verständnis für Ziel, Umfang und Prioritäten entstanden ist. Außerdem wurde besprochen, welche Teile für die Abgabe zwingend nötig sind und welche Features als Erweiterung umgesetzt oder dokumentiert werden können.

### Iterativer Entwicklungsansatz

Die Arbeit erfolgte iterativ. Das Team hat Aufgaben nicht vollständig im Voraus festgelegt, sondern regelmäßig angepasst, wenn neue Integrationsprobleme, Demo-Anforderungen oder technische Abhängigkeiten sichtbar wurden.

Der typische Ablauf war:

1. Anforderungen und Ideen sammeln.
2. Aufgabe in Trello erfassen.
3. Umsetzung in UI, Backend oder Dokumentation.
4. Schnittstellen zwischen Views, Services und DTOs abstimmen.
5. Ergebnis testen oder gemeinsam prüfen.
6. Aufgabe in Review oder Fertig verschieben.
7. Fehler oder Erweiterungen als neue Aufgaben aufnehmen.

Dieser Ansatz passte gut zum Projekt, weil UI und Backend parallel entstanden und sich Anforderungen während der Integration konkretisiert haben.

### Gelegentliches Pair Programming

Ergänzend zur individuellen Aufgabenbearbeitung wurde gelegentlich Pair Programming eingesetzt, insbesondere zwischen Luis und Josef. Dabei wurden komplexere Implementierungs- oder Integrationsaufgaben gemeinsam bearbeitet, um Probleme schneller zu identifizieren und Lösungsansätze direkt zu diskutieren. Dieses Vorgehen war besonders hilfreich, wenn technische Abhängigkeiten zwischen verschiedenen Komponenten bestanden oder Fehler im Zusammenspiel einzelner Funktionen nachvollzogen werden mussten. Durch das gemeinsame Arbeiten konnte außerdem Wissen im Team geteilt und ein einheitlicheres Verständnis der Codebasis aufgebaut werden.

## Trello

Das Team nutzte ein Trello-Board als Kanban-Board zur Organisation. Die Spalten waren:

| Spalte   | Bedeutung                                   |
| -------- | ------------------------------------------- |
| To Do    | geplante oder noch nicht begonnene Aufgaben |
| Im Gange | aktuell bearbeitete Aufgaben                |
| Review   | umgesetzte Aufgaben, die geprüft werden     |
| Fertig   | abgeschlossene Aufgaben                     |

https://trello.com/invite/b/69fdcb6ae3e23219aa1334e8/ATTI5f3a546b73406f627ff1511337d28e90C47F7F1C/pawsitter

Trello half dabei, Aufgaben sichtbar zu machen und den Projektfortschritt nachvollziehbar zu halten. Die Review-Spalte war besonders wichtig, weil Aufgaben nicht direkt nach der Umsetzung als abgeschlossen galten.

Für das Trello-Board wurden zusätzlich einige Automationen eingerichtet. Wenn eine Aufgabe in die Spalte Review verschoben wird, wird automatisch eine E-Mail an alle Teammitglieder verschickt. Außerdem wird der Karte eine Checkliste hinzugefügt, mit der die Reviews abgehakt und nachvollzogen werden können. Dadurch war klarer sichtbar, welche Aufgaben bereits geprüft wurden und bei welchen Karten noch Review-Schritte offen waren.

Beispiele für UI-Aufgaben:

- Logo und visuelle Gestaltung,
- Header und Footer,
- Landing Page,
- Login- und Registrierungsseiten,
- Profilseite,
- Angebots- und Auftragsseiten,
- Filter und Suchoberflächen,
- Chat-View,
- Detailansichten und Popups,
- Bild-Upload und UI-Verbesserungen.

Beispiele für Backend-Aufgaben:

- User Registration,
- Login und Passwortregeln,
- Haustierverwaltung,
- OfferRequest,
- Booking,
- Chat-System,
- Notification-System,
- Favoriten,
- Bewertungen,
- Wallet und Payment-nahe Demo-Logik,
- PLZ- und Distanzlogik,
- Docker-Compose-Setup,
- Tests.

Beispiele für Querschnittsaufgaben:

- Spring Security,
- CI-Pipeline,
- Datenbankmigrationen,
- Testdokumentation,
- Sicherheitskonzept,
- Architektur,
- KI-Nutzung,
- Präsentationsvorbereitung.

## Git und GitHub

Die Entwicklung erfolgte in einem gemeinsamen GitHub-Repository. Git wurde genutzt für:

- Versionskontrolle,
- regelmäßige Commits,
- Arbeit an unterschiedlichen Feature-Branches,
- Zusammenführung von UI- und Backend-Arbeit,
- Nachvollziehbarkeit der Entwicklung,
- Vorbereitung der Abgabe.

Bei größeren Änderungen wurden Branches oder getrennte Entwicklungsstände genutzt. Wichtig war vor allem, Änderungen regelmäßig zu integrieren und Konflikte zwischen UI, Services, Datenmodell und Migrationen früh zu erkennen.

## Abstimmung zwischen UI und Backend

Viele Funktionen mussten eng zwischen UI und Backend abgestimmt werden. Beispiele:

- Registrierung und Login: UI-Formulare, Validierung, Codes, Account-Status,
- Angebote: Formularfelder, Haustierauswahl, Betreuungsart, Zeitraum und Preisberechnung,
- Suche: Filter-UI, PLZ-Validierung, Distanzlogik und Ergebnisdarstellung,
- Anfragen: Request-Popups, Statuslogik und Chat-Start,
- Buchungen: Annahme, Storno, Abschluss, Zahlungsvorschau und Wallet,
- Chat: Konversationsliste, Nachrichten, Live-Events, Systemkarten und Notifications,
- Reviews: Bewertungsdialog, Rating-Summary und Anzeige in Profilen,
- Bilder: Upload, Crop, Varianten und öffentliche Darstellung.

Die Abstimmung erfolgte flexibel über Gespräche, Trello-Karten, Code-Änderungen und lokale Tests.

## Qualitätssicherung

Die Qualitätssicherung bestand aus mehreren Ebenen:

| Ebene         | Umsetzung                                                                         |
| ------------- | --------------------------------------------------------------------------------- |
| Architektur   | Monolith mit Schichtenarchitektur und fachlichen Packages                         |
| Services      | Geschäftslogik zentral im Service Layer                                           |
| Tests         | Unit-, Integration-, Smoke- und UI-nahe Tests                                     |
| CI            | Maven Verify über GitHub Actions                                                  |
| Review        | Trello-Review-Spalte und manuelle Prüfung                                         |
| Demo          | lokale Ausführung zentraler Nutzerflows                                           |
| Dokumentation | zentrale Markdown-Dateien für Architektur, Security, Tests, KI und Zusammenarbeit |

## CI

Die CI-Pipeline führt bei Pushes und Pull Requests einen Maven-Build mit Tests aus.

Der zentrale Befehl lautet:

```bash
./mvnw -B verify -Dspring.docker.compose.enabled=false
```

Docker Compose wird in der CI deaktiviert. Dadurch laufen die Tests unabhängig von lokalen MySQL- und MongoDB-Containern.

## Testing

Das Projekt enthält deutlich mehr als die geforderten 10 Unit-Tests. Getestet werden unter anderem:

- Passwortregeln,
- Registrierung und Login,
- Passwort-Reset,
- Profilaktualisierung,
- E-Mail-Änderung,
- Haustierlogik,
- Angebote und Suche,
- Anfragen,
- Buchungen,
- Wallet und wiederkehrende Zahlungen,
- Chat,
- Bewertungen,
- Bildverarbeitung,
- PLZ-Validierung und Distanz,
- Security-Integration,
- UI-nahe Komponenten.

Die detaillierte Testdokumentation steht in `TEST_DOCUMENTATION.md`.

## Dokumentation im Repository

Nach der Konsolidierung bleiben fünf Markdown-Dateien im Repository:

| Datei                   | Zweck                                                                        |
| ----------------------- | ---------------------------------------------------------------------------- |
| `ARCHITECTURE.md`       | Architektur, Module, Persistenz, Events, Grenzen                             |
| `SECURITY_CONCEPT.md`   | Schutzbedarf, Risiken, umgesetzte Maßnahmen, offene Security-Anforderungen   |
| `TEST_DOCUMENTATION.md` | Teststrategie, Testklassen, Einzeltests und Pflichtabdeckung                 |
| `KI_PROMPTS.md`         | KI-Nutzung, Prompt-Kategorien, rekonstruierte Prompts und Verantwortlichkeit |
| `DOCUMENTATION.md`      | Teamarbeit, Organisation, Git, Trello, CI, Reflexion                         |

## Erweiterungen über Mindestanforderungen hinaus

| Erweiterung              | Nutzen                                              |
| ------------------------ | --------------------------------------------------- |
| Chat-System              | Kommunikation zwischen beteiligten Nutzern          |
| MongoDB für Chat         | passende Persistenz für Nachrichtenverläufe         |
| Registrierung mit Code   | kontrollierter Account-Status                       |
| Passwort-Reset           | realistischere Account-Wiederherstellung            |
| Passwortregeln           | geringeres Risiko schwacher Passwörter              |
| Remember-Me              | bessere Demo- und Nutzererfahrung                   |
| Benachrichtigungen       | Rückmeldung bei Chat, Zahlung und Statusänderungen  |
| PLZ- und Distanzlogik    | bessere Suche und Matching                          |
| Bilder mit Varianten     | Profil- und Haustierbilder ohne externe Dateiablage |
| Bewertungen              | Vertrauen zwischen Nutzern                          |
| Wallet/Treuhandlogik     | Demo-nahes Zahlungsmodell                           |
| Wiederkehrende Buchungen | realistischere Betreuungsangebote                   |
| Docker Compose           | reproduzierbare lokale Infrastruktur                |
| Haussitting Option       | bei Buchung zusätzlich                              |
| Spring Security          | allgemeine Sicherheit                               |

## Bekannte Einschränkungen

Die Anwendung ist ein Uni-Projekt und kein produktionsreifes System. Für ein reales Produkt müssten weiter ausgebaut werden:

- Deployment mit HTTPS,
- produktiver Mailversand,
- Rate-Limiting und CAPTCHA,
- vollständige Rollen- und Rechteverwaltung,
- zentrale Audit-Logs,
- Monitoring und Alerting,
- Backup- und Restore-Konzept,
- Datenschutzkonzept inklusive Löschfristen und Auskunftsprozessen,
- automatisierte Security-Scans in CI,
- echte Browser-End-to-End-Tests,
- verteiltes Eventing für Chat in Multi-Instance-Betrieb,
- echte Payment-Provider-Integration.

## Reflexion

Die flexible Aufgabenverteilung war für das Projekt sinnvoll. UI und Backend hatten zu unterschiedlichen Zeiten unterschiedlich viel Aufwand. Kim konnte sich stark auf die Benutzeroberfläche und Gestaltung konzentrieren, während Luis und Josef viele Backend- und Querschnittsthemen übernahmen.

Trello half dabei, Aufgaben sichtbar zu halten und Fortschritt zu verfolgen. Git und GitHub machten Änderungen nachvollziehbar und ermöglichten parallele Arbeit. Die persönliche Abstimmung am Anfang war wichtig, weil dadurch ein gemeinsames fachliches Modell entstand.

Herausfordernd waren vor allem:

- die Verbindung von UI und Backend,
- die Integration von Security,
- die Kombination aus MySQL-Kerndaten und MongoDB-Chat,
- die vielen Statusübergänge bei Anfragen, Buchungen, Wallet und Reviews,
- die Demo-Tauglichkeit bei begrenzter Zeit.

Tests und CI halfen, Fehler früh zu erkennen. KI-Tools unterstützten Konzeptarbeit, Implementierungsideen, Debugging, Tests und Dokumentation. Die KI-Ergebnisse mussten aber geprüft, angepasst und in den konkreten Projektkontext integriert werden. Die Verantwortung für finalen Code, Architektur, Demo und Dokumentation blieb beim Team.
