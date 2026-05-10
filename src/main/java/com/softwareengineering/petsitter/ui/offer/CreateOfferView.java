package com.softwareengineering.petsitter.ui.offer;

import com.softwareengineering.petsitter.offer.domain.Offer;
import com.softwareengineering.petsitter.offer.domain.OfferStatus;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import com.softwareengineering.petsitter.offer.repository.OfferRepository;
import com.softwareengineering.petsitter.pet.domain.Pet;
import com.softwareengineering.petsitter.pet.repository.PetRepository;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.ui.shared.MainLayout;
import com.softwareengineering.petsitter.user.domain.User;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Route(value = "create_offer", layout = MainLayout.class)
@PageTitle("Angebot erstellen | Petsitter")
@PermitAll
public class CreateOfferView extends VerticalLayout {

    private final OfferRepository offerRepository;
    private final Optional<User> currentUser;

    private final ComboBox<OfferType> offerType = new ComboBox<>("Offer type");
    private final DatePicker startDate = new DatePicker("Start date");
    private final DatePicker endDate = new DatePicker("End date");
    private final Span dateSummary = new Span("Bitte Start- und Enddatum auswaehlen.");
    private final ComboBox<Pet> pet = new ComboBox<>("Pet");
    private final BigDecimalField price = new BigDecimalField("Price per day");
    private final TextArea description = new TextArea("Description");
    private final Button createOffer = new Button("createOffer", event -> saveOffer());

    public CreateOfferView(OfferRepository offerRepository, PetRepository petRepository,
            AuthenticatedUser authenticatedUser) {
        this.offerRepository = offerRepository;
        this.currentUser = authenticatedUser.get();

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        configureFields(loadOwnPets(petRepository));

        FormLayout form = new FormLayout(
                offerType,
                startDate,
                endDate,
                dateSummary,
                pet,
                price,
                description);
        form.setMaxWidth("720px");
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("520px", 2));
        form.setColspan(dateSummary, 2);
        form.setColspan(description, 2);

        createOffer.addThemeName("primary");
        createOffer.setEnabled(currentUser.isPresent());

        add(new H2("Angebot erstellen"), form, createOffer);
        if (currentUser.isEmpty()) {
            showError("Kein eingeloggter DB-User gefunden. Bitte mit einem gespeicherten User anmelden.");
        }
    }

    private List<Pet> loadOwnPets(PetRepository petRepository) {
        return currentUser
                .map(user -> petRepository.findAllByOwnerId(user.getId()))
                .orElseGet(List::of);
    }

    private void configureFields(List<Pet> pets) {
        offerType.setItems(OfferType.values());
        offerType.setRequiredIndicatorVisible(true);

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        startDate.setMin(today);
        startDate.setRequiredIndicatorVisible(true);
        startDate.addValueChangeListener(event -> {
            LocalDate selectedStart = event.getValue();
            LocalDate minimumEndDate = selectedStart != null && selectedStart.isAfter(tomorrow)
                    ? selectedStart
                    : tomorrow;
            endDate.setMin(minimumEndDate);
            if (endDate.getValue() != null && endDate.getValue().isBefore(minimumEndDate)) {
                endDate.clear();
            }
            updateDateSummary();
        });

        endDate.setMin(tomorrow);
        endDate.setRequiredIndicatorVisible(true);
        endDate.addValueChangeListener(event -> updateDateSummary());

        dateSummary.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        pet.setItems(pets);
        pet.setItemLabelGenerator(this::formatPet);
        pet.setClearButtonVisible(true);

        price.setPrefixComponent(new com.vaadin.flow.component.html.Span("EUR"));
        price.setClearButtonVisible(true);

        description.setMaxLength(255);
        description.setHeight("120px");
        description.setClearButtonVisible(true);
    }

    private void saveOffer() {
        if (!isValid()) {
            showError("Bitte alle Pflichtfelder korrekt ausfuellen.");
            return;
        }

        Offer offer = new Offer();
        offer.setStartDate(startDate.getValue());
        offer.setEndDate(endDate.getValue());
        offer.setCreateUser(currentUser.orElseThrow());
        offer.setUpdateUser(currentUser.orElseThrow());
        offer.setPet(pet.getValue());
        offer.setOfferType(offerType.getValue());
        offer.setPrice(price.getValue());
        offer.setDescription(description.getValue());
        offer.setStatus(OfferStatus.OPEN);

        try {
            Offer savedOffer = offerRepository.save(offer);
            clearForm();
            Notification.show("Offer erstellt: " + savedOffer.getOfferId(), 4000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (RuntimeException exception) {
            showError("Offer konnte nicht gespeichert werden: " + exception.getMessage());
        }
    }

    private boolean isValid() {
        if (currentUser.isEmpty() || offerType.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        return !startDate.getValue().isBefore(today)
                && !endDate.getValue().isBefore(tomorrow)
                && !startDate.getValue().isAfter(endDate.getValue());
    }

    private void updateDateSummary() {
        if (startDate.isEmpty() || endDate.isEmpty()) {
            dateSummary.setText("Bitte Start- und Enddatum auswaehlen.");
            return;
        }

        if (startDate.getValue().isAfter(endDate.getValue())) {
            dateSummary.setText("Das Enddatum muss am oder nach dem Startdatum liegen.");
            return;
        }

        long totalDays = ChronoUnit.DAYS.between(startDate.getValue(), endDate.getValue()) + 1;
        dateSummary.setText("Gesamtdauer: " + totalDays + " Tag(e), inklusive Start- und Enddatum.");
    }

    private void clearForm() {
        offerType.clear();
        startDate.clear();
        endDate.clear();
        updateDateSummary();
        pet.clear();
        price.clear();
        description.clear();
    }

    private String formatPet(Pet pet) {
        if (pet == null) {
            return "";
        }
        return pet.getName() + " (" + pet.getSpecies() + ")";
    }

    private void showError(String message) {
        Notification.show(message, 5000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
