package com.softwareengineering.petsitter.ui.shared;

import com.softwareengineering.petsitter.offer.domain.OfferAnimalType;
import com.softwareengineering.petsitter.offer.domain.OfferFrequency;
import com.softwareengineering.petsitter.offer.domain.OfferTimeSlot;
import com.softwareengineering.petsitter.offer.dto.OfferCardDto;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class OfferCardComponent extends Div {

    private static final String DARK        = "#4a3428";
    private static final String CARD_SHADOW = "0 12px 30px rgba(74, 52, 40, 0.10)";
    private static final int    PLACEHOLDER_STARS = 4;

    private static final Map<OfferAnimalType, String> ANIMAL_COLORS = Map.of(
            OfferAnimalType.DOG,          "#dec18d",
            OfferAnimalType.CAT,          "#f1b47a",
            OfferAnimalType.BIRD,         "#93b8c9",
            OfferAnimalType.SMALL_ANIMAL, "#94b883",
            OfferAnimalType.REPTILE,      "#a8c89b",
            OfferAnimalType.FISH,         "#bad6df",
            OfferAnimalType.OTHER,        "#f1dfb9"
    );

    public OfferCardComponent(OfferCardDto dto,
                              Consumer<OfferCardDto> onCardClick,
                              Function<OfferCardDto, Boolean> onFavoriteClick) {
        getStyle()
                .set("background", "white")
                .set("border-radius", "24px")
                .set("box-shadow", CARD_SHADOW)
                .set("overflow", "hidden")
                .set("cursor", "pointer")
                .set("transition", "transform 0.2s ease, box-shadow 0.2s ease");

        getElement().executeJs("""
                this.addEventListener('mouseenter', () => {
                    this.style.transform = 'translateY(-4px)';
                    this.style.boxShadow = '0 18px 42px rgba(74, 52, 40, 0.15)';
                });
                this.addEventListener('mouseleave', () => {
                    this.style.transform = 'translateY(0)';
                    this.style.boxShadow = '0 12px 30px rgba(74, 52, 40, 0.10)';
                });
                """);

        String topColor = dto.animalType() != null
                ? ANIMAL_COLORS.getOrDefault(dto.animalType(), "#f1dfb9")
                : "#f1dfb9";

        Div imageArea = ImageComponents.offerCover(dto.coverTiles(), "148px", topColor);
        imageArea.getStyle()
                .set("position", "relative")
                .set("width", "calc(100% - 24px)")
                .set("border-radius", "14px")
                .set("margin", "12px 12px 0 12px")
                .set("overflow", "hidden");

        Span starsBadge = new Span(buildStars(PLACEHOLDER_STARS));
        starsBadge.getStyle()
                .set("position", "absolute")
                .set("top", "10px")
                .set("left", "12px")
                .set("background", "rgba(60, 60, 50, 0.50)")
                .set("color", "#ffdf4a")
                .set("font-size", "12px")
                .set("letter-spacing", "1.5px")
                .set("border-radius", "14px")
                .set("padding", "5px 11px");

        if (dto.creatorVerified()) {
            Span verifiedBadge = new Span("✓ Verifiziert");
            verifiedBadge.getStyle()
                    .set("position", "absolute")
                    .set("top", "10px")
                    .set("right", "10px")
                    .set("background", "white")
                    .set("color", "#6b9a75")
                    .set("font-size", "12px")
                    .set("font-weight", "700")
                    .set("border-radius", "14px")
                    .set("padding", "5px 12px");
            imageArea.add(starsBadge, verifiedBadge);
        } else {
            imageArea.add(starsBadge);
        }

        Div body = new Div();
        body.getStyle().set("padding", "16px 18px 18px 18px");

        HorizontalLayout titleRow = new HorizontalLayout();
        titleRow.setWidthFull();
        titleRow.setAlignItems(FlexComponent.Alignment.CENTER);
        titleRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        titleRow.getStyle().set("margin-bottom", "10px");

        H3 cardTitle = new H3(dto.title());
        cardTitle.getStyle()
                .set("font-size", "18px")
                .set("font-weight", "800")
                .set("line-height", "1.2")
                .set("margin", "0")
                .set("color", DARK);

        Button heartBtn = new Button();
        boolean[] favorited = {dto.favorited()};
        updateFavoriteButton(heartBtn, favorited[0]);
        heartBtn.getStyle()
                .set("width", "34px")
                .set("height", "34px")
                .set("min-width", "34px")
                .set("border-radius", "50%")
                .set("background", "transparent")
                .set("box-shadow", "none")
                .set("cursor", "pointer")
                .set("flex-shrink", "0");
        heartBtn.addClickListener(e -> {
            Boolean newFavoriteState = onFavoriteClick.apply(dto.withFavorited(favorited[0]));
            if (newFavoriteState != null) {
                favorited[0] = newFavoriteState;
                updateFavoriteButton(heartBtn, favorited[0]);
            }
            playFavoriteAnimation(heartBtn);
        });
        heartBtn.getElement().executeJs("this.addEventListener('click', event => event.stopPropagation());");

        titleRow.add(cardTitle, heartBtn);

        Span location = new Span(formatLocation(dto.postalCode(), dto.city()));
        location.getStyle()
                .set("display", "block")
                .set("font-size", "13px")
                .set("font-weight", "700")
                .set("color", "#7b7069")
                .set("margin", "-2px 0 12px 0");

        HorizontalLayout facts = new HorizontalLayout();
        facts.setWidthFull();
        facts.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        facts.setAlignItems(FlexComponent.Alignment.START);
        facts.getStyle().set("gap", "4px");

        facts.add(
                factItem("Zeitraum",   formatSchedule(dto.frequency(), dto.startDate(), dto.endDate(),
                        dto.recurringWeekdays(), dto.timeSlot())),
                factItem("Verdienst",  formatPrice(dto.price())),
                factItem("Entfernung", formatDistance(dto.distanceKm()))
        );

        body.add(titleRow);
        if (!location.getText().isBlank()) {
            body.add(location);
        }
        body.add(facts);
        add(imageArea, body);

        addClickListener(e -> onCardClick.accept(dto));
    }

    private static VerticalLayout factItem(String label, String value) {
        VerticalLayout box = new VerticalLayout();
        box.setPadding(false);
        box.setSpacing(false);

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "11px")
                .set("color", "#9e8c7b");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "14px")
                .set("font-weight", "800")
                .set("color", DARK);

        box.add(labelSpan, valueSpan);
        return box;
    }

    private static void updateFavoriteButton(Button heartBtn, boolean favorited) {
        Icon icon = new Icon(favorited ? VaadinIcon.HEART : VaadinIcon.HEART_O);
        icon.setSize("19px");
        heartBtn.setIcon(icon);
        heartBtn.setAriaLabel(favorited ? "Aus Favoriten entfernen" : "Als Favorit markieren");
        heartBtn.getStyle().set("color", favorited ? "#d94b4b" : "#b0a090");
    }

    private static void playFavoriteAnimation(Button heartBtn) {
        heartBtn.getElement().executeJs("""
                this.animate([
                    { transform: 'scale(1)' },
                    { transform: 'scale(1.22)' },
                    { transform: 'scale(1)' }
                ], { duration: 180, easing: 'ease-out' });
                """);
    }

    static String colorFor(OfferAnimalType animalType) {
        return animalType != null ? ANIMAL_COLORS.getOrDefault(animalType, "#f1dfb9") : "#f1dfb9";
    }

    static String formatDateRange(LocalDate start, LocalDate end) {
        if (start == null) return "–";
        DateTimeFormatter day = DateTimeFormatter.ofPattern("d", Locale.GERMAN);
        DateTimeFormatter dayMonth = DateTimeFormatter.ofPattern("d. MMM", Locale.GERMAN);
        DateTimeFormatter month = DateTimeFormatter.ofPattern("MMMM", Locale.GERMAN);
        if (end == null || start.equals(end)) {
            return start.format(DateTimeFormatter.ofPattern("d. MMMM", Locale.GERMAN));
        }
        if (start.getMonth() == end.getMonth()) {
            return start.format(day) + ".–" + end.format(day) + ". " + start.format(month);
        }
        return start.format(dayMonth) + " – " + end.format(dayMonth);
    }

    public static String formatSchedule(
            OfferFrequency frequency,
            LocalDate start,
            LocalDate end,
            Set<DayOfWeek> recurringWeekdays,
            OfferTimeSlot timeSlot
    ) {
        if (frequency == OfferFrequency.REGULAR) {
            String weekdays = formatWeekdays(recurringWeekdays);
            if (weekdays.isBlank()) {
                return timeSlot == null ? "regelmäßig" : timeSlot.label();
            }
            return timeSlot == null ? weekdays : weekdays + " · " + timeSlot.label();
        }
        return formatDateRange(start, end);
    }

    public static String formatWeekdays(Set<DayOfWeek> weekdays) {
        if (weekdays == null || weekdays.isEmpty()) {
            return "";
        }
        return weekdays.stream()
                .sorted(java.util.Comparator.comparingInt(DayOfWeek::getValue))
                .map(OfferCardComponent::shortDayLabel)
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private static String shortDayLabel(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "Mo";
            case TUESDAY -> "Di";
            case WEDNESDAY -> "Mi";
            case THURSDAY -> "Do";
            case FRIDAY -> "Fr";
            case SATURDAY -> "Sa";
            case SUNDAY -> "So";
        };
    }

    static String formatPrice(BigDecimal price) {
        if (price == null) return "–";
        return price.stripTrailingZeros().toPlainString() + " €";
    }

    public static String formatDistance(Integer distanceKm) {
        if (distanceKm == null) return "–";
        if (distanceKm <= 0) return "< 1 km";
        return "ca. " + distanceKm + " km";
    }

    public static String formatLocation(String postalCode, String city) {
        String location = ((postalCode == null ? "" : postalCode.trim()) + " "
                + (city == null ? "" : city.trim())).trim();
        return location;
    }

    private static String buildStars(int filled) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(i < filled ? "★" : "☆");
        }
        return sb.toString();
    }
}
