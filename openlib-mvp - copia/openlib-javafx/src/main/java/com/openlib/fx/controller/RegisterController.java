package com.openlib.fx.controller;

import com.openlib.fx.service.ApiClient;
import com.openlib.fx.util.NavigationManager;
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

public class RegisterController {

    public Scene buildScene() {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: " + UiHelper.SURFACE + ";");
        root.setPadding(new Insets(40));

        VBox card = UiHelper.card();
        card.setMaxWidth(480);
        card.setSpacing(14);

        Label formTitle = UiHelper.title("Crear Cuenta");
        Label formSub   = UiHelper.muted("Completa los datos para registrarte en OpenLib Market");

        TextField nameField     = UiHelper.styledField("Nombre completo");
        TextField usernameField = UiHelper.styledField("Nombre de usuario");
        TextField emailField    = UiHelper.styledField("correo@ejemplo.com");
        PasswordField passField = UiHelper.styledPassword("Contraseña (mín. 8 chars, mayúscula + número)");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("BUYER", "SELLER");
        roleBox.setValue("BUYER");
        roleBox.setStyle("-fx-font-size: 14px;");
        roleBox.setMaxWidth(Double.MAX_VALUE);

        Label errorLbl = UiHelper.errorLabel();
        ProgressIndicator spinner = UiHelper.spinner();
        spinner.setVisible(false);

        Button registerBtn = UiHelper.primaryBtn("Crear Cuenta");
        registerBtn.setMaxWidth(Double.MAX_VALUE);

        HBox loginRow = new HBox(6);
        loginRow.setAlignment(Pos.CENTER);
        loginRow.getChildren().addAll(UiHelper.muted("¿Ya tienes cuenta?"), UiHelper.linkBtn("Inicia sesión"));
        ((Button) loginRow.getChildren().get(1)).setOnAction(e -> NavigationManager.navigateTo("login"));

        registerBtn.setOnAction(e -> {
            errorLbl.setText("");
            String name  = nameField.getText().trim();
            String user  = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String pass  = passField.getText();
            String role  = roleBox.getValue();

            if (name.isEmpty() || user.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                errorLbl.setText("Todos los campos son obligatorios.");
                return;
            }
            if (pass.length() < 8) {
                errorLbl.setText("La contraseña debe tener al menos 8 caracteres.");
                return;
            }

            registerBtn.setDisable(true);
            spinner.setVisible(true);

            new Thread(() -> {
                var resp = ApiClient.postPublic("/auth/register", Map.of(
                        "fullName", name,
                        "username", user,
                        "email", email,
                        "password", pass,
                        "role", role
                ));
                Platform.runLater(() -> {
                    spinner.setVisible(false);
                    registerBtn.setDisable(false);
                    if (resp.isSuccess()) {
                        UiHelper.alert("¡Cuenta creada!", "Tu cuenta ha sido creada exitosamente. Ahora puedes iniciar sesión.", Alert.AlertType.INFORMATION);
                        NavigationManager.navigateTo("login");
                    } else {
                        errorLbl.setText("❌ " + resp.errorMessage());
                    }
                });
            }).start();
        });

        Label roleLabel = new Label("Tipo de cuenta:");
        card.getChildren().addAll(
                formTitle, formSub, UiHelper.separator(),
                new Label("Nombre completo:"), nameField,
                new Label("Usuario:"), usernameField,
                new Label("Email:"), emailField,
                new Label("Contraseña:"), passField,
                roleLabel, roleBox,
                errorLbl, spinner, registerBtn,
                UiHelper.separator(), loginRow
        );

        root.getChildren().add(card);
        return new Scene(root, 1100, 700);
    }
}
