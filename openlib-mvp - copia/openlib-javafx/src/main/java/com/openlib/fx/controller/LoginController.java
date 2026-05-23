package com.openlib.fx.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.openlib.fx.model.Models;
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

public class LoginController {

    public Scene buildScene() {
        // ── Layout raíz ──
        HBox root = new HBox();
        root.setStyle("-fx-background-color: " + UiHelper.DARK + ";");

        // ── Panel izquierdo: branding ──
        VBox left = new VBox(20);
        left.setAlignment(Pos.CENTER);
        left.setPadding(new Insets(60));
        left.setStyle("-fx-background-color: #1E293B;");
        left.setPrefWidth(420);

        Label logo = new Label("📚");
        logo.setFont(Font.font(80));

        Label brand = new Label("OpenLib Market");
        brand.setFont(Font.font("System", FontWeight.BOLD, 32));
        brand.setTextFill(Color.WHITE);

        Label tagline = new Label("Gestión de Activos Digitales Académicos");
        tagline.setFont(Font.font(14));
        tagline.setTextFill(Color.web("#94A3B8"));
        tagline.setWrapText(true);

        Label desc = new Label("Accede a miles de libros académicos de forma segura, " +
                "con trazabilidad completa de descargas y una experiencia de e-commerce profesional.");
        desc.setFont(Font.font(13));
        desc.setTextFill(Color.web("#64748B"));
        desc.setWrapText(true);

        // Usuarios demo
        VBox demoBox = new VBox(8);
        demoBox.setStyle("-fx-background-color: #0F172A; -fx-background-radius: 10; -fx-padding: 16;");
        Label demoTitle = new Label("👤 Usuarios Demo");
        demoTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        demoTitle.setTextFill(Color.web("#94A3B8"));
        Label demoAdmin  = new Label("admin@openlib.com  |  Admin1234!  [ADMIN]");
        Label demoSeller = new Label("seller@openlib.com |  Seller1234! [SELLER]");
        Label demoBuyer  = new Label("buyer@openlib.com  |  Buyer1234!  [BUYER]");
        for (Label l : new Label[]{demoAdmin, demoSeller, demoBuyer}) {
            l.setFont(Font.font("Monospace", 11));
            l.setTextFill(Color.web("#6EE7B7"));
        }
        demoBox.getChildren().addAll(demoTitle, demoAdmin, demoSeller, demoBuyer);

        left.getChildren().addAll(logo, brand, tagline, desc, demoBox);
        HBox.setHgrow(left, Priority.NEVER);

        // ── Panel derecho: formulario ──
        VBox right = new VBox();
        right.setAlignment(Pos.CENTER);
        right.setStyle("-fx-background-color: " + UiHelper.SURFACE + ";");
        HBox.setHgrow(right, Priority.ALWAYS);

        VBox formCard = UiHelper.card();
        formCard.setMaxWidth(400);
        formCard.setSpacing(16);

        Label formTitle = UiHelper.title("Iniciar sesión");
        Label formSub   = UiHelper.muted("Ingresa tus credenciales para continuar");

        TextField emailField    = UiHelper.styledField("correo@ejemplo.com");
        emailField.setText("buyer@openlib.com");
        PasswordField passField = UiHelper.styledPassword("Contraseña");
        passField.setText("Buyer1234!");

        Label errorLbl = UiHelper.errorLabel();

        Button loginBtn = UiHelper.primaryBtn("Ingresar");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        ProgressIndicator spinner = UiHelper.spinner();
        spinner.setVisible(false);

        HBox registerRow = new HBox(6);
        registerRow.setAlignment(Pos.CENTER);
        Label noAccount = UiHelper.muted("¿No tienes cuenta?");
        Button regBtn = UiHelper.linkBtn("Regístrate aquí");
        regBtn.setOnAction(e -> NavigationManager.navigateTo("register"));
        registerRow.getChildren().addAll(noAccount, regBtn);

        // ── Acción Login ──
        Runnable doLogin = () -> {
            errorLbl.setText("");
            String email = emailField.getText().trim();
            String pass  = passField.getText();
            if (email.isEmpty() || pass.isEmpty()) {
                errorLbl.setText("Por favor ingresa email y contraseña.");
                return;
            }
            loginBtn.setDisable(true);
            spinner.setVisible(true);

            new Thread(() -> {
                var resp = ApiClient.postPublic("/auth/login",
                        Map.of("email", email, "password", pass));

                Platform.runLater(() -> {
                    spinner.setVisible(false);
                    loginBtn.setDisable(false);
                    if (resp.isSuccess()) {
                        Models.AuthResponse auth = resp.as(Models.AuthResponse.class);
                        if (auth == null) { errorLbl.setText("Error al procesar respuesta."); return; }
                        SessionManager.setSession(
                                auth.accessToken, auth.email, auth.role, auth.fullName, auth.userId);
                        // Navegar según rol
                        String dest = switch (auth.role) {
                            case "ADMIN"  -> "admin_panel";
                            case "SELLER" -> "seller_books";
                            default       -> "catalog";
                        };
                        NavigationManager.navigateTo(dest);
                    } else {
                        errorLbl.setText("❌ " + resp.errorMessage());
                    }
                });
            }).start();
        };

        loginBtn.setOnAction(e -> doLogin.run());
        passField.setOnAction(e -> doLogin.run());

        formCard.getChildren().addAll(
                formTitle, formSub, UiHelper.separator(),
                new Label("Email:"), emailField,
                new Label("Contraseña:"), passField,
                errorLbl, spinner, loginBtn,
                UiHelper.separator(), registerRow
        );

        right.getChildren().add(formCard);
        root.getChildren().addAll(left, right);

        Scene scene = new Scene(root, 1100, 700);
        return scene;
    }
}
