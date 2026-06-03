# Plan: Dynamische Ratings & Reviews

## Änderungen

### 1. PublicUserProfileDto erweitern
- Füge `UserRatingSummary` und `List<UserReviewDto>` hinzu
- Neue Eigenschaften: `averageRating`, `ratingCount`, `recentReviews`

### 2. UserService (oder UserController) anpassen
- In Methode die `PublicUserProfileDto` erzeugt:
  - `UserReviewService.getUserRatingSummary(userId)` aufrufen
  - `UserReviewService.getRecentReviews(userId, 3)` aufrufen
  - In DTO einfügen

### 3. ProfilePopUp anpassen
- Statt hardcoded Stars → aus `averageRating` verwenden
- Statt 2 Dummy-Reviews → aus `recentReviews` Liste iterieren
- Falls keine Reviews: "Keine Bewertungen vorhanden" anzeigen

### 4. OfferCard / OfferTile anpassen (falls existiert)
- Ähnlich zu ProfilePopUp
- Stars basieren auf `averageRating`
- Optional: Kurze Review-Preview zeigen

## Betroffene Klassen

- `PublicUserProfileDto.java` → Neue Felder
- `UserService.java` → Abhängigkeit von `UserReviewService`
- `ProfilePopUp.java` → Dynamisches Rendering
- `OfferCard.java` oder `OfferTile.java` → Dynamische Stars

