package com.softwareengineering.petsitter.ui.shared;

import static org.assertj.core.api.Assertions.assertThat;

import com.softwareengineering.petsitter.offer.domain.OfferDateFilterMode;
import com.softwareengineering.petsitter.offer.domain.OfferFrequency;
import com.softwareengineering.petsitter.offer.domain.OfferSearchMode;
import com.softwareengineering.petsitter.offer.domain.OfferTimeSlot;
import com.softwareengineering.petsitter.offer.dto.OfferSearchCriteria;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class FilterPopUpTest {

    @Test
    void recurringControlsAreHiddenUntilRegularFrequencyIsSelected() {
        FilterPopUp dialog = new FilterPopUp();

        Div weekdaySection = sectionWithLabel(dialog, "Wochentage");
        Div timeSlotSection = sectionWithLabel(dialog, "Tageszeit");

        assertThat(weekdaySection.isVisible()).isFalse();
        assertThat(timeSlotSection.isVisible()).isFalse();

        frequencyBox(dialog).setValue(OfferFrequency.REGULAR);

        assertThat(weekdaySection.isVisible()).isTrue();
        assertThat(timeSlotSection.isVisible()).isTrue();

        frequencyBox(dialog).clear();

        assertThat(weekdaySection.isVisible()).isFalse();
        assertThat(timeSlotSection.isVisible()).isFalse();
    }

    @Test
    void recurringControlsAreVisibleWhenInitialCriteriaIsRegular() {
        OfferSearchCriteria criteria = new OfferSearchCriteria(
                OfferSearchMode.TIERSITTER,
                null,
                null,
                OfferDateFilterMode.ANY,
                0,
                null,
                5,
                null,
                null,
                OfferFrequency.REGULAR,
                Set.of(),
                Set.of(DayOfWeek.MONDAY),
                OfferTimeSlot.MORNING);

        FilterPopUp dialog = new FilterPopUp(criteria, filters -> { });

        assertThat(sectionWithLabel(dialog, "Wochentage").isVisible()).isTrue();
        assertThat(sectionWithLabel(dialog, "Tageszeit").isVisible()).isTrue();
    }

    private Div sectionWithLabel(Component root, String label) {
        return descendants(root)
                .filter(Div.class::isInstance)
                .map(Div.class::cast)
                .filter(div -> div.getChildren()
                        .anyMatch(child -> child instanceof Span span && label.equals(span.getText())))
                .findFirst()
                .orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private ComboBox<OfferFrequency> frequencyBox(Component root) {
        List<ComboBox> boxes = descendants(root)
                .filter(ComboBox.class::isInstance)
                .map(ComboBox.class::cast)
                .toList();
        assertThat(boxes).hasSizeGreaterThanOrEqualTo(2);
        return (ComboBox<OfferFrequency>) boxes.get(1);
    }

    private Stream<Component> descendants(Component component) {
        return Stream.concat(Stream.of(component), component.getChildren().flatMap(this::descendants));
    }
}
