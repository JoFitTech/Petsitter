package com.softwareengineering.petsitter.ui.shared;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.NativeButton;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ExternalPaymentMethodsTest {

    @Test
    void rendersBrandedPayPalAndSystemApplePayButtons() {
        ExternalPaymentMethods methods = new ExternalPaymentMethods();

        NativeButton paypal = findButton(methods, "paypal-payment-button").orElseThrow();
        NativeButton applePay = findButton(methods, "apple-pay-button").orElseThrow();

        assertThat(paypal.getStyle().get("background")).isEqualTo("#ffc439");
        assertThat(paypal.getChildren().map(child -> child.getElement().getText()))
                .containsExactly("Pay", "Pal");
        assertThat(applePay.getStyle().get("-webkit-appearance")).isEqualTo("-apple-pay-button");
        assertThat(applePay.getStyle().get("-apple-pay-button-style")).isEqualTo("black");
        assertThat(applePay.isVisible()).isFalse();
    }

    private Optional<NativeButton> findButton(Component root, String className) {
        if (root instanceof NativeButton button && button.getClassNames().contains(className)) {
            return Optional.of(button);
        }
        return root.getChildren()
                .map(child -> findButton(child, className))
                .flatMap(Optional::stream)
                .findFirst();
    }
}
