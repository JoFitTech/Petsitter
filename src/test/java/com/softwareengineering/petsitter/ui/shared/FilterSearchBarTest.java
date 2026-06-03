package com.softwareengineering.petsitter.ui.shared;

import static org.assertj.core.api.Assertions.assertThat;

import com.softwareengineering.petsitter.offer.dto.OfferSearchCriteria;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.slider.Slider;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FilterSearchBarTest {

    @Test
    void distanceSliderOffersUnlimitedDistanceAfterOneHundredKilometers() {
        FilterSearchBar.SearchCriteria defaults = FilterSearchBar.defaultCriteria("10115");
        FilterSearchBar searchBar = new FilterSearchBar(
                FilterSearchBar.EarningsMode.MAXIMUM,
                new FilterSearchBar.SearchCriteria(
                        defaults.from(),
                        defaults.to(),
                        defaults.dateFilterMode(),
                        defaults.dateFlexDays(),
                        defaults.earnings(),
                        OfferSearchCriteria.ANY_DISTANCE_KM,
                        defaults.originPostalCode()),
                postalCode -> Optional.empty(),
                criteria -> { });
        Slider distanceSlider = findComponent(searchBar, Slider.class).orElseThrow();

        assertThat(searchBar.getCriteria().distanceKm()).isEqualTo(OfferSearchCriteria.ANY_DISTANCE_KM);
        assertThat(texts(searchBar)).filteredOn("egal"::equals).hasSize(3);
        assertThat(texts(searchBar)).noneMatch(text -> text.contains("-1"));

        distanceSlider.setValue(distanceSlider.getMax() - 1);

        assertThat(searchBar.getCriteria().distanceKm()).isEqualTo(100);
        assertThat(texts(searchBar)).contains("bis 100 km", "100 km");
        assertThat(texts(searchBar)).filteredOn("egal"::equals).hasSize(1);

        distanceSlider.setValue(distanceSlider.getMax());

        assertThat(searchBar.getCriteria().distanceKm()).isEqualTo(OfferSearchCriteria.ANY_DISTANCE_KM);
        assertThat(texts(searchBar)).filteredOn("egal"::equals).hasSize(3);
        assertThat(texts(searchBar)).noneMatch(text -> text.contains("-1"));
    }

    private <T extends Component> Optional<T> findComponent(Component root, Class<T> type) {
        if (type.isInstance(root)) {
            return Optional.of(type.cast(root));
        }
        return root.getChildren()
                .map(child -> findComponent(child, type))
                .flatMap(Optional::stream)
                .findFirst();
    }

    private List<String> texts(Component root) {
        List<String> texts = new ArrayList<>();
        collectTexts(root, texts);
        return texts;
    }

    private void collectTexts(Component root, List<String> texts) {
        if (root instanceof HasText hasText) {
            texts.add(hasText.getText());
        }
        root.getChildren().forEach(child -> collectTexts(child, texts));
    }
}
