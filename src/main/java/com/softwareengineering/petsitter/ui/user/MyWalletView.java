package com.softwareengineering.petsitter.ui.user;

import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.ui.shared.ExternalPaymentMethods;
import com.softwareengineering.petsitter.wallet.dto.WalletSummaryDto;
import com.softwareengineering.petsitter.wallet.dto.WalletTransactionDto;
import com.softwareengineering.petsitter.wallet.service.WalletService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class MyWalletView extends Div {

    private static final String DARK = "#4a3428";
    private static final String MUTED = "#8a7060";
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final WalletService walletService;
    private final AuthenticatedUser authenticatedUser;

    public MyWalletView(WalletService walletService, AuthenticatedUser authenticatedUser) {
        this.walletService = walletService;
        this.authenticatedUser = authenticatedUser;
        setWidthFull();
        render();
    }

    private void render() {
        removeAll();
        UUID userId = authenticatedUser.get().map(user -> user.getId()).orElse(null);
        if (userId == null) {
            add(new Paragraph("Bitte melde dich an, um dein Guthaben zu verwalten."));
            return;
        }

        WalletSummaryDto wallet = walletService.getWalletSummary(userId);
        Div panel = panel();

        H2 title = new H2("Guthaben");
        title.getStyle().set("margin", "0").set("font-size", "28px").set("font-weight", "800").set("color", DARK);
        Paragraph subtitle = new Paragraph("Verwalte dein Demo-Guthaben und behalte Treuhandzahlungen im Blick.");
        subtitle.getStyle().set("margin", "6px 0 24px 0").set("font-size", "14px").set("color", MUTED);

        HorizontalLayout balances = new HorizontalLayout(
                metric("Verfügbar", wallet.availableBalance()),
                metric("In Treuhand bezahlt", wallet.heldOutgoing()),
                metric("Erwartete Eingänge", wallet.expectedIncoming()));
        balances.setWidthFull();
        balances.getStyle().set("gap", "14px").set("flex-wrap", "wrap");

        panel.add(title, subtitle, balances, divider(), buildDemoTopUp(userId), divider(),
                new ExternalPaymentMethods(), divider(), buildTransactions(wallet));
        add(panel);
    }

    private Component buildDemoTopUp(UUID userId) {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.getStyle().set("gap", "10px");

        Span title = new Span("Demo-Guthaben aufladen");
        title.getStyle().set("font-weight", "800").set("color", DARK);
        Paragraph hint = new Paragraph("Nur für die Entwicklungsphase: Wähle einen Testbetrag.");
        hint.getStyle().set("margin", "0").set("font-size", "13px").set("color", MUTED);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.getStyle().set("gap", "10px").set("flex-wrap", "wrap");
        buttons.add(topUpButton(userId, "25 EUR", new BigDecimal("25.00")));
        buttons.add(topUpButton(userId, "50 EUR", new BigDecimal("50.00")));
        buttons.add(topUpButton(userId, "100 EUR", new BigDecimal("100.00")));
        section.add(title, hint, buttons);
        return section;
    }

    private Button topUpButton(UUID userId, String label, BigDecimal amount) {
        Button button = new Button(label);
        button.getStyle()
                .set("border-radius", "22px")
                .set("background", DARK)
                .set("color", "white")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("cursor", "pointer");
        button.addClickListener(event -> {
            walletService.devTopUp(userId, amount);
            Notification.show("Demo-Guthaben wurde aufgeladen.")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            render();
        });
        return button;
    }

    private Component buildTransactions(WalletSummaryDto wallet) {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.getStyle().set("gap", "10px");

        H3 title = new H3("Transaktionsverlauf");
        title.getStyle().set("margin", "0").set("font-size", "19px").set("color", DARK);
        section.add(title);

        if (wallet.transactions().isEmpty()) {
            Paragraph empty = new Paragraph("Noch keine Transaktionen vorhanden.");
            empty.getStyle().set("margin", "0").set("font-size", "14px").set("color", MUTED);
            section.add(empty);
            return section;
        }

        wallet.transactions().forEach(transaction -> section.add(transactionRow(transaction)));
        return section;
    }

    private Component transactionRow(WalletTransactionDto transaction) {
        Div row = new Div();
        row.getStyle()
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("gap", "16px")
                .set("padding", "12px 14px")
                .set("border", "1px solid #ead5ae")
                .set("border-radius", "12px")
                .set("background", "#fffdf8");

        Div copy = new Div();
        Span description = new Span(transaction.description());
        description.getStyle().set("display", "block").set("font-weight", "700").set("color", DARK);
        Span meta = new Span(transaction.createdAt().format(DATE_TIME) + " · Saldo: "
                + money(transaction.balanceAfter()));
        meta.getStyle().set("font-size", "12px").set("color", MUTED);
        copy.add(description, meta);

        Span amount = new Span((transaction.amount().signum() > 0 ? "+" : "") + money(transaction.amount()));
        amount.getStyle()
                .set("font-weight", "800")
                .set("color", transaction.amount().signum() < 0 ? "#9a4f36" : "#4f7f45");

        row.add(copy, amount);
        return row;
    }

    private Component metric(String labelText, BigDecimal value) {
        Div card = new Div();
        card.getStyle()
                .set("flex", "1")
                .set("min-width", "180px")
                .set("padding", "18px")
                .set("background", "#fffdf8")
                .set("border", "1px solid #ead5ae")
                .set("border-radius", "14px");
        Span label = new Span(labelText);
        label.getStyle().set("display", "block").set("font-size", "13px").set("color", MUTED);
        Span amount = new Span(money(value));
        amount.getStyle().set("display", "block").set("font-size", "23px").set("font-weight", "800").set("color", DARK);
        card.add(label, amount);
        return card;
    }

    private Div divider() {
        Div divider = new Div();
        divider.getStyle().set("height", "1px").set("background", "#ead5ae").set("margin", "24px 0");
        return divider;
    }

    private Div panel() {
        Div panel = new Div();
        panel.setWidthFull();
        panel.getStyle()
                .set("background", "white")
                .set("border-radius", "20px")
                .set("padding", "36px")
                .set("box-shadow", "0 8px 32px rgba(74,52,40,0.09)")
                .set("box-sizing", "border-box");
        return panel;
    }

    private String money(BigDecimal amount) {
        return amount.setScale(2) + " EUR";
    }
}
