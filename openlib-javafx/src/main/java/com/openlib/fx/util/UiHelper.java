package com.openlib.fx.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Helpers de UI reutilizables para construir la interfaz de forma consistente.
 */
public class UiHelper {

    // ── Colores ────────────────────────────────────────────────────
    public static final String PRIMARY   = "#2563EB";   // Azul
    public static final String SUCCESS   = "#16A34A";   // Verde
    public static final String DANGER    = "#DC2626";   // Rojo
    public static final String WARNING   = "#D97706";   // Naranja
    public static final String DARK      = "#1E293B";   // Fondo oscuro
    public static final String SURFACE   = "#F8FAFC";   // Fondo claro
    public static final String CARD      = "#FFFFFF";   // Tarjeta
    public static final String BORDER    = "#E2E8F0";   // Borde
    public static final String TEXT_DARK = "#0F172A";   // Texto principal
    public static final String TEXT_MUTED= "#64748B";   // Texto secundario

    // ── Botones ────────────────────────────────────────────────────

    public static Button primaryBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + PRIMARY + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 24; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #1D4ED8; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 24; " +
                "-fx-background-radius: 8; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + PRIMARY + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 24; " +
                "-fx-background-radius: 8; -fx-cursor: hand;"));
        return btn;
    }

    public static Button dangerBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + DANGER + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 8 18; " +
                "-fx-background-radius: 6; -fx-cursor: hand;");
        return btn;
    }

    public static Button successBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + SUCCESS + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 8 18; " +
                "-fx-background-radius: 6; -fx-cursor: hand;");
        return btn;
    }

    public static Button outlineBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY + "; " +
                "-fx-border-color: " + PRIMARY + "; -fx-border-radius: 6; " +
                "-fx-font-size: 13px; -fx-padding: 7 16; -fx-cursor: hand;");
        return btn;
    }

    public static Button linkBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY + "; " +
                "-fx-font-size: 13px; -fx-cursor: hand; -fx-underline: true;");
        return btn;
    }

    // ── Labels ─────────────────────────────────────────────────────

    public static Label title(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 26));
        lbl.setTextFill(Color.web(TEXT_DARK));
        return lbl;
    }

    public static Label subtitle(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 18));
        lbl.setTextFill(Color.web(TEXT_DARK));
        return lbl;
    }

    public static Label muted(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font(13));
        lbl.setTextFill(Color.web(TEXT_MUTED));
        return lbl;
    }

    public static Label badge(String text, String color) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-background-color: " + color + "22; -fx-text-fill: " + color + "; " +
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 2 8; " +
                "-fx-background-radius: 12;");
        return lbl;
    }

    // ── Inputs ─────────────────────────────────────────────────────

    public static TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-color: white; -fx-border-color: " + BORDER + "; " +
                "-fx-border-radius: 6; -fx-background-radius: 6; " +
                "-fx-padding: 9 12; -fx-font-size: 14px;");
        tf.setPrefHeight(38);
        return tf;
    }

    public static PasswordField styledPassword(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setStyle("-fx-background-color: white; -fx-border-color: " + BORDER + "; " +
                "-fx-border-radius: 6; -fx-background-radius: 6; " +
                "-fx-padding: 9 12; -fx-font-size: 14px;");
        pf.setPrefHeight(38);
        return pf;
    }

    public static TextArea styledTextArea(String prompt) {
        TextArea ta = new TextArea();
        ta.setPromptText(prompt);
        ta.setWrapText(true);
        ta.setPrefRowCount(3);
        ta.setStyle("-fx-background-color: white; -fx-border-color: " + BORDER + "; " +
                "-fx-border-radius: 6; -fx-background-radius: 6; " +
                "-fx-padding: 9 12; -fx-font-size: 14px;");
        return ta;
    }

    // ── Contenedores ───────────────────────────────────────────────

    public static VBox card() {
        VBox box = new VBox(12);
        box.setStyle("-fx-background-color: " + CARD + "; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        box.setPadding(new Insets(20));
        return box;
    }

    public static VBox infoCard(String title, String message) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color: #E0F2FE; -fx-background-radius: 12; " +
                "-fx-border-color: #7DD3FC; -fx-border-width: 1;");
        Label titleLbl = new Label(title);
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLbl.setTextFill(Color.web("#0C4A6E"));
        Label msgLbl = new Label(message);
        msgLbl.setFont(Font.font(13));
        msgLbl.setTextFill(Color.web("#1E3A8A"));
        msgLbl.setWrapText(true);
        box.getChildren().addAll(titleLbl, msgLbl);
        return box;
    }

    public static HBox navBar(String currentRole) {
        HBox nav = new HBox(8);
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setPadding(new Insets(12, 24, 12, 24));
        nav.setStyle("-fx-background-color: " + DARK + ";");
        nav.setPrefHeight(56);

        Label logo = new Label("📚 OpenLib");
        logo.setFont(Font.font("System", FontWeight.BOLD, 20));
        logo.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userLbl = new Label("👤 " + SessionManager.getUserFullName() + " (" + currentRole + ")");
        userLbl.setTextFill(Color.web("#94A3B8"));
        userLbl.setFont(Font.font(13));

        Button logoutBtn = new Button("Salir");
        logoutBtn.setStyle("-fx-background-color: #374151; -fx-text-fill: white; " +
                "-fx-font-size: 12px; -fx-padding: 5 12; -fx-background-radius: 6; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> {
            SessionManager.clear();
            NavigationManager.navigateTo("login");
        });

        nav.getChildren().addAll(logo, spacer, userLbl, logoutBtn);
        return nav;
    }

    public static Separator separator() {
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: " + BORDER + ";");
        return sep;
    }

    public static Label errorLabel() {
        Label lbl = new Label();
        lbl.setTextFill(Color.web(DANGER));
        lbl.setFont(Font.font(13));
        lbl.setWrapText(true);
        return lbl;
    }

    public static ProgressIndicator spinner() {
        ProgressIndicator pi = new ProgressIndicator();
        pi.setMaxSize(40, 40);
        return pi;
    }

    public static Label emptyState(String msg) {
        Label lbl = new Label(msg);
        lbl.setFont(Font.font(16));
        lbl.setTextFill(Color.web(TEXT_MUTED));
        lbl.setAlignment(Pos.CENTER);
        return lbl;
    }

    public static Node bookCover(String imageUrl, double width, double height, String placeholder) {
        if (imageUrl != null && !imageUrl.isBlank()) {
            try {
                Image image = new Image(imageUrl, width, height, true, true, true);
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(width);
                imageView.setFitHeight(height);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                imageView.setStyle("-fx-background-color: #EFF6FF; -fx-background-radius: 12;");
                return imageView;
            } catch (Exception ignored) {
                // fallback to placeholder
            }
        }

        Label cover = new Label(placeholder);
        cover.setFont(Font.font(48));
        cover.setAlignment(Pos.CENTER);
        cover.setMinSize(width, height);
        cover.setMaxSize(width, height);
        cover.setStyle("-fx-background-color: #EFF6FF; -fx-background-radius: 12; -fx-padding: 12;");
        return cover;
    }

    public static void alert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
