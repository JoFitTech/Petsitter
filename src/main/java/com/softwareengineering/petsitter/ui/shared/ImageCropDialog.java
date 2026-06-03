package com.softwareengineering.petsitter.ui.shared;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.Base64;
import java.util.function.Consumer;

@JsModule("./image-crop-dialog.ts")
public class ImageCropDialog extends Dialog {

    public ImageCropDialog(byte[] sourceImage, String mimeType, Consumer<byte[]> onConfirm) {
        setWidth("680px");
        setMaxWidth("95vw");
        setCloseOnOutsideClick(false);

        H2 title = new H2("Bildausschnitt auswählen");
        title.getStyle().set("margin", "0").set("font-size", "22px").set("color", "#4a3428");

        Paragraph hint = new Paragraph("Verschiebe und zoome das Bild im runden Ausschnitt.");
        hint.getStyle().set("margin", "0").set("color", "#7a6050").set("font-size", "14px");

        Div cropHost = new Div();
        cropHost.addClassName("image-crop-host");
        cropHost.getStyle()
                .set("height", "440px")
                .set("width", "100%")
                .set("overflow", "hidden")
                .set("background", "#eadfce")
                .set("border-radius", "16px");
        cropHost.setText("Bild wird geladen ...");

        Button cancel = new Button("Abbrechen", event -> close());
        Button confirm = new Button("Übernehmen");
        confirm.getStyle()
                .set("background", "#774f35")
                .set("color", "white")
                .set("border-radius", "22px")
                .set("font-weight", "700");
        confirm.addClickListener(event -> {
            confirm.setEnabled(false);
            cropHost.getElement().executeJs("return window.PawsitterImageCrop.exportCrop(this)")
                    .then(String.class, dataUrl -> {
                        try {
                            onConfirm.accept(decodeDataUrl(dataUrl));
                            close();
                        } catch (RuntimeException exception) {
                            confirm.setEnabled(true);
                            Notification.show("Fehler: " + exception.getMessage(), 3500,
                                    Notification.Position.TOP_CENTER);
                        }
                    }, error -> {
                        confirm.setEnabled(true);
                        Notification.show("Der Bildausschnitt konnte nicht übernommen werden.", 3500,
                                Notification.Position.TOP_CENTER);
                    });
        });

        HorizontalLayout actions = new HorizontalLayout(cancel, confirm);
        actions.setWidthFull();
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        VerticalLayout content = new VerticalLayout(title, hint, cropHost, actions);
        content.setPadding(false);
        content.getStyle().set("gap", "14px");
        add(content);

        String dataUrl = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(sourceImage);
        addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                cropHost.getElement()
                        .executeJs("return window.PawsitterImageCrop.attach(this, $0)", dataUrl)
                        .then(Boolean.class, ignored -> {
                        }, error -> {
                            cropHost.setText("Das Bild konnte nicht geladen werden.");
                            Notification.show("Das Bild konnte nicht geladen werden.", 3500,
                                    Notification.Position.TOP_CENTER);
                        });
            } else {
                cropHost.getElement().executeJs("window.PawsitterImageCrop?.destroy(this)");
            }
        });
    }

    private byte[] decodeDataUrl(String dataUrl) {
        int separator = dataUrl == null ? -1 : dataUrl.indexOf(',');
        if (separator < 0) {
            throw new IllegalArgumentException("Ungültiger Bildausschnitt.");
        }
        return Base64.getDecoder().decode(dataUrl.substring(separator + 1));
    }
}
