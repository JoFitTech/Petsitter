# KI_PROMPTS.md

## Zweck

Diese Datei dokumentiert die KI-Nutzung im Projekt Pawsitters. Nicht alle ursprünglichen Prompts wurden während der Entwicklung vollständig gespeichert. Die folgenden Prompts enthalten daher eine Mischung aus tatsächlich genutzten Prompt-Typen und sinngemäß rekonstruierten Prompts. Sie bilden die realen Einsatzbereiche der KI ab.

## Verwendete Tools

| Tool | Zweck |
|---|---|
| ChatGPT | Konzept, Architektur, Präsentation, Dokumentation, technische Erklärungen |
| Claude | Implementierungsideen, Refactoring, Debugging |
| Codex | Implementierungsunterstützung, Code, Tests, Anpassungen im Repository |

## Qualitätsregeln

KI-Ergebnisse wurden nach folgenden Regeln behandelt:

- Keine ungeprüfte Übernahme von Code.
- Code muss zur bestehenden Projektstruktur passen.
- Code muss kompilieren.
- Tests müssen laufen.
- Security-Aussagen müssen zum implementierten Stand passen.
- Nicht umgesetzte Maßnahmen werden als zukünftige Maßnahmen dokumentiert.
- Teammitglieder müssen finalen Code erklären können.

## Prompt-Kategorien

### 1. Konzept und Projektstruktur

| Zweck | Prompt |
|---|---|
| Grundkonzept strukturieren | Hilf uns, das Konzept für eine Pet-Holiday-Plattform zu strukturieren. Die Plattform soll Tierhalter und Tiersitter verbinden. Berücksichtige Profile, Haustiere, Angebote, Anfragen und Buchungen. |
| Stakeholder identifizieren | Welche Stakeholder gibt es bei einer Plattform für Tierbetreuung im Uni-Projekt? Berücksichtige Tierhalter, Tiersitter, Auftraggeberin und Entwicklerteam. |
| Präsentationsstruktur planen | Erstelle eine sinnvolle Präsentationsstruktur für ein 25-minütiges Software-Engineering-Projekt mit Konzept, Architektur, Demo, Security, CI, Tests und KI-Nutzung. |

### 2. Architektur

| Zweck | Prompt |
|---|---|
| Architekturentscheidung | Vergleiche Monolith und Microservices für ein kleines Java-Uni-Projekt mit drei Personen. Begründe, warum ein Monolith mit Schichtenarchitektur sinnvoll sein kann. |
| Schichtenarchitektur | Beschreibe eine Schichtenarchitektur für Spring Boot mit Vaadin, Services, Repositories, Domain-Entities und Datenbank. |
| Chat-Architektur | Beschreibe, wie ein Chat-Modul in einer Petsitter-Plattform aufgebaut werden kann. Berücksichtige Buchungsbezug, Zugriffskontrolle und Persistenz. |

### 3. Backend-Implementierung

| Zweck | Prompt |
|---|---|
| Entity-Modell | Erstelle ein mögliches Domain-Modell für User, Pet, Offer, OfferRequest, Booking und Notification. |
| Geschäftsregeln | Welche Geschäftsregeln sollte ein Offer- und Booking-Service enthalten, damit Anfragen akzeptiert und konkurrierende Anfragen abgelehnt werden? |
| Service-Struktur | Hilf bei der Strukturierung von Spring Services für User, Pet, Offer, Request, Booking und Notification. |
| Fehleranalyse | Analysiere folgenden Fehler aus einer Spring-Boot-Anwendung und schlage konkrete Debugging-Schritte vor. |
| Refactoring | Schlage vor, wie diese Service-Methode lesbarer und testbarer gemacht werden kann, ohne die Fachlogik zu ändern. |

### 4. UI und Vaadin

| Zweck | Prompt |
|---|---|
| Layout-Ideen | Erstelle UI-Ideen für eine Vaadin-Landing-Page einer Petsitter-App mit freundlicher Farbpalette. |
| View-Struktur | Wie kann man Vaadin-Views für Landing Page, Login, Registrierung, Profil, Angebote und Chat strukturieren? |
| Formularvalidierung | Wie können Pflichtfelder, E-Mail, Passwortbestätigung und Mindestalter in einer Registrierungsseite sinnvoll validiert werden? |
| Demo-Flow | Welche UI-Flows eignen sich für eine Demo einer Petsitter-Plattform? |

### 5. Security

| Zweck | Prompt |
|---|---|
| Security-Konzept | Erstelle ein Sicherheitskonzept für eine Petsitter-Plattform. Berücksichtige sensible Daten, Risiken, umgesetzte Maßnahmen und zukünftige Maßnahmen. |
| Shift Security Left | Erkläre Shift Security Left und wende es auf ein Java-Spring-Boot-Uni-Projekt mit GitHub Actions, Tests und Security-Konzept an. |
| Passwortsicherheit | Welche Passwortregeln sind für eine Webanwendung sinnvoll und wie kann man sie in Java testen? |
| Zugriffskontrolle Chat | Wie prüft man, ob nur Owner und Sitter einer Buchung auf einen Chat zugreifen dürfen? |
| Demo-User bewerten | Welche Risiken entstehen, wenn ein Demo-User in einer Abgabe aktiv bleibt, und wie sollte das dokumentiert werden? |

### 6. Testing

| Zweck | Prompt |
|---|---|
| Testfälle sammeln | Schlage Unit-Tests für Registrierung, Login, Passwortregeln, Profilaktualisierung und Chatzugriff vor. |
| Edge Cases | Welche Edge Cases sollte man bei Registrierung, Login-Code, Passwortregeln und Postleitzahlenvalidierung testen? |
| Testdokumentation | Erstelle eine Struktur für eine TEST_DOCUMENTATION.md, in der pro Test Zweck, Erwartung und Normalfall oder Edge Case dokumentiert werden. |
| Integrationstest-Ideen | Welche Integrationstests sind für eine Vaadin-Spring-Boot-App mit Security sinnvoll? |

### 7. CI und Build

| Zweck | Prompt |
|---|---|
| CI verbessern | Was kann man in eine GitHub-Actions-CI für ein Maven-Spring-Boot-Projekt ergänzen, wenn aktuell nur Tests und Build laufen? |
| Maven Verify | Erkläre den Unterschied zwischen `mvn test` und `mvn verify` für ein Maven-Projekt. |
| Docker Compose in CI | Wie kann man verhindern, dass Spring Boot Docker Compose in der CI startet? |

### 8. Dokumentation

| Zweck | Prompt |
|---|---|
| Projektdokumentation | Erstelle eine Projektdokumentation für ein Java-Spring-Boot-Projekt namens Pawsitters mit Vaadin, MySQL, MongoDB, Docker, CI, Tests und Trello-Kanban. |
| KI-Nutzungserklärung | Erstelle eine sachliche KI-Nutzungserklärung für ein Uni-Softwareprojekt. KI wurde für Konzept, Implementierung, Debugging, Tests, Security, Dokumentation und Präsentation genutzt. |
| Security-Dokumentation | Formuliere ein Security-Konzept mit sensiblen Daten, Risiken, vorhandenen Maßnahmen, zukünftigen Maßnahmen und Shift Security Left. |

### 9. Präsentation

| Zweck | Prompt |
|---|---|
| Einstieg | Formuliere einen Einstieg für eine Präsentation über eine Petsitter-Plattform. |
| Ablauf | Hilf bei einem Präsentationsablauf: Konzept, Landing Page, Trello, Diagramme, Techstack, CI, Security, Demo, KI-Nutzung. |
| Sprechertext | Formuliere einen Sprechertext für die ersten sieben Minuten einer Projektpräsentation mit Stakeholdern und Vorgehen. |

## Iterationen und Anpassungen

Viele KI-Antworten wurden nicht direkt übernommen. Typische Anpassungen waren:

- Umbenennung auf konkrete Klassen und Packages im Repository.
- Vereinfachung auf den Projektumfang.
- Anpassung an Vaadin statt generischem HTML-Frontend.
- Anpassung an MySQL für Kerndaten und MongoDB für Chat.
- Ergänzung projektspezifischer Geschäftsregeln.
- Entfernung von Vorschlägen, die für die Demo zu aufwendig waren.
- Korrektur von Code, der nicht zur bestehenden Struktur passte.
- Ergänzung von Tests oder manueller Prüfung.

## Verworfene oder reduzierte Vorschläge

Folgende KI-Vorschläge wurden nicht oder nur reduziert übernommen:

| Vorschlag | Grund |
|---|---|
| vollständige Microservice-Architektur | zu hoher Integrationsaufwand für Teamgröße und Projektumfang |
| produktiver Mailversand über SMTP | für lokale Demo nicht zwingend erforderlich |
| vollständiges Admin-Backend | nicht Kernanforderung |
| komplexes Payment-System | zu groß für Projektumfang |
| umfangreiche Monitoring-Infrastruktur | nicht notwendig für Uni-Demo |
| externe Security-Scanner in CI | sinnvoll, aber für Abgabe nicht vollständig umgesetzt |

## Verantwortlichkeit

Die KI erzeugte Vorschläge. Das Team entschied, welche Vorschläge verwendet werden. Finaler Code, Architekturentscheidungen, Tests, Demo und Dokumentation wurden durch das Team geprüft und verantwortet.
