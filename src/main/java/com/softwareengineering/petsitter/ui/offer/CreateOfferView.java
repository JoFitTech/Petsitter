package com.softwareengineering.petsitter.ui.offer;

import com.softwareengineering.petsitter.offer.domain.OfferType;
import com.softwareengineering.petsitter.offer.dto.CreateOfferDateSelection;
import com.softwareengineering.petsitter.offer.dto.CreateOfferFormData;
import com.softwareengineering.petsitter.offer.dto.CreateOfferResult;
import com.softwareengineering.petsitter.offer.dto.OfferPetOptionDto;
import com.softwareengineering.petsitter.offer.service.OfferService;
import com.softwareengineering.petsitter.ui.shared.MainLayout;
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

@Route(value = "create_offer", layout = MainLayout.class)
@PageTitle("Angebot erstellen | Petsitter")
@PermitAll
public class CreateOfferView extends VerticalLayout {

    private final OfferService offerService;

    private final ComboBox<OfferType> offerType = new ComboBox<>("Offer type");
    private final DatePicker startDate = new DatePicker("Start date");
    private final DatePicker endDate = new DatePicker("End date");
    private final Span dateSummary = new Span();
    private final ComboBox<OfferPetOptionDto> pet = new ComboBox<>("Pet");
    private final BigDecimalField price = new BigDecimalField("Price per day");
    private final TextArea description = new TextArea("Description");
    private final Button createOffer = new Button("createOffer", event -> saveOffer());

    public CreateOfferView(OfferService offerService) {
        this.offerService = offerService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        boolean hasAuthenticatedUser = offerService.hasAuthenticatedUser();
        configureFields(offerService.getCreateOfferFormData());

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
        createOffer.setEnabled(hasAuthenticatedUser);

        add(new H2("Angebot erstellen"), form, createOffer);
        if (!hasAuthenticatedUser) {
            showError("Kein eingeloggter DB-User gefunden. Bitte mit einem gespeicherten User anmelden.");
        }
    }

    private void configureFields(CreateOfferFormData formData) {
        offerType.setItems(formData.offerTypes());
        offerType.setRequiredIndicatorVisible(true);

        startDate.setMin(formData.minimumStartDate());
        startDate.setRequiredIndicatorVisible(true);
        startDate.addValueChangeListener(event -> applyDateSelection(
                offerService.updateCreateOfferDateSelection(event.getValue(), endDate.getValue())));

        applyDateSelection(formData.dateSelection());
        endDate.setRequiredIndicatorVisible(true);
        endDate.addValueChangeListener(event -> applyDateSelection(
                offerService.updateCreateOfferDateSelection(startDate.getValue(), event.getValue())));

        dateSummary.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        pet.setItems(formData.pets());
        pet.setItemLabelGenerator(OfferPetOptionDto::label);
        pet.setClearButtonVisible(true);

        price.setPrefixComponent(new com.vaadin.flow.component.html.Span("EUR"));
        price.setClearButtonVisible(true);

        description.setMaxLength(formData.descriptionMaxLength());
        description.setHeight("120px");
        description.setClearButtonVisible(true);
    }

    private void saveOffer() {
        try {
            CreateOfferResult result = offerService.createOffer(
                    offerType.getValue(),
                    startDate.getValue(),
                    endDate.getValue(),
                    pet.getValue(),
                    price.getValue(),
                    description.getValue());
            clearForm();
            Notification.show("Offer erstellt: " + result.offerId(), 4000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (RuntimeException exception) {
            showError("Offer konnte nicht gespeichert werden: " + exception.getMessage());
        }
    }

    private void applyDateSelection(CreateOfferDateSelection dateSelection) {
        endDate.setMin(dateSelection.minimumEndDate());
        if (dateSelection.clearEndDate()) {
            endDate.clear();
        }
        dateSummary.setText(dateSelection.summary());
    }

    private void clearForm() {
        offerType.clear();
        startDate.clear();
        endDate.clear();
        applyDateSelection(offerService.updateCreateOfferDateSelection(startDate.getValue(), endDate.getValue()));
        pet.clear();
        price.clear();
        description.clear();
    }

    private void showError(String message) {
        Notification.show(message, 5000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
