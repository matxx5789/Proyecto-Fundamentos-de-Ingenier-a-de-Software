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

public class OrdersController {

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
        toolbar.getChildren().add(UiHelper.subtitle("📋 Historial de Órdenes"));
        top.getChildren().add(toolbar);
        root.setTop(top);

        VBox content = new VBox(12);
        content.setPadding(new Insets(24));
        content.getChildren().add(UiHelper.spinner());

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: " + UiHelper.SURFACE + ";");
        root.setCenter(scroll);

        new Thread(() -> {
            var resp = ApiClient.get("/orders");
            Platform.runLater(() -> {
                content.getChildren().clear();
                if (!resp.isSuccess()) {
                    content.getChildren().add(UiHelper.emptyState("❌ " + resp.errorMessage()));
                    return;
                }
                try {
                    var node = resp.json();
                    List<Models.OrderResponse> orders;
                    if (node.has("content")) {
                        orders = ApiClient.mapper.readValue(node.get("content").toString(), new TypeReference<>() {});
                    } else {
                        orders = ApiClient.mapper.readValue(resp.body(), new TypeReference<>() {});
                    }
                    if (orders.isEmpty()) {
                        content.getChildren().add(UiHelper.emptyState("📋 No tienes órdenes aún.\nRealiza tu primera compra en el catálogo."));
                        return;
                    }
                    for (var order : orders) content.getChildren().add(buildOrderRow(order));
                } catch (Exception ex) {
                    content.getChildren().add(UiHelper.emptyState("Error: " + ex.getMessage()));
                }
            });
        }).start();

        return new Scene(root, 1280, 800);
    }

    private VBox buildOrderRow(Models.OrderResponse order) {
        VBox card = UiHelper.card();

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Label idLbl = new Label("Orden #" + order.id);
        idLbl.setFont(Font.font("System", FontWeight.BOLD, 15));

        String statusColor = switch (order.status != null ? order.status : "") {
            case "CONFIRMED" -> UiHelper.SUCCESS;
            case "PENDING"   -> UiHelper.WARNING;
            case "CANCELLED" -> UiHelper.DANGER;
            default          -> UiHelper.TEXT_MUTED;
        };
        Label statusBadge = UiHelper.badge(order.status != null ? order.status : "?", statusColor);

        Label totalLbl = new Label(order.displayTotal());
        totalLbl.setFont(Font.font("System", FontWeight.BOLD, 16));
        totalLbl.setTextFill(Color.web(UiHelper.PRIMARY));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dateLbl = UiHelper.muted(order.createdAt != null ? order.createdAt.substring(0, 10) : "");

        Button detailBtn = UiHelper.outlineBtn("Ver detalle →");
        detailBtn.setOnAction(e -> NavigationManager.navigateTo("order_detail", order.id));

        header.getChildren().addAll(idLbl, statusBadge, spacer, totalLbl, dateLbl, detailBtn);
        card.getChildren().add(header);

        if (order.items != null && !order.items.isEmpty()) {
            card.getChildren().add(UiHelper.separator());
            for (var item : order.items) {
                Label itemLbl = UiHelper.muted("📖 " + item.bookTitle + " — " + (item.price == 0 ? "Gratis" : String.format("$%.2f", item.price)));
                card.getChildren().add(itemLbl);
            }
        }

        return card;
    }
}
