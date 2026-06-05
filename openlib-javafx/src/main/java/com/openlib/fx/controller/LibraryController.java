package com.openlib.fx.controller;

import com.fasterxml.jackson.core.type.TypeReference;
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

import java.awt.Desktop;
import java.net.URI;
import java.util.List;

public class LibraryController {

    public Scene buildScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UiHelper.SURFACE + ";");

        VBox top = new VBox();
        HBox nav = UiHelper.navBar(SessionManager.getUserRole());
        Button backBtn = UiHelper.outlineBtn("← Catálogo");
        backBtn.setOnAction(e -> NavigationManager.navigateTo("catalog"));
        nav.getChildren().add(1, backBtn);
        top.getChildren().add(nav);

        HBox toolbar = new HBox(12);
        toolbar.setPadding(new Insets(16, 24, 12, 24));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: white; -fx-border-color: " + UiHelper.BORDER + "; -fx-border-width: 0 0 1 0;");
        toolbar.getChildren().addAll(UiHelper.subtitle("📚 Mi Biblioteca Personal"),
                UiHelper.muted(" — Libros adquiridos disponibles para descarga"));
        top.getChildren().add(toolbar);

        // Menú de navegación
        top.getChildren().add(createBuyerMenu());
        root.setTop(top);

        VBox content = new VBox(12);
        content.setPadding(new Insets(24));
        content.getChildren().add(UiHelper.spinner());

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: " + UiHelper.SURFACE + ";");
        root.setCenter(scroll);

        new Thread(() -> {
            var resp = ApiClient.get("/library");
            Platform.runLater(() -> {
                content.getChildren().clear();
                if (!resp.isSuccess()) {
                    content.getChildren().add(UiHelper.emptyState("❌ Error: " + resp.errorMessage()));
                    return;
                }
                try {
                    List<Models.BookResponse> books = ApiClient.mapper.readValue(resp.body(), new TypeReference<>() {});
                    if (books.isEmpty()) {
                        content.getChildren().add(UiHelper.emptyState(
                                "📚 Tu biblioteca está vacía.\n" +
                                "Explora el catálogo y agrega libros al carrito para adquirirlos."));
                        Button goBtn = UiHelper.primaryBtn("Ir al Catálogo");
                        goBtn.setOnAction(e -> NavigationManager.navigateTo("catalog"));
                        content.setAlignment(Pos.CENTER);
                        content.getChildren().add(goBtn);
                        return;
                    }
                    content.getChildren().add(UiHelper.muted(books.size() + " libro(s) en tu biblioteca"));
                    for (var book : books) {
                        content.getChildren().add(buildLibraryItem(book));
                    }
                } catch (Exception ex) {
                    content.getChildren().add(UiHelper.emptyState("Error: " + ex.getMessage()));
                }
            });
        }).start();

        return new Scene(root, 1280, 800);
    }

    private HBox buildLibraryItem(Models.BookResponse book) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 8, 0, 0, 2);");

        var cover = UiHelper.bookCover(book.coverImageUrl, 100, 120, "📖");

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label titleLbl = new Label(book.title);
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 16));

        Label authorLbl = UiHelper.muted("por " + book.author);

        HBox meta = new HBox(8);
        meta.setAlignment(Pos.CENTER_LEFT);
        if (book.category != null) meta.getChildren().add(UiHelper.badge(book.category, UiHelper.PRIMARY));
        meta.getChildren().add(UiHelper.badge("Adquirido ✓", UiHelper.SUCCESS));
        if (book.downloadCount != null)
            meta.getChildren().add(UiHelper.muted("📥 " + book.downloadCount + " descargas globales"));

        info.getChildren().addAll(titleLbl, authorLbl, meta);

        // Botones
        VBox actions = new VBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button downloadBtn = UiHelper.successBtn("⬇️ Descargar");
        downloadBtn.setOnAction(e -> requestDownload(book.id, book.title, downloadBtn));

        Button detailBtn = UiHelper.outlineBtn("Ver detalles");
        detailBtn.setOnAction(e -> NavigationManager.navigateTo("book_detail", book.id));

        actions.getChildren().addAll(downloadBtn, detailBtn);

        row.getChildren().addAll(cover, info, actions);
        return row;
    }

    private void requestDownload(Long bookId, String title, Button btn) {
        btn.setDisable(true);
        btn.setText("⏳ Generando enlace...");

        new Thread(() -> {
            var resp = ApiClient.get("/library/" + bookId + "/download-url");
            Platform.runLater(() -> {
                btn.setDisable(false);
                btn.setText("⬇️ Descargar");

                if (!resp.isSuccess()) {
                    UiHelper.alert("Error", resp.errorMessage(), Alert.AlertType.ERROR);
                    return;
                }

                Models.DownloadResponse dl = resp.as(Models.DownloadResponse.class);
                if (dl == null || dl.signedUrl == null) {
                    UiHelper.alert("Error", "No se pudo obtener el enlace de descarga.", Alert.AlertType.ERROR);
                    return;
                }

                // Intentar abrir en el navegador del sistema
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Enlace de Descarga Generado");
                confirm.setHeaderText("\"" + title + "\"");
                confirm.setContentText(
                        "Se generó un enlace seguro (válido 5 minutos, 1 uso).\n\n" +
                        "URL: " + dl.signedUrl + "\n\n" +
                        "¿Deseas abrir el enlace en tu navegador?"
                );
                confirm.showAndWait().ifPresent(btn2 -> {
                    if (btn2 == ButtonType.OK) {
                        try {
                            Desktop.getDesktop().browse(new URI(dl.signedUrl));
                        } catch (Exception ex) {
                            UiHelper.alert("Info", "Copia este enlace en tu navegador:\n" + dl.signedUrl, Alert.AlertType.INFORMATION);
                        }
                    }
                });
            });
        }).start();
    }

    private HBox createBuyerMenu() {
        HBox menu = new HBox(10);
        menu.setPadding(new Insets(0, 24, 12, 24));
        menu.setAlignment(Pos.CENTER_LEFT);

        Button catalogBtn = UiHelper.outlineBtn("🛍️ Catálogo");
        Button ordersBtn = UiHelper.outlineBtn("📋 Órdenes");
        Button reportsBtn = UiHelper.outlineBtn("📊 Reportes");
        Button wishlistBtn = UiHelper.outlineBtn("❤️ Favoritos");
        Button cartBtn = UiHelper.outlineBtn("🛒 Carrito");

        catalogBtn.setOnAction(e -> NavigationManager.navigateTo("catalog"));
        ordersBtn.setOnAction(e -> NavigationManager.navigateTo("orders"));
        reportsBtn.setOnAction(e -> NavigationManager.navigateTo("reports"));
        wishlistBtn.setOnAction(e -> NavigationManager.navigateTo("wishlist"));
        cartBtn.setOnAction(e -> NavigationManager.navigateTo("cart"));

        menu.getChildren().addAll(catalogBtn, ordersBtn, reportsBtn, wishlistBtn, cartBtn);
        return menu;
    }
}
