package com.openlib.fx.controller;

import com.openlib.fx.service.ApiClient;
import com.openlib.fx.util.NavigationManager;
import com.openlib.fx.util.SessionManager;
import com.openlib.fx.util.UiHelper;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Map;

public class CheckoutController {

    public Scene buildScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UiHelper.SURFACE + ";");

        VBox top = new VBox();
        HBox nav = UiHelper.navBar(SessionManager.getUserRole());
        Button backBtn = UiHelper.outlineBtn("← Carrito");
        backBtn.setOnAction(e -> NavigationManager.navigateTo("cart"));
        nav.getChildren().add(1, backBtn);
        top.getChildren().add(nav);

        HBox toolbar = new HBox(12);
        toolbar.setPadding(new Insets(16, 24, 12, 24));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: white; -fx-border-color: " + UiHelper.BORDER + "; -fx-border-width: 0 0 1 0;");
        toolbar.getChildren().add(UiHelper.subtitle("💳 Checkout"));
        top.getChildren().add(toolbar);
        root.setTop(top);

        // Stepper visual
        HBox stepper = buildStepper();

        // Form principal
        VBox content = new VBox(24);
        content.setPadding(new Insets(30, 60, 30, 60));
        content.setAlignment(Pos.TOP_CENTER);
        content.getChildren().add(stepper);

        // Sección 1: Datos de facturación
        VBox billing = UiHelper.card();
        billing.setMaxWidth(600);
        billing.getChildren().add(UiHelper.subtitle("📋 Datos de Facturación"));
        billing.getChildren().add(UiHelper.separator());

        TextField nameField    = UiHelper.styledField("Nombre completo");
        nameField.setText(SessionManager.getUserFullName());
        TextField emailField   = UiHelper.styledField("Email de facturación");
        emailField.setText(SessionManager.getUserEmail());
        TextField addressField = UiHelper.styledField("Dirección (calle, ciudad, país)");
        TextField phoneField   = UiHelper.styledField("Teléfono (opcional)");

        billing.getChildren().addAll(
                new Label("Nombre:"), nameField,
                new Label("Email:"), emailField,
                new Label("Dirección:"), addressField,
                new Label("Teléfono:"), phoneField
        );

        // Sección 2: Método de pago
        VBox payment = UiHelper.card();
        payment.setMaxWidth(600);
        payment.getChildren().add(UiHelper.subtitle("💳 Método de Pago (Simulado)"));
        payment.getChildren().add(UiHelper.separator());
        payment.getChildren().add(UiHelper.muted("Los libros son gratuitos. Selecciona un método de pago simbólico."));

        ToggleGroup payGroup = new ToggleGroup();
        RadioButton cardRb   = new RadioButton("💳 Tarjeta de Crédito/Débito");
        RadioButton paypalRb = new RadioButton("🅿️ PayPal");
        RadioButton cryptoRb = new RadioButton("₿ Criptomoneda");
        RadioButton freeRb   = new RadioButton("🎁 Pago Gratuito ($0.00)");
        for (var rb : new RadioButton[]{cardRb, paypalRb, cryptoRb, freeRb}) {
            rb.setToggleGroup(payGroup);
            rb.setFont(Font.font(14));
            payment.getChildren().add(rb);
        }
        freeRb.setSelected(true);

        // Sección 3: Confirmar
        VBox confirm = UiHelper.card();
        confirm.setMaxWidth(600);
        Label errorLbl = UiHelper.errorLabel();
        ProgressIndicator spinner = UiHelper.spinner();
        spinner.setVisible(false);

        Button confirmBtn = UiHelper.successBtn("✅ Confirmar Pedido");
        confirmBtn.setFont(Font.font("System", FontWeight.BOLD, 16));
        confirmBtn.setMaxWidth(Double.MAX_VALUE);

        confirmBtn.setOnAction(e -> {
            errorLbl.setText("");
            String name    = nameField.getText().trim();
            String email   = emailField.getText().trim();
            String address = addressField.getText().trim();

            if (name.isEmpty() || email.isEmpty() || address.isEmpty()) {
                errorLbl.setText("Por favor completa nombre, email y dirección.");
                return;
            }

            RadioButton selectedPay = (RadioButton) payGroup.getSelectedToggle();
            String payMethod = "FREE";
            if (selectedPay == cardRb)   payMethod = "CREDIT_CARD";
            else if (selectedPay == paypalRb) payMethod = "PAYPAL";
            else if (selectedPay == cryptoRb) payMethod = "CRYPTO";

            confirmBtn.setDisable(true);
            spinner.setVisible(true);

            final String finalPayMethod = payMethod;
            new Thread(() -> {
                var resp = ApiClient.post("/orders", Map.of(
                        "billingName", name,
                        "billingEmail", email,
                        "billingAddress", address,
                        "paymentMethod", finalPayMethod
                ));
                Platform.runLater(() -> {
                    spinner.setVisible(false);
                    confirmBtn.setDisable(false);
                    if (resp.isSuccess()) {
                        UiHelper.alert("¡Pedido Confirmado! 🎉",
                                "Tu pedido fue procesado exitosamente.\n" +
                                "Puedes descargar los libros desde tu Biblioteca Personal.",
                                Alert.AlertType.INFORMATION);
                        NavigationManager.navigateTo("library");
                    } else {
                        errorLbl.setText("❌ " + resp.errorMessage());
                    }
                });
            }).start();
        });

        confirm.getChildren().addAll(UiHelper.subtitle("✅ Confirmar Pedido"), UiHelper.separator(),
                errorLbl, spinner, confirmBtn);

        content.getChildren().addAll(billing, payment, confirm);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: " + UiHelper.SURFACE + ";");
        root.setCenter(scroll);

        return new Scene(root, 1280, 800);
    }

    private HBox buildStepper() {
        HBox stepper = new HBox(0);
        stepper.setAlignment(Pos.CENTER);
        stepper.setPadding(new Insets(0, 0, 20, 0));

        String[] steps = {"1. Carrito", "2. Facturación", "3. Pago", "4. Confirmación"};
        int current = 2;

        for (int i = 0; i < steps.length; i++) {
            VBox step = new VBox(4);
            step.setAlignment(Pos.CENTER);
            step.setPadding(new Insets(8, 20, 8, 20));

            String color = i < current ? UiHelper.SUCCESS : (i == current ? UiHelper.PRIMARY : UiHelper.BORDER);
            Label circle = new Label(i < current ? "✓" : String.valueOf(i + 1));
            circle.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-background-radius: 50; -fx-padding: 4 9;");

            Label lbl = new Label(steps[i]);
            lbl.setFont(Font.font("System", i == current ? FontWeight.BOLD : FontWeight.NORMAL, 12));
            lbl.setTextFill(Color.web(i == current ? UiHelper.PRIMARY : UiHelper.TEXT_MUTED));

            step.getChildren().addAll(circle, lbl);

            if (i < steps.length - 1) {
                Region line = new Region();
                line.setPrefWidth(60);
                line.setPrefHeight(2);
                line.setStyle("-fx-background-color: " + (i < current ? UiHelper.SUCCESS : UiHelper.BORDER) + ";");
                line.setTranslateY(0);
                stepper.getChildren().addAll(step, line);
            } else {
                stepper.getChildren().add(step);
            }
        }
        return stepper;
    }
}
