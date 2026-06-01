package com.softwareengineering.petsitter.ui.offer;

import com.softwareengineering.petsitter.offer.domain.OfferAnimalType;
import com.softwareengineering.petsitter.offer.domain.OfferCareType;
import com.softwareengineering.petsitter.offer.domain.OfferFrequency;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import com.softwareengineering.petsitter.user.domain.AccountRole;
import com.softwareengineering.petsitter.offer.dto.CreateOfferDateSelection;
import com.softwareengineering.petsitter.offer.dto.CreateOfferFormData;
import com.softwareengineering.petsitter.offer.dto.CreateOfferRequest;
import com.softwareengineering.petsitter.offer.dto.CreateOfferResult;
import com.softwareengineering.petsitter.offer.dto.OfferPetOptionDto;
import com.softwareengineering.petsitter.offer.service.OfferService;
import com.softwareengineering.petsitter.ui.shared.MainLayout;
import com.softwareengineering.petsitter.ui.user.LoginView;
import com.softwareengineering.petsitter.ui.user.UserView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.popover.PopoverVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Route(value = "auftrag-erstellen", layout = MainLayout.class)
@PageTitle("Auftrag erstellen | Pawsitter")
@RolesAllowed(AccountRole.ROLE_SIGNED_IN_USER)
@CssImport(value = "./styles/filter-search-popover.css", themeFor = "vaadin-popover-overlay")
@CssImport(value = "./styles/custom-readonly-field.css", themeFor = "vaadin-text-field")
public class CreateOfferView extends VerticalLayout implements BeforeEnterObserver {

    private static final String DARK = "#4a3428";
    private static final String BROWN = "#7b5236";
    private static final String LIGHT_BG = "#fbf8f1";
    private static final String CARD_BG = "#ffffff";
    private static final String CARD_SHADOW = "0 12px 30px rgba(74, 52, 40, 0.10)";
    private static final String BEIGE = "#f6e8d0";
    private static final String BORDER = "#eadfce";
    private static final Locale GERMAN = Locale.GERMANY;
    private static final DateTimeFormatter DAY_MONTH_FORMATTER =
            DateTimeFormatter.ofPattern("d. MMMM", GERMAN);
    private static final DateTimeFormatter MONTH_YEAR_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM yyyy", GERMAN);
    private static final String RETURN_TO_PARAMETER = "returnTo";
    private static final NavigationTarget DEFAULT_RETURN_TARGET =
            new NavigationTarget("profile", QueryParameters.of("tab", "offers"));

    private final OfferService offerService;

    private Upload imageUpload;
    private MultiFileMemoryBuffer uploadBuffer;
    private Div imagePreviewArea;
    private final List<String> uploadedFileNames = new ArrayList<>();

    private CreateOfferFormData formData;
    private OfferType currentOfferType;
    private UUID editingOfferId;
    private CreateOfferRequest editOfferData;
    private NavigationTarget returnTarget = DEFAULT_RETURN_TARGET;
    private TextField titleField;
    private Select<OfferAnimalType> animalTypeSelect;
    private MultiSelectComboBox<OfferPetOptionDto> petSelect;
    private RadioButtonGroup<OfferFrequency> frequencyGroup;
    private TextField dateRangeField;
    private Popover whenPopover;
    private Div dateCalendar;
    private Span dateRangeStatus;
    private YearMonth displayedMonth;
    private LocalDate selectedFrom;
    private LocalDate selectedTo;
    private boolean selectingRangeEnd;
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
        if (!offerService.hasAuthenticatedUser()) {
            event.rerouteTo(LoginView.class);
            return;
        }

        removeAll();
        java.util.Map<String, java.util.List<String>> parameters =
                event.getLocation().getQueryParameters().getParameters();
        returnTarget = returnTargetFrom(parameters);
        if (!loadEditState(parameters, event)) {
            return;
        }

        if (!isEditMode()) {
            java.util.List<String> modes = parameters.get("mode");
            String mode = (modes != null && !modes.isEmpty()) ? modes.get(0) : "offer";
            currentOfferType = "request".equals(mode) ? OfferType.OWNER_OFFER : OfferType.SITTER_OFFER;
        }

        formData = offerService.getCreateOfferFormData();
        animalTypeSelect = null;
        petSelect = null;
        uploadedFileNames.clear();

        String mode = modeForCurrentOfferType();
        String pageBg = isOwnerOffer() ? LIGHT_BG : "#ebf6f0";
        getStyle().set("background", pageBg);

        add(createPageWrapper(mode, pageBg));
        if (isEditMode()) {
            populateEditForm();
        } else if (isOwnerOffer() && formData.pets().isEmpty()) {
            UI.getCurrent().beforeClientResponse(this, context -> openMissingPetDialog());
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
                .set("background", "request".equals(mode) ? "#f6ead5" : "#e2f5ec")
                .set("border-radius", "50%")
                .set("z-index", "0");

        Div rightBlob = new Div();
        rightBlob.getStyle()
                .set("position", "absolute")
                .set("right", "-100px")
                .set("top", "100px")
                .set("width", "440px")
                .set("height", "440px")
                .set("background", "request".equals(mode) ? "#e7f0f0" : "#eef0fa")
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

        String headlineText = isEditMode()
                ? (isOwnerOffer() ? "Tierhalter Angebot bearbeiten" : "Tiersitter Angebot bearbeiten")
                : ("request".equals(mode) ? "Neues Tierhalter Angebot erstellen" : "Neues Tiersitter Angebot erstellen");
        H1 headline = new H1(headlineText);
        headline.getStyle()
                .set("font-size", "28px")
                .set("color", DARK)
                .set("margin", "0 0 4px 0");

        String subtitleText = isEditMode()
                ? "Passe die gespeicherten Details an."
                : ("request".equals(mode) ? "Details für dein Tierhalter Angebot angeben"
                : "Details für dein Tiersitting Angebot angeben");
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

        if (isEditMode()) {
            topRow.add(titleLayout);
        } else {
            topRow.add(titleLayout, draftsButton);
        }

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
                createSpacer("16px"));

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
                createActionButtons());

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
                ? " Lade hier Bilder deiner Haustiere hoch"
                : " Lade hier Bilder von dir hoch";
        Icon cameraIcon = new Icon(VaadinIcon.CAMERA);
        cameraIcon.setSize("22px");
        cameraIcon.getStyle()
                .set("color", "white")
                .set("flex-shrink", "0");
        Button uploadBtn = new Button(btnText, cameraIcon);
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
        titleField.addValueChangeListener(event -> titleField.setInvalid(false));
        titleField.getStyle()
                .set("border-radius", "12px")
                .set("--vaadin-input-field-background", "#fffdf8")
                .set("border", "1px solid #ead5ae");

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
        animalTypeSelect.setItemLabelGenerator(
                animalType -> animalType == null ? "Egal / keine Präferenz" : animalType.label());
        animalTypeSelect.setEmptySelectionAllowed(true);
        animalTypeSelect.setEmptySelectionCaption("Egal / keine Präferenz");
        animalTypeSelect.setWidthFull();
        animalTypeSelect.getStyle()
                .set("border-radius", "12px")
                .set("--vaadin-input-field-background", "#fffdf8")
                .set("border", "1px solid #ead5ae");

        section.add(label, animalTypeSelect);
        return section;
    }

    private Component createPetSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);

        NativeLabel label = new NativeLabel("Haustiere auswählen");
        label.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "700")
                .set("color", BROWN)
                .set("margin-bottom", "8px")
                .set("display", "block");

        petSelect = new MultiSelectComboBox<>();
        petSelect.setItems(formData.pets());
        petSelect.setItemLabelGenerator(OfferPetOptionDto::label);
        petSelect.setPlaceholder("Wähle Haustiere");
        petSelect.setClearButtonVisible(true);
        petSelect.setWidthFull();
        petSelect.addValueChangeListener(event -> petSelect.setInvalid(false));
        petSelect.getStyle()
                .set("border-radius", "12px")
                .set("--vaadin-input-field-background", "#fffdf8")
                .set("border", "1px solid #ead5ae");

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

        dateRangeField = new TextField();
        dateRangeField.setPlaceholder("Wann?");
        dateRangeField.setReadOnly(true);
        dateRangeField.setWidthFull();
        dateRangeField.getStyle()
                .set("border-radius", "12px")
                .set("border", "none")
                .set("cursor", "pointer")
                .set("margin-top", "10px");

        displayedMonth = initialDisplayedMonth();

        whenPopover = new Popover();
        whenPopover.setTarget(dateRangeField);
        whenPopover.setPosition(PopoverPosition.BOTTOM_START);
        whenPopover.setCloseOnEsc(true);
        whenPopover.setCloseOnOutsideClick(true);
        whenPopover.setOpenOnClick(true);
        whenPopover.setWidth("500px");
        whenPopover.addThemeVariants(PopoverVariant.LUMO_NO_PADDING);
        whenPopover.getElement().getThemeList().add("filter-search-popover");
        whenPopover.add(buildDatePopover());

        dateRangeField.addFocusListener(event -> whenPopover.open());

        dateSummary = new Span();
        dateSummary.getStyle()
                .set("display", "block")
                .set("margin-top", "8px")
                .set("color", "#7a6050")
                .set("font-size", "13px")
                .set("font-weight", "600");

        applyDateSelection(formData.dateSelection());

        section.add(label, frequencyGroup, dateRangeField, whenPopover, dateSummary);
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
        priceField.addValueChangeListener(event -> {
            priceField.setInvalid(false);
            updatePriceSummary();
        });
        priceField.getStyle()
                .set("border-radius", "12px")
                .set("--vaadin-input-field-background", "#fffdf8")
                .set("border", "1px solid #ead5ae");

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
                .set("--vaadin-input-field-background", "#fffdf8")
                .set("border", "1px solid #ead5ae");

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

        Button publishButton = new Button(isEditMode() ? "Änderungen speichern" : "Hochladen");
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

        if (isEditMode()) {
            row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
            row.add(createDeleteOfferButton(), publishButton);
        } else {
            row.add(saveDraftButton, publishButton);
        }
        return row;
    }

    private Button createDeleteOfferButton() {
        Button deleteButton = new Button("Löschen");
        deleteButton.getStyle()
                .set("background", "#f4e0d8")
                .set("color", "#9a4f36")
                .set("border", "1px solid #e2b5a5")
                .set("border-radius", "22px")
                .set("padding", "0 28px")
                .set("height", "44px")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("cursor", "pointer");
        deleteButton.addClickListener(event -> openDeleteConfirmDialog());
        return deleteButton;
    }

    private void openDeleteConfirmDialog() {
        Dialog confirm = new Dialog();
        confirm.setWidth("380px");
        confirm.setCloseOnEsc(true);
        confirm.setCloseOnOutsideClick(false);

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getStyle().set("gap", "18px");

        H3 title = new H3("Offer löschen?");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "20px")
                .set("font-weight", "800")
                .set("color", DARK);

        Paragraph message = new Paragraph("Möchtest du \"" + currentOfferTitle()
                + "\" wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.");
        message.getStyle()
                .set("margin", "0")
                .set("font-size", "14px")
                .set("color", "#7b7069")
                .set("line-height", "1.5");

        Button cancel = new Button("Abbrechen");
        cancel.getStyle()
                .set("background", BEIGE)
                .set("color", BROWN)
                .set("border", "1px solid " + BORDER)
                .set("border-radius", "22px")
                .set("height", "40px")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("cursor", "pointer");
        cancel.addClickListener(event -> confirm.close());

        Button delete = new Button("Löschen");
        delete.getStyle()
                .set("background", "#9a4f36")
                .set("color", "white")
                .set("border-radius", "22px")
                .set("height", "40px")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("cursor", "pointer");
        delete.addClickListener(event -> {
            try {
                offerService.deleteCurrentUserOffer(editingOfferId);
                confirm.close();
                showSuccess("Offer wurde gelöscht.");
                navigateToReturnTarget();
            } catch (RuntimeException exception) {
                showError("Offer konnte nicht gelöscht werden: " + exception.getMessage());
            }
        });

        HorizontalLayout buttons = new HorizontalLayout(cancel, delete);
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.getStyle().set("gap", "10px");

        layout.add(title, message, buttons);
        confirm.add(layout);
        confirm.open();
    }

    private void onDraftsClicked() {
        System.out.println("Entwürfe anzeigen geklickt");
        // TODO: UI.getCurrent().navigate("entwuerfe");
    }

    private void onImageUploaded(String fileName) {
        System.out.println("Bild-Upload erfolgreich: " + fileName);
        // TODO: Vorschau-Thumbnail aus uploadBuffer rendern und in imagePreviewArea
        // einfügen
        // Beispiel:
        // InputStream stream = uploadBuffer.getInputStream(fileName);
        // StreamResource resource = new StreamResource(fileName, () -> stream);
        // Image img = new Image(resource, fileName);
        // img.setWidth("100px"); img.setHeight("100px");
        // imagePreviewArea.add(img);

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
            System.out.println("  Haustiere:   " + petSelect.getValue());
        }
        System.out.println("  Häufigkeit:  " + frequencyGroup.getValue());
        System.out.println("  Von:         " + selectedFrom);
        System.out.println("  Bis:         " + selectedTo);
        System.out.println("  Betreuung:   " + careTypeGroup.getValue());
        System.out.println("  Zusatzinfo:  " + additionalInfoArea.getValue());
        System.out.println("  Bilder:      " + uploadedFileNames);

        // TODO: draftService.saveDraft(buildDraftDto());
    }

    private void onPublishClicked() {
        try {
            List<OfferPetOptionDto> selectedPets = isOwnerOffer() ? selectedPets() : List.of();
            if (isOwnerOffer() && formData.pets().isEmpty()) {
                openMissingPetDialog();
                return;
            }
            if (!validatePublishForm(selectedPets)) {
                return;
            }

            OfferAnimalType selectedAnimalType = isSitterOffer() ? animalTypeSelect.getValue() : null;

            CreateOfferResult result;
            if (isEditMode()) {
                result = offerService.updateCurrentUserOffer(editingOfferId, buildRequest(selectedPets, selectedAnimalType));
                showSuccess("Änderungen wurden gespeichert: " + result.offerId());
                navigateToReturnTarget();
                return;
            }

            result = offerService.createOffer(buildRequest(selectedPets, selectedAnimalType));
            showSuccess("Eintrag wurde gespeichert: " + result.offerId());
            navigateToReturnTarget();
        } catch (RuntimeException exception) {
            showError("Eintrag konnte nicht gespeichert werden: " + exception.getMessage());
        }
    }

    private void openMissingPetDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("420px");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(false);

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getStyle().set("gap", "18px");

        H3 title = new H3("Haustier erforderlich");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "20px")
                .set("font-weight", "800")
                .set("color", DARK);

        Paragraph message = new Paragraph(
                "Für einen Auftrag musst du zuerst mindestens ein Haustier in deinem Profil hinterlegen.");
        message.getStyle()
                .set("margin", "0")
                .set("font-size", "14px")
                .set("color", "#7b7069")
                .set("line-height", "1.5");

        Button cancel = new Button("Abbrechen");
        cancel.getStyle()
                .set("background", BEIGE)
                .set("color", BROWN)
                .set("border", "1px solid " + BORDER)
                .set("border-radius", "22px")
                .set("height", "40px")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("cursor", "pointer");
        cancel.addClickListener(event -> {
            dialog.close();
            navigateToReturnTarget();
        });

        Button addPet = new Button("Haustier hinzufügen");
        addPet.getStyle()
                .set("background", BROWN)
                .set("color", "white")
                .set("border-radius", "22px")
                .set("height", "40px")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("cursor", "pointer");
        addPet.addClickListener(event -> {
            dialog.close();
            UI.getCurrent().navigate("profile", QueryParameters.of("tab", "pets"));
        });

        HorizontalLayout buttons = new HorizontalLayout(cancel, addPet);
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.getStyle().set("gap", "10px");

        layout.add(title, message, buttons);
        dialog.add(layout);
        dialog.open();
    }

    private List<OfferPetOptionDto> selectedPets() {
        return petSelect == null ? List.of() : new ArrayList<>(petSelect.getValue());
    }

    private boolean validatePublishForm(List<OfferPetOptionDto> selectedPets) {
        boolean valid = true;

        if (titleField.getValue().isBlank()) {
            titleField.setInvalid(true);
            titleField.setErrorMessage("Pflichtfeld");
            valid = false;
        }

        if (isOwnerOffer() && selectedPets.isEmpty() && petSelect != null) {
            petSelect.setInvalid(true);
            petSelect.setErrorMessage("Pflichtfeld");
            valid = false;
        }

        LocalDate startDate = selectedFrom;
        LocalDate endDate = selectedTo;

        if (startDate == null) {
            dateRangeField.setInvalid(true);
            dateRangeField.setErrorMessage("Bitte wähle einen Zeitraum aus");
            valid = false;
        } else if (endDate == null) {
            dateRangeField.setInvalid(true);
            dateRangeField.setErrorMessage("Bitte wähle ein Enddatum aus");
            valid = false;
        } else {
            dateRangeField.setInvalid(false);
        }

        if (priceField.getValue() == null) {
            priceField.setInvalid(true);
            priceField.setErrorMessage("Pflichtfeld");
            valid = false;
        }

        return valid;
    }

    private CreateOfferRequest buildRequest(List<OfferPetOptionDto> selectedPets, OfferAnimalType selectedAnimalType) {
        return new CreateOfferRequest(
                currentOfferType,
                selectedFrom,
                selectedTo,
                null,
                selectedPets.stream().map(OfferPetOptionDto::id).toList(),
                priceField.getValue(),
                titleField.getValue(),
                frequencyGroup.getValue(),
                careTypeGroup.getValue(),
                selectedAnimalType,
                additionalInfoArea.getValue());
    }

    private void applyDateSelection(CreateOfferDateSelection dateSelection) {
        if (dateSelection.clearEndDate()) {
            selectedTo = null;
            updateDateFilter();
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
                selectedFrom,
                selectedTo,
                price));
    }

    private void clearForm() {
        titleField.clear();
        titleField.setInvalid(false);
        if (animalTypeSelect != null) {
            animalTypeSelect.clear();
        }
        if (petSelect != null) {
            petSelect.clear();
            petSelect.setInvalid(false);
        }
        frequencyGroup.setValue(OfferFrequency.ONE_TIME);
        selectedFrom = null;
        selectedTo = null;
        if (dateRangeField != null) {
            dateRangeField.clear();
            dateRangeField.setInvalid(false);
        }
        updateDateFilter();
        applyDateSelection(
                offerService.updateCreateOfferDateSelection(null, null));
        careTypeGroup.setValue(OfferCareType.PET_SITTING);
        priceField.clear();
        priceField.setInvalid(false);
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

    private boolean isEditMode() {
        return editingOfferId != null && editOfferData != null;
    }

    private String modeForCurrentOfferType() {
        return isOwnerOffer() ? "request" : "offer";
    }

    private boolean loadEditState(java.util.Map<String, java.util.List<String>> parameters, BeforeEnterEvent event) {
        editingOfferId = null;
        editOfferData = null;

        java.util.List<String> editIds = parameters.get("edit");
        if (editIds == null || editIds.isEmpty() || editIds.get(0) == null || editIds.get(0).isBlank()) {
            return true;
        }

        try {
            editingOfferId = UUID.fromString(editIds.get(0));
            editOfferData = offerService.getCurrentUserOfferForEdit(editingOfferId);
            currentOfferType = editOfferData.offerType();
            return true;
        } catch (RuntimeException exception) {
            Notification.show("Offer kann nicht bearbeitet werden: " + exception.getMessage(),
                    5000,
                    Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
            event.rerouteTo(UserView.class);
            return false;
        }
    }

    private void populateEditForm() {
        titleField.setValue(valueOrEmpty(editOfferData.title()));
        if (animalTypeSelect != null) {
            animalTypeSelect.setValue(editOfferData.animalType());
        }
        if (petSelect != null) {
            petSelect.setValue(findPetOptions(editOfferData.petIds()));
        }
        frequencyGroup.setValue(editOfferData.frequency() != null ? editOfferData.frequency() : OfferFrequency.ONE_TIME);
        selectedFrom = editableDateOrNull(editOfferData.startDate());
        selectedTo = editableDateOrNull(editOfferData.endDate());
        displayedMonth = initialDisplayedMonth();
        updateDateFilter();
        applyDateSelection(offerService.updateCreateOfferDateSelection(selectedFrom, selectedTo));
        careTypeGroup.setValue(editOfferData.careType() != null ? editOfferData.careType() : OfferCareType.PET_SITTING);
        priceField.setValue(editOfferData.price());
        additionalInfoArea.setValue(valueOrEmpty(editOfferData.description()));
        updatePriceSummary();
    }

    private LinkedHashSet<OfferPetOptionDto> findPetOptions(List<UUID> petIds) {
        LinkedHashSet<OfferPetOptionDto> selectedOptions = new LinkedHashSet<>();
        if (petIds == null || petIds.isEmpty()) {
            return selectedOptions;
        }
        for (UUID petId : petIds) {
            formData.pets().stream()
                    .filter(option -> petId.equals(option.id()))
                    .findFirst()
                    .ifPresent(selectedOptions::add);
        }
        return selectedOptions;
    }

    private LocalDate editableDateOrNull(LocalDate value) {
        return value != null && value.isBefore(formData.minimumStartDate()) ? null : value;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String imagePreviewPlaceholderText() {
        return isOwnerOffer() ? "Vorschau deiner Haustierbilder" : "Vorschau deiner Bilder";
    }

    private String currentOfferTitle() {
        if (titleField != null && !titleField.getValue().isBlank()) {
            return titleField.getValue();
        }
        if (editOfferData != null && editOfferData.title() != null && !editOfferData.title().isBlank()) {
            return editOfferData.title();
        }
        return isOwnerOffer() ? "Auftrag" : "Angebot";
    }

    private void showError(String message) {
        Notification.show(message, 5000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void showSuccess(String message) {
        Notification.show(message, 4000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void navigateToReturnTarget() {
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.navigate(returnTarget.path(), returnTarget.queryParameters());
        }
    }

    private NavigationTarget returnTargetFrom(Map<String, List<String>> parameters) {
        List<String> values = parameters.get(RETURN_TO_PARAMETER);
        String value = values == null || values.isEmpty() ? null : values.get(0);
        return returnTargetFrom(value);
    }

    private NavigationTarget returnTargetFrom(String value) {
        if (value == null) {
            return DEFAULT_RETURN_TARGET;
        }

        return switch (value.trim()) {
            case "/" -> new NavigationTarget("", new QueryParameters(Map.of()));
            case "/tierhalter-finden", "tierhalter-finden" ->
                    new NavigationTarget("tierhalter-finden", new QueryParameters(Map.of()));
            case "/profile?tab=offers", "profile?tab=offers" -> DEFAULT_RETURN_TARGET;
            default -> DEFAULT_RETURN_TARGET;
        };
    }

    private VerticalLayout popoverContent() {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.getStyle()
                .set("background", "white")
                .set("border-radius", "32px")
                .set("padding", "24px")
                .set("box-sizing", "border-box")
                .set("gap", "14px")
                .set("box-shadow", "0 12px 28px rgba(74, 52, 40, 0.12)");
        return content;
    }

    private Span popoverTitle(String text) {
        Span title = new Span(text);
        title.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "800")
                .set("color", DARK);
        return title;
    }

    private Component buildDatePopover() {
        VerticalLayout content = popoverContent();
        content.getStyle()
                .set("gap", "5px")
                .set("min-height", "370px")
                .set("padding", "12px 16px");

        dateRangeStatus = new Span();
        dateRangeStatus.getStyle()
                .set("background", "#fdf6ec")
                .set("border", "1px solid " + BORDER)
                .set("border-radius", "18px")
                .set("box-sizing", "border-box")
                .set("color", DARK)
                .set("font-size", "12px")
                .set("font-weight", "800")
                .set("padding", "4px 11px")
                .set("width", "100%");

        dateCalendar = new Div();
        dateCalendar.setWidthFull();

        updateDateFilter();

        content.add(popoverTitle("Zeitraum"), dateRangeStatus, dateCalendar);
        return content;
    }

    private void updateDateFilter() {
        if (selectedFrom != null && selectedTo != null) {
            String formatted = formatPlainDateRange();
            dateRangeField.setValue(formatted);
            dateRangeStatus.setText(formatted);
        } else if (selectedFrom != null) {
            String formatted = "ab " + selectedFrom.format(DAY_MONTH_FORMATTER);
            dateRangeField.setValue(formatted);
            dateRangeStatus.setText(formatted);
        } else {
            if (dateRangeField != null) {
                dateRangeField.clear();
            }
            if (dateRangeStatus != null) {
                dateRangeStatus.setText("Wann?");
            }
        }
        refreshDateCalendar();
        updatePriceSummary();
    }

    private YearMonth initialDisplayedMonth() {
        LocalDate today = LocalDate.now();
        if (selectedFrom != null && !selectedFrom.isBefore(today)) {
            return YearMonth.from(selectedFrom);
        }
        if (selectedTo != null && !selectedTo.isBefore(today)) {
            return YearMonth.from(selectedTo);
        }
        return YearMonth.from(today);
    }

    private void refreshDateCalendar() {
        if (dateCalendar == null) {
            return;
        }
        dateCalendar.removeAll();
        dateCalendar.add(calendarHeader(), weekdayHeader(), dayGrid());
    }

    private Component calendarHeader() {
        Button previousMonth = calendarNavButton(VaadinIcon.CHEVRON_LEFT, "Vorheriger Monat");
        previousMonth.setEnabled(displayedMonth.isAfter(YearMonth.from(LocalDate.now())));
        previousMonth.addClickListener(event -> {
            displayedMonth = displayedMonth.minusMonths(1);
            refreshDateCalendar();
        });

        Button nextMonth = calendarNavButton(VaadinIcon.CHEVRON_RIGHT, "Nächster Monat");
        nextMonth.addClickListener(event -> {
            displayedMonth = displayedMonth.plusMonths(1);
            refreshDateCalendar();
        });

        Span monthLabel = new Span(displayedMonth.atDay(1).format(MONTH_YEAR_FORMATTER));
        monthLabel.getStyle()
                .set("color", DARK)
                .set("font-size", "15px")
                .set("font-weight", "900")
                .set("text-transform", "capitalize");

        HorizontalLayout header = new HorizontalLayout(previousMonth, monthLabel, nextMonth);
        header.setWidthFull();
        header.setPadding(false);
        header.setSpacing(false);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        return header;
    }

    private Button calendarNavButton(VaadinIcon icon, String ariaLabel) {
        Button button = new Button(new Icon(icon));
        button.setAriaLabel(ariaLabel);
        button.getStyle()
                .set("background", "#fdf6ec")
                .set("border", "1px solid " + BORDER)
                .set("border-radius", "50%")
                .set("box-shadow", "none")
                .set("color", BROWN)
                .set("cursor", "pointer")
                .set("height", "28px")
                .set("min-width", "28px")
                .set("padding", "0")
                .set("width", "28px");
        return button;
    }

    private Component weekdayHeader() {
        Div weekdays = calendarGrid();
        List<String> labels = List.of("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So");
        for (String label : labels) {
            Span day = new Span(label);
            day.getStyle()
                    .set("color", "#9e8c7b")
                    .set("font-size", "11px")
                    .set("font-weight", "900")
                    .set("line-height", "20px")
                    .set("text-align", "center");
            weekdays.add(day);
        }
        return weekdays;
    }

    private Component dayGrid() {
        Div grid = calendarGrid();
        LocalDate firstDay = displayedMonth.atDay(1);
        int leadingBlankDays = firstDay.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
        for (int i = 0; i < leadingBlankDays; i++) {
            grid.add(blankCalendarCell());
        }
        for (int day = 1; day <= displayedMonth.lengthOfMonth(); day++) {
            grid.add(calendarDayButton(displayedMonth.atDay(day)));
        }
        return grid;
    }

    private Div calendarGrid() {
        Div grid = new Div();
        grid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(7, minmax(0, 1fr))")
                .set("gap", "0")
                .set("width", "100%");
        return grid;
    }

    private Component blankCalendarCell() {
        Div blank = new Div();
        blank.getStyle().set("height", "30px");
        return blank;
    }

    private Component calendarDayButton(LocalDate date) {
        Div cell = new Div();
        Button button = new Button(String.valueOf(date.getDayOfMonth()));
        boolean disabled = date.isBefore(LocalDate.now());
        boolean selectedStart = date.equals(selectedFrom);
        boolean selectedEnd = date.equals(selectedTo);
        boolean insideRange = isInsideSelectedRange(date);
        boolean hasCompleteRange = hasCompleteSelectedRange();

        cell.getStyle()
                .set("align-items", "center")
                .set("box-sizing", "border-box")
                .set("display", "flex")
                .set("height", "30px")
                .set("justify-content", "center")
                .set("width", "100%");
        applyRangeCellBackground(cell, selectedStart, selectedEnd, insideRange, hasCompleteRange);

        button.setEnabled(!disabled);
        button.setAriaLabel(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
        button.getStyle()
                .set("border", "none")
                .set("box-shadow", "none")
                .set("font-size", "13px")
                .set("font-weight", "800")
                .set("height", "30px")
                .set("margin", "0")
                .set("min-width", "0")
                .set("padding", "0")
                .set("width", "100%");

        if (disabled) {
            button.getStyle()
                    .set("background", "transparent")
                    .set("color", "#c8b9aa")
                    .set("cursor", "not-allowed");
            cell.add(button);
            return cell;
        }

        button.getStyle()
                .set("background", "transparent")
                .set("color", DARK)
                .set("cursor", "pointer");
        if (date.equals(LocalDate.now())) {
            button.getStyle().set("box-shadow", "inset 0 0 0 1px " + BROWN);
        }
        if (insideRange) {
            button.getStyle()
                    .set("background", "transparent")
                    .set("border-radius", "0")
                    .set("color", DARK);
        }
        if (selectedStart || selectedEnd) {
            button.getStyle()
                    .set("background", BROWN)
                    .set("border-radius", "999px")
                    .set("box-shadow", "none")
                    .set("color", "white");
        }

        button.addClickListener(event -> selectCalendarDate(date));
        cell.add(button);
        return cell;
    }

    private void applyRangeCellBackground(Div cell, boolean selectedStart, boolean selectedEnd,
            boolean insideRange, boolean hasCompleteRange) {
        if (!hasCompleteRange) {
            cell.getStyle().set("background", "transparent");
            return;
        }
        String rangeColor = "#f1dfcf";
        if (insideRange) {
            cell.getStyle().set("background", rangeColor);
        } else if (selectedStart && !selectedEnd) {
            cell.getStyle().set("background",
                    "linear-gradient(to right, transparent 0 50%, " + rangeColor + " 50% 100%)");
        } else if (selectedEnd && !selectedStart) {
            cell.getStyle().set("background",
                    "linear-gradient(to right, " + rangeColor + " 0 50%, transparent 50% 100%)");
        } else {
            cell.getStyle().set("background", "transparent");
        }
    }

    private void selectCalendarDate(LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            return;
        }
        if (selectedFrom == null || selectedTo != null || !selectingRangeEnd) {
            selectedFrom = date;
            selectedTo = null;
            selectingRangeEnd = true;
        } else if (date.isBefore(selectedFrom)) {
            selectedFrom = date;
            selectedTo = null;
            selectingRangeEnd = true;
        } else {
            selectedTo = date;
            selectingRangeEnd = false;
            if (whenPopover != null) {
                whenPopover.close();
            }
        }
        displayedMonth = YearMonth.from(date);
        updateDateFilter();
    }

    private boolean isInsideSelectedRange(LocalDate date) {
        return selectedFrom != null
                && selectedTo != null
                && date.isAfter(selectedFrom)
                && date.isBefore(selectedTo);
    }

    private boolean hasCompleteSelectedRange() {
        return selectedFrom != null
                && selectedTo != null
                && !selectedFrom.equals(selectedTo);
    }

    private String formatPlainDateRange() {
        if (selectedFrom != null && selectedTo != null) {
            if (selectedFrom.getMonth().equals(selectedTo.getMonth())) {
                return selectedFrom.getDayOfMonth() + ".–" + selectedTo.format(DAY_MONTH_FORMATTER);
            }
            return selectedFrom.format(DAY_MONTH_FORMATTER) + " – " + selectedTo.format(DAY_MONTH_FORMATTER);
        }
        if (selectedFrom != null) {
            return "ab " + selectedFrom.format(DAY_MONTH_FORMATTER);
        }
        if (selectedTo != null) {
            return "bis " + selectedTo.format(DAY_MONTH_FORMATTER);
        }
        return "Wann?";
    }

    private record NavigationTarget(String path, QueryParameters queryParameters) {
    }
}
