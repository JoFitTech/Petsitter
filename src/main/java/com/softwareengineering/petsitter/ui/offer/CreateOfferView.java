package com.softwareengineering.petsitter.ui.offer;

import com.softwareengineering.petsitter.offer.domain.Offer;
import com.softwareengineering.petsitter.offer.domain.OfferStatus;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import com.softwareengineering.petsitter.offer.repository.OfferRepository;
import com.softwareengineering.petsitter.pet.domain.Pet;
import com.softwareengineering.petsitter.pet.repository.PetRepository;
import com.softwareengineering.petsitter.ui.shared.MainLayout;
import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.user.repository.UserRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import java.util.List;

@Route(value = "create_offer", layout = MainLayout.class)
@PageTitle("Angebot erstellen | Petsitter")
@PermitAll
public class CreateOfferView extends VerticalLayout {

    private final OfferRepository offerRepository;

    private final DatePicker startDate = new DatePicker("Start date");
    private final DatePicker endDate = new DatePicker("End date");
    private final ComboBox<User> createUser = new ComboBox<>("Create user");
    private final ComboBox<User> updateUser = new ComboBox<>("Update user");
    private final ComboBox<Pet> pet = new ComboBox<>("Pet");
    private final ComboBox<OfferType> offerType = new ComboBox<>("Offer type");
    private final BigDecimalField price = new BigDecimalField("Price");
    private final TextArea description = new TextArea("Description");
    private final ComboBox<OfferStatus> status = new ComboBox<>("Status");

    public CreateOfferView(OfferRepository offerRepository, UserRepository userRepository,
            PetRepository petRepository) {
        this.offerRepository = offerRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        configureFields(userRepository.findAll(), petRepository.findAll());

        FormLayout form = new FormLayout(
                startDate,
                endDate,
                createUser,
                updateUser,
                pet,
                offerType,
                price,
                description,
                status);
        form.setMaxWidth("720px");
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("520px", 2));
        form.setColspan(description, 2);

        Button createOffer = new Button("createOffer", event -> saveOffer());
        createOffer.addThemeName("primary");

        add(new H2("Angebot erstellen"), form, createOffer);
    }

    private void configureFields(List<User> users, List<Pet> pets) {
        startDate.setRequiredIndicatorVisible(true);
        endDate.setRequiredIndicatorVisible(true);

        createUser.setItems(users);
        createUser.setItemLabelGenerator(this::formatUser);
        createUser.setRequiredIndicatorVisible(true);
        createUser.addValueChangeListener(event -> {
            if (updateUser.isEmpty()) {
                updateUser.setValue(event.getValue());
            }
        });

        updateUser.setItems(users);
        updateUser.setItemLabelGenerator(this::formatUser);
        updateUser.setRequiredIndicatorVisible(true);

        pet.setItems(pets);
        pet.setItemLabelGenerator(this::formatPet);
        pet.setClearButtonVisible(true);

        offerType.setItems(OfferType.values());
        offerType.setRequiredIndicatorVisible(true);

        price.setPrefixComponent(new com.vaadin.flow.component.html.Span("EUR"));
        price.setClearButtonVisible(true);

        description.setMaxLength(255);
        description.setHeight("120px");
        description.setClearButtonVisible(true);

        status.setItems(OfferStatus.values());
        status.setValue(OfferStatus.OPEN);
        status.setRequiredIndicatorVisible(true);
    }

    private void saveOffer() {
        if (!isValid()) {
            showError("Bitte alle Pflichtfelder korrekt ausfuellen.");
            return;
        }

        Offer offer = new Offer();
        offer.setStartDate(startDate.getValue());
        offer.setEndDate(endDate.getValue());
        offer.setCreateUser(createUser.getValue());
        offer.setUpdateUser(updateUser.getValue());
        offer.setPet(pet.getValue());
        offer.setOfferType(offerType.getValue());
        offer.setPrice(price.getValue());
        offer.setDescription(description.getValue());
        offer.setStatus(status.getValue());

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
        if (startDate.isEmpty() || endDate.isEmpty() || createUser.isEmpty()
                || updateUser.isEmpty() || offerType.isEmpty() || status.isEmpty()) {
            return false;
        }
        return !startDate.getValue().isAfter(endDate.getValue());
    }

    private void clearForm() {
        startDate.clear();
        endDate.clear();
        createUser.clear();
        updateUser.clear();
        pet.clear();
        offerType.clear();
        price.clear();
        description.clear();
        status.setValue(OfferStatus.OPEN);
    }

    private String formatUser(User user) {
        if (user == null) {
            return "";
        }
        return user.getFirstName() + " " + user.getLastName() + " (" + user.getEmail() + ")";
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
