package com.openlib.fx.controller;

import com.openlib.fx.model.Models;
import com.openlib.fx.service.ApiClient;
import com.openlib.fx.util.NavigationManager;
import com.openlib.fx.util.UiHelper;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class BuyerReportsController {

    private VBox reportArea;

    public Scene buildScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UiHelper.SURFACE + ";");

        VBox top = new VBox();
        top.getChildren().add(UiHelper.navBar("BUYER"));

        HBox toolbar = new HBox(12);
        toolbar.setPadding(new Insets(16, 24, 12, 24));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: white; -fx-border-color: " + UiHelper.BORDER + "; -fx-border-width: 0 0 1 0;");
        toolbar.getChildren().add(UiHelper.subtitle("📊 Mis Reportes"));

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Button backBtn = UiHelper.outlineBtn("← Menú principal");
        backBtn.setOnAction(e -> NavigationManager.navigateTo("catalog"));
        toolbar.getChildren().addAll(sp, backBtn);
        top.getChildren().add(toolbar);

        VBox instructions = UiHelper.infoCard(
                "Resumen de actividad",
                "Aquí verás lo que has hecho en OpenLib: compras, gasto total y tu historial reciente."
        );
        instructions.setPadding(new Insets(0, 24, 12, 24));
        top.getChildren().add(instructions);

        top.getChildren().add(createBuyerMenu());

        reportArea = new VBox(16);
        reportArea.setPadding(new Insets(24));

        ScrollPane scroll = new ScrollPane(reportArea);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        root.setCenter(scroll);

        loadReport();
        return new Scene(root, 1280, 800);
    }

    private HBox createBuyerMenu() {
        HBox menu = new HBox(10);
        menu.setPadding(new Insets(0, 24, 12, 24));
        menu.setAlignment(Pos.CENTER_LEFT);

        Button catalogBtn = UiHelper.outlineBtn("Catálogo");
        Button libraryBtn = UiHelper.outlineBtn("Biblioteca");
        Button ordersBtn = UiHelper.outlineBtn("Órdenes");
        Button wishlistBtn = UiHelper.outlineBtn("Favoritos");
        Button cartBtn = UiHelper.outlineBtn("Carrito");

        catalogBtn.setOnAction(e -> NavigationManager.navigateTo("catalog"));
        libraryBtn.setOnAction(e -> NavigationManager.navigateTo("library"));
        ordersBtn.setOnAction(e -> NavigationManager.navigateTo("orders"));
        wishlistBtn.setOnAction(e -> NavigationManager.navigateTo("wishlist"));
        cartBtn.setOnAction(e -> NavigationManager.navigateTo("cart"));

        menu.getChildren().addAll(catalogBtn, libraryBtn, ordersBtn, wishlistBtn, cartBtn);
        return menu;
    }

    private void loadReport() {
        reportArea.getChildren().clear();
        reportArea.getChildren().add(UiHelper.spinner());

        new Thread(() -> {
            var resp = ApiClient.get("/reports/buyer");
            Platform.runLater(() -> {
                reportArea.getChildren().clear();
                if (!resp.isSuccess()) {
                    reportArea.getChildren().add(UiHelper.emptyState("No se pudo cargar el informe: " + resp.errorMessage()));
                    return;
                }
                try {
                    Models.BuyerReportResponse report = ApiClient.mapper.readValue(resp.body(), Models.BuyerReportResponse.class);
                    reportArea.getChildren().add(createStatsRow(report));
                    reportArea.getChildren().add(createRecentPurchases(report.recentPurchases));
                } catch (Exception e) {
                    reportArea.getChildren().add(UiHelper.emptyState("Error cargando informe: " + e.getMessage()));
                }
            });
        }).start();
    }

    private HBox createStatsRow(Models.BuyerReportResponse report) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER);
        row.setPrefHeight(140);

        row.getChildren().addAll(
                createStatCard("Pedidos completados", report.totalCompletedOrders != null ? report.totalCompletedOrders.toString() : "0", "Cantidad de pedidos satisfechos."),
                createStatCard("Libros comprados", report.totalBooksPurchased != null ? report.totalBooksPurchased.toString() : "0", "Artículos totales en tus pedidos."),
                createStatCard("Total gastado", report.displayTotalSpent(), "Monto total gastado en la plataforma.")
        );
        return row;
    }

    private VBox createStatCard(String title, String value, String subtitle) {
        VBox card = UiHelper.card();
        card.setPrefWidth(300);
        Label titleLbl = UiHelper.muted(title);
        Label valueLbl = new Label(value);
        valueLbl.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 22));
        Label subtitleLbl = new Label(subtitle);
        subtitleLbl.setFont(javafx.scene.text.Font.font(13));
        subtitleLbl.setTextFill(javafx.scene.paint.Color.web(UiHelper.TEXT_MUTED));
        card.getChildren().addAll(titleLbl, valueLbl, subtitleLbl);
        return card;
    }

    private VBox createRecentPurchases(List<Models.BookItem> books) {
        VBox box = new VBox(12);
        box.getChildren().add(UiHelper.subtitle("Compras recientes"));
        if (books == null || books.isEmpty()) {
            box.getChildren().add(UiHelper.emptyState("Aún no has comprado ningún libro."));
            return box;
        }
        for (Models.BookItem book : books) {
            HBox item = new HBox(12);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(12));
            item.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: " + UiHelper.BORDER + "; -fx-border-width: 1;");
            item.getChildren().add(UiHelper.bookCover(null, 72, 96, "📘"));

            VBox info = new VBox(4);
            Label title = new Label(book.title);
            title.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 14));
            Label author = UiHelper.muted(book.author);
            Label price = new Label(book.displayPrice());
            price.setFont(javafx.scene.text.Font.font(13));
            info.getChildren().addAll(title, author, price);
            item.getChildren().add(info);
            box.getChildren().add(item);
        }
        return box;
    }
}
