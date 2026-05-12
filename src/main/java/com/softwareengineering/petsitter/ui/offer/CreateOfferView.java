package com.softwareengineering.petsitter.ui.offer;

import com.softwareengineering.petsitter.offer.domain.OfferAnimalType;
import com.softwareengineering.petsitter.offer.domain.OfferCareType;
import com.softwareengineering.petsitter.offer.domain.OfferFrequency;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import com.softwareengineering.petsitter.offer.dto.CreateOfferDateSelection;
import com.softwareengineering.petsitter.offer.dto.CreateOfferFormData;
import com.softwareengineering.petsitter.offer.dto.CreateOfferResult;
import com.softwareengineering.petsitter.offer.dto.OfferPetOptionDto;
import com.softwareengineering.petsitter.offer.service.OfferService;
import com.softwareengineering.petsitter.ui.shared.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Route(value = "auftrag-erstellen", layout = MainLayout.class)
@PageTitle("Auftrag erstellen | Pawsitter")
public class CreateOfferView extends VerticalLayout implements BeforeEnterObserver {

    private static final String DARK       = "#4a3428";
    private static final String BROWN      = "#7b5236";
    private static final String LIGHT_BG   = "#fbf8f1";
    private static final String CARD_BG    = "#ffffff";
    private static final String CARD_SHADOW = "0 12px 30px rgba(74, 52, 40, 0.10)";
    private static final String BEIGE      = "#f6e8d0";
    private static final String BORDER     = "#eadfce";

    private final OfferService offerService;

    private Upload imageUpload;
    private MultiFileMemoryBuffer uploadBuffer;
    private Div imagePreviewArea;
    private final List<String> uploadedFileNames = new ArrayList<>();

    private CreateOfferFormData formData;
    private OfferType currentOfferType;
    private TextField titleField;
    private Select<OfferAnimalType> animalTypeSelect;
    private ComboBox<OfferPetOptionDto> petSelect;
    private RadioButtonGroup<OfferFrequency> frequencyGroup;
    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;
    private Span dateSummary;
    private RadioButtonGroup<OfferCareType> careTypeGroup;
    private BigDecimalField priceField;
    private Span priceSummary;
    private TextArea additionalInfoArea;

    public CreateOfferView(OfferService offerService) {
        this.offerService = offerService;

        setWidthFull();
        setPadding(false);
        setSpacing(false);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        getStyle()
                .set("background", LIGHT_BG)
                .set("overflow-x", "hidden");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        removeAll();
        java.util.List<String> modes = event.getLocation().getQueryParameters().getParameters().get("mode");
        String mode = (modes != null && !modes.isEmpty()) ? modes.get(0) : "offer";
        currentOfferType = "request".equals(mode) ? OfferType.OWNER_OFFER : OfferType.SITTER_OFFER;
        formData = offerService.getCreateOfferFormData();
        animalTypeSelect = null;
        petSelect = null;
        uploadedFileNames.clear();
        
        String pageBg = "request".equals(mode) ? "#ebf6f0" : LIGHT_BG;
        getStyle().set("background", pageBg);
        
        add(createPageWrapper(mode, pageBg));
        if (!offerService.hasAuthenticatedUser()) {
            showError("Kein eingeloggter DB-User gefunden. Bitte mit einem gespeicherten User anmelden.");
        }
    }

    // ── Page wrapper with background blobs ────────────────────────────────
    private Component createPageWrapper(String mode, String pageBg) {
        Div wrapper = new Div();
        wrapper.setWidthFull();
        wrapper.getStyle()
                .set("position", "relative")
                .set("overflow-x", "hidden")
                .set("background", pageBg);

        wrapper.add(createBackgroundBlobs(mode), createContentSection(mode));
        return wrapper;
    }

    private Component createBackgroundBlobs(String mode) {
        Div container = new Div();

        Div leftBlob = new Div();
        leftBlob.getStyle()
                .set("position", "absolute")
                .set("left", "-130px")
                .set("top", "60px")
                .set("width", "400px")
                .set("height", "400px")
                .set("background", "request".equals(mode) ? "#e2f5ec" : "#f6ead5")
                .set("border-radius", "50%")
                .set("z-index", "0");

        Div rightBlob = new Div();
        rightBlob.getStyle()
                .set("position", "absolute")
                .set("right", "-100px")
                .set("top", "100px")
                .set("width", "440px")
                .set("height", "440px")
                .set("background", "request".equals(mode) ? "#eef0fa" : "#e7f0f0")
                .set("border-radius", "50%")
                .set("z-index", "0");

        container.add(leftBlob, rightBlob);
        return container;
    }

    // ── Main content ──────────────────────────────────────────────────────
    private Component createContentSection(String mode) {
        Div section = new Div();
        section.getStyle()
                .set("position", "relative")
                .set("z-index", "1")
                .set("max-width", "860px")
                .set("width", "100%")
                .set("margin", "40px auto 60px auto")
                .set("padding", "0 24px")
                .set("box-sizing", "border-box");

        // ── Top row: headline + drafts button ────────────────────────────
        HorizontalLayout topRow = new HorizontalLayout();
        topRow.setWidthFull();
        topRow.setAlignItems(FlexComponent.Alignment.CENTER);
        topRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        topRow.getStyle().set("margin-bottom", "16px");

        VerticalLayout titleLayout = new VerticalLayout();
        titleLayout.setPadding(false);
        titleLayout.setSpacing(false);

        String headlineText = "request".equals(mode) ? "Neuen Auftrag erstellen" : "Neues Angebot erstellen";
        H1 headline = new H1(headlineText);
        headline.getStyle()
                .set("font-size", "28px")
                .set("color", DARK)
                .set("margin", "0 0 4px 0");

        String subtitleText = "request".equals(mode) ? "Details für deine Haustierbetreuung angeben" : "Details für dein Betreuungsangebot angeben";
        Paragraph subtitle = new Paragraph(subtitleText);
        subtitle.getStyle()
                .set("font-size", "15px")
                .set("color", "#7b7069")
                .set("margin", "0");

        titleLayout.add(headline, subtitle);

        Button draftsButton = new Button("Entwürfe");
        draftsButton.getStyle()
                .set("background", BEIGE)
                .set("color", BROWN)
                .set("border", "1px solid " + BORDER)
                .set("border-radius", "22px")
                .set("padding", "0 24px")
                .set("height", "40px")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("cursor", "pointer");
        draftsButton.addClickListener(e -> onDraftsClicked());

        topRow.add(titleLayout, draftsButton);

        // ── Card ─────────────────────────────────────────────────────────
        Div card = new Div();
        card.getStyle()
                .set("background", CARD_BG)
                .set("border-radius", "24px")
                .set("box-shadow", CARD_SHADOW)
                .set("padding", "32px 40px 36px 40px")
                .set("box-sizing", "border-box")
                .set("width", "100%");

        card.add(
                createImageUploadSection(mode),
                createImagePreviewSection(mode),
                createSpacer("16px"),
                createTitleSection(),
                createSpacer("16px")
        );

        if (isSitterOffer()) {
            card.add(createAnimalTypeSection(), createSpacer("16px"));
        }
        if (isOwnerOffer()) {
            card.add(createPetSection(), createSpacer("16px"));
        }

        card.add(
                createZeitraumSection(),
                createSpacer("16px"),
                createCareTypeSection(),
                createSpacer("16px"),
                createPriceSection(),
                createSpacer("16px"),
                createAdditionalInfoSection(mode),
                createSpacer("24px"),
                createActionButtons()
        );

        section.add(topRow, card);
        return section;
    }

    // ── Helper: vertical spacer ───────────────────────────────────────────
    private Component createSpacer(String height) {
        Div spacer = new Div();
        spacer.getStyle().set("height", height);
        return spacer;
    }

    // ── 1. Image upload button ────────────────────────────────────────────
    private Component createImageUploadSection(String mode) {
        uploadBuffer = new MultiFileMemoryBuffer();
        imageUpload = new Upload(uploadBuffer);
        imageUpload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp", "image/gif");
        imageUpload.setMaxFiles(10);
        imageUpload.setMaxFileSize(10 * 1024 * 1024); // 10 MB

        String btnText = isOwnerOffer()
                ? "📷  Lade hier Bilder deiner Haustiere hoch"
                : "📷  Lade hier Bilder von dir hoch";
        Button uploadBtn = new Button(btnText);
        uploadBtn.getStyle()
                .set("width", "100%")
                .set("height", "56px")
                .set("background", BROWN)
                .set("color", "white")
                .set("border-radius", "28px")
                .set("font-size", "16px")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("cursor", "pointer");

        imageUpload.setUploadButton(uploadBtn);

        // Hide the default drag-drop text
        imageUpload.setDropLabel(new Span(""));
        imageUpload.setDropLabelIcon(new Span(""));

        imageUpload.getStyle()
                .set("width", "100%")
                .set("border", "none")
                .set("background", "transparent");

        imageUpload.addSucceededListener(event -> {
            uploadedFileNames.add(event.getFileName());
            System.out.println("Bild hochgeladen: " + event.getFileName());
            onImageUploaded(event.getFileName());
        });

        return imageUpload;
    }

    // ── 2. Image preview area ─────────────────────────────────────────────
    private Component createImagePreviewSection(String mode) {
        imagePreviewArea = new Div();
        imagePreviewArea.getStyle()
                .set("width", "100%")
                .set("min-height", "140px")
                .set("background", BEIGE)
                .set("border-radius", "16px")
                .set("margin-top", "16px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("flex-wrap", "wrap")
                .set("gap", "10px")
                .set("padding", "16px")
                .set("box-sizing", "border-box");

        String previewText = imagePreviewPlaceholderText();
        Span placeholder = new Span(previewText);
        placeholder.setId("preview-placeholder");
        placeholder.getStyle()
                .set("color", BROWN)
                .set("font-size", "16px")
                .set("font-weight", "600");

        imagePreviewArea.add(placeholder);
        return imagePreviewArea;
    }

    // ── 3. Title ──────────────────────────────────────────────────────────
    private Component createTitleSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);

        NativeLabel label = new NativeLabel(isOwnerOffer() ? "Titel des Auftrags" : "Titel des Angebots");
        label.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "700")
                .set("color", BROWN)
                .set("margin-bottom", "8px")
                .set("display", "block");

        titleField = new TextField();
        titleField.setWidthFull();
        titleField.setMaxLength(formData.titleMaxLength());
        titleField.getStyle()
                .set("border-radius", "12px")
                .set("border", "1px solid " + BORDER);

        section.add(label, titleField);
        return section;
    }

    // ── 4. Animal type select ─────────────────────────────────────────────
    private Component createAnimalTypeSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);

        NativeLabel label = new NativeLabel("Tierart auswählen (optional)");
        label.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "700")
                .set("color", BROWN)
                .set("margin-bottom", "8px")
                .set("display", "block");

        animalTypeSelect = new Select<>();
        animalTypeSelect.setItems(formData.animalTypes());
        animalTypeSelect.setItemLabelGenerator(animalType ->
                animalType == null ? "Egal / keine Präferenz" : animalType.label());
        animalTypeSelect.setEmptySelectionAllowed(true);
        animalTypeSelect.setEmptySelectionCaption("Egal / keine Präferenz");
        animalTypeSelect.setWidthFull();
        animalTypeSelect.getStyle()
                .set("border-radius", "12px");

        section.add(label, animalTypeSelect);
        return section;
    }

    private Component createPetSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);

        NativeLabel label = new NativeLabel("Haustier auswählen");
        label.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "700")
                .set("color", BROWN)
                .set("margin-bottom", "8px")
                .set("display", "block");

        petSelect = new ComboBox<>();
        petSelect.setItems(formData.pets());
        petSelect.setItemLabelGenerator(OfferPetOptionDto::label);
        petSelect.setPlaceholder("Wähle ein Haustier");
        petSelect.setClearButtonVisible(true);
        petSelect.setWidthFull();

        section.add(label, petSelect);
        return section;
    }

    // ── 5. Zeitraum (frequency + date range) ──────────────────────────────
    private Component createZeitraumSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);

        NativeLabel label = new NativeLabel("Zeitraum");
        label.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "700")
                .set("color", BROWN)
                .set("margin-bottom", "4px")
                .set("display", "block");

        frequencyGroup = new RadioButtonGroup<>();
        frequencyGroup.setItems(formData.frequencies());
        frequencyGroup.setItemLabelGenerator(OfferFrequency::label);
        frequencyGroup.setValue(OfferFrequency.ONE_TIME);
        frequencyGroup.getStyle()
                .set("color", DARK)
                .set("font-size", "14px");

        HorizontalLayout dateRow = new HorizontalLayout();
        dateRow.setWidthFull();
        dateRow.setSpacing(true);
        dateRow.getStyle().set("margin-top", "10px").set("gap", "16px");

        fromDatePicker = new DatePicker("von");
        fromDatePicker.setWidthFull();
        fromDatePicker.setMin(formData.minimumStartDate());
        fromDatePicker.getStyle().set("border-radius", "12px");
        fromDatePicker.addValueChangeListener(event -> applyDateSelection(
                offerService.updateCreateOfferDateSelection(event.getValue(), toDatePicker.getValue())));

        toDatePicker = new DatePicker("bis");
        toDatePicker.setWidthFull();
        toDatePicker.getStyle().set("border-radius", "12px");
        toDatePicker.addValueChangeListener(event -> applyDateSelection(
                offerService.updateCreateOfferDateSelection(fromDatePicker.getValue(), event.getValue())));

        dateSummary = new Span();
        dateSummary.getStyle()
                .set("display", "block")
                .set("margin-top", "8px")
                .set("color", "#7a6050")
                .set("font-size", "13px")
                .set("font-weight", "600");

        dateRow.add(fromDatePicker, toDatePicker);
        applyDateSelection(formData.dateSelection());

        section.add(label, frequencyGroup, dateRow, dateSummary);
        return section;
    }

    // ── 6. Care type (Tiersitting / Tiersitting + Haussitting) ───────────
    private Component createCareTypeSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);

        NativeLabel label = new NativeLabel("Art der Betreuung");
        label.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "700")
                .set("color", BROWN)
                .set("margin-bottom", "4px")
                .set("display", "block");

        careTypeGroup = new RadioButtonGroup<>();
        careTypeGroup.setItems(formData.careTypes());
        careTypeGroup.setItemLabelGenerator(OfferCareType::label);
        careTypeGroup.setValue(OfferCareType.PET_SITTING);
        careTypeGroup.getStyle()
                .set("color", DARK)
                .set("font-size", "14px");

        section.add(label, careTypeGroup);
        return section;
    }

    private Component createPriceSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);

        NativeLabel label = new NativeLabel("Preis pro Tag");
        label.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "700")
                .set("color", BROWN)
                .set("margin-bottom", "8px")
                .set("display", "block");

        priceField = new BigDecimalField();
        priceField.setPrefixComponent(new Span("EUR"));
        priceField.setClearButtonVisible(true);
        priceField.setWidthFull();
        priceField.addValueChangeListener(event -> updatePriceSummary());

        priceSummary = new Span();
        priceSummary.getStyle()
                .set("display", "block")
                .set("margin-top", "8px")
                .set("color", "#7a6050")
                .set("font-size", "13px")
                .set("font-weight", "600");
        updatePriceSummary();

        section.add(label, priceField, priceSummary);
        return section;
    }

    // ── 7. Additional info ────────────────────────────────────────────────
    private Component createAdditionalInfoSection(String mode) {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);

        String infoText = isOwnerOffer()
                ? "Zusätzliche Informationen zu deinem Auftrag"
                : "Zusätzliche Informationen über dich";
        NativeLabel label = new NativeLabel(infoText);
        label.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "700")
                .set("color", BROWN)
                .set("margin-bottom", "8px")
                .set("display", "block");

        additionalInfoArea = new TextArea();
        additionalInfoArea.setWidthFull();
        additionalInfoArea.setMinHeight("120px");
        additionalInfoArea.setMaxLength(formData.descriptionMaxLength());
        additionalInfoArea.getStyle()
                .set("border-radius", "12px")
                .set("border", "1px solid " + BORDER);

        section.add(label, additionalInfoArea);
        return section;
    }

    // ── 8. Action buttons (Entwurf speichern + Hochladen) ─────────────────
    private Component createActionButtons() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setSpacing(true);
        row.getStyle().set("gap", "12px");

        Button saveDraftButton = new Button("Entwurf speichern");
        saveDraftButton.getStyle()
                .set("background", BEIGE)
                .set("color", BROWN)
                .set("border", "1px solid " + BORDER)
                .set("border-radius", "22px")
                .set("padding", "0 28px")
                .set("height", "44px")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("cursor", "pointer");
        saveDraftButton.addClickListener(e -> onSaveDraftClicked());

        Button publishButton = new Button("Hochladen");
        publishButton.getStyle()
                .set("background", BROWN)
                .set("color", "white")
                .set("border-radius", "22px")
                .set("padding", "0 32px")
                .set("height", "44px")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("cursor", "pointer");
        publishButton.addClickListener(e -> onPublishClicked());

        row.add(saveDraftButton, publishButton);
        return row;
    }

    private void onDraftsClicked() {
        System.out.println("Entwürfe anzeigen geklickt");
        // TODO: UI.getCurrent().navigate("entwuerfe");
    }

    private void onImageUploaded(String fileName) {
        System.out.println("Bild-Upload erfolgreich: " + fileName);
        // TODO: Vorschau-Thumbnail aus uploadBuffer rendern und in imagePreviewArea einfügen
        //       Beispiel:
        //       InputStream stream = uploadBuffer.getInputStream(fileName);
        //       StreamResource resource = new StreamResource(fileName, () -> stream);
        //       Image img = new Image(resource, fileName);
        //       img.setWidth("100px"); img.setHeight("100px");
        //       imagePreviewArea.add(img);

        // Minimal visual feedback: remove placeholder text on first upload
        imagePreviewArea.getChildren()
                .filter(c -> c.getId().map(id -> id.equals("preview-placeholder")).orElse(false))
                .findFirst()
                .ifPresent(imagePreviewArea::remove);

        Span hint = new Span("📎 " + fileName);
        hint.getStyle()
                .set("background", BORDER)
                .set("border-radius", "8px")
                .set("padding", "4px 10px")
                .set("font-size", "13px")
                .set("color", DARK);
        imagePreviewArea.add(hint);
    }

    private void onSaveDraftClicked() {
        System.out.println("Entwurf speichern geklickt");
        System.out.println("  Titel:       " + titleField.getValue());
        if (isSitterOffer() && animalTypeSelect != null) {
            System.out.println("  Tierart:     " + animalTypeSelect.getValue());
        }
        if (isOwnerOffer() && petSelect != null) {
            System.out.println("  Haustier:    " + petSelect.getValue());
        }
        System.out.println("  Häufigkeit:  " + frequencyGroup.getValue());
        System.out.println("  Von:         " + fromDatePicker.getValue());
        System.out.println("  Bis:         " + toDatePicker.getValue());
        System.out.println("  Betreuung:   " + careTypeGroup.getValue());
        System.out.println("  Zusatzinfo:  " + additionalInfoArea.getValue());
        System.out.println("  Bilder:      " + uploadedFileNames);

        // TODO: draftService.saveDraft(buildDraftDto());
    }

    private void onPublishClicked() {
        try {
            OfferPetOptionDto selectedPet = isOwnerOffer() ? petSelect.getValue() : null;
            OfferAnimalType selectedAnimalType = isSitterOffer() ? animalTypeSelect.getValue() : null;
            if (isOwnerOffer() && selectedPet == null) {
                showError("Bitte wähle ein Haustier für deinen Auftrag aus.");
                return;
            }

            CreateOfferResult result = offerService.createOffer(
                    currentOfferType,
                    fromDatePicker.getValue(),
                    toDatePicker.getValue(),
                    selectedPet,
                    priceField.getValue(),
                    titleField.getValue(),
                    frequencyGroup.getValue(),
                    careTypeGroup.getValue(),
                    selectedAnimalType,
                    additionalInfoArea.getValue());
            showSuccess("Eintrag wurde gespeichert: " + result.offerId());
            clearForm();
        } catch (RuntimeException exception) {
            showError("Eintrag konnte nicht gespeichert werden: " + exception.getMessage());
        }
    }

    private void applyDateSelection(CreateOfferDateSelection dateSelection) {
        toDatePicker.setMin(dateSelection.minimumEndDate());
        if (dateSelection.clearEndDate()) {
            toDatePicker.clear();
        }
        dateSummary.setText(dateSelection.summary());
        updatePriceSummary();
    }

    private void updatePriceSummary() {
        if (priceSummary == null) {
            return;
        }
        BigDecimal price = priceField == null ? null : priceField.getValue();
        priceSummary.setText(offerService.summarizeCreateOfferTotalPrice(
                fromDatePicker == null ? null : fromDatePicker.getValue(),
                toDatePicker == null ? null : toDatePicker.getValue(),
                price));
    }

    private void clearForm() {
        titleField.clear();
        if (animalTypeSelect != null) {
            animalTypeSelect.clear();
        }
        if (petSelect != null) {
            petSelect.clear();
        }
        frequencyGroup.setValue(OfferFrequency.ONE_TIME);
        fromDatePicker.clear();
        toDatePicker.clear();
        applyDateSelection(offerService.updateCreateOfferDateSelection(fromDatePicker.getValue(), toDatePicker.getValue()));
        careTypeGroup.setValue(OfferCareType.PET_SITTING);
        priceField.clear();
        additionalInfoArea.clear();
        uploadedFileNames.clear();
        if (imagePreviewArea != null) {
            imagePreviewArea.removeAll();
            Span placeholder = new Span(imagePreviewPlaceholderText());
            placeholder.setId("preview-placeholder");
            placeholder.getStyle()
                    .set("color", BROWN)
                    .set("font-size", "16px")
                    .set("font-weight", "600");
            imagePreviewArea.add(placeholder);
        }
    }

    private boolean isOwnerOffer() {
        return currentOfferType == OfferType.OWNER_OFFER;
    }

    private boolean isSitterOffer() {
        return currentOfferType == OfferType.SITTER_OFFER;
    }

    private String imagePreviewPlaceholderText() {
        return isOwnerOffer() ? "Vorschau deiner Haustierbilder" : "Vorschau deiner Bilder";
    }

    private void showError(String message) {
        Notification.show(message, 5000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void showSuccess(String message) {
        Notification.show(message, 4000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}
