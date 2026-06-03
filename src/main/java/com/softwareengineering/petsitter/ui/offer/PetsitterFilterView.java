package com.softwareengineering.petsitter.ui.offer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwareengineering.petsitter.favorite.service.FavoriteService;
import com.softwareengineering.petsitter.location.dto.PostalCodeMapLocation;
import com.softwareengineering.petsitter.offer.domain.OfferAnimalType;
import com.softwareengineering.petsitter.offer.domain.OfferCareType;
import com.softwareengineering.petsitter.offer.domain.OfferDateFilterMode;
import com.softwareengineering.petsitter.offer.domain.OfferFrequency;
import com.softwareengineering.petsitter.offer.domain.OfferSearchMode;
import com.softwareengineering.petsitter.offer.domain.OfferTimeSlot;
import com.softwareengineering.petsitter.offer.dto.OfferCardDto;
import com.softwareengineering.petsitter.offer.dto.OfferMapLocation;
import com.softwareengineering.petsitter.offer.dto.OfferSearchCriteria;
import com.softwareengineering.petsitter.booking.service.BookingService;
import com.softwareengineering.petsitter.chat.service.ChatService;
import com.softwareengineering.petsitter.offer.service.OfferService;
import com.softwareengineering.petsitter.offerrequest.service.RequestService;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.ui.shared.FilterPopUp;
import com.softwareengineering.petsitter.ui.shared.FilterSearchBar;
import com.softwareengineering.petsitter.ui.shared.MainLayout;
import com.softwareengineering.petsitter.ui.shared.OfferCardComponent;
import com.softwareengineering.petsitter.ui.shared.PetsitterDetailPopUp;
import com.softwareengineering.petsitter.user.service.UserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Route(value = "petsitter-suche", layout = MainLayout.class)
@PageTitle("Suche | Pawsitter")
@PermitAll
@JsModule("./maps/pawsitter-map.ts")
public class PetsitterFilterView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PetsitterFilterView.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    private static final String DARK       = "#4a3428";
    private static final String CREAM      = "#fbf8f1";
    private static final String ACCENT     = "#7b5236";
    private static final String CARD_BG    = "#ffffff";

    private final OfferService offerService;
    private final FavoriteService favoriteService;
    private final RequestService requestService;
    private final ChatService chatService;
    private final AuthenticatedUser authenticatedUser;
    private final UserService userService;
    private final BookingService bookingService;

    private H1 pageTitle;
    private Div leftBlob;
    private Div rightBlob;
    private H2 resultsLabel;
    private Div filterBarContainer;
    private Div offerGrid;
    private Div mapContainer;
    private Span mapLocationLabel;
    private Span mapStatusLabel;
    private OfferSearchCriteria currentCriteria;

    @Autowired
    public PetsitterFilterView(OfferService offerService, FavoriteService favoriteService,
                               RequestService requestService, ChatService chatService,
                               AuthenticatedUser authenticatedUser, UserService userService,
                               BookingService bookingService) {
        this.offerService = offerService;
        this.favoriteService = favoriteService;
        this.requestService = requestService;
        this.chatService = chatService;
        this.authenticatedUser = authenticatedUser;
        this.userService = userService;
        this.bookingService = bookingService;
        this.currentCriteria = defaultCriteria(OfferSearchMode.TIERSITTER);

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle()
                .set("background", CREAM)
                .set("font-family", "Inter, Arial, sans-serif")
                .set("color", DARK)
                .set("position", "relative");

        add(buildDecoBlobs());
        add(buildPageTitle());
        add(buildFilterBarContainer());
        add(buildResultsLabel());
        add(buildContentArea());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Map<String, List<String>> parameters = event.getLocation().getQueryParameters().getParameters();
        currentCriteria = parseCriteria(parameters);

        applyModeChrome();
        renderFilterBar();
        renderOffers();
    }

    private void applyModeChrome() {
        SearchDisplay display = displayFor(currentCriteria.mode());
        pageTitle.setText(display.pageTitle);
        getStyle().set("background", display.pageBgColor);
        leftBlob.getStyle().set("background", display.leftBlobColor);
        rightBlob.getStyle().set("background", display.rightBlobColor);
    }

    private OfferSearchCriteria parseCriteria(Map<String, List<String>> parameters) {
        OfferSearchMode mode = OfferSearchMode.fromQueryValue(firstValue(parameters, "mode"));
        FilterSearchBar.SearchCriteria defaultCriteria = FilterSearchBar.defaultCriteria(
                offerService.getCurrentUserPostalCode().orElse(null));
        if (!containsAnyFilterParameter(parameters)) {
            return new OfferSearchCriteria(
                mode,
                defaultCriteria.from(),
                defaultCriteria.to(),
                defaultCriteria.dateFilterMode(),
                defaultCriteria.dateFlexDays(),
                defaultCriteria.earnings(),
                defaultCriteria.distanceKm(),
                defaultCriteria.originPostalCode(),
                null,
                null,
                Set.of(),
                Set.of(),
                null);
        }

        return new OfferSearchCriteria(
                mode,
                parseDate(firstValue(parameters, "from")),
                parseDate(firstValue(parameters, "to")),
                OfferDateFilterMode.fromQueryValue(firstValue(parameters, "dateMode")),
                parseFlexDays(firstValue(parameters, "dateFlexDays")),
                parseAmount(firstValue(parameters, "earnings")),
                parseDistance(firstValue(parameters, "distanceKm"), defaultCriteria.distanceKm()),
                parseOriginPostalCode(firstValue(parameters, "originPostalCode")),
                OfferCareType.fromQueryValue(firstValue(parameters, "careType")),
                OfferFrequency.fromQueryValue(firstValue(parameters, "frequency")),
                parseAnimalTypes(parameters.get("animalType")),
                parseWeekdays(parameters.get("weekday")),
                OfferTimeSlot.fromQueryValue(firstValue(parameters, "timeSlot")));
    }

    private boolean containsAnyFilterParameter(Map<String, List<String>> parameters) {
        return parameters.containsKey("from")
                || parameters.containsKey("to")
                || parameters.containsKey("dateMode")
                || parameters.containsKey("dateFlexDays")
                || parameters.containsKey("earnings")
                || parameters.containsKey("distanceKm")
                || parameters.containsKey("originPostalCode")
                || parameters.containsKey("careType")
                || parameters.containsKey("frequency")
                || parameters.containsKey("animalType")
                || parameters.containsKey("weekday")
                || parameters.containsKey("timeSlot");
    }

    private String firstValue(Map<String, List<String>> parameters, String key) {
        List<String> values = parameters.get(key);
        return values == null || values.isEmpty() ? null : values.getFirst();
    }

    private Set<OfferAnimalType> parseAnimalTypes(List<String> values) {
        Set<OfferAnimalType> animalTypes = new LinkedHashSet<>();
        if (values == null) {
            return animalTypes;
        }
        for (String value : values) {
            OfferAnimalType animalType = OfferAnimalType.fromQueryValue(value);
            if (animalType != null) {
                animalTypes.add(animalType);
            }
        }
        return animalTypes;
    }

    private Set<DayOfWeek> parseWeekdays(List<String> values) {
        Set<DayOfWeek> weekdays = new LinkedHashSet<>();
        if (values == null) {
            return weekdays;
        }
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            try {
                weekdays.add(DayOfWeek.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
                // Ignore invalid query parameters.
            }
        }
        return weekdays;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private BigDecimal parseAmount(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            BigDecimal amount = new BigDecimal(value.replace(",", "."));
            return amount.compareTo(BigDecimal.ZERO) > 0 ? amount : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private int parseDistance(String value, int defaultDistance) {
        if (value == null || value.isBlank()) {
            return defaultDistance;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return defaultDistance;
        }
    }

    private String parseOriginPostalCode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().replace(" ", "");
    }

    private int parseFlexDays(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            int parsedFlexDays = Integer.parseInt(value);
            return switch (parsedFlexDays) {
                case 0, 1, 2, 3, 7 -> parsedFlexDays;
                default -> 0;
            };
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private Component buildDecoBlobs() {
        Div container = new Div();
        container.getStyle()
                .set("position", "absolute")
                .set("top", "0")
                .set("left", "0")
                .set("width", "100%")
                .set("height", "100%")
                .set("overflow", "hidden")
                .set("z-index", "0")
                .set("pointer-events", "none");

        leftBlob = new Div();
        leftBlob.getStyle()
                .set("position", "absolute")
                .set("width", "700px")
                .set("height", "700px")
                .set("border-radius", "50%")
                .set("background", "#e2f2eb")
                .set("top", "-150px")
                .set("left", "-200px");

        rightBlob = new Div();
        rightBlob.getStyle()
                .set("position", "absolute")
                .set("width", "800px")
                .set("height", "800px")
                .set("border-radius", "50%")
                .set("background", "#edf1f9")
                .set("top", "50px")
                .set("right", "-150px");

        container.add(leftBlob, rightBlob);
        return container;
    }

    private Component buildPageTitle() {
        Div wrapper = new Div();
        wrapper.getStyle()
                .set("padding", "44px 48px 16px 48px")
                .set("position", "relative")
                .set("z-index", "1");

        pageTitle = new H1(displayFor(OfferSearchMode.TIERSITTER).pageTitle);
        pageTitle.getStyle()
                .set("margin", "0")
                .set("font-size", "32px")
                .set("font-weight", "800")
                .set("color", DARK)
                .set("line-height", "1.25");

        wrapper.add(pageTitle);
        return wrapper;
    }

    private Component buildFilterBarContainer() {
        filterBarContainer = new Div();
        filterBarContainer.getStyle()
                .set("padding", "0 48px 28px 48px")
                .set("position", "relative")
                .set("z-index", "1")
                .set("box-sizing", "border-box");
        return filterBarContainer;
    }

    private void renderFilterBar() {
        filterBarContainer.removeAll();

        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setSpacing(false);
        row.getStyle()
                .set("gap", "28px")
                .set("flex-wrap", "nowrap");

        SearchDisplay display = displayFor(currentCriteria.mode());
        FilterSearchBar searchBar = new FilterSearchBar(
                display.earningsMode,
                toFilterCriteria(currentCriteria),
                offerService::validateOriginPostalCode,
                this::onSearchClicked);
        searchBar.setWidthFull();
        searchBar.getStyle()
                .set("margin", "0")
                .set("max-width", "none")
                .set("flex-grow", "1")
                .set("flex-shrink", "1")
                .set("flex-basis", "auto");

        Div filterBtnWrapper = new Div();
        filterBtnWrapper.getStyle()
                .set("width", "380px")
                .set("min-width", "320px")
                .set("display", "flex")
                .set("justify-content", "flex-start")
                .set("flex-shrink", "0");

        Button filterBtn = new Button("Filter");
        Icon filterIcon = new Icon(VaadinIcon.FILTER);
        filterIcon.setSize("16px");
        filterBtn.setIcon(filterIcon);
        filterBtn.getStyle()
                .set("height", "52px")
                .set("padding", "0 32px")
                .set("border-radius", "50px")
                .set("background", ACCENT)
                .set("color", "white")
                .set("font-weight", "700")
                .set("font-size", "15px")
                .set("box-shadow", "0 6px 18px rgba(74,52,40,0.25)")
                .set("cursor", "pointer")
                .set("gap", "8px");
        filterBtn.addClickListener(e -> new FilterPopUp(
                currentCriteria,
                this::onAdditionalFiltersApplied).open());

        filterBtnWrapper.add(filterBtn);
        row.add(searchBar, filterBtnWrapper);
        row.setFlexGrow(1, searchBar);
        filterBarContainer.add(row);
    }

    private void onSearchClicked(FilterSearchBar.SearchCriteria criteria) {
        OfferSearchCriteria nextCriteria = new OfferSearchCriteria(
                currentCriteria.mode(),
                criteria.from(),
                criteria.to(),
                criteria.dateFilterMode(),
                criteria.dateFlexDays(),
                criteria.earnings(),
                criteria.distanceKm(),
                criteria.originPostalCode(),
                currentCriteria.careType(),
                currentCriteria.frequency(),
                currentCriteria.animalTypes(),
                currentCriteria.recurringWeekdays(),
                currentCriteria.timeSlot());
        UI.getCurrent().navigate("petsitter-suche", queryParametersFor(nextCriteria));
    }

    private void onAdditionalFiltersApplied(FilterPopUp.AdditionalFilters filters) {
        OfferSearchCriteria nextCriteria = new OfferSearchCriteria(
                currentCriteria.mode(),
                currentCriteria.from(),
                currentCriteria.to(),
                currentCriteria.dateFilterMode(),
                currentCriteria.dateFlexDays(),
                currentCriteria.earnings(),
                currentCriteria.distanceKm(),
                currentCriteria.originPostalCode(),
                filters.careType(),
                filters.frequency(),
                filters.animalTypes(),
                filters.recurringWeekdays(),
                filters.timeSlot());
        UI.getCurrent().navigate("petsitter-suche", queryParametersFor(nextCriteria));
    }

    private FilterSearchBar.SearchCriteria toFilterCriteria(OfferSearchCriteria criteria) {
        return new FilterSearchBar.SearchCriteria(
                criteria.from(),
                criteria.to(),
                criteria.dateFilterMode(),
                criteria.dateFlexDays(),
                criteria.earnings(),
                criteria.distanceKm(),
                criteria.originPostalCode());
    }

    private QueryParameters queryParametersFor(OfferSearchCriteria criteria) {
        Map<String, List<String>> parameters = new LinkedHashMap<>();
        parameters.put("mode", List.of(criteria.mode().queryValue()));
        if (criteria.from() != null) {
            parameters.put("from", List.of(criteria.from().toString()));
        }
        if (criteria.to() != null) {
            parameters.put("to", List.of(criteria.to().toString()));
        }
        parameters.put("dateMode", List.of(criteria.dateFilterMode().queryValue()));
        parameters.put("dateFlexDays", List.of(String.valueOf(criteria.dateFlexDays())));
        if (criteria.earnings() != null) {
            parameters.put("earnings", List.of(criteria.earnings().stripTrailingZeros().toPlainString()));
        }
        parameters.put("distanceKm", List.of(String.valueOf(criteria.distanceKm())));
        if (criteria.originPostalCode() != null) {
            parameters.put("originPostalCode", List.of(criteria.originPostalCode()));
        }
        if (criteria.careType() != null) {
            parameters.put("careType", List.of(criteria.careType().queryValue()));
        }
        if (criteria.frequency() != null) {
            parameters.put("frequency", List.of(criteria.frequency().queryValue()));
        }
        if (!criteria.recurringWeekdays().isEmpty()) {
            parameters.put("weekday", criteria.recurringWeekdays().stream()
                    .sorted(java.util.Comparator.comparingInt(DayOfWeek::getValue))
                    .map(day -> day.name().toLowerCase(java.util.Locale.ROOT))
                    .toList());
        }
        if (criteria.timeSlot() != null) {
            parameters.put("timeSlot", List.of(criteria.timeSlot().queryValue()));
        }
        if (!criteria.animalTypes().isEmpty()) {
            parameters.put("animalType", criteria.animalTypes().stream()
                    .map(OfferAnimalType::queryValue)
                    .toList());
        }
        return new QueryParameters(parameters);
    }

    private Component buildResultsLabel() {
        Div wrapper = new Div();
        wrapper.getStyle()
                .set("padding", "0 48px 20px 48px")
                .set("position", "relative")
                .set("z-index", "1");

        resultsLabel = new H2();
        resultsLabel.getStyle()
                .set("margin", "0")
                .set("font-size", "20px")
                .set("font-weight", "800")
                .set("color", DARK);
        wrapper.add(resultsLabel);
        return wrapper;
    }

    private Component buildContentArea() {
        HorizontalLayout area = new HorizontalLayout();
        area.setWidthFull();
        area.setPadding(false);
        area.setSpacing(false);
        area.getStyle()
                .set("padding", "0 48px 64px 48px")
                .set("gap", "28px")
                .set("align-items", "flex-start")
                .set("box-sizing", "border-box")
                .set("position", "relative")
                .set("z-index", "1");

        offerGrid = new Div();
        offerGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(300px, 1fr))")
                .set("gap", "20px")
                .set("flex", "1");

        Div map = buildMapPanel();
        area.add(offerGrid, map);
        area.setFlexGrow(1, offerGrid);
        return area;
    }

    private void renderOffers() {
        Optional<PostalCodeMapLocation> originLocation = resolveMapOriginLocation();
        List<OfferCardDto> offers = withFavoriteState(offerService.searchOpenOffers(currentCriteria));
        renderMap(originLocation, offers);

        resultsLabel.setText(resultText(offers.size()));
        offerGrid.removeAll();
        if (offers.isEmpty()) {
            offerGrid.add(buildEmptyState());
            return;
        }

        offers.forEach(dto -> offerGrid.add(new OfferCardComponent(
                dto,
                this::openOfferDialog,
                this::onFavoriteClicked)));
    }

    private Optional<PostalCodeMapLocation> resolveMapOriginLocation() {
        String originPostalCode = currentCriteria.originPostalCode();
        if (originPostalCode == null || originPostalCode.isBlank()) {
            return Optional.empty();
        }
        return offerService.resolveSearchOriginLocation(originPostalCode);
    }

    private void renderMap(Optional<PostalCodeMapLocation> originLocation, List<OfferCardDto> offers) {
        if (mapContainer == null) {
            return;
        }

        String originPostalCode = currentCriteria.originPostalCode();
        List<OfferMapLocation> offerLocations = offerService.resolveOfferMapLocations(offers);
        if (originLocation.isPresent() || !offerLocations.isEmpty()) {
            showSearchMap(originLocation.orElse(null), offerLocations, offers.size());
            return;
        }

        if (originPostalCode == null || originPostalCode.isBlank()) {
            showDefaultMap(
                    "Deutschland",
                    offers.isEmpty()
                            ? "Keine Ausgangs-PLZ gefiltert."
                            : "Keine Angebotskoordinaten verfügbar.");
        } else {
            showDefaultMap("Standort nicht verfügbar", "Die gefilterte PLZ konnte nicht aufgelöst werden.");
        }
    }

    private void showSearchMap(
            PostalCodeMapLocation originLocation,
            List<OfferMapLocation> offerLocations,
            int filteredOfferCount) {
        mapLocationLabel.setText(mapTitle(originLocation, offerLocations));
        mapStatusLabel.setText(mapStatus(originLocation, offerLocations, filteredOfferCount));
        try {
            String payload = JSON.writeValueAsString(new SearchMapPayload(originLocation, offerLocations));
            mapContainer.getElement().executeJs(
                    """
                    const element = this;
                    const payload = JSON.parse($0);
                    const showResults = () => window.PawsitterMap.showSearchResults(element, payload);
                    const run = (attempts) => window.PawsitterMap
                        ? showResults()
                        : attempts > 0 && window.requestAnimationFrame(() => run(attempts - 1));
                    run(20);
                    """,
                    payload);
        } catch (JsonProcessingException ex) {
            LOGGER.info("Search map payload could not be serialized.", ex);
            showDefaultMap("Karte nicht verfügbar", "Die Angebotsstandorte konnten nicht angezeigt werden.");
        }
    }

    private void showDefaultMap(String title, String status) {
        mapLocationLabel.setText(title);
        mapStatusLabel.setText(status);
        mapContainer.getElement().executeJs(
                """
                const element = this;
                const showDefault = () => window.PawsitterMap.showDefault(element);
                const run = (attempts) => window.PawsitterMap
                    ? showDefault()
                    : attempts > 0 && window.requestAnimationFrame(() => run(attempts - 1));
                run(20);
                """);
    }

    private String mapTitle(PostalCodeMapLocation originLocation, List<OfferMapLocation> offerLocations) {
        if (originLocation != null) {
            return formatMapLocation(originLocation);
        }
        int offerCount = offerLocations.size();
        return offerCount == 1 ? "1 Angebot auf der Karte" : offerCount + " Angebote auf der Karte";
    }

    private String mapStatus(
            PostalCodeMapLocation originLocation,
            List<OfferMapLocation> offerLocations,
            int filteredOfferCount) {
        int offerCount = offerLocations.size();
        String offerText = offerCount == 1 ? "1 gefiltertes Angebot" : offerCount + " gefilterte Angebote";
        if (originLocation != null && offerCount > 0) {
            return "Ausgangs-PLZ und " + offerText;
        }
        if (originLocation != null && filteredOfferCount > 0) {
            return "Gefilterte Ausgangs-PLZ; keine Angebotskoordinaten verfügbar.";
        }
        if (originLocation != null) {
            return "Gefilterte Ausgangs-PLZ";
        }
        return offerText;
    }

    private String formatMapLocation(PostalCodeMapLocation location) {
        String placeName = location.placeName() == null ? "" : location.placeName().trim();
        if (placeName.isBlank()) {
            return location.postalCode();
        }
        return location.postalCode() + " " + placeName;
    }

    private String resultText(int count) {
        String resultNoun = displayFor(currentCriteria.mode()).resultNoun;
        return count == 1
                ? "1 " + resultNoun + " gefunden"
                : count + " " + resultNoun + " gefunden";
    }

    private Component buildEmptyState() {
        Div empty = new Div();
        empty.getStyle()
                .set("background", CARD_BG)
                .set("border-radius", "20px")
                .set("box-shadow", "0 6px 24px rgba(74,52,40,0.10)")
                .set("padding", "28px")
                .set("box-sizing", "border-box")
                .set("grid-column", "1 / -1");

        H3 title = new H3("Keine passenden " + displayFor(currentCriteria.mode()).resultNoun + " gefunden");
        title.getStyle()
                .set("margin", "0 0 8px 0")
                .set("font-size", "18px")
                .set("font-weight", "800")
                .set("color", DARK);

        Paragraph copy = new Paragraph("Passe Zeitraum oder Verdienst an, um weitere Angebote zu sehen.");
        copy.getStyle()
                .set("margin", "0")
                .set("color", "#7b7069")
                .set("font-size", "14px");

        empty.add(title, copy);
        return empty;
    }

    private void openOfferDialog(OfferCardDto dto) {
        new PetsitterDetailPopUp(dto, OfferCardComponent.formatDistance(dto.distanceKm()),
                OfferCardComponent.starsForAverage(dto.creatorAverageRating()),
                offerService, requestService, chatService, authenticatedUser, userService, bookingService).open();
    }

    private boolean onFavoriteClicked(OfferCardDto dto) {
        try {
            boolean favorited = favoriteService.toggleCurrentUserFavorite(dto.id());
            Notification.show(
                    favorited ? "Offer zu Favoriten hinzugefügt." : "Offer aus Favoriten entfernt.",
                    2500,
                    Notification.Position.TOP_CENTER);
            return favorited;
        } catch (RuntimeException exception) {
            Notification.show(exception.getMessage(), 3500, Notification.Position.TOP_CENTER);
            return dto.favorited();
        }
    }

    private List<OfferCardDto> withFavoriteState(List<OfferCardDto> offers) {
        Set<UUID> favoriteOfferIds = favoriteService.favoriteOfferIdsForCurrentUser(
                offers.stream().map(OfferCardDto::id).toList());
        return offers.stream()
                .map(offer -> offer.withFavorited(favoriteOfferIds.contains(offer.id())))
                .toList();
    }

    private Div buildMapPanel() {
        Div mapWrapper = new Div();
        mapWrapper.getStyle()
                .set("width", "380px")
                .set("min-width", "320px")
                .set("border-radius", "20px")
                .set("overflow", "hidden")
                .set("box-shadow", "0 8px 32px rgba(74,52,40,0.14)")
                .set("position", "sticky")
                .set("top", "24px")
                .set("align-self", "flex-start")
                .set("background", CARD_BG);

        Div mapHeader = new Div();
        mapHeader.getStyle()
                .set("padding", "14px 16px 12px 16px")
                .set("border-bottom", "1px solid #ead5ae")
                .set("box-sizing", "border-box");

        mapLocationLabel = new Span("Deutschland");
        mapLocationLabel.getStyle()
                .set("display", "block")
                .set("font-size", "15px")
                .set("font-weight", "800")
                .set("line-height", "1.25")
                .set("color", DARK);

        mapStatusLabel = new Span("Keine Ausgangs-PLZ gefiltert.");
        mapStatusLabel.getStyle()
                .set("display", "block")
                .set("font-size", "12px")
                .set("font-weight", "700")
                .set("line-height", "1.35")
                .set("margin-top", "3px")
                .set("color", "#7b7069");
        mapHeader.add(mapLocationLabel, mapStatusLabel);

        mapContainer = new Div();
        mapContainer.getStyle()
                .set("width", "100%")
                .set("height", "586px")
                .set("background", "#e8efe8")
                .set("position", "relative")
                .set("overflow", "hidden");

        mapWrapper.add(mapHeader, mapContainer);
        return mapWrapper;
    }

    private OfferSearchCriteria defaultCriteria(OfferSearchMode mode) {
        FilterSearchBar.SearchCriteria defaultCriteria = FilterSearchBar.defaultCriteria(
                offerService.getCurrentUserPostalCode().orElse(null));
        return new OfferSearchCriteria(
                mode,
                defaultCriteria.from(),
                defaultCriteria.to(),
                defaultCriteria.dateFilterMode(),
                defaultCriteria.dateFlexDays(),
                defaultCriteria.earnings(),
                defaultCriteria.distanceKm(),
                defaultCriteria.originPostalCode(),
                null,
                null,
                Set.of(),
                Set.of(),
                null);
    }

    private SearchDisplay displayFor(OfferSearchMode mode) {
        return switch (mode) {
            case TIERHALTER -> new SearchDisplay(
                    FilterSearchBar.EarningsMode.MINIMUM,
                    "Finde liebevolle Tierhalter in deiner Nähe",
                    "Tierhalter",
                    CREAM,
                    "#f6ead5",
                    "#e7f0f0");
            case TIERSITTER -> new SearchDisplay(
                    FilterSearchBar.EarningsMode.MAXIMUM,
                    "Finde liebevolle Tiersitter in deiner Nähe",
                    "Tiersitter",
                    "#ebf6f0",
                    "#e2f5ec",
                    "#eef0fa");
        };
    }

    private record SearchDisplay(
            FilterSearchBar.EarningsMode earningsMode,
            String pageTitle,
            String resultNoun,
            String pageBgColor,
            String leftBlobColor,
            String rightBlobColor
    ) {
    }

    private record SearchMapPayload(
            PostalCodeMapLocation origin,
            List<OfferMapLocation> offers
    ) {
    }
}
