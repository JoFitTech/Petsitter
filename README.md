# Petsitter / Pawsitters

Technisches Grundgeruest fuer das Uni-Projekt.

## Status

- Projektgeruest initialisiert
- Noch keine Fachlogik implementiert

## Lokaler Start (ohne Docker Compose)

```powershell
.\mvnw spring-boot:run
```

## Lokaler Start (mit Docker Compose Profil)

```powershell
docker compose up -d
.\mvnw spring-boot:run -Dspring-boot.run.profiles=compose
```

## Testlauf

```powershell
.\mvnw test
```

## Projektstruktur

- `src/main/java/com/softwareengineering/petsitter/config`
- `src/main/java/com/softwareengineering/petsitter/security`
- `src/main/java/com/softwareengineering/petsitter/ui`
- `src/main/java/com/softwareengineering/petsitter/service`
- `src/main/java/com/softwareengineering/petsitter/repository`
- `src/main/java/com/softwareengineering/petsitter/domain`
- `src/main/java/com/softwareengineering/petsitter/dto`
- `src/main/java/com/softwareengineering/petsitter/util`

## TODO

- TODO: Endgueltigen Projektnamen (`Petsitter` vs. `Pawsitters`) fachlich entscheiden.
- TODO: Fachliche Domene und Rollenmodell definieren.
- TODO: CI-Qualitaetsregeln erweitern (z. B. Coverage, Checks).
