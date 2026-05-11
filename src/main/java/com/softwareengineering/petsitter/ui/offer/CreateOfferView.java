package com.softwareengineering.petsitter.ui.offer;

// TODO: Diese Seite muss noch für den Tierhalter angepasst werden.
//       Derzeit zeigt sie das Formular zum Erstellen eines Auftrags aus der
//       Perspektive des Tierhalters, aber Felder, Validierungen und Backend-Hooks
//       müssen im Kontext des eingeloggten Nutzers (Tierhalter) ergänzt werden.

import com.softwareengineering.petsitter.ui.shared.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

@Route(value = "auftrag-erstellen", layout = MainLayout.class)
public class CreateOfferView extends VerticalLayout {

    private static final String DARK       = "#4a3428";
    private static final String BROWN      = "#7b5236";
    private static final String LIGHT_BG   = "#fbf8f1";
    private static final String CARD_BG    = "#ffffff";
    private static final String CARD_SHADOW = "0 12px 30px rgba(74, 52, 40, 0.10)";
    private static final String BEIGE      = "#f6e8d0";
    private static final String BORDER     = "#eadfce";

    // ── Form fields (accessible for backend wiring later) ─────────────────
    private Upload imageUpload;
    private MultiFileMemoryBuffer uploadBuffer;
    private Div imagePreviewArea;
    private final List<String> uploadedFileNames = new ArrayList<>();

    private TextField titleField;
    private Select<String> animalTypeSelect;
    private RadioButtonGroup<String> frequencyGroup;
    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;
    private RadioButtonGroup<String> careTypeGroup;
    private TextArea additionalInfoArea;

    // ──────────────────────────────────────────────────────────────────────

    public CreateOfferView() {
        setWidthFull();
        setPadding(false);
        setSpacing(false);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        getStyle()
                .set("background", LIGHT_BG)
                .set("overflow-x", "hidden");

        add(createPageWrapper());
    }

    // ── Page wrapper with background blobs ────────────────────────────────
    private Component createPageWrapper() {
        Div wrapper = new Div();
        wrapper.setWidthFull();
        wrapper.getStyle()
                .set("position", "relative")
                .set("overflow-x", "hidden")
                .set("background", LIGHT_BG);

        wrapper.add(createBackgroundBlobs(), createContentSection());
        return wrapper;
    }

    private Component createBackgroundBlobs() {
        Div container = new Div();

        Div leftBlob = new Div();
        leftBlob.getStyle()
                .set("position", "absolute")
                .set("left", "-130px")
                .set("top", "60px")
                .set("width", "400px")
                .set("height", "400px")
                .set("background", "#f6ead5")
                .set("border-radius", "50%")
                .set("z-index", "0");

        Div rightBlob = new Div();
        rightBlob.getStyle()
                .set("position", "absolute")
                .set("right", "-100px")
                .set("top", "100px")
                .set("width", "440px")
                .set("height", "440px")
                .set("background", "#e7f0f0")
                .set("border-radius", "50%")
                .set("z-index", "0");

        container.add(leftBlob, rightBlob);
        return container;
    }

    // ── Main content ──────────────────────────────────────────────────────
    private Component createContentSection() {
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

        H1 headline = new H1("Neuen Auftrag erstellen");
        headline.getStyle()
                .set("font-size", "28px")
                .set("color", DARK)
                .set("margin", "0 0 4px 0");

        Paragraph subtitle = new Paragraph("Details für deine Haustierbetreung angeben");
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
                createImageUploadSection(),
                createImagePreviewSection(),
                createSpacer("16px"),
                createTitleSection(),
                createSpacer("16px"),
                createAnimalTypeSection(),
                createSpacer("16px"),
                createZeitraumSection(),
                createSpacer("16px"),
                createCareTypeSection(),
                createSpacer("16px"),
                createAdditionalInfoSection(),
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
    private Component createImageUploadSection() {
        uploadBuffer = new MultiFileMemoryBuffer();
        imageUpload = new Upload(uploadBuffer);
        imageUpload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp", "image/gif");
        imageUpload.setMaxFiles(10);
        imageUpload.setMaxFileSize(10 * 1024 * 1024); // 10 MB

        // Style the upload button itself
        Button uploadBtn = new Button("📷  Lade hier Bilder deiner Haustiere hoch");
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
    private Component createImagePreviewSection() {
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

        Span placeholder = new Span("Vorschau deiner Haustiere");
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

        NativeLabel label = new NativeLabel("Titel des Auftrags");
        label.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "700")
                .set("color", BROWN)
                .set("margin-bottom", "8px")
                .set("display", "block");

        titleField = new TextField();
        titleField.setWidthFull();
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

        NativeLabel label = new NativeLabel("Tierarten auswählen");
        label.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "700")
                .set("color", BROWN)
                .set("margin-bottom", "8px")
                .set("display", "block");

        animalTypeSelect = new Select<>();
        animalTypeSelect.setItems("Hund", "Katze", "Kleintier", "Vogel", "Reptil", "Fisch", "Sonstiges");
        animalTypeSelect.setWidthFull();
        animalTypeSelect.getStyle()
                .set("border-radius", "12px");

        section.add(label, animalTypeSelect);
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
        frequencyGroup.setItems("einmalig", "regelmäßig");
        frequencyGroup.setValue("einmalig");
        frequencyGroup.getStyle()
                .set("color", DARK)
                .set("font-size", "14px");

        HorizontalLayout dateRow = new HorizontalLayout();
        dateRow.setWidthFull();
        dateRow.setSpacing(true);
        dateRow.getStyle().set("margin-top", "10px").set("gap", "16px");

        fromDatePicker = new DatePicker("von");
        fromDatePicker.setWidthFull();
        fromDatePicker.getStyle().set("border-radius", "12px");

        toDatePicker = new DatePicker("bis");
        toDatePicker.setWidthFull();
        toDatePicker.getStyle().set("border-radius", "12px");

        dateRow.add(fromDatePicker, toDatePicker);

        section.add(label, frequencyGroup, dateRow);
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
        // Note: Only "Tiersitting" or "Tiersitting + Haussitting" are allowed.
        //       Pure "Haussitting" is intentionally excluded per business rule.
        careTypeGroup.setItems("Tiersitting", "Tiersitting + Haussitting");
        careTypeGroup.setValue("Tiersitting");
        careTypeGroup.getStyle()
                .set("color", DARK)
                .set("font-size", "14px");

        section.add(label, careTypeGroup);
        return section;
    }

    // ── 7. Additional info ────────────────────────────────────────────────
    private Component createAdditionalInfoSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);

        NativeLabel label = new NativeLabel("Zusätzliche Informationen und Bedürfnisse deiner Haustiere");
        label.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "700")
                .set("color", BROWN)
                .set("margin-bottom", "8px")
                .set("display", "block");

        additionalInfoArea = new TextArea();
        additionalInfoArea.setWidthFull();
        additionalInfoArea.setMinHeight("120px");
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

    // ── Backend / Navigation stubs ────────────────────────────────────────
    // All methods below are intentionally left as print-only placeholders.
    // Connect them to backend services once the API layer is ready.

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
        System.out.println("  Tierart:     " + animalTypeSelect.getValue());
        System.out.println("  Häufigkeit:  " + frequencyGroup.getValue());
        System.out.println("  Von:         " + fromDatePicker.getValue());
        System.out.println("  Bis:         " + toDatePicker.getValue());
        System.out.println("  Betreuung:   " + careTypeGroup.getValue());
        System.out.println("  Zusatzinfo:  " + additionalInfoArea.getValue());
        System.out.println("  Bilder:      " + uploadedFileNames);

        // TODO: draftService.saveDraft(buildDraftDto());
    }

    private void onPublishClicked() {
        System.out.println("Auftrag hochladen (veröffentlichen) geklickt");
        System.out.println("  Titel:       " + titleField.getValue());
        System.out.println("  Tierart:     " + animalTypeSelect.getValue());
        System.out.println("  Häufigkeit:  " + frequencyGroup.getValue());
        System.out.println("  Von:         " + fromDatePicker.getValue());
        System.out.println("  Bis:         " + toDatePicker.getValue());
        System.out.println("  Betreuung:   " + careTypeGroup.getValue());
        System.out.println("  Zusatzinfo:  " + additionalInfoArea.getValue());
        System.out.println("  Bilder:      " + uploadedFileNames);

        // TODO: offerService.publish(buildOfferDto());
        //       UI.getCurrent().navigate("meine-auftraege");
    }
}
