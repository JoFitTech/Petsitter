# Testdokumentation

## Ziel

Dieses Dokument beschreibt die Tests im Projekt Pawsitters. Die Projektvorgabe verlangt mindestens 10 Unit-Tests und eine separate Dokumentation, in der pro Test beschrieben wird, was getestet wird, welches Ergebnis erwartet wird und ob es sich um einen Normalfall oder Edge Case handelt.

Das Projekt enthält deutlich mehr als 10 Tests. Neben Unit-Tests gibt es auch Integrationstests und UI-nahe Tests.

## Testausführung

Alle Tests ausführen:

```bash
./mvnw test
```

Vollständige Maven-Verifikation wie in der CI:

```bash
./mvnw -B verify -Dspring.docker.compose.enabled=false
```

Einzelne Testklasse ausführen:

```bash
./mvnw -Dtest="UserServiceTest" test
```

## Testarten

| Art | Beschreibung |
|---|---|
| Unit-Test | Testet einzelne Services oder Hilfsklassen isoliert mit Fakes oder Mocks |
| Integrationstest | Testet das Zusammenspiel mehrerer Komponenten oder Spring-Kontext |
| UI-naher Test | Testet Logik oder Verhalten von Vaadin-Komponenten ohne vollständigen Browser-E2E-Test |

## Übersicht der Testklassen

| Testklasse | Bereich | Art |
|---|---|---|
| `PasswordPolicyServiceTest` | Passwortregeln | Unit |
| `UserServiceTest` | Registrierung, Login, Profil, E-Mail-Änderung | Unit |
| `OfferServiceTest` | Angebotserstellung, Angebotsdaten, Suchlogik | Unit |
| `CreateOfferFormRulesTest` | Regeln des Angebotformulars | Unit |
| `RequestServiceTest` | Anfragen erstellen, abbrechen, Berechtigungen | Unit |
| `BookingServiceTest` | Buchung erstellen, Anfrage akzeptieren, Statuslogik | Unit |
| `PetServiceTest` | Haustiere, Haustierlöschung, Angebotsfolgen | Unit |
| `FavoriteServiceTest` | Favoritenlogik | Unit |
| `WalletServiceTest` | Wallet-Logik | Unit |
| `ChatServiceTest` | Chatkonversationen und Nachrichten | Unit |
| `ChatAccessServiceTest` | Chat-Zugriffskontrolle | Unit |
| `ChatEventBusTest` | Chat-Events | Unit |
| `ChatViewIntegrationTest` | Chat-View-Integration | Integration |
| `SecurityIntegrationTest` | Zugriffsschutz und Security-Kontext | Integration |
| `DatabaseUserDetailsServiceTest` | Laden von UserDetails | Unit |
| `PasswordResetServiceTest` | Passwort-Reset | Unit |
| `PostalCodeServiceTest` | PLZ-Validierung und Ortsdaten | Unit |
| `PetsitterApplicationTests` | Spring-Kontext | Smoke-Test |
| `DockerComposeStartupGuardTest` | Test-/CI-Konfiguration | Unit |
| `ProfileMenuTest` | UI-Komponentenlogik | UI-nah |
| `PetsitterFilterViewTest` | UI-Filterlogik | UI-nah |

## Dokumentierte Einzeltests

### Passwortregeln

| Testklasse | Test | Was wird getestet? | Erwartetes Ergebnis | Falltyp |
|---|---|---|---|---|
| `PasswordPolicyServiceTest` | `acceptsStrongPasswordWithoutForbiddenPatterns` | Starkes Passwort ohne verbotene Muster | Passwort ist gültig | Normalfall |
| `PasswordPolicyServiceTest` | `rejectsThreeIdenticalCharactersInARow` | Drei gleiche Zeichen nacheinander | Passwort wird abgelehnt | Edge Case |
| `PasswordPolicyServiceTest` | `rejectsAnyFourConsecutiveDigits` | Vierstellige Zahlenfolgen | Passwort wird abgelehnt | Edge Case |
| `PasswordPolicyServiceTest` | `rejectsAscendingOrDescendingDigitSequencesOfThreeOrMore` | Auf- und absteigende Zahlenfolgen | Passwort wird abgelehnt | Edge Case |
| `PasswordPolicyServiceTest` | `rejectsCuratedWeakTermsAsCaseInsensitiveSubstrings` | Schwache Wörter unabhängig von Groß-/Kleinschreibung | Passwort wird abgelehnt | Edge Case |
| `PasswordPolicyServiceTest` | `ignoresBlankForbiddenTermsFromResource` | Leere Einträge in der Liste verbotener Wörter | Leere Einträge werden ignoriert | Edge Case |

### Benutzer, Registrierung und Login

| Testklasse | Test | Was wird getestet? | Erwartetes Ergebnis | Falltyp |
|---|---|---|---|---|
| `UserServiceTest` | `findUserByIdLoadsUserFromRepository` | Laden eines Users über Repository | User wird gefunden | Normalfall |
| `UserServiceTest` | `getCurrentUserProfileMapsAuthenticatedUserToDto` | Mapping des angemeldeten Users auf Profil-DTO | DTO enthält erwartete Felder | Normalfall |
| `UserServiceTest` | `getPublicUserProfileMapsOnlyPublicFieldsForVerifiedUser` | Öffentliches Profil | Nur öffentliche Felder werden geliefert | Normalfall |
| `UserServiceTest` | `getPublicUserProfileReturnsEmptyForMissingOrUnverifiedUser` | Fehlender oder unverifizierter User | Kein öffentliches Profil | Edge Case |
| `UserServiceTest` | `getCurrentUserUsesAuthenticatedUsersFullName` | Anzeigename des angemeldeten Users | Vollständiger Name wird zurückgegeben | Normalfall |
| `UserServiceTest` | `getCurrentUserFallsBackToGuestWhenNoUserIsAuthenticated` | Kein eingeloggter User | Rückfall auf `Gast` | Edge Case |
| `UserServiceTest` | `updateCurrentUserProfileSavesTrimmedProfileDataForAuthenticatedUser` | Profilaktualisierung mit Leerzeichen | Werte werden getrimmt gespeichert | Normalfall |
| `UserServiceTest` | `updateCurrentUserProfileRejectsMissingRequiredFieldsAndMissingAuthentication` | Pflichtfelder und fehlende Authentifizierung | Aktualisierung wird abgelehnt | Edge Case |
| `UserServiceTest` | `requestCurrentUserEmailChangeStoresPendingEmailAndSendsCode` | E-Mail-Änderung | Pending-Mail wird gespeichert und Code gesendet | Normalfall |
| `UserServiceTest` | `requestCurrentUserEmailChangeRejectsDuplicateEmail` | E-Mail bereits vergeben | Änderung wird abgelehnt | Edge Case |
| `UserServiceTest` | `confirmCurrentUserEmailChangeValidatesCodeAndUpdatesLoginEmail` | E-Mail-Code ist gültig | Login-E-Mail wird aktualisiert | Normalfall |
| `UserServiceTest` | `confirmCurrentUserEmailChangeRejectsInvalidCode` | Ungültiger E-Mail-Code | Änderung bleibt offen | Edge Case |
| `UserServiceTest` | `loginSucceedsForVerifiedUserWithPassword` | Login mit gültigem Passwort | Login erfolgreich | Normalfall |
| `UserServiceTest` | `loginRejectsWrongPasswordOrUnknownEmail` | Falsches Passwort oder unbekannte E-Mail | Login wird abgelehnt | Edge Case |
| `UserServiceTest` | `loginRejectsPendingAndBlockedUsers` | Pending- oder gesperrter Account | Login wird abgelehnt | Edge Case |
| `UserServiceTest` | `startRegistrationCreatesPendingUserAndSendsCode` | Neue Registrierung | Pending-User wird erzeugt und Code gesendet | Normalfall |
| `UserServiceTest` | `startRegistrationRejectsVerifiedUser` | E-Mail existiert bereits verifiziert | Registrierung wird abgelehnt | Edge Case |
| `UserServiceTest` | `startRegistrationUpdatesPendingUserAndSendsNewCode` | Bestehende offene Registrierung | Pending-User wird aktualisiert, neuer Code wird gesendet | Normalfall |
| `UserServiceTest` | `startRegistrationRejectsPasswordShorterThan14Characters` | Passwort zu kurz | Registrierung wird abgelehnt | Edge Case |
| `UserServiceTest` | `startRegistrationRejectsPasswordWithoutUppercaseLetter` | Kein Großbuchstabe | Registrierung wird abgelehnt | Edge Case |
| `UserServiceTest` | `startRegistrationRejectsPasswordWithoutLowercaseLetter` | Kein Kleinbuchstabe | Registrierung wird abgelehnt | Edge Case |
| `UserServiceTest` | `startRegistrationRejectsPasswordWithoutDigit` | Keine Zahl | Registrierung wird abgelehnt | Edge Case |
| `UserServiceTest` | `startRegistrationRejectsPasswordWithoutSpecialCharacter` | Kein Sonderzeichen | Registrierung wird abgelehnt | Edge Case |
| `UserServiceTest` | `startRegistrationRejectsPasswordPatternFromPolicy` | Verbotener Begriff im Passwort | Registrierung wird abgelehnt | Edge Case |
| `UserServiceTest` | `startRegistrationRejectsInvalidPostalCode` | Ungültige PLZ | Registrierung wird abgelehnt | Edge Case |
| `UserServiceTest` | `completeRegistrationVerifiesPendingUser` | Gültiger Registrierungscode | Account wird verifiziert | Normalfall |
| `UserServiceTest` | `completeRegistrationRejectsInvalidCode` | Ungültiger Registrierungscode | Account bleibt pending | Edge Case |
| `UserServiceTest` | `cleanupExpiredPendingUsersDeletesExpiredPendingUsersAndInvalidatesCodes` | Abgelaufene Pending-User | User werden gelöscht, Codes invalidiert | Edge Case |

### Angebote

| Testklasse | Test | Was wird getestet? | Erwartetes Ergebnis | Falltyp |
|---|---|---|---|---|
| `OfferServiceTest` | `findCurrentUserPetOptionsLoadsCurrentUsersPetsFromRepository` | Haustieroptionen des aktuellen Users | Eigene Haustiere werden geladen | Normalfall |
| `OfferServiceTest` | `getCreateOfferFormDataLoadsPetsAndBackendFormRules` | Formulardaten für Angebotserstellung | Enums, Haustiere und Regeln werden geliefert | Normalfall |
| `OfferServiceTest` | `summarizeCreateOfferTotalPriceUsesBackendFormRules` | Gesamtpreisberechnung | Erwarteter Preis wird berechnet | Normalfall |
| `OfferServiceTest` | `createOfferSavesOpenOfferWithCurrentUserAndSelectedPet` | Angebot mit ausgewähltem Haustier | Angebot wird offen gespeichert | Normalfall |
| `OfferServiceTest` | `createOfferSavesAllSelectedOwnerPets` | Angebot mit mehreren Haustieren | Alle gewählten Haustiere werden übernommen | Normalfall |
| `OfferServiceTest` | `createOfferAcceptsSelectedPetOptionWithoutFrontendPetIdMapping` | Fallback bei Pet-Auswahl | Angebot wird korrekt erstellt | Edge Case |

### Anfragen

| Testklasse | Test | Was wird getestet? | Erwartetes Ergebnis | Falltyp |
|---|---|---|---|---|
| `RequestServiceTest` | `createRequest_savesPendingRequestWithTrimmedMessage` | Anfrageerstellung mit Nachricht | Anfrage wird pending gespeichert, Nachricht getrimmt | Normalfall |
| `RequestServiceTest` | `createRequest_rejectsSelfRequest` | Anfrage auf eigenes Angebot | Zugriff wird verweigert | Edge Case |
| `RequestServiceTest` | `createRequest_rejectsDuplicateRequest` | Doppelte Anfrage | DuplicateRequestException | Edge Case |
| `RequestServiceTest` | `cancelRequest_setsCancelledStatusForPendingRequest` | Pending-Anfrage abbrechen | Status wird `CANCELLED` | Normalfall |
| `RequestServiceTest` | `cancelRequest_rejectsInvalidStatus` | Nicht abbrechbarer Status | BusinessRuleViolationException | Edge Case |
| `RequestServiceTest` | `findRequestsForOffer_rejectsNonCreator` | Fremder User ruft Anfragen ab | Zugriff wird verweigert | Edge Case |
| `RequestServiceTest` | `findMyRequests_returnsRepositoryResult` | Eigene Anfragen laden | Repository-Ergebnis wird zurückgegeben | Normalfall |
| `RequestServiceTest` | `createRequest_throwsNotFoundWhenOfferMissing` | Angebot existiert nicht | NotFoundException | Edge Case |

### Buchungen

| Testklasse | Test | Was wird getestet? | Erwartetes Ergebnis | Falltyp |
|---|---|---|---|---|
| `BookingServiceTest` | `acceptRequest_createsBookingWithCorrectDataAndSetsOfferBooked` | Anfrage akzeptieren | Booking wird erstellt, Angebot gebucht, Anfrage accepted | Normalfall |
| `BookingServiceTest` | `acceptRequest_deniesAllOtherPendingRequests` | Konkurrenzanfragen | Andere Pending-Anfragen werden denied | Normalfall |
| `BookingServiceTest` | `acceptRequest_failsWhenNotOfferCreator` | Fremder User akzeptiert Anfrage | ForbiddenOperationException | Edge Case |
| `BookingServiceTest` | `acceptRequest_failsWhenRequestNotPending` | Anfrage nicht pending | BusinessRuleViolationException | Edge Case |
| `BookingServiceTest` | `acceptRequest_failsWhenOfferNotOpen` | Angebot nicht offen | BusinessRuleViolationException | Edge Case |
| `BookingServiceTest` | `acceptRequest_failsWhenRequestNotFound` | Anfrage existiert nicht | NotFoundException | Edge Case |
| `BookingServiceTest` | `cancelBooking_setsStatusCancelled` | Buchung stornieren | Status wird `CANCELLED` | Normalfall |

### Haustiere

| Testklasse | Test | Was wird getestet? | Erwartetes Ergebnis | Falltyp |
|---|---|---|---|---|
| `PetServiceTest` | `getPetsForOwnerLoadsPetsFromRepository` | Haustiere eines Owners laden | Haustiere werden geladen | Normalfall |
| `PetServiceTest` | `getPetSummaryForOwnerFormatsSpeciesCounts` | Zusammenfassung nach Tierart | Text wird korrekt formatiert | Normalfall |
| `PetServiceTest` | `getPetSummaryForOwnerHandlesEmptyPets` | Keine Haustiere vorhanden | Rückgabe `Keine Haustiere` | Edge Case |
| `PetServiceTest` | `createPetForCurrentUserDefaultsUnknownVaccinationStatusAndEmptyTags` | Haustier ohne optionale Werte | Defaults werden gesetzt | Edge Case |
| `PetServiceTest` | `updatePetStoresVaccinationStatusAndTags` | Haustier aktualisieren | Impfstatus und Tags werden gespeichert | Normalfall |
| `PetServiceTest` | `analyzeCurrentUserPetDeletionDescribesAffectedOffers` | Löschfolgen für Angebote | Betroffene Angebote werden beschrieben | Normalfall |
| `PetServiceTest` | `deleteCurrentUserPetDeletesPetWithoutOffers` | Haustier ohne Angebote löschen | Haustier wird gelöscht | Normalfall |
| `PetServiceTest` | `deleteCurrentUserPetDeletesSinglePetOfferAndPet` | Haustier mit Einzelangebot löschen | Angebot und Haustier werden gelöscht | Normalfall |
| `PetServiceTest` | `deleteCurrentUserPetCanRemovePetFromMultiPetOffer` | Haustier aus Mehrtier-Angebot entfernen | Haustier wird aus Angebot entfernt | Edge Case |

### Chat

| Testklasse | Test | Was wird getestet? | Erwartetes Ergebnis | Falltyp |
|---|---|---|---|---|
| `ChatServiceTest` | `createConversationForBooking_createsConversation` | Chatkonversation zu Booking erstellen | Conversation wird gespeichert | Normalfall |
| `ChatServiceTest` | `createConversationForBooking_isIdempotent` | Mehrfaches Erstellen | Nur eine Conversation pro Booking | Edge Case |
| `ChatServiceTest` | `sendMessage_persistsMessage` | Nachricht senden | Nachricht wird gespeichert | Normalfall |
| `ChatServiceTest` | `sendMessage_updatesConversationPreview` | Preview nach Nachricht | Conversation-Preview wird aktualisiert | Normalfall |
| `ChatServiceTest` | `sendMessage_createsNotification` | Nachricht erzeugt Notification | Notification wird erstellt | Normalfall |
| `ChatServiceTest` | `markConversationAsRead_marksMessagesAndNotifications` | Lesestatus | Nachrichten und Notifications werden gelesen markiert | Normalfall |
| `ChatServiceTest` | `sendMessage_rejectsEmptyText` | Leere Nachricht | Nachricht wird abgelehnt | Edge Case |
| `ChatServiceTest` | `sendMessage_createsNotification_viaUserFallbackWhenBookingLookupFails` | Fallback bei Booking-Lookup | Notification wird trotzdem erzeugt | Edge Case |
| `ChatAccessServiceTest` | `verifyAccess_allowsOwnerOrSitter` | Owner/Sitter-Zugriff | Zugriff erlaubt | Normalfall |
| `ChatAccessServiceTest` | `verifyAccess_rejectsForeignUser` | Fremder Nutzer | Zugriff verweigert | Edge Case |
| `ChatAccessServiceTest` | `verifyBookingAccess_allowsOwnerOrSitter` | Booking-Zugriff Owner/Sitter | Zugriff erlaubt | Normalfall |
| `ChatAccessServiceTest` | `verifyBookingAccess_rejectsUnknownBooking` | Unbekannte Buchung | NotFoundException | Edge Case |
| `ChatEventBusTest` | `chatEventBus_notifiesRecipient` | Event an Empfänger | Empfänger wird benachrichtigt | Normalfall |
| `ChatEventBusTest` | `chatEventBus_doesNotNotifyOtherUsers` | Fremde Nutzer | Keine fremde Benachrichtigung | Edge Case |
| `ChatViewIntegrationTest` | `chatView_loadsConversationList` | ChatView lädt Konversationen | Liste wird angezeigt | Integration |
| `ChatViewIntegrationTest` | `chatView_updatesMessageAreaForActiveConversation` | Auswahl einer Konversation | Nachrichtenbereich wird aktualisiert | Integration |

### Security und Infrastruktur

| Testklasse | Testbereich | Was wird getestet? | Erwartetes Ergebnis | Falltyp |
|---|---|---|---|---|
| `SecurityIntegrationTest` | Security-Kontext und Zugriffsschutz | Routen und Authentifizierung | Geschützte Bereiche erfordern Login | Integration |
| `DatabaseUserDetailsServiceTest` | UserDetailsService | Laden von Security-Usern aus DB | Spring Security erhält korrekte UserDetails | Unit |
| `PasswordResetServiceTest` | Passwort-Reset | Reset-Code, Passwortänderung, Edge Cases | Passwort-Reset funktioniert kontrolliert | Unit |
| `PostalCodeServiceTest` | Postleitzahlen | PLZ-Validierung und Ortsdaten | gültige PLZ wird akzeptiert, ungültige abgelehnt | Unit |
| `DockerComposeStartupGuardTest` | CI/Testkonfiguration | Docker Compose wird in Tests/CI kontrolliert | Tests laufen unabhängig von lokalen Containern | Unit |
| `PetsitterApplicationTests` | Spring-Kontext | Anwendungskontext startet | Smoke-Test erfolgreich | Smoke-Test |

## Abdeckung der Pflichtanforderung

Die Pflichtanforderung von mindestens 10 Unit-Tests ist erfüllt. Bereits die folgenden 10 Unit-Tests decken die Pflichtanforderung ab:

1. `PasswordPolicyServiceTest.acceptsStrongPasswordWithoutForbiddenPatterns`
2. `PasswordPolicyServiceTest.rejectsThreeIdenticalCharactersInARow`
3. `UserServiceTest.loginSucceedsForVerifiedUserWithPassword`
4. `UserServiceTest.loginRejectsWrongPasswordOrUnknownEmail`
5. `UserServiceTest.startRegistrationCreatesPendingUserAndSendsCode`
6. `OfferServiceTest.createOfferSavesOpenOfferWithCurrentUserAndSelectedPet`
7. `RequestServiceTest.createRequest_savesPendingRequestWithTrimmedMessage`
8. `RequestServiceTest.createRequest_rejectsDuplicateRequest`
9. `BookingServiceTest.acceptRequest_createsBookingWithCorrectDataAndSetsOfferBooked`
10. `BookingServiceTest.acceptRequest_failsWhenNotOfferCreator`

Das Projekt enthält darüber hinaus weitere Unit-Tests, Edge-Case-Tests und Integrationstests.

## Teststrategie

Die Teststrategie folgt drei Prinzipien:

### 1. Geschäftsregeln in Services testen

Zentrale Regeln liegen im Service Layer und werden dort gezielt getestet. Beispiele:

- Registrierung erzeugt Pending-User,
- Login blockiert falsche Zugangsdaten,
- Buchung entsteht nur aus gültiger Pending-Anfrage,
- fremde Nutzer dürfen keine fremden Anfragen akzeptieren,
- doppelte Anfragen werden verhindert.

### 2. Edge Cases explizit testen

Viele Tests prüfen bewusst Fehlerfälle:

- falsches Passwort,
- unbekannte E-Mail,
- Pending- oder Blocked-User,
- ungültiger Registrierungscode,
- ungültige PLZ,
- Anfrage auf eigenes Angebot,
- doppelte Anfrage,
- nicht offene Angebote,
- fremder Chat-Zugriff,
- leere Chatnachricht.

### 3. CI als automatischer Qualitätscheck

Die CI führt Maven Verify aus. Dadurch werden Build und Tests bei Pushes und Pull Requests automatisch geprüft.

## Bekannte Grenzen

| Grenze | Bewertung |
|---|---|
| Keine vollständigen Browser-End-to-End-Tests | Für Uni-Projekt akzeptabel, später mit Playwright oder Selenium ergänzbar |
| Keine vollständige Testabdeckungsmessung dokumentiert | Coverage-Tool wäre mögliche Erweiterung |
| Security-Scans nicht als Tests integriert | Dependency-Check und Secret Scanning wären sinnvolle Ergänzungen |
| Einige UI-Tests sind komponentennah statt echte Browsertests | Für Vaadin-Demo ausreichend |

## Fazit

Die Testbasis deckt die zentrale Geschäftslogik und mehrere Edge Cases ab. Die Mindestanforderung von 10 Unit-Tests ist erfüllt. Integrationstests und UI-nahe Tests ergänzen die Unit-Tests und erhöhen die Aussagekraft für Demo und Abgabe.
