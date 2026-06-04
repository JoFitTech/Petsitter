package com.softwareengineering.petsitter.ui.user;

import com.softwareengineering.petsitter.pet.dto.PetDeletionAction;
import com.softwareengineering.petsitter.pet.dto.PetDeletionDecision;
import com.softwareengineering.petsitter.pet.dto.PetDeletionImpact;
import com.softwareengineering.petsitter.pet.dto.PetDeletionOfferImpact;
import com.softwareengineering.petsitter.pet.domain.PetSpecies;
import com.softwareengineering.petsitter.pet.domain.PetTag;
import com.softwareengineering.petsitter.pet.domain.PetVaccinationStatus;
import com.softwareengineering.petsitter.pet.dto.PetDto;
import com.softwareengineering.petsitter.pet.service.PetService;
import com.softwareengineering.petsitter.ui.shared.ImageComponents;
import com.softwareengineering.petsitter.ui.shared.PendingImageChange;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import java.time.LocalDate;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MyPetView extends Div {

    private static final String DARK = "#4a3428";
    private static final String CARD_BG = "#ffffff";

    private final PetService petService;
    private final Div petListContainer;

    public MyPetView(PetService petService) {
        this.petService = petService;

        setWidthFull();
        getStyle()
                .set("background", CARD_BG)
                .set("border-radius", "20px")
                .set("padding", "36px")
                .set("box-shadow", "0 8px 32px rgba(74,52,40,0.09)")
                .set("box-sizing", "border-box");

        add(buildHeader());

        petListContainer = new Div();
        petListContainer.setWidthFull();
        add(petListContainer);

        loadPets();
    }

    private void loadPets() {
        petListContainer.removeAll();
        List<PetDto> pets = petService.getPetDtosForCurrentUser();
        if (pets.isEmpty()) {
            Span empty = new Span("Noch keine Tiere hinterlegt.");
            empty.getStyle().set("color", "#a08060").set("font-style", "italic").set("font-size", "15px");
            petListContainer.add(empty);
        } else {
            pets.forEach(pet -> petListContainer.add(buildPetCard(pet)));
        }
    }

    private Div buildHeader() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        row.getStyle().set("margin-bottom", "28px");

        VerticalLayout titleLayout = new VerticalLayout();
        titleLayout.setPadding(false);
        titleLayout.setSpacing(false);

        H2 title = new H2("Meine Tiere");
        title.getStyle().set("margin", "0").set("font-size", "28px").set("font-weight", "800").set("color", DARK);

        Span subtitle = new Span("*Diese Inhalte werden automatisch in deine Aufträge übernommen*");
        subtitle.getStyle().set("margin", "4px 0 0 0").set("font-size", "13px").set("color", "#a08060")
                .set("font-style", "italic").set("font-weight", "700");

        titleLayout.add(title, subtitle);

        Button addBtn = new Button("Tier hinzufügen", new Icon(VaadinIcon.PLUS));
        addBtn.getStyle()
                .set("border-radius", "24px")
                .set("background", "#774f35")
                .set("color", "white")
                .set("box-shadow", "none")
                .set("font-weight", "600")
                .set("font-size", "14px")
                .set("padding", "0 20px")
                .set("height", "40px")
                .set("cursor", "pointer");
        addBtn.addClickListener(e -> openPetDialog(null));

        row.add(titleLayout, addBtn);

        Div wrapper = new Div();
        wrapper.setWidthFull();
        wrapper.add(row);
        return wrapper;
    }

    private Div buildPetCard(PetDto pet) {
        Div card = new Div();
        card.addClassName("pet-card");
        card.getStyle()
                .set("border", "1px solid #ead5ae")
                .set("border-radius", "16px")
                .set("padding", "28px")
                .set("margin-bottom", "20px")
                .set("background", "#ffffff")
                .set("box-sizing", "border-box");

        HorizontalLayout topRow = new HorizontalLayout();
        topRow.addClassName("pet-card-top-row");
        topRow.setWidthFull();
        topRow.setAlignItems(FlexComponent.Alignment.START);
        topRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        HorizontalLayout leftSection = new HorizontalLayout();
        leftSection.setSpacing(false);
        leftSection.getStyle().set("gap", "24px");
        leftSection.setAlignItems(FlexComponent.Alignment.START);

        Div avatar = ImageComponents.avatar(pet.profileImage(), 100, "#e3cda8");

        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);

        H3 nameSpan = new H3(pet.name());
        nameSpan.getStyle().set("margin", "0 0 8px 0").set("font-size", "22px").set("font-weight", "800").set("color",
                DARK);
        info.add(nameSpan);

        String speciesText = displaySpeciesLabel(pet);
        String breedText = pet.breed() != null && !pet.breed().isBlank() ? pet.breed() : "";
        String ageText = pet.birthDate() != null ? formatAge(pet.birthDate()) : "";

        for (String detail : new String[] { speciesText, breedText, ageText }) {
            if (!detail.isBlank()) {
                Span s = new Span(detail);
                s.getStyle().set("font-size", "15px").set("color", "#7a6050").set("font-weight", "700")
                        .set("font-style", "italic").set("line-height", "1.4");
                info.add(s);
            }
        }
        info.add(buildPetTagChips(pet));

        leftSection.add(avatar, info);

        HorizontalLayout btnRow = new HorizontalLayout();
        btnRow.addClassName("pet-card-btn-row");
        btnRow.setSpacing(false);
        btnRow.getStyle().set("gap", "10px");

        Button editBtn = new Button(new Icon(VaadinIcon.PENCIL));
        Span editBtnText = new Span("Bearbeiten");
        editBtnText.addClassName("pet-edit-btn-text");
        editBtn.getElement().appendChild(editBtnText.getElement());
        editBtn.addClassName("pet-edit-btn");
        editBtn.getStyle()
                .set("border-radius", "24px")
                .set("background", "#774f35")
                .set("color", "white")
                .set("box-shadow", "none")
                .set("font-weight", "600")
                .set("font-size", "14px")
                .set("padding", "0 20px")
                .set("height", "40px")
                .set("cursor", "pointer");
        editBtn.addClickListener(e -> openPetDialog(pet));

        Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
        deleteBtn.getStyle()
                .set("border-radius", "24px")
                .set("background", "#e8ddd4")
                .set("color", DARK)
                .set("box-shadow", "none")
                .set("height", "40px")
                .set("width", "40px")
                .set("padding", "0")
                .set("cursor", "pointer");
        deleteBtn.addClickListener(e -> confirmDelete(pet));

        btnRow.add(editBtn, deleteBtn);
        topRow.add(leftSection, btnRow);

        VerticalLayout bottomSection = new VerticalLayout();
        bottomSection.setPadding(false);
        bottomSection.setSpacing(false);
        bottomSection.getStyle().set("margin-top", "24px");

        Span infosLabel = new Span("Wichtige Infos");
        infosLabel.getStyle().set("font-weight", "800").set("font-size", "18px").set("color", DARK).set("margin-bottom",
                "10px");

        Div infosBox = new Div();
        infosBox.addClassName("pet-card-infos-box");
        infosBox.getStyle()
                .set("border", "1px solid #ead5ae")
                .set("border-radius", "12px")
                .set("min-height", "80px")
                .set("padding", "14px 16px")
                .set("width", "100%")
                .set("background", "#fbf8f1")
                .set("font-size", "14px")
                .set("color", "#7a6050")
                .set("line-height", "1.6")
                .set("box-sizing", "border-box");

        if (pet.notes() != null && !pet.notes().isBlank()) {
            infosBox.add(new Span(pet.notes()));
        } else {
            Span noNotes = new Span("Keine besonderen Infos hinterlegt.");
            noNotes.getStyle().set("font-style", "italic").set("color", "#b0907a");
            infosBox.add(noNotes);
        }

        bottomSection.add(infosLabel, infosBox);
        card.add(topRow, bottomSection);
        return card;
    }

    private void openPetDialog(PetDto existing) {
        AddPetPopUp dialog = new AddPetPopUp(existing, petService, result -> {
            PetDto dto = result.pet();
            UUID petId;
            if (existing == null) {
                petId = petService.createPetForCurrentUser(dto).id();
                Notification.show("Tier hinzugefügt.", 2500, Notification.Position.TOP_CENTER);
            } else {
                petService.updatePet(existing.id(), dto);
                petId = existing.id();
                Notification.show("Änderungen gespeichert.", 2500, Notification.Position.TOP_CENTER);
            }
            applyImageChange(petId, result.imageChange());
            loadPets();
        });
        dialog.open();
    }

    private void applyImageChange(UUID petId, PendingImageChange imageChange) {
        if (imageChange.type() == PendingImageChange.Type.REPLACE) {
            petService.replaceCurrentUserPetImage(petId, imageChange.content());
        } else if (imageChange.type() == PendingImageChange.Type.REMOVE) {
            petService.removeCurrentUserPetImage(petId);
        }
    }

    private void confirmDelete(PetDto pet) {
        PetDeletionImpact impact;
        try {
            impact = petService.analyzeCurrentUserPetDeletion(pet.id());
        } catch (Exception ex) {
            Notification.show("Fehler: " + ex.getMessage(), 3500, Notification.Position.TOP_CENTER);
            return;
        }

        if (impact.hasBlockingOffers()) {
            openBlockedDeleteDialog(pet, impact);
            return;
        }
        openDeleteDecisionDialog(pet, impact);
    }

    private void openDeleteDecisionDialog(PetDto pet, PetDeletionImpact impact) {
        Dialog confirm = new Dialog();
        confirm.setWidth(impact.hasAffectedOffers() ? "560px" : "400px");
        confirm.setMaxWidth("95vw");
        confirm.setCloseOnEsc(false);
        confirm.setCloseOnOutsideClick(false);
        confirm.getElement().getThemeList().add("no-padding");
        confirm.getElement().getStyle()
                .set("border-radius", "20px")
                .set("font-family", "'Inter', sans-serif");

        Div wrapper = new Div();
        wrapper.getStyle()
                .set("background-color", "#f3eada")
                .set("border-radius", "20px")
                .set("padding", "32px 48px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "20px")
                .set("font-family", "'Inter', sans-serif")
                .set("position", "relative")
                .set("box-sizing", "border-box");

        Button closeBtn = new Button(new Icon(VaadinIcon.CLOSE));
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeBtn.getStyle()
                .set("position", "absolute")
                .set("top", "24px")
                .set("right", "24px")
                .set("color", DARK)
                .set("font-size", "22px")
                .set("cursor", "pointer")
                .set("background", "transparent")
                .set("border", "none")
                .set("box-shadow", "none")
                .set("padding", "0")
                .set("z-index", "10");
        closeBtn.addClickListener(e -> confirm.close());

        H3 title = new H3("Tier löschen?");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "22px")
                .set("font-weight", "800")
                .set("padding-right", "32px")
                .set("color", DARK);

        Paragraph msg = new Paragraph(deleteDialogMessage(pet, impact));
        msg.getStyle()
                .set("margin", "0")
                .set("font-size", "15px")
                .set("color", DARK)
                .set("font-weight", "600")
                .set("line-height", "1.5");

        Map<UUID, RadioButtonGroup<PetDeletionAction>> decisionGroups = new LinkedHashMap<>();
        if (impact.hasAffectedOffers()) {
            wrapper.add(closeBtn, title, msg, buildAffectedOffersSection(impact, decisionGroups));
        } else {
            wrapper.add(closeBtn, title, msg);
        }

        Button yes = styledSaveBtn("Löschen");
        yes.addClickListener(e -> {
            try {
                petService.deleteCurrentUserPet(pet.id(), selectedDecisions(impact, decisionGroups));
                confirm.close();
                Notification.show(pet.name() + " wurde gelöscht.", 2500, Notification.Position.TOP_CENTER);
                loadPets();
            } catch (Exception ex) {
                Notification.show("Fehler: " + ex.getMessage(), 3500, Notification.Position.TOP_CENTER);
            }
        });

        HorizontalLayout btns = new HorizontalLayout(yes);
        btns.setWidthFull();
        btns.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        wrapper.add(btns);
        confirm.add(wrapper);
        confirm.open();
    }

    private void openBlockedDeleteDialog(PetDto pet, PetDeletionImpact impact) {
        Dialog dialog = new Dialog();
        dialog.setWidth("560px");
        dialog.setMaxWidth("95vw");
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.getElement().getThemeList().add("no-padding");
        dialog.getElement().getStyle()
                .set("border-radius", "20px")
                .set("font-family", "'Inter', sans-serif");

        Div wrapper = new Div();
        wrapper.getStyle()
                .set("background-color", "#f3eada")
                .set("border-radius", "20px")
                .set("padding", "32px 48px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "18px")
                .set("font-family", "'Inter', sans-serif")
                .set("position", "relative")
                .set("box-sizing", "border-box");

        Button closeBtn = new Button(new Icon(VaadinIcon.CLOSE));
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeBtn.getStyle()
                .set("position", "absolute")
                .set("top", "24px")
                .set("right", "24px")
                .set("color", DARK)
                .set("font-size", "22px")
                .set("cursor", "pointer")
                .set("background", "transparent")
                .set("border", "none")
                .set("box-shadow", "none")
                .set("padding", "0")
                .set("z-index", "10");
        closeBtn.addClickListener(e -> dialog.close());

        H3 title = new H3("Tier kann noch nicht gelöscht werden");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "22px")
                .set("font-weight", "800")
                .set("padding-right", "32px")
                .set("color", DARK);

        Paragraph msg = new Paragraph(pet.name()
                + " ist noch in gebuchten oder stornierten Angeboten hinterlegt. Diese Fälle werden später über die Buchungslogik bereinigt, damit beteiligte Sitter informiert werden können.");
        msg.getStyle()
                .set("margin", "0")
                .set("font-size", "15px")
                .set("color", DARK)
                .set("line-height", "1.5");

        Button ok = styledSaveBtn("Verstanden");
        ok.addClickListener(event -> dialog.close());
        HorizontalLayout btns = new HorizontalLayout(ok);
        btns.setWidthFull();
        btns.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        wrapper.add(closeBtn, title, msg, buildBlockingOffersSection(impact), btns);
        dialog.add(wrapper);
        dialog.open();
    }

    private Component buildAffectedOffersSection(PetDeletionImpact impact,
            Map<UUID, RadioButtonGroup<PetDeletionAction>> decisionGroups) {
        VerticalLayout list = new VerticalLayout();
        list.setPadding(false);
        list.setSpacing(false);
        list.getStyle().set("gap", "12px");

        for (PetDeletionOfferImpact offer : impact.offers()) {
            Div item = offerImpactItem(offer);
            if (offer.requiresDecision()) {
                RadioButtonGroup<PetDeletionAction> group = new RadioButtonGroup<>();
                group.setItems(offer.availableActions());
                group.setItemLabelGenerator(this::actionLabel);
                group.setValue(PetDeletionAction.REMOVE_PET_FROM_OFFER);
                group.getStyle().set("margin-top", "8px");
                decisionGroups.put(offer.offerId(), group);
                item.add(group);
            } else {
                Span action = new Span("Dieses Angebot enthält nur dieses Tier und wird mit gelöscht.");
                action.getStyle().set("display", "block").set("margin-top", "8px")
                        .set("font-size", "13px").set("font-weight", "700").set("color", "#9a4f36");
                item.add(action);
            }
            list.add(item);
        }
        return list;
    }

    private Component buildBlockingOffersSection(PetDeletionImpact impact) {
        VerticalLayout list = new VerticalLayout();
        list.setPadding(false);
        list.setSpacing(false);
        list.getStyle().set("gap", "12px");

        impact.offers().stream()
                .filter(PetDeletionOfferImpact::blocksDeletion)
                .map(this::offerImpactItem)
                .forEach(list::add);
        return list;
    }

    private Div offerImpactItem(PetDeletionOfferImpact offer) {
        Div item = new Div();
        item.getStyle()
                .set("border", "1px solid #ead5ae")
                .set("border-radius", "12px")
                .set("background", "#fbf8f1")
                .set("padding", "14px 16px");

        Span title = new Span(offer.title());
        title.getStyle().set("display", "block").set("font-weight", "800").set("font-size", "15px").set("color", DARK);

        Span details = new Span(statusLabel(offer) + " · " + offer.petCount()
                + (offer.petCount() == 1 ? " Tier" : " Tiere"));
        details.getStyle().set("display", "block").set("margin-top", "4px")
                .set("font-size", "13px").set("font-weight", "700").set("color", "#7a6050");

        item.add(title, details);
        return item;
    }

    private List<PetDeletionDecision> selectedDecisions(PetDeletionImpact impact,
            Map<UUID, RadioButtonGroup<PetDeletionAction>> decisionGroups) {
        return impact.offers().stream()
                .map(offer -> new PetDeletionDecision(
                        offer.offerId(),
                        offer.requiresDecision()
                                ? decisionGroups.get(offer.offerId()).getValue()
                                : PetDeletionAction.DELETE_OFFER))
                .toList();
    }

    private String deleteDialogMessage(PetDto pet, PetDeletionImpact impact) {
        if (!impact.hasAffectedOffers()) {
            return "Möchtest du " + pet.name() + " wirklich löschen?";
        }
        return pet.name()
                + " ist noch in offenen Angeboten hinterlegt. Prüfe die Auswirkungen und bestätige die Löschung.";
    }

    private String actionLabel(PetDeletionAction action) {
        return switch (action) {
            case REMOVE_PET_FROM_OFFER -> "Nur Tier aus Angebot entfernen";
            case DELETE_OFFER -> "Gesamtes Angebot löschen";
        };
    }

    private String statusLabel(PetDeletionOfferImpact offer) {
        return switch (offer.status()) {
            case DRAFT -> "Entwurf";
            case OPEN -> "Offen";
            case BOOKED -> "Gebucht";
            case CANCELLED -> "Storniert";
        };
    }

    private String displaySpeciesLabel(PetDto pet) {
        if (pet.species() == PetSpecies.OTHER && pet.customSpecies() != null && !pet.customSpecies().isBlank()) {
            return pet.customSpecies();
        }
        return PetService.speciesLabel(pet.species());
    }

    private HorizontalLayout buildPetTagChips(PetDto pet) {
        HorizontalLayout chips = new HorizontalLayout();
        chips.setPadding(false);
        chips.setSpacing(false);
        chips.getStyle()
                .set("gap", "8px")
                .set("flex-wrap", "wrap")
                .set("margin-top", "12px");

        PetVaccinationStatus vaccinationStatus = pet.vaccinationStatus() == null
                ? PetVaccinationStatus.UNBEKANNT
                : pet.vaccinationStatus();
        chips.add(tagChip(vaccinationStatus.label()));
        if (pet.tags() != null) {
            pet.tags().stream()
                    .filter(java.util.Objects::nonNull)
                    .map(PetTag::label)
                    .map(this::tagChip)
                    .forEach(chips::add);
        }
        return chips;
    }

    private Span tagChip(String label) {
        Span chip = new Span(label);
        chip.getStyle()
                .set("border", "1px solid #ead5ae")
                .set("border-radius", "999px")
                .set("background", "#fbf8f1")
                .set("color", "#7a6050")
                .set("font-size", "12px")
                .set("font-weight", "800")
                .set("padding", "5px 10px")
                .set("line-height", "1.2");
        return chip;
    }

    private String formatAge(LocalDate birthDate) {
        int years = Period.between(birthDate, LocalDate.now()).getYears();
        if (years == 0) {
            int months = Period.between(birthDate, LocalDate.now()).getMonths();
            return months <= 1 ? "weniger als 1 Monat alt" : months + " Monate alt";
        }
        return years == 1 ? "1 Jahr alt" : years + " Jahre alt";
    }

    private Button styledSaveBtn(String label) {
        Button btn = new Button(label);
        btn.getStyle()
                .set("border-radius", "24px")
                .set("background", "#5c3d1e")
                .set("color", "white")
                .set("box-shadow", "none")
                .set("font-weight", "700")
                .set("font-size", "15px")
                .set("height", "48px")
                .set("cursor", "pointer")
                .set("border", "none")
                .set("font-family", "'Inter', sans-serif");
        return btn;
    }

    private Button styledCancelBtn(String label) {
        Button btn = new Button(label);
        btn.getStyle()
                .set("border-radius", "24px")
                .set("background", "#e8ddd4")
                .set("color", DARK)
                .set("box-shadow", "none")
                .set("font-weight", "600")
                .set("height", "38px")
                .set("cursor", "pointer");
        return btn;
    }
}
