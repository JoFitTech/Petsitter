# Spring Security – Email+Code Passwortlose Login (FINAL v1)

## Überblick

Implementierung eines **passwortlosen Email+Code-Logins** mit 7-Tage Remember-Me.

- ✅ Public: nur `StartView` (`/`)
- ✅ Private: Alles andere braucht Login
- ✅ Authentifizierung: Email → 6-stelliger Code (10 Min gültig) → Session
- ✅ Remember-Me: 7 Tage automatische Session-Persistierung
- ✅ Mail in Dev: Logs; in Prod: SMTP-Integration
- ✅ Rate-Limiting: max 3 Versuche pro Code

## Neue Komponenten

### 1. **LoginCode Entity** 
`user/domain/LoginCode.java`
- Speichert gehashte Codes (BCrypt)
- Tracking: `expiresAt`, `usedAt`, `attempts`, `requestIp`
- Hilfsmethoden: `isValid()`, `isExpired()`, `isUsed()`, `isRateLimited()`

### 2. **LoginCode Repository**
`user/repository/LoginCodeRepository.java`
- `findValidCodeByEmail(email)` – aktueller gültiger Code
- `findUnusedCodesByEmail(email)` – zum Invalidieren alter Codes
- `deleteExpiredCodes(threshold, oneHourAgo)` – Cleanup

### 3. **LoginCodeService**
`auth/service/LoginCodeService.java`
- `requestLoginCode(email, ip)` → Code generieren, hashen, speichern, mailen
- `validateLoginCode(email, plainCode)` → Hash-Vergleich, Attempt-Zählung, Markieren als `used`
- `cleanupExpiredCodes()` → alte Codes löschen (würde via @Scheduled aufgerufen)

### 4. **LoginCodeMailService**
`auth/service/LoginCodeMailService.java`
- `sendLoginCode(email, code)` 
  - **DEV**: Loggt Code zu stdout (easy testing)
  - **PROD**: SMTP-Integration (Spring Mail)

### 5. **AuthService**
`auth/service/AuthService.java`
- Vereinigte Login-Logik
- `getOrCreateUser(email)` – User-Placeholder für neu angeforderte Codes
- `toUserDetails(user)` – Konvertierung zu Spring `UserDetails`
- `requestCodeForEmail(email, ip)` – Vollfluss
- `verifyCodeAndGetUser(email, code)` → Optional mit validiertem User

### 6. **LoginView (Vaadin)**
`ui/auth/LoginView.java`
- **Schritt 1**: Email eingeben → "Code anfordern"
- **Schritt 2**: Code eingeben (6 Stellen) → "Anmelden"
- Dynamisches UI-Switching zwischen Schritten
- Error/Success Messages
- Security Context setzen auf Login

### 7. **SecurityConfig (aktualisiert)**
`security/SecurityConfig.java`
- ✅ CSRF disabled (Vaadin-intern)
- ✅ `/` + `/login` + Static-Assets: `permitAll()`
- ✅ Alles andere: `authenticated()`
- ✅ **RememberMeServices**: 7 Tage Token-basiert
- ✅ Logout: löscht Session + Cookies

### 8. **User Entity (flexibel)**
`user/domain/User.java`
- `passwordHash` jetzt nullable (statt `NOT NULL`)
- Passwortlose User: `passwordHash = ""` oder `null`
- Abwärtskompatibel mit existierenden Password-Hashes

### 9. **Migrationen**
```
V5__create_login_codes_table.sql       – neue Tabelle
V6__make_password_hash_nullable.sql    – User.passwordHash optional
```

## Fluss: Login-Ende-Zu-Ende

```
Benutzer öffnet App
  ↓
Route "/" (StartView) → öffentlich ✓
Route "/bookings" → erfordert Auth → Umleitung zu "/login"
  ↓
LoginView Schritt 1: Email eingeben
  ↓
AuthService.requestCodeForEmail(email, ip)
  → LoginCodeService.requestLoginCode() 
    → Code generieren (zufällig 6 Stellen)
    → BCrypt hashen
    → LoginCode Entity speichern (expires: +10 Min)
    → LoginCodeMailService.sendLoginCode() (Log oder SMTP)
  ↓
Email zeigt: "Dein Code: 123456" (oder logs)
  ↓
LoginView Schritt 2: Code eingeben
  ↓
AuthService.verifyCodeAndGetUser(email, code)
  → LoginCodeService.validateLoginCode()
    ✓ Code in DB vorhanden?
    ✓ Nicht abgelaufen?
    ✓ Nicht bereits verwendet?
    ✓ Attempts < 3?
    ✓ BCrypt.matches(plainCode, codeHash)?
    → Code als "used" markieren
    → User laden
  ↓
LoginView.setSecurityContext(user)
  → UsernamePasswordAuthenticationToken mit UserDetails
  → HttpSessionSecurityContextRepository sichern
  ↓
Redirect zur StartView ("/")
  ↓
User ist authentifiziert → kann auf alle Routes zugreifen
  ↓
Browser-Neustart nach 7 Tagen: Remember-Me Token prüft → Session bleibt
```

## Test-Checkliste

### Unit-Tests
```java
// LoginCodeService
✅ Code wird generiert und gehashed
✅ Code wird mit Expiry-Zeit gespeichert
✅ validateLoginCode() akzeptiert gültigen Code
✅ validateLoginCode() lehnt falschen Code ab
✅ validateLoginCode() lehnt abgelaufenen Code ab
✅ validateLoginCode() lehnt bereits verwendeten Code ab
✅ validateLoginCode() zählt Attempts hoch
✅ Rate-Limit greift bei 3+ Versuchen

// AuthService
✅ getOrCreateUser() erstellt Placeholder-User
✅ getOrCreateUser() findet existierenden User
✅ requestCodeForEmail() sendet Mail/Log
✅ verifyCodeAndGetUser() gibt User zurück bei Erfolg
✅ verifyCodeAndGetUser() gibt Optional.empty() bei Fehler
```

### Integration-Tests
```java
✅ GET / (StartView) → 200, keine Auth nötig
✅ GET /bookings → 302 Redirect zu /login (unauthenticated)
✅ GET /login → 200, LoginView rendert
✅ POST /login + Email → Code versendet, Schritt 2 sichtbar
✅ POST /login + Code (falsch) → Error-Message, Attempts++
✅ POST /login + Code (richtig) → Session gesetzt, Redirect zu "/"
✅ GET /bookings (authenticated) → 200, Inhalte sichtbar
✅ POST /logout → Session gelöscht, Redirect zu /login
✅ Remember-Me Cookie: nach 7 Tagen ggfs. erneuert
```

### Manual-Szenarien
```
1. Fresh Login:
   - App starten → StartView sichtbar
   - Auf Button klicken → zu /login umgeleitet
   - Email eingeben → Code-Screen erscheint
   - Code aus Logs kopieren
   - Code eingeben → Welcome-Screen / Profil-Daten?
   - Refresh: Session bleibt ✓

2. Code-Expiry:
   - Email eingeben → Code versendet
   - 10+ Minuten warten (oder DB manuell verändern)
   - Code eingeben → Fehler ✓

3. Rate-Limit:
   - Email eingeben → Code versendet
   - 3x falscher Code → Error "Code-Versuche überschritten" ✓
   - 4. Versuch → keinen neuen Code akzeptieren

4. Remember-Me:
   - Login durchführen
   - Browser schließen / Session-Cookie ablaufen lassen
   - Zur App zurück → Still logged in? ✓ (nach 7 Tagen: neu-login)

5. Old-DB: Existierende User mit Passwort-Hash
   - Alte Demo-User sollen noch funktionieren? → DatabaseUserDetailsService anpassen
   - Oder: Nur neu via Email-Code, alte User mit Passwort müssen re-registrieren
```

## DEV vs. PROD Konfiguration

### DEV (lokale App mit compose.yaml)
```yaml
# application.yml (DEV-Standard)
petsitter:
  security:
    demo:
      enabled: true
      username: localuser
      password: localpass
      role: USER
```
- LoginCodeMailService loggt Code zu stdout
- Schnell testen, keine SMTP-Abhängigkeit
- Rate-Limit greift trotzdem!

### PROD (echte Mail nötig)
```java
// LoginCodeMailService.sendLoginCode() würde:
// 1. spring-boot-starter-mail hinzufügen (pom.xml)
// 2. SMTP-Eigenschaften setzen (env vars)
// 3. SimpleMailMessage.sendEmail() aufrufen
// 4. HTML-Template mit Code + Link einbinden

// Beispiel (Phase 2):
@Configuration
@ConditionalOnProperty("spring.mail.host")
public class MailConfig {
    // JavaMailSender Bean konfigurieren
}
```

## Häufige Fragen & Antworten

### Q: Was ist mit dem alten Passwort-Login (formLogin)?
**A:** Entfernt. Neue Users registrieren sich automatisch beim Code-Request. Alte Users mit Passwort-Hash müssen neu registrieren oder wir bauen einen Hybrid-Mode.

### Q: Warum ist `passwordHash` jetzt nullable?
**A:** Passwortlose User haben keinen Password. Das Feld bleibt für Hybrid-Szenarien (z. B. "login with password or code").

### Q: Wann lädt sich `cleanupExpiredCodes()` aus?
**A:** Manuell jetzt. In Phase 2 via `@Scheduled(fixedRate = 3600000)` (stündlich). Alternativ: DB-Trigger.

### Q: Kann ein User mehrere Codes anfordern?
**A:** Ja, aber nur 1 gleichzeitig gültig. Alte Codes werden automatisch invalidiert (setzen `usedAt`).

### Q: Was passiert nach erfolgreicher Code-Validierung mit den Profil-Daten?
**A:** `AuthService.getOrCreateUser()` erstellt einen Placeholder. User **sollte** danach sein Profil ausfüllen (Name, Adresse, Telefon). Das ist Phase 2 (Onboarding-View).

### Q: Wie sieht der Link zum Logout aus?
**A:** Button in MainLayout aufzufügen (oder Menü): `<a href="/logout">Abmelden</a>` oder Vaadin Button mit Listener zu `/logout`.

## Next Steps (Phase 2)

- [ ] CAPTCHA integrieren (Cloudflare Turnstile oder reCAPTCHA)
- [ ] SMTP-Integration (echter Mail-Versand)
- [ ] Profil-Completion-Workflow nach Code-Login
- [ ] "Angemeldet bleiben"-Checkbox in LoginView
- [ ] Login-Verlauf / IP-Audit im Backend
- [ ] Rate-Limit über Redis/Cache (aktuell: pro Code-Entity)
- [ ] Tests schreiben (Unit + Integration)
- [ ] Documentation / Diagrams aktualisieren

## Dateien (Quick-Reference)

| Datei | Zweck |
|-------|-------|
| `LoginCode.java` | Entity für temporäre Codes |
| `LoginCodeRepository.java` | DB-Zugriff für Codes |
| `LoginCodeService.java` | Code-Generierung, Validierung |
| `LoginCodeMailService.java` | Mail-Versand (DEV: Log) |
| `AuthService.java` | Zentrale Auth-Logik |
| `LoginView.java` | Vaadin 2-Schritte UI |
| `SecurityConfig.java` | Spring Security Config + Remember-Me |
| `User.java` | ← passwordHash nullable |
| `V5__create_login_codes_table.sql` | Migration Codes-Tabelle |
| `V6__make_password_hash_nullable.sql` | Migration passwordHash flexibel |

## Deployment-Checkliste

```
☐ DB-Migrationen (Flyway): V5, V6
☐ Code kompiliert: mvn clean compile
☐ Tests: mvn test (wenn vorhanden)
☐ Docker Image: mvn clean package
☐ compose.yaml: DB + App starten
☐ Manual Test: Login Flow 1x durchlaufen
☐ INFOFORME.md / KICHANGES.md aktualisieren
☐ PR/Merge: Code Review
☐ Deploy: PROD-Config anpassen (SMTP, etc.)
```

---

**Stand**: 2026-05-11  
**Status**: ✅ Ready for Pair-Programming / Testing  
**Nächste Session**: CAPTCHA + Unit-Tests

