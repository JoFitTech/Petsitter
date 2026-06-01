# KI-Nutzungserklärung

## Zweck

Dieses Dokument beschreibt den Einsatz von KI-Tools im Projekt Pawsitters. KI wurde als unterstützendes Werkzeug genutzt. Die fachliche Entscheidung, Prüfung, Anpassung und Integration der Ergebnisse lagen beim Team.

## Verwendete KI-Tools

| Tool | Einsatzbereich |
|---|---|
| ChatGPT | Konzeptarbeit, Strukturierung, Präsentationsvorbereitung, Dokumentationsentwürfe, technische Erklärungen |
| Claude | Implementierungsunterstützung, Codeideen, Refactoring-Vorschläge, Fehlersuche |
| Codex | Implementierungsunterstützung, Codegenerierung, Tests, technische Anpassungen |

Die genaue Modellversion wurde nicht in jedem Fall separat protokolliert. Wenn Modellversionen in der jeweiligen Tool-Oberfläche sichtbar waren, wurden sie situativ genutzt. Für die Abgabe wird daher nicht behauptet, dass für jede einzelne KI-Interaktion eine exakt rekonstruierbare Modellversion vorliegt.

## Nutzung nach Teammitgliedern

| Person | Nutzung |
|---|---|
| Kim Reger | Unterstützung bei UI-Ideen, Layoutformulierungen, Strukturierung von Views und Designentscheidungen |
| Luis Schirmbeck | Unterstützung bei Konzept, Backend-Implementierung, CI, Security, Testing, Dokumentation und Präsentation |
| Josef Lautner | Unterstützung bei Backend-Implementierung, Security, Tests, Docker, Fehleranalyse und technischer Dokumentation |

## Aufgabenbereiche mit KI-Unterstützung

KI wurde projektbegleitend genutzt, insbesondere für:

- Konzeptideen und Strukturierung,
- Architekturüberlegungen,
- Formulierung von Anforderungen,
- UI- und UX-Ideen,
- Backend-Implementierungsideen,
- Security-Konzept und Security-Reflexion,
- Testideen und Edge Cases,
- CI-Konfiguration,
- Debugging,
- Dokumentationsentwürfe,
- Präsentationsstruktur und Präsentationstext.

KI wurde nicht als Ersatz für Projektverständnis verwendet. Der finale Code musste vom Team verstanden, angepasst und in der Demo erklärbar sein.

## Prüfung der KI-Ergebnisse

KI-Ergebnisse wurden nicht ungeprüft als verbindlich übernommen. Die Prüfung erfolgte durch:

| Prüfschritt | Beschreibung |
|---|---|
| Manuelles Lesen | Code und Texte wurden fachlich geprüft |
| Anpassung an Projektkontext | Vorschläge wurden an bestehende Packages, Klassen und Services angepasst |
| Ausführen der Anwendung | Funktionen wurden lokal getestet |
| Tests | Unit- und Integrationstests wurden ausgeführt |
| CI | GitHub Actions prüft Build und Tests automatisch |
| Teamabstimmung | Kritische Entscheidungen wurden im Team besprochen |
| Demo-Prüfung | zentrale Flows wurden auf Demo-Tauglichkeit geprüft |

## Anpassungen an KI-Ergebnissen

KI-Vorschläge mussten regelmäßig angepasst werden, weil:

- Paketstruktur und Klassennamen projektspezifisch waren,
- bestehende Vaadin-Views berücksichtigt werden mussten,
- vorhandene Datenbankmigrationen nicht überschrieben werden durften,
- Security-Logik mit bestehendem Login und Registrierung zusammenspielen musste,
- Testdaten und Demo-Flows zum Projekt passen mussten,
- manche Vorschläge zu allgemein oder zu komplex für den Projektumfang waren.

## Eigenständige Leistungen des Teams

Eigenständig durch das Team entstanden insbesondere:

- fachliche Entscheidungen über Zielumfang und Demo-Flow,
- finale Architekturentscheidung für den Monolithen,
- Auswahl und Integration des Techstacks,
- Verbindung von UI und Backend,
- Anpassung und Integration der Services,
- Review und Korrektur von KI-Vorschlägen,
- lokale Tests und Fehlerbehebung,
- Trello-Organisation,
- Git-Arbeit und Merge-Entscheidungen,
- Verständnis und Präsentationsfähigkeit des finalen Codes.

KI hat die Arbeit unterstützt, aber nicht die Verantwortung für die Lösung übernommen.

## Einfluss von KI auf Softwareentwicklung

KI kann Softwareentwicklung beschleunigen, weil sie schnell Vorschläge für Code, Struktur, Tests und Dokumentation liefert. Besonders hilfreich war KI bei wiederkehrenden Aufgaben, bei der Suche nach möglichen Edge Cases und bei der Formulierung technischer Texte.

Gleichzeitig erzeugt KI Risiken:

| Risiko | Umgang im Projekt |
|---|---|
| Falscher Code | Vorschläge wurden geprüft, angepasst und getestet |
| Nicht passende Architektur | Vorschläge wurden an die Monolith-Struktur angepasst |
| Sicherheitslücken | Security-Vorschläge wurden kritisch geprüft und dokumentiert |
| Halluzinierte APIs | Code musste im Projekt kompilieren und laufen |
| Unverständlicher Code | Code musste vom Team nachvollzogen und erklärbar sein |
| Zu komplexe Lösungen | Vorschläge wurden auf Projektumfang reduziert |

## Einfluss von KI auf Testing

KI half bei der Identifikation möglicher Testfälle und Edge Cases. Dazu gehören zum Beispiel ungültige Passwörter, doppelte E-Mail-Adressen, ungültige Codes, fehlende Authentifizierung und ungültige Postleitzahlen.

Die Tests wurden nicht allein dadurch korrekt, dass KI sie vorgeschlagen hat. Entscheidend war, ob sie im Projekt ausführbar waren und reale Geschäftsregeln prüfen.

## Einfluss von KI auf Security

KI half dabei, Risiken systematisch zu sammeln und Maßnahmen zu strukturieren. Dazu gehören Datenschutz, Zugriffsschutz, Passwortspeicherung, Session-Handling, Demo-Konfiguration und Shift Security Left.

Security-Aussagen wurden am implementierten Stand gespiegelt. Nicht umgesetzte Maßnahmen werden als zukünftige Maßnahmen beschrieben und nicht als bereits vorhanden dargestellt.

## Grenzen der Dokumentation

Nicht alle einzelnen KI-Interaktionen wurden während der Entwicklung vollständig protokolliert. Die Datei `KI_PROMPTS.md` enthält daher neben konkreten Prompts auch sinngemäß rekonstruierte Prompt-Kategorien. Diese Rekonstruktion basiert auf den tatsächlich genutzten Aufgabenbereichen und dient der transparenten Nachvollziehbarkeit.

## Zusammenfassung

KI wurde breit als unterstützendes Entwicklungswerkzeug genutzt. Der finale Stand des Projekts entstand durch Auswahl, Anpassung, Integration, Tests und Reviews durch das Team. Die Verantwortung für Code, Architektur, Security-Verständnis und Präsentation liegt beim Team.
