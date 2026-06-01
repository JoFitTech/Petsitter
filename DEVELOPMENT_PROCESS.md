# Entwicklungsprozess und Zusammenarbeit

## Ziel

Dieses Dokument beschreibt die Organisation der Zusammenarbeit im Projekt Pawsitters. Es dokumentiert Teamaufteilung, Entwicklungsansatz, Trello-Nutzung, Git-Nutzung, CI und Reflexion des Projektverlaufs.

## Team

| Person | Schwerpunkt |
|---|---|
| Kim Reger | UI, Vaadin-Views, Layout, visuelle Gestaltung, Nutzerführung |
| Luis Schirmbeck | Backend, Services, Security, Tests, CI, Dokumentation, Integration |
| Josef Lautner | Backend, Services, Security, Tests, Docker, Datenmodell, Integration |

Die Aufteilung war flexibel. Aufgaben wurden nicht dauerhaft starr verteilt, sondern nach Workload, Bedarf und aktuellem Projektstand übernommen. Kim arbeitete im Wesentlichen an der Benutzeroberfläche. Luis und Josef arbeiteten schwerpunktmäßig am Backend und an technischen Querschnittsthemen.

## Entwicklungsansatz

Das Team nutzte einen Kanban-orientierten Ansatz mit Trello.

### Trello-Board

Das Trello-Board hieß `Pawsitter`. Es enthielt folgende Spalten:

| Spalte | Bedeutung |
|---|---|
| To Do | geplante Aufgaben |
| Im Gange | aktuell bearbeitete Aufgaben |
| Review | umgesetzte Aufgaben zur Prüfung |
| Fertig | abgeschlossene Aufgaben |

Die Trello-JSON zeigt 88 Karten und regelmäßige Bewegungen zwischen den Spalten. Zusätzlich wurden Review-Checklisten genutzt. Dadurch war erkennbar, welche Aufgaben noch offen, in Bearbeitung, im Review oder abgeschlossen waren.

## Aufgabenarten

Die Aufgaben wurden in funktionale, technische und dokumentationsbezogene Aufgaben gegliedert.

### UI-Aufgaben

Beispiele:

- Logo entwerfen,
- Header bauen,
- Footer bauen,
- Landing Page erstellen,
- Login-Seite erstellen,
- Registrierungsseite erstellen,
- Profilseite erstellen,
- Chat-Forum,
- UI für Aufträge und Angebote,
- Design-Verbesserungen,
- Filteroptionen,
- Request-Popups,
- responsive oder adaptive UI-Anpassungen.

### Backend-Aufgaben

Beispiele:

- User Registration,
- Haustier hinzufügen,
- OfferRequest erstellen,
- Booking-Funktionalität,
- Chat-System,
- Notification-System,
- Password-Security,
- Karten- und Distanzlogik,
- Offer-Cards mit Datenbank verbinden,
- Favoritenlogik,
- Bewertungslogik,
- Wallet- oder Payment-nahe Funktionen,
- Docker-Compose-Setup,
- Tests.

### Querschnittsaufgaben

Beispiele:

- Spring Security,
- CI-Pipeline,
- Datenbankmigrationen,
- Dokumentation,
- Testdokumentation,
- Security-Konzept,
- KI-Nutzungserklärung,
- Präsentationsvorbereitung.

## Zusammenarbeit

Die Zusammenarbeit erfolgte iterativ:

1. Anforderungen und Ideen wurden gesammelt.
2. Aufgaben wurden als Trello-Karten formuliert.
3. Karten wurden je nach Priorität und Verfügbarkeit bearbeitet.
4. UI und Backend wurden über konkrete Views, Services und Datenflüsse integriert.
5. Umgesetzte Aufgaben wurden geprüft.
6. Fertige Aufgaben wurden nach `Fertig` verschoben.
7. Fehler und Verbesserungen wurden als neue Karten oder Anpassungen aufgenommen.

Die Abstimmung erfolgte besonders dort, wo UI und Backend direkt aufeinander angewiesen waren. Beispiele sind Registrierung, Login, Angebotskarten, Anfragen, Buchungen und Chat.

## Git-Nutzung

Das Projekt wurde in einem gemeinsamen GitHub-Repository entwickelt. Git wurde genutzt für:

- Versionskontrolle,
- Branch-basierte Entwicklung,
- regelmäßige Commits,
- Nachvollziehbarkeit der Änderungen,
- Zusammenführung von UI- und Backend-Arbeit,
- Vorbereitung der Abgabe.

Die Beiträge der Teammitglieder sind über GitHub, Commit-Historie und Trello-Aktivitäten nachvollziehbar.

## Branching und Integration

Die Arbeit erfolgte mit mehreren Entwicklungsständen und Feature-orientierten Änderungen. Bei größeren Änderungen wurden Branches genutzt. Änderungen wurden anschließend in den Hauptstand integriert.

Wichtige Integrationspunkte waren:

- UI-Views an Backend-Services anbinden,
- Datenmodell und Views synchron halten,
- Security mit Login und geschützten Routen abstimmen,
- Chat mit Booking und Notifications verbinden,
- Tests an geänderte Geschäftslogik anpassen.

## CI-Pipeline

Die CI-Pipeline liegt unter `.github/workflows/ci.yml` und läuft bei:

- Pushes auf `main` oder `master`,
- Pull Requests.

Die Pipeline:

1. checkt das Repository aus,
2. richtet JDK 21 ein,
3. nutzt Maven-Cache,
4. führt Maven Verify aus.

Der zentrale Befehl lautet:

```bash
./mvnw -B verify -Dspring.docker.compose.enabled=false
```

Docker Compose wird in der CI deaktiviert. Dadurch muss die CI keine lokalen MySQL- oder MongoDB-Container starten.

## Qualitätssicherung

Die Qualitätssicherung bestand aus mehreren Ebenen:

| Ebene | Umsetzung |
|---|---|
| Code-Struktur | Schichtenarchitektur, Packages nach Fachbereichen |
| Tests | Unit-Tests, Integrationstests, UI-nahe Tests |
| CI | automatischer Build und Testlauf |
| Review | Trello-Review-Spalte und Review-Checklisten |
| Manuelle Prüfung | lokale Ausführung und Demo-Flows |
| Dokumentation | Markdown-Dateien für Architektur, Tests, Security, Prozess und KI |

## Umgang mit KI

KI wurde als unterstützendes Werkzeug genutzt. Eingesetzt wurden ChatGPT, Claude und Codex. Die Nutzung umfasste Konzeptarbeit, Implementierungsideen, Debugging, Tests, Security, Dokumentation und Präsentationsvorbereitung.

Die Ergebnisse wurden nicht als automatisch korrekt betrachtet. Sie wurden geprüft, angepasst, integriert und getestet. Die genaue Dokumentation steht in:

- `AI_USAGE_DECLARATION.md`,
- `KI_PROMPTS.md`.

## Projektverlauf

### Anfangsphase

Zu Beginn wurden Anforderungen verstanden, erste Ideen gesammelt und ein grobes Konzept entwickelt. Dabei ging es vor allem um die Kernobjekte User, Pet, Offer, Request und Booking sowie um die Frage, wie die Plattform in der Demo gezeigt werden kann.

### Aufbauphase

Danach wurden Backend-Struktur, Datenmodell, Repositories, Services und erste UI-Strukturen aufgebaut. Parallel entstand das Trello-Board als zentrales Arbeitsmittel.

### Integrationsphase

In der Integrationsphase wurden UI und Backend verbunden. Dabei entstanden mehrere Anpassungen an Services, Views und DTOs. Besonders relevant waren Registrierung, Login, Angebotslogik, Anfragen, Buchungen und Chat.

### Stabilisierungsphase

Später lag der Fokus auf Tests, Security, CI, Dokumentation, Demo-Tauglichkeit und kleineren UI-Verbesserungen.

## Herausforderungen

| Herausforderung | Umgang damit |
|---|---|
| UI und Backend parallel entwickeln | Abstimmung über konkrete Views, Services und DTOs |
| Security nachträglich integrieren | Spring Security, Login, Rollen, Passwortregeln und Zugriffsschutz ergänzt |
| Chat mit Booking verknüpfen | Chat nur im Kontext einer Buchung erlaubt |
| Datenhaltung aufteilen | MySQL für Kerndaten, MongoDB für Chat |
| Demo-Fähigkeit sicherstellen | Fokus auf funktionale Kernflows |
| Umfang kontrollieren | Bonusfeatures ausgewählt, aber nicht jedes mögliche Feature umgesetzt |

## Beiträge der Teammitglieder

### Kim Reger

Schwerpunkt:

- UI-Design,
- Vaadin-Views,
- Layout und visuelle Gestaltung,
- Nutzerführung,
- Design-Verbesserungen,
- UI-nahe Anpassungen für Demo-Flows.

### Luis Schirmbeck

Schwerpunkt:

- Backend-Services,
- Security,
- CI,
- Tests,
- Dokumentation,
- Integrationsarbeit,
- Chat- und Notification-nahe Aufgaben,
- Abstimmung von Datenflüssen zwischen UI und Backend.

### Josef Lautner

Schwerpunkt:

- Backend-Services,
- Datenmodell,
- Security,
- Docker und Infrastruktur,
- Tests,
- technische Integration,
- Geschäftslogik für Angebote, Anfragen, Buchungen und verwandte Bereiche.

Die Abgrenzung ist nicht absolut. Mehrere Aufgaben wurden gemeinsam abgestimmt oder überlappend bearbeitet.

## Reflexion

Der flexible Kanban-Ansatz war für das Projekt geeignet. Die Aufgabenlage änderte sich im Verlauf des Projekts, besonders durch Integration, Security und Demo-Vorbereitung. Eine starre Sprintplanung hätte weniger gut zur wechselnden Arbeitslast gepasst.

Trello half dabei, Aufgaben sichtbar zu machen und den Fortschritt nachvollziehbar zu halten. Die Review-Spalte war hilfreich, weil fertige Aufgaben nicht direkt als abgeschlossen behandelt wurden.

Die Monolith-Architektur war für das Team sinnvoll, weil sie den Integrationsaufwand reduzierte. Die Entscheidung für Vaadin ermöglichte eine Java-basierte UI ohne separates Frontend-Projekt. Die Kombination aus MySQL und MongoDB war für die unterschiedlichen Datenarten nachvollziehbar: relationale Kerndaten und dokumentenartige Chatnachrichten.

Die größte Herausforderung war die Verbindung der vielen Teilbereiche zu einer konsistenten Anwendung. Das betraf insbesondere Login, Registrierung, Angebote, Buchungen und Chat. Tests und CI halfen, Fehler früher sichtbar zu machen.

## Verbesserungsmöglichkeiten

Für eine weitere Iteration wären sinnvoll:

- klarere Branch-Konventionen,
- Pull-Request-Reviews mit festen Review-Kriterien,
- automatische Testabdeckungsauswertung,
- automatisierte Security-Scans,
- mehr echte End-to-End-Tests,
- strukturierteres Protokollieren von KI-Prompts während der Entwicklung,
- detailliertere Definition von Rollen und Berechtigungen.

## Fazit

Die Zusammenarbeit war flexibel, aber durch Trello, Git und CI nachvollziehbar. Die Beiträge aller Teammitglieder sind über Aufgaben, Repository und Projektartefakte erkennbar. Der gewählte Prozess passte zum Umfang eines Uni-Projekts und ermöglichte die Umsetzung einer funktionsfähigen Anwendung mit zusätzlichen Features wie Chat, Security-Basis, Docker und CI.
