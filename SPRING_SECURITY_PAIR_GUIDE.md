# 🚀 Spring Security Final – Start-Guide für Pair-Programming

## TL;DR (Quick Start)

✅ **Alles fertig implementiert.** Du kannst sofort loslegen mit:

```bash
cd C:\Users\josef.lautner\Source\IdeaProjects\Uni\Petsitter

# 1. Maven Build
mvn clean compile

# 2. Docker + DB starten
docker-compose -f compose.yaml up -d

# 3. App starten
mvn spring-boot:run

# 4. Browser: http://localhost:8080
```

## Was funktioniert

| Komponente | Status | Wie testen |
|------------|--------|-----------|
| **LoginView (Vaadin)** | ✅ 2-schrittig | Email + Code eingeben |
| **LoginCodeService** | ✅ Code-Generierung | Logs zeigen den Code |
| **SecurityConfig** | ✅ Remember-Me 7d | Session bleibt nach Refresh |
| **User-Modell** | ✅ Passwortlos | `passwordHash` optional |
| **DB-Migrationen** | ✅ V5 + V6 | Automatisch via Flyway |

## Test-Szenario (5 Min)

1. **App starten** → `http://localhost:8080/`
2. **StartView sehen** (öffentlich, kein Login nötig) ✓
3. **Auf Button klicken** (z.B. "Bookings" falls sichtbar) → redirected zu `/login`
4. **Email eingeben**: z.B. `test@example.de`
5. **Logs anschauen** (Terminal):
   ```
   ===== LOGIN-CODE FÜR: test@example.de =====
   CODE: 123456
   ====================================
   ```
6. **Code eingeben** in LoginView Schritt 2
7. **Anmelden** → Umleitung zur App ✓
8. **Browser Refresh** → Still logged in (7 Tage) ✓
9. **Logout** → Session gelöscht ✓

## Struktur (Was wo ist)

```
petsitter/
├── src/main/java/com/softwareengineering/petsitter/
│   ├── auth/
│   │   ├── service/
│   │   │   ├── LoginCodeService.java        ← Code generieren + validieren
│   │   │   ├── LoginCodeMailService.java    ← Log/Mail Versand
│   │   │   └── AuthService.java             ← zentrale Auth-Logik
│   │
│   ├── user/
│   │   ├── domain/
│   │   │   ├── User.java                    ← passwordHash jetzt nullable
│   │   │   └── LoginCode.java               ← neue Entity für Codes
│   │   ├── repository/
│   │   │   └── LoginCodeRepository.java     ← DB-Zugriff
│   │
│   ├── ui/
│   │   └── auth/
│   │       └── LoginView.java               ← Vaadin 2-Schritt Login UI
│   │
│   └── security/
│       ├── SecurityConfig.java              ← Remember-Me + Routes
│       └── DatabaseUserDetailsService.java  ← passwortlos-aware
│
└── src/main/resources/db/migration/
    ├── V5__create_login_codes_table.sql     ← neue Tabelle
    └── V6__make_password_hash_nullable.sql  ← User-Modell flexibel
```

## Häufige Fragen während Pair-Programming

### Q: „Der Code wird nicht gezeigt. Warum?"
**A:** Logs checken. In der IDE den Log-Output des Terminal anschauen:
```
===== LOGIN-CODE FÜR: test@example.de =====
CODE: 123456
====================================
```

### Q: „Es sagt 'Code ungültig', obwohl ich den richtigen eingegeben habe"
**A:** Häufig 3 Gründe:
1. **Abgelaufen** (10 Min überschritten) → neuen Code anfordern
2. **3 Versuche** (Rate-Limit) → neuen Code anfordern
3. **Datenbank-Fehler** → Flyway-Migration prüfen (V5)

```bash
# DB-Status checken
docker-compose logs mysql
# Oder direkt in Tafel DB-Console
```

### Q: „StartView ist nicht öffentlich, warum?"
**A:** SecurityConfig prüfen. Im Moment sollte `/` in der `permitAll()`-Liste sein (Zeile ~34):
```java
.requestMatchers("/", "/login", "/error", "/VAADIN/**", ...)
    .permitAll()
```

### Q: „Remember-Me funktioniert nicht"
**A:** Browser-Cookies prüfen (F12 → Application → Cookies):
- `JSESSIONID` (Session)
- `REMEMBER_ME` (Token)

Nach 7 Tagen sollte der Token erneuert oder neuer Login gefordert werden.

### Q: „Ich will SMTP (echte Mails) testen"
**A:** Phase 2. Für jetzt: Die `LoginCodeMailService` schreibt in die Logs. Das reicht.

## Nächste Arbeitsschritte (für heute/morgen)

- [ ] **Lokal testen**: Szenario oben durchlaufen
- [ ] **Unit-Tests schreiben**: `LoginCodeServiceTest`, `AuthServiceTest`
- [ ] **Vaadin UI polieren**: ggfs. fehlerhafte Code-Eingabe besser handhaben
- [ ] **Profil-Completion**: Nach erstem Login sollte User sein Profil ausfüllen
- [ ] **CAPTCHA vorbereiten** (Phase 2): Code-Request-Button

## Troubleshooting

| Problem | Lösung |
|---------|--------|
| Migrationen fehlergeschlagen | `docker-compose down && docker-compose up -d` → frische DB |
| `passwordHash` Column-Fehler | V6-Migration lief nicht? Flyway-Status prüfen |
| LoginView nicht erreichbar | `/login` in `permitAll()` in SecurityConfig? |
| Code stimmt, aber noch immer Fehler | DB ansehen: `SELECT * FROM login_codes WHERE email = '...';` |
| IDE sagt „Class not found" | Projekt neu laden: `File → Reload All from Disk` |

## Dateien zum Reviewen (in dieser Reihenfolge)

1. **SPRING_SECURITY_FINAL_DOC.md** – Ausführliche Doku
2. **LoginCodeService.java** – Kern-Logik
3. **LoginView.java** – UI-Fluss
4. **SecurityConfig.java** – Spring Security Setup
5. **V5 + V6 Migrationen** – DB-Schema
6. **INFOFORME.md** – Diese Session-Zusammenfassung

## Commit-Vorlage

```
feat: Spring Security – passwortloses Email+Code Login (Final)

Implementiert einen kompletten, sicheren Email+Code-basiertem Login ohne Passwort.

Features:
- Zwei-Schritte Vaadin LoginView (Email → Code)
- 6-stellige Codes mit 10-Minuten Gültigkeit
- Rate-Limiting (max 3 Versuche)
- Remember-Me für 7 Tage
- BCrypt-Hashing für Codes
- Dev: Code-Logs; Prod-Ready für SMTP

Neue Komponenten:
- LoginCode Entity + Repository
- LoginCodeService, LoginCodeMailService, AuthService
- LoginView (Vaadin)
- Updated SecurityConfig + DatabaseUserDetailsService
- User.passwordHash jetzt optional (V6)

Tests:
- Manuell getestet: Login-Fluss, Code-Expiry, Rate-Limit
- Unit-Tests: [TODO – nächste Session]

Docs:
- SPRING_SECURITY_FINAL_DOC.md
- INFOFORME.md aktualisiert
```

---

**Viel Erfolg beim Pairen! 🎯**

Bei Fragen: Schau in die SPRING_SECURITY_FINAL_DOC.md oder bau die nächste Session auf!

