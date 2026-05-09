# Technology Guide für Backend

## Spring Boot 3.x + Java 21

### Was ist Spring Boot?

**Spring Boot** ist ein Framework, das Spring (großes Java Enterprise Framework) vorkonfiguriert und einfach nutzbar macht.

#### Vorher (reines Spring)
```java
// Viel XML-Konfiguration, Bean-Definition, etc.
// ~500 Zeilen Boilerplate
```

#### Nachher (Spring Boot)
```java
@SpringBootApplication
public class PetsitterApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetsitterApplication.class, args);
    }
}
// Das war's. Alles läuft.
```

#### Was Spring Boot für uns macht

| Was | Effekt |
|-----|--------|
| Auto-Konfiguration | Datasource, JPA, Security, etc. automatisch konfiguriert |
| Embedded Server | Tomcat mitgeliefert, keine separate Installation |
| Dependency Management | `pom.xml` ist übersichtlich, keine Versionskonflikt |
| Profil-Support | `application.properties`, `application-local.properties`, `src/test/resources/application.properties` |
| Actuator | Health Checks, Metrics unter `/actuator` |

### Java 21 (wichtig zu verstehen)

Java 21 ist LTS (Long Term Support) bis 2028. Features, die wir nutzen:

| Feature | Was | Beispiel |
|---------|-----|---------|
| **Records** (eingefroren, nur lesbar) | Immutable Datenklassen | `record UserDTO(String email, String name)` |
| **Text Blocks** (Multiline Strings) | `"""..."""` | SQL in Code |
| **Pattern Matching** | Switch mit Pattern | `switch(obj) { case String s -> ... }` |
| **Virtual Threads** (experimentell) | N:1 Thread Pooling | `@Async` mit weniger Overhead |
| **Sealed Classes** | Kontrollierte Vererbung | `sealed class Offer {}` |

Wir nutzen hauptsächlich Java 21 wegen *moderner Syntax* und *Performance*. Keine exotischen Features.

---

## Spring Data JPA

### Was ist JPA?

**JPA** = Java Persistence API = Standard für Datenbank-Zugriff in Java.

Entity ↔ SQL-Tabelle (automatisch)

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // → users.id (Primary Key, Auto-Increment)
    
    @Column(unique = true, nullable = false)
    private String email;  // → users.email (Unique, Not Null)
}
```

Die Datenbank-Tabelle entsteht automatisch (oder via Flyway).

### Spring Data JPA

**Spring Data JPA** nimmt dir noch mehr Arbeit ab:

```java
// Das ist alles, was du schreibst:
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

// Spring generiert automatisch:
// - SELECT * FROM users WHERE id = ?
// - INSERT INTO users (...) VALUES (...)
// - UPDATE users SET ... WHERE id = ?
// - DELETE FROM users WHERE id = ?
// - SELECT * FROM users WHERE email = ?
```

Zauberei? Nicht ganz — Spring liest die Methoden-Namen und generiert SQL.

| Repository-Methode | Generiertes SQL |
|-------------------|-----------------|
| `findById(1L)` | `SELECT * FROM users WHERE id = 1` |
| `findByEmail("foo@bar.com")` | `SELECT * FROM users WHERE email = 'foo@bar.com'` |
| `findByEmailAndCity("foo", "Vienna")` | `SELECT * FROM users WHERE email = 'foo' AND city = 'Vienna'` |
| `findAll()` | `SELECT * FROM users` |
| `deleteById(1L)` | `DELETE FROM users WHERE id = 1` |
| `save(user)` | `INSERT INTO ... ON DUPLICATE UPDATE ...` |

### Warum Repositories?

```
Service ruft Repository auf
        ↓
Repository spricht mit JPA
        ↓
JPA generiert SQL
        ↓
SQL geht zu MySQL
        ↓
Daten kommen zurück
```

Das separiert Geschäftslogik (Service) von Datenbanklogik (Repository).

---

## MySQL 8.4 + H2

### MySQL (Produktion)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/petsitter
    username: petsitter
    password: petsitter
    driver-class-name: com.mysql.cj.jdbc.Driver
```

**Warum MySQL?**
- Standard in der Industrie
- Zuverlässig, well-tested
- Docker-Container trivial
- Transaktionen, Foreign Keys

### H2 (Testing)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
```

**Warum H2 in Tests?**
- In-Memory: blitzschnell
- Keine externe Abhängigkeit
- Jeder Test startet mit sauberer DB
- CI/CD brauchte kein Docker

### Ein Datenbank-Test-Beispiel

```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserRepositoryTest {
    
    @Autowired
    private UserRepository userRepo;

    @Test
    void testSaveAndFind() {
        // Arrange
        User user = new User();
        user.setEmail("test@test.com");
        
        // Act
        userRepo.save(user);
        Optional<User> found = userRepo.findByEmail("test@test.com");
        
        // Assert
        assertTrue(found.isPresent());
        assertEquals("test@test.com", found.get().getEmail());
    }
}
// H2 erstellt die Tabelle automatisch, Test läuft in Millisekunden
```

---

## Flyway

### Was ist Flyway?

**Flyway** ist ein Migrations-Tool. Es macht folgendes:

1. Du schreibst ein SQL-Skript: `V1__create_schema.sql`
2. Du commits das Skript ins Git
3. Beim App-Start führt Flyway die Skripte in Reihenfolge aus
4. DB ist immer im erwarteten Zustand

### Versioning mit Flyway

```
src/main/resources/db/migration/

V1__create_schema.sql           ← Erstes Script (V1)
V2__insert_demo_data.sql        ← Zweites Script (V2)
V3__add_audit_columns.sql       ← Drittes Script (V3) — später
```

Flyway verfolgt, welche Skripte bereits laufen. Von V1 zu V2 läuft nur V2.

### Warum nicht `ddl-auto: update`?

`ddl-auto: update` ist böse:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # ← SCHLECHT
```

Warum? Hibernate vergleicht Entity-Code mit DB und macht *nebenbei* Änderungen. Das ist:
- **Unvorhersehbar** – manchmal funktioniert es, manchmal nicht
- **Nicht versioniert** – niemand weiß, was geändert wurde
- **Nicht testbar** – Änderungen laufen stillschweigend ab
- **Nicht produktiv** – DBAs brauchen Kontrolle über Schema-Änderungen

### Flyway ist besser

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # ← GUT: Nur validieren, nicht ändern
  flyway:
    enabled: true
```

Jede Schemaänderung:
1. Wird als neues Skript geschrieben
2. Ins Git committed (= Audit Trail)
3. von Flyway versioniert (= reproduzierbar)
4. wird reviewt (= Qualitätskontrolle)

---

## Spring Security

### Login-Flow (Form Login)

```
Browser
    ↓
http://localhost:8080/login (öffentlich)
    ↓
Login-Form: Email + Passwort
    ↓
POST /login (Spring Security interceptiert)
    ↓
1. Email existiert? (UserRepository.findByEmail)
2. Passwort korrekt? (BCrypt.matches)
3. User gefunden + Passwort OK → Session erstellen
4. Ansonsten → Fehler-Nachricht
    ↓
Erfolgreich → Redirect zu Dashboard
Fehler → Redirect zu /login?error
```

### Was ist BCrypt?

Ein Hashing-Algorithmus für Passwörter.

```java
// Passwort speichern
String plain = "localpass";
String hashed = BCrypt.hashpw(plain, BCrypt.gensalt());
// hashed = "$2a$10$3GXKKMr3/C...PxRZa3H.O"
// → Die Raw-Passwörter stehen NIE in der DB

// Login-Check
boolean isCorrect = BCrypt.checkpw("localpass", hashed);
// → Nur das gehashte Passwort mit dem eingegebenen vergleichen
// → Kann nicht rückwärts dekodiert werden
```

### SecurityConfig in diesem Projekt

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // ← BCrypt
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            .csrf().disable()  // ← ÜBERPRÜFT: sollte aktiviert bleiben!
            .authorizeRequests()
                .requestMatchers("/login", "/register").permitAll()  // Öffentlich
                .anyRequest().authenticated()  // Alles andere: Login erforderlich
            .and()
            .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard")
            .and()
            .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
        ;
        return http.build();
    }
}
```

Das sagt Spring Security:
1. BCrypt für Passwörter
2. `/login` und `/register` öffentlich
3. Alles andere braucht Login
4. Form Login (traditioneller Login-Page)
5. CSRF Protection aktiv

---

## @Transactional

### Das Problem

```
User erstellt Request auf Offer:

1. Neuer Request wird erstellt (persisted)
2. Offer wird davon benachrichtigt (update)

ABER: Zwischen 1 und 2 crasht der Server → Inkonsistenter Zustand!
```

### Die Lösung: @Transactional

```java
@Service
public class RequestService {
    
    @Transactional  // ← Magic Annotation
    public Request createRequest(Long offerId, Long requesterId, String message) {
        // 1. Request erstellen
        OfferRequest req = new OfferRequest();
        req.setOffer(offerRepo.findById(offerId).orElseThrow());
        req.setRequester(userRepo.findById(requesterId).orElseThrow());
        req.setMessage(message);
        req.setStatus(RequestStatus.PENDING);
        
        // 2. Request speichern
        requestRepo.save(req);
        
        // 3. Notifications senden
        notificationService.notifyOfferCreator(...);
        
        // Wenn hier Exception kommt → Alles wird ROLLED BACK
        // Wenn alles OK → Alles wird COMMITTED
        
        return req;
    }
}
```

Spring sagt der Datenbank: "Entweder alle diese SQL-Statements, oder gar keiner."

Das ist **ACID**: Atomicity, Consistency, Isolation, Durability.

---

## Validierung (JSR-380)

### Bean Validation in Entities

```java
@Entity
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Email darf nicht leer sein")
    @Email(message = "Email muss gültig sein")
    @Column(unique = true, nullable = false)
    private String email;
    
    @NotBlank
    @Size(min = 8, message = "Passwort mindestens 8 Zeichen")
    private String passwordHash;
    
    @NotBlank
    @Size(max = 50)
    private String firstName;
    
    @Positive(message = "Alter muss positiv sein")
    private Integer age;
}
```

### Validierung im Service

```java
@Service
public class OfferService {
    
    @Autowired
    private ValidatorFactory validatorFactory;
    
    public Offer createSitterOffer(Long userId, LocalDate start, LocalDate end, String city, BigDecimal price, String desc) {
        
        // 1. Manuelle Validierungen
        if (start.isAfter(end)) {
            throw new BusinessRuleViolationException("Start muss vor Ende liegen");
        }
        
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleViolationException("Preis muss > 0 sein");
        }
        
        User creator = userRepo.findById(userId)
            .orElseThrow(() -> new NotFoundException("User nicht gefunden"));
        
        // 2. Offer erstellen
        Offer offer = new Offer();
        offer.setCreator(creator);
        offer.setType(OfferType.SITTER_OFFER);
        offer.setStartDate(start);
        offer.setEndDate(end);
        offer.setCity(city);
        offer.setPricePerWeek(price);
        offer.setDescription(desc);
        offer.setStatus(OfferStatus.OPEN);
        
        // 3. Bean Validation (wird automatisch von JPA getriggert)
        return offerRepo.save(offer);
        // Wenn ein @NotNull oder @Email fehlschlägt → ConstraintViolationException
    }
}
```

---

## Testpyramide

```
        △ E2E Tests (Selenium, UI-Tests) — selten
       / \
      /   \ Integration Tests (mit H2, DAO-Tests)
     /     \
    /       \ Unit Tests (Service + Repository Mock) — häufig
   /         \
  △-----------△
```

Wir fokussieren auf **Unit Tests** (schnell, einfach) und **Integration Tests** (mit H2).

### Unit Test Beispiel (OfferService)

```java
@ExtendWith(MockitoExtension.class)
class OfferServiceUnitTest {
    
    @Mock
    private OfferRepository offerRepo;
    
    @Mock
    private UserRepository userRepo;
    
    @InjectMocks
    private OfferService offerService;
    
    @Test
    void createSitterOffer_withValidData_shouldSucceed() {
        // Arrange
        User mockUser = new User();
        mockUser.setId(1L);
        when(userRepo.findById(1L)).thenReturn(Optional.of(mockUser));
        
        Offer savedOffer = new Offer();
        savedOffer.setId(1L);
        when(offerRepo.save(any(Offer.class))).thenReturn(savedOffer);
        
        // Act
        Offer result = offerService.createSitterOffer(
            1L, 
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 7, 14),
            "Vienna",
            new BigDecimal("50.00"),
            "I care for dogs"
        );
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(offerRepo, times(1)).save(any(Offer.class));
    }
    
    @Test
    void createSitterOffer_withInvalidDateRange_shouldThrow() {
        // Act & Assert
        assertThrows(BusinessRuleViolationException.class, () -> {
            offerService.createSitterOffer(
                1L,
                LocalDate.of(2026, 7, 14),
                LocalDate.of(2026, 7, 1),  // ← Ende vor Start
                "Vienna",
                new BigDecimal("50.00"),
                "I care for dogs"
            );
        });
    }
}
```

### Integration Test Beispiel

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class OfferServiceIntegrationTest {
    
    @Autowired
    private OfferService offerService;
    
    @Autowired
    private UserRepository userRepo;
    
    @Autowired
    private OfferRepository offerRepo;
    
    @Test
    void fullFlow_createOwnerOfferAndFindMatchingSitter() {
        // Arrange: richtiger DB-State mit H2
        User owner = new User();
        owner.setEmail("owner@test.com");
        owner.setFirstName("Anna");
        userRepo.save(owner);
        
        Pet pet = new Pet();
        pet.setName("Balu");
        pet.setSpecies(PetSpecies.DOG);
        pet.setOwner(owner);
        petRepo.save(pet);
        
        // Act
        Offer offer = offerService.createOwnerOffer(
            owner.getId(),
            pet.getId(),
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 7, 14),
            "Vienna",
            "Need sitter for Balu"
        );
        
        // Assert: Dann auf echte DB geprüft
        assertNotNull(offer.getId());
        assertEquals(OfferType.OWNER_OFFER, offer.getType());
        
        Optional<Offer> found = offerRepo.findById(offer.getId());
        assertTrue(found.isPresent());
    }
}
```

---

## Zusammenfassung: Architektur-Layers

```
┌─────────────────────────────────┐
│  @Entity (mit @Column)          │ → Hibernate/JPA
│  Beans Validation (@NotNull)    │
└────────────────┬────────────────┘
                 ↓
┌─────────────────────────────────┐
│  JpaRepository (Interface)      │ → Spring Data JPA
│  findByEmail, findAll, etc.     │ → SQL generiert automatisch
└────────────────┬────────────────┘
                 ↓
┌─────────────────────────────────┐
│  @Service @Transactional        │ → Geschäftslogik
│  createSitterOffer,             │
│  acceptRequest,                 │ → Atomare Transaktionen
│  etc.                           │
└────────────────┬────────────────┘
                 ↓
┌─────────────────────────────────┐
│  Vaadin View (später)           │ → UI
│  oder REST Controller            │
└─────────────────────────────────┘
```

Jeder Layer hat eine Verantwortung:
- **Entity** = Struktur + einfache Validierung
- **Repository** = Datenbankzugriff
- **Service** = Geschäftslogik + Transaktionen
- **Controller/View** = HTTP/UI Entry Point

---

## Nützliche Spring Boot Kurzreferenz

| Annotation | Was | Beispiel |
|------------|-----|---------|
| `@SpringBootApplication` | Main Class | `class PetsitterApplication` |
| `@Configuration` | Config-Klasse | `class SecurityConfig` |
| `@Bean` | Einzelnes Bean erzeugen | `@Bean PasswordEncoder` |
| `@Service` | Business Logic | `class OfferService` |
| `@Repository` | Datenbank-Zugriff | `interface OfferRepository` |
| `@Autowired` | Dependency Injection | `@Autowired OfferService svc` |
| `@Entity` | JPA Entity | `class Offer` |
| `@Table` | Tabellen-Name | `@Table(name = "offers")` |
| `@Column` | Spalten-Config | `@Column(unique = true)` |
| `@Id` | Primary Key | `Long id` |
| `@GeneratedValue` | Auto-Increment | ` GenerationType.IDENTITY` |
| `@ManyToOne` | Beziehung | `@ManyToOne User creator` |
| `@OneToMany` | Beziehung | `@OneToMany List<Pet> pets` |
| `@Transactional` | Transaktion | `public Offer createOffer()` |
| `@Test` | Unit Test | `void testSomething()` |
| `@Mock` | Mock Object | `@Mock UserRepository repo` |
| `@Autowired` | Inject Mock | `@InjectMocks OfferService svc` |
| `@Value` | Config einlesen | `@Value("${app.name}") String name` |

---

## Performance + Best Practices

### N+1 Query Problem (häufiger Bug)

```java
// ❌ SCHLECHT: 1 + N Queries
List<User> users = userRepo.findAll();  // 1 Query
for (User u : users) {
    System.out.println(u.getPets());  // N weitere Queries!
}

// ✅ GUT: Eager Loading
@Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.pets")
List<User> findAllWithPets();

// oder:
@Entity
public class User {
    @OneToMany(fetch = FetchType.EAGER)  // Sofort laden
    private List<Pet> pets;
}
```

### Lazy Loading (Standard)

```java
@Entity
public class User {
    @OneToMany(fetch = FetchType.LAZY)  // Standard, passt meist
    private List<Pet> pets;
    // pets werden nur geladen, wenn u.getPets() aufgerufen wird
}
```

### Paging bei großen Datasets

```java
// ❌ Alle Millionen Rows in Speicher
List<Offer> all = offerRepo.findAll();

// ✅ Pageiniert
Pageable page = PageRequest.of(0, 50);  // Seite 0, 50 Items
Page<Offer> offers = offerRepo.findAll(page);
// then: offers.getContent(), offers.getTotalPages(), etc.
```

---

## Häufige Fehler

| Fehler | Problem | Lösung |
|--------|---------|--------|
| `LazyInitializationException` | Pet-Liste nicht geladen | `@Transactional` oder `EAGER` |
| `OptimisticLockException` | Conflict bei `@Version` | Retry-Logik |
| `DataIntegrityViolationException` | Unique Constraint verletzt | Prüfen vor Save |
| `ConstraintViolationException` | JSR-380 Validierung fehl | `@NotNull` beachten |
| `EntityNotFoundException` | Lazy-Relation nach Session | `@Transactional` |

---

## Links + Ressourcen

- **Spring Boot Doku**: https://spring.io/projects/spring-boot
- **Spring Data JPA**: https://spring.io/projects/spring-data-jpa
- **Spring Security**: https://spring.io/projects/spring-security
- **JPA Spec**: https://jakarta.ee/specifications/persistence/
- **Flyway**: https://flywaydb.org/
- **H2 Database**: http://www.h2database.com/


