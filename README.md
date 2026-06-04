# Petsitter / Pawsitters starten

Diese Anleitung beschreibt den Start der Anwendung auf einem blanken Rechner.
Sie gilt fuer Windows, macOS und Linux und setzt keine lokal installierte
Maven-Version voraus.

## 1. Voraussetzungen installieren

Installiere zuerst diese Programme:

| Programm                  | Zweck                           | Hinweis                             |
| ------------------------- | ------------------------------- | ----------------------------------- |
| Git                       | Repository herunterladen        | <https://git-scm.com/downloads>     |
| Java JDK 21               | Anwendung bauen und starten     | Nicht nur JRE installieren          |
| Docker mit Docker Compose | MySQL und MongoDB lokal starten | Unter Windows/macOS: Docker Desktop |

Beim ersten Start wird zusaetzlich Internetzugang benoetigt, weil Maven,
Vaadin und Docker benoetigte Abhaengigkeiten und Images herunterladen.

Pruefe die Installation in einem Terminal:

```bash
git --version
java -version
docker version
docker compose version
```

`java -version` muss Java 21 anzeigen. Falls eine andere Java-Version aktiv
ist, muss `JAVA_HOME` auf ein JDK 21 zeigen.

## 2. Projekt herunterladen

```bash
git clone https://github.com/JoFitTech/Petsitter.git
cd Petsitter
```

## 3. Docker starten

Starte Docker, bevor du die Anwendung startest.

- Windows/macOS: Docker Desktop oeffnen und warten, bis Docker laeuft.
- Linux: Docker-Dienst starten, z. B. mit `sudo systemctl start docker`.

Die Anwendung nutzt `compose.yaml`. Spring Boot startet darueber automatisch:

- MySQL auf Port `3306`
- MongoDB auf Port `27017`

Die Datenbanktabellen und Demo-Daten werden beim Start automatisch angelegt.

## 4. Anwendung starten

### Windows PowerShell

```powershell
.\mvnw.cmd spring-boot:run
```

### macOS oder Linux

```bash
./mvnw spring-boot:run
```

Falls macOS oder Linux meldet, dass `mvnw` nicht ausfuehrbar ist:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

Der erste Start kann mehrere Minuten dauern, weil Maven-Abhaengigkeiten,
Frontend-Abhaengigkeiten und Docker-Images geladen werden.

## 5. Anwendung oeffnen

Nach erfolgreichem Start ist die Anwendung erreichbar unter:

```text
http://localhost:8080
```

In der lokalen Entwicklung oeffnet Vaadin den Browser ggf. automatisch.

## 6. Login-Daten fuer Demo und lokale Tests

Seed-User aus der Datenbank:

| E-Mail                         | Passwort    |
| ------------------------------ | ----------- |
| `admin@petsitter.local`        | `localpass` |
| `anna.mueller@petsitter.local` | `localpass` |
| `ben.schmidt@petsitter.local`  | `localpass` |
| `lara.weber@petsitter.local`   | `localpass` |

## Tests ausfuehren

Alle Tests:

```bash
./mvnw test
```

Vollstaendige Verifikation wie in der CI:

```bash
./mvnw -B verify -Dspring.docker.compose.enabled=false
```

Bei Tests wird Docker Compose deaktiviert; die Tests laufen dadurch ohne lokale
MySQL- und MongoDB-Container.

## Troubleshooting

### Docker ist nicht erreichbar

Wenn beim Start eine Meldung wie `DOCKER NICHT ERREICHBAR` erscheint, ist Docker
nicht gestartet oder fuer das Terminal nicht erreichbar.

Loesung:

1. Docker Desktop bzw. den Docker-Dienst starten.
2. Kurz warten, bis Docker vollstaendig bereit ist.
3. Den Startbefehl erneut ausfuehren.

### Port ist bereits belegt

Die Anwendung und Datenbanken nutzen lokal diese Ports:

|    Port | Dienst              |
| ------: | ------------------- |
|  `8080` | Petsitter-Anwendung |
|  `3306` | MySQL               |
| `27017` | MongoDB             |

Wenn einer dieser Ports bereits belegt ist, beende den anderen Dienst oder passe
die Konfiguration ueber Umgebungsvariablen an.

### Falsche Java-Version

Wenn Maven oder Spring Boot wegen der Java-Version fehlschlaegt, pruefe:

```bash
java -version
```

Aktiv sein muss Java 21. Stelle ggf. `JAVA_HOME` auf dein installiertes JDK 21.

### Maven ist nicht installiert

Das ist normal und kein Problem. Das Projekt bringt den Maven Wrapper mit:

- Windows: `mvnw.cmd`
- macOS/Linux: `mvnw`

Nutze deshalb die Befehle aus dieser README statt `mvn`.

### Docker-Container oder Datenbanken zuruecksetzen

Wenn die lokalen Datenbanken in einen kaputten Zustand geraten sind, kannst du
die Container und Volumes zuruecksetzen.

Achtung: Dadurch werden alle lokalen Datenbankdaten geloescht.

```bash
docker compose down -v
```

Danach die Anwendung erneut starten. Die Datenbanken werden neu angelegt und die
Migrationen laufen erneut.

## Weitere Dokumentation

- Architektur: `ARCHITECTURE.md`
- Testdokumentation: `TEST_DOCUMENTATION.md`
- Sicherheitskonzept: `SECURITY_CONCEPT.md`
- Projektdokumentation: `DOCUMENTATION.md`
