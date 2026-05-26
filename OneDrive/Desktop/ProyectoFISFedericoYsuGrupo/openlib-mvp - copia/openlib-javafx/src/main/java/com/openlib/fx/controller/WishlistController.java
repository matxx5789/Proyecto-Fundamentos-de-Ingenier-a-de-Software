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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class WishlistController {

    private VBox content;

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
        toolbar.getChildren().add(UiHelper.subtitle("❤️ Mis Favoritos"));
        top.getChildren().add(toolbar);
        root.setTop(top);

        content = new VBox(12);
        content.setPadding(new Insets(24));
        content.getChildren().add(UiHelper.spinner());

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        root.setCenter(scroll);

        loadWishlist();
        return new Scene(root, 1280, 800);
    }

    private void loadWishlist() {
        content.getChildren().clear();
        content.getChildren().add(UiHelper.spinner());

        new Thread(() -> {
            var resp = ApiClient.get("/wishlist");
            Platform.runLater(() -> {
                content.getChildren().clear();
                if (!resp.isSuccess()) {
                    content.getChildren().add(UiHelper.emptyState("❌ " + resp.errorMessage()));
                    return;
                }
                try {
                    List<Models.BookResponse> books = ApiClient.mapper.readValue(resp.body(), new TypeReference<>() {});
                    if (books.isEmpty()) {
                        content.getChildren().add(UiHelper.emptyState("❤️ Tu lista de favoritos está vacía.\nAgrega libros desde el catálogo."));
                        return;
                    }
                    content.getChildren().add(UiHelper.muted(books.size() + " libro(s) en favoritos"));
                    for (var book : books) content.getChildren().add(buildWishlistItem(book));
                } catch (Exception ex) {
                    content.getChildren().add(UiHelper.emptyState("Error: " + ex.getMessage()));
                }
            });
        }).start();
    }

    private HBox buildWishlistItem(Models.BookResponse book) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);");

        Label cover = new Label("📖");
        cover.setFont(Font.font(36));

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label titleLbl = new Label(book.title);
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
        Label authorLbl = UiHelper.muted("por " + book.author);
        Label rating = new Label(book.displayRating());
        info.getChildren().addAll(titleLbl, authorLbl, rating);

        if (book.category != null) info.getChildren().add(UiHelper.badge(book.category, UiHelper.PRIMARY));

        Button viewBtn = UiHelper.outlineBtn("Ver libro");
        viewBtn.setOnAction(e -> NavigationManager.navigateTo("book_detail", book.id));

        Button addCartBtn = UiHelper.primaryBtn("🛒 Agregar");
        addCartBtn.setOnAction(e -> {
            addCartBtn.setDisable(true);
            new Thread(() -> {
                var r = ApiClient.post("/cart", java.util.Map.of("bookId", book.id));
                Platform.runLater(() -> {
                    addCartBtn.setDisable(false);
                    if (r.isSuccess()) UiHelper.alert("¡Listo!", "Libro agregado al carrito.", Alert.AlertType.INFORMATION);
                    else UiHelper.alert("Error", r.errorMessage(), Alert.AlertType.ERROR);
                });
            }).start();
        });

        Button removeBtn = UiHelper.dangerBtn("❌ Quitar");
        removeBtn.setOnAction(e -> {
            removeBtn.setDisable(true);
            new Thread(() -> {
                var r = ApiClient.delete("/wishlist/" + book.id);
                Platform.runLater(() -> {
                    if (r.isSuccess()) loadWishlist();
                    else { removeBtn.setDisable(false); UiHelper.alert("Error", r.errorMessage(), Alert.AlertType.ERROR); }
                });
            }).start();
        });

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.getChildren().addAll(viewBtn, addCartBtn, removeBtn);

        row.getChildren().addAll(cover, info, actions);
        return row;
    }
}
