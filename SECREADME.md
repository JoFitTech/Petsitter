# Security README (Spring Security)

Dieses Dokument beschreibt den aktuellen Security-Stand im Projekt, wie Spring Security eingebunden ist und wie der Login-Flow funktioniert.

---

## 1) Aktueller Status

Stand heute ist die Security-Basis umgesetzt:

- Spring Security ist aktiv und erzwingt Authentifizierung fuer geschuetzte Routen.
- Es gibt eine eigene Vaadin-Login-Seite unter `/login`.
- Benutzer werden primaer aus der Datenbank geladen (ueber `UserRepository`).
- Optional gibt es einen konfigurierbaren Demo-User als Fallback.
- Passwoerter werden mit BCrypt geprueft/gespeichert.
- Logout invalidiert Session + entfernt `JSESSIONID`.
- Method Security ist aktiv (`@EnableMethodSecurity`), inklusive JSR-250-Unterstuetzung (`@PermitAll`, `@RolesAllowed`, ...).

Dateien:

- `src/main/java/com/softwareengineering/petsitter/security/SecurityConfig.java`
- `src/main/java/com/softwareengineering/petsitter/security/DatabaseUserDetailsService.java`
- `src/main/java/com/softwareengineering/petsitter/security/AuthenticatedUser.java`
- `src/main/java/com/softwareengineering/petsitter/ui/security/LoginView.java`
- `src/main/java/com/softwareengineering/petsitter/config/PetsitterSecurityProperties.java`

---

## 2) Wie Spring Security hier eingebunden ist

### 2.1 SecurityFilterChain

In `SecurityConfig` wird die zentrale `SecurityFilterChain` konfiguriert:

- Oeffentliche Endpunkte (`permitAll`):
  - `/login`
  - `/error`
  - statische Vaadin-Ressourcen (`/VAADIN/**`, `/frontend/**`, `/webjars/**`, ...)
- Alles andere: `authenticated()`
- Form Login:
  - Login-Page: `/login`
  - Success URL: `/`
- Logout:
  - URL: `/logout`
  - Redirect nach Logout: `/login?logout`
  - Session-Invalidierung + Cookie-Loeschung

### 2.2 Passwort-Encoder

`PasswordEncoder` ist auf BCrypt gesetzt:

- Bean in `SecurityConfig`: `new BCryptPasswordEncoder()`
- Erwartung: `User.passwordHash` enthaelt BCrypt-Hash

### 2.3 Method Security

Durch `@EnableMethodSecurity(jsr250Enabled = true)` koennen methoden-/klassenbezogene Security-Annotations genutzt werden, z. B.:

- `@PermitAll`
- `@RolesAllowed("USER")`
- `@DenyAll`

---

## 3) User-Laden (Auth) im Detail

`DatabaseUserDetailsService` implementiert `UserDetailsService`:

1. Loginname (Username) wird als Email behandelt.
2. DB Lookup via `UserRepository.findByEmail(username)`.
3. Falls gefunden:
   - Spring `UserDetails` wird aus Domain-User gebaut
   - Passwort kommt aus `user.getPasswordHash()`
   - aktuell wird Rolle `USER` gesetzt
4. Falls nicht gefunden:
   - optionaler Demo-User wird geprueft (wenn aktiviert)
5. Wenn weder DB-User noch Demo-User passt:
   - `UsernameNotFoundException`

---

## 4) Login-Flow (End-to-End)

1. User oeffnet geschuetzte Seite.
2. Spring Security leitet auf `/login` um.
3. `LoginView` zeigt Vaadin `LoginForm`.
4. Formular postet auf Spring Security Endpoint `login`.
5. Spring ruft `DatabaseUserDetailsService` auf.
6. Passwortabgleich ueber BCrypt.
7. Bei Erfolg: Redirect auf `/`.
8. Bei Fehler: Redirect auf `/login?error` (UI zeigt Fehlerstatus).

---

## 5) Konfiguration (YAML)

Basis in `src/main/resources/application.yml`:

```yaml
petsitter:
  security:
    demo:
      enabled: ${PETSITTER_DEMO_ENABLED:true}
      username: ${PETSITTER_DEMO_USERNAME:localuser}
      password: ${PETSITTER_DEMO_PASSWORD:localpass}
      role: ${PETSITTER_DEMO_ROLE:USER}
```

Hinweis:

- Demo-User ist fuer Entwicklung/Demo praktisch.
- In produktionsnahen Umgebungen sollte `enabled=false` gesetzt werden.

---

## 6) AuthenticatedUser Helper

`AuthenticatedUser` ist eine kleine Hilfskomponente, die den aktuell eingeloggten Domain-User liefert:

- liest `Authentication` aus `SecurityContextHolder`
- ignoriert anonyme Authentifizierungen
- mappt `authentication.getName()` -> `UserRepository.findByEmail(...)`

Verwendung:

- praktisch in Services/UI, wenn der aktuelle User benoetigt wird
- reduziert wiederholten SecurityContext-Code

---

## 7) Was schon gut ist

- Security nicht nur In-Memory, sondern DB-first
- BCrypt korrekt eingebunden
- Login/Logout sauber verdrahtet
- Vaadin + Spring Security kompatibel konfiguriert
- Method Security vorbereitet

---

## 8) Was als naechstes empfohlen ist

1. Rollenmodell fachlich erweitern (`USER`, `ADMIN`, evtl. `OWNER`, `SITTER` je nach Design).
2. Registrierungs-Flow bauen (mit BCrypt-Hashing beim Speichern).
3. Security-Tests ergaenzen:
   - Login success/failure
   - Zugriffsschutz auf Views/Endpoints
4. Demo-User in spaeteren Umgebungen deaktivieren.
5. Optional: Audit-Logging fuer sicherheitsrelevante Events (Login, Logout, denied access).

---

## 9) Lokaler Start (Security + Infra)

```powershell
docker compose up -d
.\mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Optional Testlauf:

```powershell
.\mvnw test
```

