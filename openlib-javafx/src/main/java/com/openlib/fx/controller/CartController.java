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

import java.util.List;

public class CartController {

    private VBox itemsBox;
    private Label totalLbl;
    private Label emptyLbl;

    public Scene buildScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UiHelper.SURFACE + ";");

        VBox top = new VBox();
        HBox nav = UiHelper.navBar(SessionManager.getUserRole());
        Button backBtn = UiHelper.outlineBtn("← Catálogo");
        backBtn.setOnAction(e -> NavigationManager.navigateTo("catalog"));
        nav.getChildren().add(1, backBtn);
        top.getChildren().add(nav);

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setPadding(new Insets(16, 24, 12, 24));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: white; -fx-border-color: " + UiHelper.BORDER + "; -fx-border-width: 0 0 1 0;");
        toolbar.getChildren().add(UiHelper.subtitle("🛒 Mi Carrito"));
        top.getChildren().add(toolbar);

        // Menú de navegación
        top.getChildren().add(createBuyerMenu());
        root.setTop(top);

        // Contenido
        HBox content = new HBox(20);
        content.setPadding(new Insets(24));
        HBox.setHgrow(content, Priority.ALWAYS);

        // Lista de items
        itemsBox = new VBox(12);
        HBox.setHgrow(itemsBox, Priority.ALWAYS);
        emptyLbl = UiHelper.emptyState("🛒 Tu carrito está vacío.\nExplora el catálogo para agregar libros.");
        emptyLbl.setVisible(false);
        itemsBox.getChildren().add(emptyLbl);

        ScrollPane scroll = new ScrollPane(itemsBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: " + UiHelper.SURFACE + ";");
        HBox.setHgrow(scroll, Priority.ALWAYS);

        // Panel de resumen
        VBox summary = UiHelper.card();
        summary.setPrefWidth(280);
        summary.setMaxWidth(280);
        summary.setSpacing(14);

        Label summaryTitle = UiHelper.subtitle("Resumen del Pedido");
        totalLbl = new Label("Total: $0.00");
        totalLbl.setFont(Font.font("System", FontWeight.BOLD, 20));
        totalLbl.setTextFill(Color.web(UiHelper.PRIMARY));

        Button checkoutBtn = UiHelper.primaryBtn("💳 Proceder al Checkout");
        checkoutBtn.setMaxWidth(Double.MAX_VALUE);
        checkoutBtn.setOnAction(e -> NavigationManager.navigateTo("checkout"));

        Button clearBtn = UiHelper.dangerBtn("🗑️ Vaciar Carrito");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> clearCart(clearBtn));

        summary.getChildren().addAll(summaryTitle, UiHelper.separator(), totalLbl, checkoutBtn, clearBtn);

        content.getChildren().addAll(scroll, summary);
        root.setCenter(content);

        loadCart();
        return new Scene(root, 1280, 800);
    }

    private void loadCart() {
        itemsBox.getChildren().clear();
        itemsBox.getChildren().add(UiHelper.spinner());

        new Thread(() -> {
            var resp = ApiClient.get("/cart");
            Platform.runLater(() -> {
                itemsBox.getChildren().clear();
                if (!resp.isSuccess()) {
                    itemsBox.getChildren().add(UiHelper.emptyState("❌ Error al cargar el carrito."));
                    return;
                }
                try {
                    List<Models.CartItemResponse> items = ApiClient.mapper.readValue(resp.body(), new TypeReference<>() {});
                    if (items.isEmpty()) {
                        itemsBox.getChildren().add(UiHelper.emptyState("🛒 Tu carrito está vacío."));
                        totalLbl.setText("Total: $0.00");
                        return;
                    }
                    double total = 0;
                    for (var item : items) {
                        itemsBox.getChildren().add(buildCartItem(item));
                        total += item.price != null ? item.price : 0;
                    }
                    totalLbl.setText("Total: " + (total == 0 ? "¡Gratis!" : String.format("$%.2f", total)));
                } catch (Exception ex) {
                    itemsBox.getChildren().add(UiHelper.emptyState("Error: " + ex.getMessage()));
                }
            });
        }).start();
    }

    private HBox buildCartItem(Models.CartItemResponse item) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);");

        var cover = UiHelper.bookCover(item.bookCover, 80, 100, "📖");

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label titleLbl = new Label(item.bookTitle);
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
        Label authorLbl = UiHelper.muted(item.bookAuthor != null ? item.bookAuthor : "");
        info.getChildren().addAll(titleLbl, authorLbl);

        Label priceLbl = new Label(item.price == null || item.price == 0 ? "Gratis" : String.format("$%.2f", item.price));
        priceLbl.setFont(Font.font("System", FontWeight.BOLD, 16));
        priceLbl.setTextFill(Color.web(UiHelper.SUCCESS));

        Button removeBtn = UiHelper.dangerBtn("Eliminar");
        removeBtn.setOnAction(e -> removeItem(item.id, removeBtn));

        row.getChildren().addAll(cover, info, priceLbl, removeBtn);
        return row;
    }

    private void removeItem(Long itemId, Button btn) {
        btn.setDisable(true);
        new Thread(() -> {
            var resp = ApiClient.delete("/cart/" + itemId);
            Platform.runLater(() -> {
                if (resp.isSuccess()) loadCart();
                else UiHelper.alert("Error", resp.errorMessage(), Alert.AlertType.ERROR);
            });
        }).start();
    }

    private void clearCart(Button btn) {
        btn.setDisable(true);
        new Thread(() -> {
            var resp = ApiClient.delete("/cart");
            Platform.runLater(() -> {
                btn.setDisable(false);
                if (resp.isSuccess()) loadCart();
                else UiHelper.alert("Error", resp.errorMessage(), Alert.AlertType.ERROR);
            });
        }).start();
    }

    private HBox createBuyerMenu() {
        HBox menu = new HBox(10);
        menu.setPadding(new Insets(0, 24, 12, 24));
        menu.setAlignment(Pos.CENTER_LEFT);

        Button catalogBtn = UiHelper.outlineBtn("🛍️ Catálogo");
        Button libraryBtn = UiHelper.outlineBtn("📚 Biblioteca");
        Button ordersBtn = UiHelper.outlineBtn("📋 Órdenes");
        Button reportsBtn = UiHelper.outlineBtn("📊 Reportes");
        Button wishlistBtn = UiHelper.outlineBtn("❤️ Favoritos");

        catalogBtn.setOnAction(e -> NavigationManager.navigateTo("catalog"));
        libraryBtn.setOnAction(e -> NavigationManager.navigateTo("library"));
        ordersBtn.setOnAction(e -> NavigationManager.navigateTo("orders"));
        reportsBtn.setOnAction(e -> NavigationManager.navigateTo("reports"));
        wishlistBtn.setOnAction(e -> NavigationManager.navigateTo("wishlist"));

        menu.getChildren().addAll(catalogBtn, libraryBtn, ordersBtn, reportsBtn, wishlistBtn);
        return menu;
    }
}
