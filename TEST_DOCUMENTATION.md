# Testdokumentation

## Ziel

Dieses Dokument beschreibt die Tests im Projekt Pawsitters. Die Projektvorgabe verlangt mindestens 10 Unit-Tests und eine separate Dokumentation, in der beschrieben wird, was getestet wird, welches Ergebnis erwartet wird und ob es sich um einen Normalfall oder Edge Case handelt.

Das Projekt enthält deutlich mehr als 10 Tests. Der aktuelle Testbestand umfasst 36 Testklassen mit 243 `@Test`-Methoden. Neben Unit-Tests gibt es Integrationstests, Smoke-Tests und UI-nahe Komponententests.

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
| Integrationstest | Testet Zusammenspiel mehrerer Komponenten oder Spring-Kontext |
| UI-naher Test | Testet Logik oder Struktur von Vaadin-Komponenten ohne vollständigen Browser-E2E-Test |
| Smoke-Test | Prüft, ob ein grundlegender Kontext oder ein Eventobjekt korrekt funktioniert |

## Übersicht der Testklassen

| Testklasse | Anzahl | Bereich | Art |
|---|---:|---|---|
| `OfferServiceTest` | 52 | Angebotserstellung, Bearbeitung, Suche, Filter, PLZ und Distanz | Unit |
| `UserServiceTest` | 29 | Registrierung, Login, Profil, E-Mail-Änderung, öffentliche Profile | Unit |
| `UserReviewServiceTest` | 14 | Bewertungen und Rating-Summary | Unit |
| `PetServiceTest` | 13 | Haustiere, Tags, Löschfolgen | Unit |
| `BookingServiceTest` | 11 | Anfrage akzeptieren, Buchungen, Storno, Abschluss | Unit |
| `PostalCodeServiceTest` | 11 | PLZ-Validierung, Cache, Koordinaten | Unit |
| `WalletServiceTest` | 10 | Wallet, Treuhand, Auszahlung, wiederkehrende Zahlungen | Unit |
| `ChatServiceTest` | 10 | Konversationen, Nachrichten, Notifications, Review-Karten | Unit |
| `RequestServiceTest` | 8 | Anfragen, Status, Berechtigungen | Unit |
| `FavoriteServiceTest` | 7 | Favoritenlogik | Unit |
| `PasswordPolicyServiceTest` | 6 | Passwortregeln | Unit |
| `DockerComposeStartupGuardTest` | 6 | Docker-Startverhalten | Unit |
| `DatabaseUserDetailsServiceTest` | 5 | UserDetails und Demo-Fallback | Unit |
| `SecurityIntegrationTest` | 5 | Security Filter Chain, Login, Logout, öffentliche Routen | Integration |
| `PasswordResetServiceTest` | 5 | Passwort-Reset-Code und Passwortänderung | Unit |
| `CreateOfferFormRulesTest` | 5 | Angebotsformular-Regeln | Unit |
| `ChatAccessServiceTest` | 4 | Chat-Zugriffskontrolle | Unit |
| `ProfileMenuTest` | 4 | Profilmenü-UI | UI-nah |
| `ImageAssetServiceTest` | 4 | Bildvalidierung und Bildvarianten | Unit |
| `ChatBookingListenerReviewReminderTest` | 3 | Review-Reminder nach Booking-Abschluss | Unit |
| `ChatEventBusTest` | 3 | Chat- und Typing-Events | Unit |
| `ChatViewIntegrationTest` | 3 | ChatView-Integration | Integration |
| `ImageComponentsTest` | 3 | UI-Bildkomponenten | UI-nah |
| `OfferHeroStatisticsCardTest` | 3 | UI-Statistikkarte | UI-nah |
| `PetsitterDetailPopUpTest` | 3 | Detail-Popup | UI-nah |
| `FilterPopUpTest` | 2 | Wiederkehrende Filtersteuerung | UI-nah |
| `BookingCompletedEventTest` | 2 | Eventdaten für Booking-Abschluss | Smoke/Unit |
| `OfferImagePresentationMapperTest` | 2 | Offer-Cover-Bildlogik | Unit |
| `ImageMediaControllerTest` | 2 | Öffentliche Medienroute | Unit |
| `PetsitterFilterViewTest` | 2 | Filter-View und Query-Parameter | UI-nah |
| `BookingCompletionReviewFlowTest` | 1 | Abschluss- und Review-Flow | Integration/Unit-nah |
| `ImageAssetMigrationTest` | 1 | Bildmigration und Constraints | Unit |
| `ExternalPaymentMethodsTest` | 1 | UI-Anzeige externer Zahlungsarten | UI-nah |
| `FilterSearchBarTest` | 1 | Distanzslider | UI-nah |
| `ProfilePopUpTest` | 1 | Profil-Popup mit Bewertungen | UI-nah |
| `PetsitterApplicationTests` | 1 | Spring-Kontext | Smoke-Test |

## Dokumentierte Einzeltests nach Bereich

### Passwortregeln

| Testklasse | Test | Was wird getestet? | Erwartetes Ergebnis | Falltyp |
|---|---|---|---|---|
| `PasswordPolicyServiceTest` | `acceptsStrongPasswordWithoutForbiddenPatterns` | Starkes Passwort ohne verbotene Muster | Passwort ist gültig | Normalfall |
| `PasswordPolicyServiceTest` | `rejectsThreeIdenticalCharactersInARow` | Drei gleiche Zeichen nacheinander | Passwort wird abgelehnt | Edge Case |
| `PasswordPolicyServiceTest` | `rejectsAnyFourConsecutiveDigits` | Vierstellige Zahlenfolgen | Passwort wird abgelehnt | Edge Case |
| `PasswordPolicyServiceTest` | `rejectsAscendingOrDescendingDigitSequencesOfThreeOrMore` | Auf- und absteigende Zahlenfolgen | Passwort wird abgelehnt | Edge Case |
| `PasswordPolicyServiceTest` | `rejectsCuratedWeakTermsAsCaseInsensitiveSubstrings` | Schwache Begriffe unabhängig von Groß-/Kleinschreibung | Passwort wird abgelehnt | Edge Case |
| `PasswordPolicyServiceTest` | `ignoresBlankForbiddenTermsFromResource` | Leere Einträge in der Verbotsliste | Leere Einträge werden ignoriert | Edge Case |

### Benutzer, Registrierung, Login und Passwort-Reset

| Testklasse | Testbereich | Was wird getestet? | Erwartetes Ergebnis | Falltyp |
|---|---|---|---|---|
| `UserServiceTest` | Profil laden und mappen | aktueller und öffentlicher User werden auf DTOs gemappt | DTOs enthalten erwartete und nur erlaubte Felder | Normalfall |
| `UserServiceTest` | Gast-Fallback | kein authentifizierter User | Rückgabe fällt auf `Gast` oder leere Optional-Werte zurück | Edge Case |
| `UserServiceTest` | Profil aktualisieren | Pflichtfelder, Trimming, PLZ und Bildreferenzen | valide Daten werden gespeichert | Normalfall |
| `UserServiceTest` | Profilaktualisierung ablehnen | fehlende Pflichtfelder oder fehlende Authentifizierung | Änderung wird abgelehnt | Edge Case |
| `UserServiceTest` | E-Mail-Änderung | Pending-Mail und Code werden gespeichert | Änderung bleibt bis Codebestätigung offen | Normalfall |
| `UserServiceTest` | doppelte E-Mail | neue E-Mail ist bereits vergeben | Änderung wird abgelehnt | Edge Case |
| `UserServiceTest` | Login | verifizierter User mit richtigem Passwort | Login ist erfolgreich | Normalfall |
| `UserServiceTest` | Login ablehnen | falsches Passwort, unbekannte E-Mail, Pending oder Blocked User | Login wird abgelehnt | Edge Case |
| `UserServiceTest` | Registrierung starten | valide Registrierung | Pending-User wird erzeugt und Code versendet | Normalfall |
| `UserServiceTest` | Registrierung aktualisieren | bestehender Pending-User registriert sich erneut | Pending-Daten und Code werden aktualisiert | Normalfall |
| `UserServiceTest` | Registrierung ablehnen | verifizierte E-Mail, schwaches Passwort, ungültige PLZ | Registrierung wird abgelehnt | Edge Case |
| `UserServiceTest` | Registrierung abschließen | gültiger Code | Account wird `VERIFIED` | Normalfall |
| `UserServiceTest` | Registrierungscode falsch | ungültiger oder abgelaufener Code | Account bleibt pending | Edge Case |
| `UserServiceTest` | Pending-Cleanup | abgelaufene Pending-Accounts | User werden gelöscht und Codes invalidiert | Edge Case |
| `PasswordResetServiceTest` | `requestPasswordResetCreatesCodeForVerifiedUserAndSendsMail` | Reset für verifizierten User | Code wird gespeichert und Mailservice aufgerufen | Normalfall |
| `PasswordResetServiceTest` | `requestPasswordResetDoesNotCreateCodeForUnknownOrPendingUser` | unbekannte oder pending E-Mail | generische Antwort, kein Code | Edge Case |
| `PasswordResetServiceTest` | `completePasswordResetRejectsWrongCodeAndIncrementsAttempts` | falscher Reset-Code | Versuchszähler steigt, Passwort bleibt gleich | Edge Case |
| `PasswordResetServiceTest` | `completePasswordResetRejectsWeakPasswordBeforeChangingHash` | schwaches neues Passwort | Änderung wird vor Hash-Update abgelehnt | Edge Case |
| `PasswordResetServiceTest` | `completePasswordResetUpdatesPasswordAndMarksCodeUsed` | gültiger Code und starkes Passwort | Passwort-Hash wird aktualisiert, Code verbraucht | Normalfall |

### Security und Infrastruktur

| Testklasse | Test | Was wird getestet? | Erwartetes Ergebnis | Falltyp |
|---|---|---|---|---|
| `SecurityIntegrationTest` | `startPageIsPublic` | Startseite ohne Login | Request wird nicht umgeleitet | Normalfall |
| `SecurityIntegrationTest` | `profileImageRouteIsPublic` | öffentliche Medienroute | Bildroute ist ohne Auth erreichbar | Normalfall |
| `SecurityIntegrationTest` | `protectedRouteRedirectsAnonymousUserToLogin` | geschützte Route ohne Login | Redirect auf `/login` | Edge Case |
| `SecurityIntegrationTest` | `demoLoginCreatesAuthenticatedSession` | Demo-Login | SecurityContext wird in Session gespeichert | Normalfall |
| `SecurityIntegrationTest` | `logoutInvalidatesSessionAndRedirectsToLogin` | Logout | Session ist invalidiert und Redirect erfolgt | Normalfall |
| `DatabaseUserDetailsServiceTest` | DB-User laden | verifizierter User aus Repository | Spring Security erhält UserDetails | Normalfall |
| `DatabaseUserDetailsServiceTest` | Demo-Fallback | Demo-User aktiv oder DB-Lookup fehlerhaft | Demo-User kann geladen werden | Normalfall/Demo |
| `DatabaseUserDetailsServiceTest` | Demo deaktiviert oder unbekannter User | kein gültiger User | `UsernameNotFoundException` | Edge Case |
| `DockerComposeStartupGuardTest` | Docker Compose Flag | Compose deaktiviert oder Docker erreichbar | App wird nicht blockiert | Normalfall |
| `DockerComposeStartupGuardTest` | Docker nicht erreichbar | Compose aktiv, Docker fehlt | verständliche Blockiermeldung | Edge Case |
| `PetsitterApplicationTests` | `contextLoads` | Spring-Kontext | Anwendungskontext startet | Smoke-Test |

### Angebote, Suche und Angebotsformular

| Testklasse | Testbereich | Was wird getestet? | Erwartetes Ergebnis | Falltyp |
|---|---|---|---|---|
| `OfferServiceTest` | Formulardaten | Haustiere, Enums und Backend-Regeln | DTO enthält erwartete Optionen | Normalfall |
| `OfferServiceTest` | Preisberechnung | Tagespreise und wiederkehrende Tage | Gesamtpreis wird korrekt berechnet | Normalfall |
| `OfferServiceTest` | Owner-Angebot | Angebot mit einem oder mehreren eigenen Haustieren | Offer wird offen gespeichert | Normalfall |
| `OfferServiceTest` | Sitter-Angebot | Angebot ohne Pet, aber mit Tierartpräferenz | Offer wird korrekt gespeichert | Normalfall |
| `OfferServiceTest` | Authentifizierung fehlt | Angebotserstellung ohne User | Erstellung wird abgelehnt | Edge Case |
| `OfferServiceTest` | Pflichtfelder und Datum | fehlende Felder, ungültige Zeiträume, falsche Kombinationen | fachliche Validierungsfehler | Edge Case |
| `OfferServiceTest` | fremdes oder unbekanntes Haustier | Pet-ID gehört nicht aktuellem User oder existiert nicht | Erstellung wird abgelehnt | Edge Case |
| `OfferServiceTest` | eigene Offers | aktuelle Offers eines Users laden | eigene Offers werden als Karten gemappt | Normalfall |
| `OfferServiceTest` | Bearbeitung | nur eigene offene Offers editierbar | erlaubte Änderungen werden gespeichert | Normalfall |
| `OfferServiceTest` | Bearbeitung ablehnen | fremde, nicht offene oder ungültige Updates | Änderung wird abgelehnt | Edge Case |
| `OfferServiceTest` | Löschen | eigenes offenes Offer | Offer wird gelöscht | Normalfall |
| `OfferServiceTest` | Löschen ablehnen | fremdes, nicht offenes oder anonymer User | Löschung wird abgelehnt | Edge Case |
| `OfferServiceTest` | öffentliche Listen | offene, fremde, nicht abgelaufene Offers | passende Offers werden angezeigt | Normalfall |
| `OfferServiceTest` | Datumssuche | `ANY`, `CONTAINED`, `OVERLAP`, Flex-Fenster | passende Offers bleiben im Ergebnis | Normalfall |
| `OfferServiceTest` | Preisfilter | Mindest- oder Maximalverdienst | Offers werden nach Preis gefiltert | Normalfall |
| `OfferServiceTest` | Zusatzfilter | CareType, Frequency, AnimalType | Filter wirken additiv | Normalfall |
| `OfferServiceTest` | Distanzfilter | PLZ, Koordinaten und maximale Entfernung | nahe Offers werden zuerst sortiert | Normalfall |
| `OfferServiceTest` | unbegrenzte Distanz | Sliderwert unbegrenzt | alle auflösbaren Orte bleiben enthalten | Edge Case |
| `OfferServiceTest` | fehlende oder ungültige Start-PLZ | Suche ohne valide Herkunft | Distanz wird ignoriert oder Validierungsfehler geliefert | Edge Case |
| `OfferServiceTest` | Kartenstandorte | gefilterte Offers mit PLZ-Cache | Map-Locations werden geliefert | Normalfall |
| `CreateOfferFormRulesTest` | Angebotsformular-Regeln | erlaubte Kombinationen aus Frequenz, Datum und Wochentagen | Regeln liefern erwartete Fehler oder Erfolg | Normalfall/Edge Case |
| `OfferImagePresentationMapperTest` | Sitter-Cover | Sitter-Angebot nutzt Profilbild | Cover-Tile enthält Userbild | Normalfall |
| `OfferImagePresentationMapperTest` | Owner-Cover | mehrere Haustiere werden sortiert | primäres Pet zuerst, danach Name | Normalfall |

### Anfragen und Buchungen

| Testklasse | Test | Was wird getestet? | Erwartetes Ergebnis | Falltyp |
|---|---|---|---|---|
| `RequestServiceTest` | `createRequest_savesPendingRequestWithTrimmedMessage` | Anfrageerstellung mit Nachricht | Anfrage wird pending gespeichert, Nachricht getrimmt | Normalfall |
| `RequestServiceTest` | `createRequest_rejectsSelfRequest` | Anfrage auf eigenes Angebot | Zugriff wird verweigert | Edge Case |
| `RequestServiceTest` | `createRequest_rejectsDuplicateRequest` | doppelte Anfrage | `DuplicateRequestException` | Edge Case |
| `RequestServiceTest` | `cancelRequest_setsCancelledStatusForPendingRequest` | Pending-Anfrage abbrechen | Status wird `CANCELLED` | Normalfall |
| `RequestServiceTest` | `cancelRequest_rejectsInvalidStatus` | nicht abbrechbarer Status | Businessregel verletzt | Edge Case |
| `RequestServiceTest` | `findRequestsForOffer_rejectsNonCreator` | fremder User ruft Offer-Anfragen ab | Zugriff wird verweigert | Edge Case |
| `RequestServiceTest` | `findMyRequests_returnsRepositoryResult` | eigene Anfragen laden | Repository-Ergebnis wird geliefert | Normalfall |
| `RequestServiceTest` | `createRequest_throwsNotFoundWhenOfferMissing` | fehlendes Angebot | `NotFoundException` | Edge Case |
| `BookingServiceTest` | Anfrage akzeptieren | Booking wird erstellt, Offer gebucht, Anfrage accepted | konsistenter Booking-Status | Normalfall |
| `BookingServiceTest` | Konkurrenzanfragen | andere Pending-Anfragen | Status wird `DENIED` | Normalfall |
| `BookingServiceTest` | Akzeptieren ablehnen | fremder Creator, Request nicht pending, Offer nicht open, Request fehlt | fachliche Exception | Edge Case |
| `BookingServiceTest` | Storno | Owner oder Sitter storniert vor Start | Booking cancelled, Offer wieder offen, Zahlung erstattet | Normalfall |
| `BookingServiceTest` | Abschluss | Teilnehmer markiert vergangene Buchung als completed | Status `COMPLETED`, Event veröffentlicht | Normalfall |
| `BookingServiceTest` | Abschluss ablehnen | fremder User, falscher Status oder Enddatum nicht vorbei | Abschluss wird verweigert | Edge Case |
| `BookingServiceTest` | wiederkehrende Buchung | Ende, Pause und Zahlungsdelegation | Status und Zahlungslogik werden korrekt aktualisiert | Normalfall |
| `BookingCompletedEventTest` | Eventdaten | Event trägt Booking-ID | Getter liefert erwartete ID | Smoke-Test |
| `BookingCompletionReviewFlowTest` | Abschluss zu Review | abgeschlossene Buchung ermöglicht Review-Flow | Review nach Completion möglich | Integration |

### Chat und Notifications

| Testklasse | Test | Was wird getestet? | Erwartetes Ergebnis | Falltyp |
|---|---|---|---|---|
| `ChatServiceTest` | `createConversationForBooking_createsConversation` | Chatkonversation zu Booking | Conversation wird gespeichert | Normalfall |
| `ChatServiceTest` | `createConversationForBooking_isIdempotent` | mehrfaches Erstellen | nur eine Conversation pro Booking | Edge Case |
| `ChatServiceTest` | `sendMessage_persistsMessage` | Nachricht senden | Nachricht wird gespeichert | Normalfall |
| `ChatServiceTest` | `sendMessage_updatesConversationPreview` | Preview nach Nachricht | Last-Message-Daten werden aktualisiert | Normalfall |
| `ChatServiceTest` | `sendMessage_createsNotification_viaUserFallbackWhenBookingLookupFails` | Notification-Fallback | Notification wird trotzdem erzeugt | Edge Case |
| `ChatServiceTest` | `markConversationAsRead_marksMessagesAndNotifications` | Lesestatus | Nachrichten und Chat-Notifications werden gelesen | Normalfall |
| `ChatServiceTest` | `sendMessage_rejectsEmptyText` | leere Nachricht | Nachricht wird abgelehnt | Edge Case |
| `ChatServiceTest` | `saveReviewNotificationMessage_persistsReviewCardAndPublishesEvent` | Review-Karte | Karte wird gespeichert und Event publiziert | Normalfall |
| `ChatServiceTest` | `postReviewReminderCard_persistsReminderCardAndUpdatesConversation` | Review-Reminder | Systemkarte wird gespeichert und Preview aktualisiert | Normalfall |
| `ChatServiceTest` | `postReviewReminderCard_throwsNotFoundWhenConversationDoesNotExist` | Reminder ohne Conversation | `NotFoundException` | Edge Case |
| `ChatAccessServiceTest` | Owner/Sitter-Zugriff | berechtigte Teilnehmer | Zugriff erlaubt | Normalfall |
| `ChatAccessServiceTest` | fremder oder unbekannter Zugriff | fremder User oder Booking fehlt | Zugriff wird verweigert | Edge Case |
| `ChatEventBusTest` | Empfänger-Events | Chat- und Typing-Events | nur registrierter Empfänger wird benachrichtigt | Normalfall |
| `ChatEventBusTest` | fremde Nutzer | Event für andere User | keine Benachrichtigung | Edge Case |
| `ChatBookingListenerReviewReminderTest` | Conversation vorhanden | BookingCompletedEvent | Reminder wird gepostet | Normalfall |
| `ChatBookingListenerReviewReminderTest` | Conversation fehlt | Event ohne Conversation | Listener überspringt ohne Fehler | Edge Case |
| `ChatBookingListenerReviewReminderTest` | ChatService wirft Fehler | Fehler im Reminder | Fehler wird abgefangen | Edge Case |
| `ChatViewIntegrationTest` | ChatView | Konversationsliste und Nachrichtenbereich | UI lädt und aktualisiert Daten | Integration |

### Bewertungen

| Testklasse | Test | Was wird getestet? | Erwartetes Ergebnis | Falltyp |
|---|---|---|---|---|
| `UserReviewServiceTest` | `submitReview_ownerCanRateSitter_afterCompletedBooking` | Owner bewertet Sitter nach Abschluss | Review wird gespeichert | Normalfall |
| `UserReviewServiceTest` | `submitReview_sendsReviewChatCard_whenConversationExists` | Review mit bestehendem Chat | Review-Karte wird gesendet | Normalfall |
| `UserReviewServiceTest` | `submitReview_doesNotSendReviewChatCard_whenConversationMissing` | Review ohne Chat | Review bleibt gespeichert, Chat-Karte wird übersprungen | Edge Case |
| `UserReviewServiceTest` | `submitReview_failsWhenBookingNotCompleted` | Bewertung vor Abschluss | Bewertung wird abgelehnt | Edge Case |
| `UserReviewServiceTest` | `submitReview_failsWhenReviewerNotBookingParticipant` | fremder User bewertet | Zugriff wird verweigert | Edge Case |
| `UserReviewServiceTest` | `submitReview_failsWhenRatingOutOfRange` | Sterne außerhalb 1 bis 5 | Bewertung wird abgelehnt | Edge Case |
| `UserReviewServiceTest` | `submitReview_failsWhenReviewerAlreadyReviewedBooking` | doppelte Bewertung | Bewertung wird abgelehnt | Edge Case |
| `UserReviewServiceTest` | `submitReview_rejectsTooLongComment` | Kommentar zu lang | Bewertung wird abgelehnt | Edge Case |
| `UserReviewServiceTest` | `getUserRatingSummary_returnsRoundedAverage` | Durchschnittsbewertung | Wert wird auf eine Nachkommastelle gerundet | Normalfall |
| `UserReviewServiceTest` | weitere Summary- und Recent-Review-Tests | leere Reviews und begrenzte Listen | sinnvolle Defaults oder sortierte DTOs | Normalfall/Edge Case |
| `ProfilePopUpTest` | Profil-Popup mit Ratings | dynamische Profilbewertung | UI nutzt Backenddaten statt statischer Werte | UI-nah |

### Wallet und wiederkehrende Zahlungen

| Testklasse | Test | Was wird getestet? | Erwartetes Ergebnis | Falltyp |
|---|---|---|---|---|
| `WalletServiceTest` | `devTopUpCreditsWalletAndCreatesLedgerEntry` | Demo-Aufladung | Guthaben steigt, Ledger-Eintrag entsteht | Normalfall |
| `WalletServiceTest` | `holdForBookingDebitsOwnerAndCreatesEscrowTransaction` | Treuhandreservierung | Owner-Guthaben sinkt, Zahlung `HELD` | Normalfall |
| `WalletServiceTest` | `holdForBookingRejectsInsufficientBalanceWithoutMutation` | zu wenig Guthaben | keine Mutation, Exception | Edge Case |
| `WalletServiceTest` | `holdForBookingRejectsDuplicateReservation` | doppelte Reservierung | Reservierung wird abgelehnt | Edge Case |
| `WalletServiceTest` | `refundForCancelledBookingReturnsHeldAmountToOwner` | Storno | gehaltenes Geld wird erstattet | Normalfall |
| `WalletServiceTest` | `releasePaymentCreditsSitterAndCompletesBooking` | Auszahlung | Sitter erhält Betrag, Zahlung `RELEASED` | Normalfall |
| `WalletServiceTest` | `releasePaymentRejectsReleaseBeforeEndDate` | Auszahlung vor Ende | Auszahlung wird verweigert | Edge Case |
| `WalletServiceTest` | `releasePaymentRejectsReleaseOnEndDate` | Auszahlung am Enddatum | Auszahlung wird verweigert | Edge Case |
| `WalletServiceTest` | `requestReleaseStoresRequestForFinishedBooking` | Sitter fordert Auszahlung an | Status `RELEASE_REQUESTED` | Normalfall |
| `WalletServiceTest` | `releaseExpiredRequestsPaysOutSevenDaysAfterSitterRequest` | automatische Freigabe | Zahlung wird nach Frist ausgezahlt | Normalfall |

### Haustiere, Favoriten und Bilder

| Testklasse | Testbereich | Was wird getestet? | Erwartetes Ergebnis | Falltyp |
|---|---|---|---|---|
| `PetServiceTest` | Haustiere laden und zusammenfassen | Haustiere eines Owners, Species-Zählung, leere Liste | erwartete Liste oder Text | Normalfall/Edge Case |
| `PetServiceTest` | Haustier erstellen und aktualisieren | Defaults, Impfstatus, Tags | Werte werden gespeichert | Normalfall |
| `PetServiceTest` | Löschfolgen | betroffene Offers werden analysiert | korrekte Beschreibung der Folgen | Normalfall |
| `PetServiceTest` | Haustier löschen | ohne Offer, Einzeloffer, Mehrtieroffer | Pet wird gelöscht oder aus Offer entfernt | Normalfall/Edge Case |
| `FavoriteServiceTest` | Favorit toggeln | fremdes verfügbares Offer | Favorit wird gesetzt oder entfernt | Normalfall |
| `FavoriteServiceTest` | Favorit ablehnen | unbekanntes, eigenes, nicht offenes oder abgelaufenes Offer | Aktion wird verweigert | Edge Case |
| `FavoriteServiceTest` | Favoriten laden | aktuelle User-Favoriten | nur verfügbare fremde Offers werden geliefert | Normalfall |
| `ImageAssetServiceTest` | `storesOptimizedJpegVariantsForPngUpload` | PNG-Upload | JPEG-Varianten `AVATAR` und `DISPLAY` werden gespeichert | Normalfall |
| `ImageAssetServiceTest` | `rejectsUnsupportedMimeTypeOversizedAndInvalidContent` | falscher Typ, zu groß, kaputter Inhalt | Upload wird abgelehnt | Edge Case |
| `ImageAssetServiceTest` | `replacingProfileImageDeletesOldAssetBeforeSavingNewOne` | Profilbild ersetzen | altes Asset wird entfernt | Normalfall |
| `ImageAssetServiceTest` | `refusesImageUpdateForPetOwnedByAnotherUser` | fremdes Haustierbild | Zugriff wird verweigert | Edge Case |
| `ImageAssetMigrationTest` | Migration | Ownership, Uniqueness, Cascade | SQL enthält erwartete Constraints | Unit |
| `ImageMediaControllerTest` | Medienauslieferung | öffentliche Bildantwort | Inhalt, MIME und `nosniff` passen | Normalfall |
| `ImageMediaControllerTest` | unbekanntes Asset | fehlendes Bild oder Variante | `404` | Edge Case |

### PLZ, Distanz und UI-nahe Komponenten

| Testklasse | Testbereich | Was wird getestet? | Erwartetes Ergebnis | Falltyp |
|---|---|---|---|---|
| `PostalCodeServiceTest` | Formatvalidierung | ungültige deutsche PLZ | kein API-Aufruf, Fehler | Edge Case |
| `PostalCodeServiceTest` | API und Cache | gültige PLZ wird gefunden | Location wird gespeichert und akzeptiert | Normalfall |
| `PostalCodeServiceTest` | unbekannte PLZ | API liefert nichts | PLZ wird abgelehnt | Edge Case |
| `PostalCodeServiceTest` | Stadtvergleich | Umlaute und Transliteration | passende Städte werden akzeptiert | Normalfall |
| `PostalCodeServiceTest` | Stadt passt nicht | PLZ-Ort und Eingabe widersprechen sich | Validierung schlägt fehl | Edge Case |
| `PostalCodeServiceTest` | Cache-Nutzung | vorhandene plausible Koordinaten | kein API-Aufruf | Normalfall |
| `PostalCodeServiceTest` | unplausible Koordinaten | Cacheeintrag außerhalb Deutschland | Cache wird ignoriert oder aktualisiert | Edge Case |
| `PostalCodeServiceTest` | API-Ausfall | kein Cache und Clientfehler | fachlicher Fehler | Edge Case |
| `FilterSearchBarTest` | Distanzslider | Wert über 100 km | unbegrenzte Distanz wird angeboten | UI-nah |
| `FilterPopUpTest` | wiederkehrende Filter | Frequency `REGULAR` | Recurring-Controls werden sichtbar | UI-nah |
| `PetsitterFilterViewTest` | Query-Parameter | unbegrenzte Distanz bleibt erhalten | Roundtrip funktioniert | UI-nah |
| `OfferHeroStatisticsCardTest` | Statistiktext | Singular, Plural, Ratingformat | deutsche Texte und Zahlen stimmen | UI-nah |
| `ProfileMenuTest` | Profilmenü | Anzeige, Initialen, Links, Logout-Zustand | erwartete UI-Elemente | UI-nah |
| `ImageComponentsTest` | Bildkomponenten | Avatar- und Cover-Darstellung | Fallback und URLs stimmen | UI-nah |
| `PetsitterDetailPopUpTest` | Detailpopup | Angebotsdetails, Ratings, Bilder | UI zeigt erwartete Informationen | UI-nah |
| `ExternalPaymentMethodsTest` | Zahlungsarten-UI | externe Methoden | UI rendert bekannte Optionen | UI-nah |

## Abdeckung der Pflichtanforderung

Die Pflichtanforderung von mindestens 10 Unit-Tests ist erfüllt. Bereits diese 10 Unit-Tests reichen für die Mindestanforderung:

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

Das Projekt enthält darüber hinaus viele weitere Unit-, Edge-Case-, Integrations- und UI-nahe Tests.

## Teststrategie

### 1. Geschäftsregeln in Services testen

Zentrale Regeln liegen im Service Layer und werden dort gezielt getestet:

- Registrierung erzeugt Pending-User,
- Login blockiert falsche Zugangsdaten,
- Passwort-Reset ändert nur mit gültigem Code,
- Buchung entsteht nur aus gültiger Pending-Anfrage,
- fremde Nutzer dürfen keine fremden Anfragen oder Chats nutzen,
- Wallet-Zahlungen dürfen nur mit genügend Guthaben und richtiger Rolle ausgelöst werden,
- Reviews sind erst nach Abschluss und nur für Teilnehmer erlaubt.

### 2. Edge Cases explizit testen

Viele Tests prüfen bewusst Fehlerfälle:

- schwache Passwörter,
- falsche Codes,
- Pending- oder Blocked-User,
- ungültige PLZ,
- API-Ausfall,
- Anfrage auf eigenes Angebot,
- doppelte Anfrage,
- fremdes Haustier,
- nicht offene Offers,
- fehlendes Guthaben,
- Auszahlung zu früh,
- fremder Chat-Zugriff,
- leere Chatnachricht,
- zu lange Review-Kommentare,
- falsche Bildtypen.

### 3. Integration und UI-nahe Tests ergänzen Unit-Tests

Security-Integrationstests prüfen die Filter Chain. UI-nahe Tests prüfen Vaadin-Komponentenlogik ohne vollständigen Browser-E2E-Test. Dadurch bleiben Tests schnell, decken aber trotzdem wichtige Darstellung und Zustandslogik ab.

### 4. CI als automatischer Qualitätscheck

Die CI führt Maven Verify aus. Dadurch werden Build und Tests bei Pushes und Pull Requests automatisch geprüft.

## Bekannte Grenzen

| Grenze | Bewertung |
|---|---|
| Keine vollständigen Browser-End-to-End-Tests | Für Uni-Projekt akzeptabel, später mit Playwright oder Selenium ergänzbar |
| Keine dokumentierte Coverage-Messung | Coverage-Tool wäre sinnvolle Erweiterung |
| Keine automatisierten Security-Scans | Dependency-Check, Secret Scanning und SAST wären sinnvoll |
| UI-Tests sind komponentennah | Für Vaadin-Demo ausreichend, echte Browser-Flows wären produktionsnäher |
| MongoDB wird in vielen Tests gemockt oder isoliert | Schnell und stabil, aber weniger echte Persistenzabdeckung |

## Fazit

Die Testbasis deckt zentrale Geschäftslogik und viele Edge Cases ab. Die Mindestanforderung von 10 Unit-Tests ist deutlich erfüllt. Besonders stark abgedeckt sind User- und Offer-Logik, Passwortregeln, Chat, Wallet, Reviews, PLZ-Suche, Bilder, Buchungen und Security-Grundverhalten. Integrationstests und UI-nahe Tests ergänzen die Unit-Tests und erhöhen die Aussagekraft für Demo und Abgabe.
