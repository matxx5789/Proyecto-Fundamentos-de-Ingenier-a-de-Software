package com.openlib.fx.controller;

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

public class OrderDetailController {

    private final Long orderId;

    public OrderDetailController(Long orderId) {
        this.orderId = orderId;
    }

    public Scene buildScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UiHelper.SURFACE + ";");

        VBox top = new VBox();
        HBox nav = UiHelper.navBar(SessionManager.getUserRole());
        Button backBtn = UiHelper.outlineBtn("← Órdenes");
        backBtn.setOnAction(e -> NavigationManager.navigateTo("orders"));
        nav.getChildren().add(1, backBtn);
        top.getChildren().add(nav);
        root.setTop(top);

        VBox content = new VBox(16);
        content.setPadding(new Insets(32, 60, 32, 60));
        content.getChildren().add(UiHelper.spinner());

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        root.setCenter(scroll);

        new Thread(() -> {
            var resp = ApiClient.get("/orders/" + orderId);
            Platform.runLater(() -> {
                content.getChildren().clear();
                if (!resp.isSuccess()) {
                    content.getChildren().add(UiHelper.emptyState("❌ " + resp.errorMessage()));
                    return;
                }
                Models.OrderResponse order = resp.as(Models.OrderResponse.class);
                if (order == null) return;

                content.getChildren().add(UiHelper.title("Detalle de Orden #" + order.id));

                // Info principal
                VBox info = UiHelper.card();
                info.setMaxWidth(600);
                info.getChildren().addAll(
                        UiHelper.subtitle("Información de la Orden"),
                        UiHelper.separator()
                );
                addInfoRow(info, "Estado:", order.status);
                addInfoRow(info, "Método de pago:", order.paymentMethod);
                addInfoRow(info, "Total:", order.displayTotal());
                addInfoRow(info, "Fecha:", order.createdAt != null ? order.createdAt.substring(0, 10) : "N/A");

                content.getChildren().add(info);

                // Panel de acciones para cambiar estado
                VBox actions = UiHelper.card();
                actions.setMaxWidth(600);
                actions.setStyle("-fx-background-color: " + UiHelper.PRIMARY.replace("#", "#") + "15;");
                actions.getChildren().addAll(
                        UiHelper.subtitle("🔄 Estado de la Orden"),
                        UiHelper.separator()
                );

                HBox actionButtons = new HBox(12);
                actionButtons.setPadding(new Insets(8, 0, 0, 0));

                // Mostrar acciones según el estado actual
                if ("PENDING".equals(order.status)) {
                    // Estado: PENDING - el admin debe confirmar el pago
                    Label statusMsg = new Label("⏳ Esperando confirmación del admin...");
                    statusMsg.setFont(Font.font("System", FontWeight.BOLD, 13));
                    statusMsg.setTextFill(Color.web(UiHelper.WARNING));
                    
                    Button cancelBtn = UiHelper.dangerBtn("❌ Cancelar Orden");
                    cancelBtn.setOnAction(e -> changeOrderStatus(orderId, "CANCELLED", content, "cancelada"));
                    
                    actionButtons.getChildren().addAll(statusMsg, cancelBtn);
                } else if ("CONFIRMED".equals(order.status)) {
                    // Estado: CONFIRMED - puede cancelar o marcar completada
                    Label statusMsg = new Label("✅ Orden confirmada. Puedes descargar tus libros.");
                    statusMsg.setFont(Font.font("System", FontWeight.BOLD, 13));
                    statusMsg.setTextFill(Color.web(UiHelper.SUCCESS));
                    
                    Button cancelBtn = UiHelper.dangerBtn("❌ Cancelar Orden");
                    cancelBtn.setOnAction(e -> changeOrderStatus(orderId, "CANCELLED", content, "cancelada"));

                    Button completeBtn = UiHelper.successBtn("✅ Marcar Completada");
                    completeBtn.setOnAction(e -> changeOrderStatus(orderId, "COMPLETED", content, "completada"));

                    actionButtons.getChildren().addAll(statusMsg, cancelBtn, completeBtn);
                } else if ("CANCELLED".equals(order.status)) {
                    // Estado: CANCELLED - no se puede cambiar
                    Label statusMsg = new Label("🚫 Esta orden ha sido cancelada y no se puede cambiar.");
                    statusMsg.setFont(Font.font("System", FontWeight.BOLD, 13));
                    statusMsg.setTextFill(Color.web(UiHelper.DANGER));
                    actionButtons.getChildren().add(statusMsg);
                } else if ("COMPLETED".equals(order.status)) {
                    // Estado: COMPLETED - orden finalizada
                    Label statusMsg = new Label("✅ Esta orden ha sido completada.");
                    statusMsg.setFont(Font.font("System", FontWeight.BOLD, 13));
                    statusMsg.setTextFill(Color.web(UiHelper.SUCCESS));
                    actionButtons.getChildren().add(statusMsg);
                }

                actions.getChildren().add(actionButtons);
                content.getChildren().add(actions);

                // Datos de facturación
                VBox billing = UiHelper.card();
                billing.setMaxWidth(600);
                billing.getChildren().addAll(UiHelper.subtitle("Datos de Facturación"), UiHelper.separator());
                addInfoRow(billing, "Nombre:", order.billingName);
                addInfoRow(billing, "Email:", order.billingEmail);
                addInfoRow(billing, "Dirección:", order.billingAddress);
                content.getChildren().add(billing);

                // Libros
                VBox items = UiHelper.card();
                items.setMaxWidth(600);
                items.getChildren().addAll(UiHelper.subtitle("Libros Adquiridos"), UiHelper.separator());
                if (order.items != null) {
                    for (var item : order.items) {
                        HBox row = new HBox(12);
                        row.setAlignment(Pos.CENTER_LEFT);
                        Label icon = new Label("📖");
                        icon.setFont(Font.font(20));
                        VBox itemInfo = new VBox(2);
                        Label t = new Label(item.bookTitle);
                        t.setFont(Font.font("System", FontWeight.BOLD, 14));
                        Label a = UiHelper.muted(item.bookAuthor != null ? item.bookAuthor : "");
                        itemInfo.getChildren().addAll(t, a);
                        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
                        Label price = new Label(item.price == null || item.price == 0 ? "Gratis" : String.format("$%.2f", item.price));
                        price.setFont(Font.font("System", FontWeight.BOLD, 14));
                        price.setTextFill(Color.web(UiHelper.SUCCESS));
                        row.getChildren().addAll(icon, itemInfo, sp, price);
                        items.getChildren().add(row);
                    }
                }
                content.getChildren().add(items);

                Button libBtn = UiHelper.primaryBtn("📚 Ir a mi Biblioteca");
                libBtn.setOnAction(e -> NavigationManager.navigateTo("library"));
                content.getChildren().add(libBtn);
            });
        }).start();

        return new Scene(root, 1280, 800);
    }

    private void addInfoRow(VBox parent, String key, String value) {
        HBox row = new HBox(8);
        Label k = new Label(key);
        k.setFont(Font.font("System", FontWeight.BOLD, 13));
        k.setMinWidth(130);
        Label v = new Label(value != null ? value : "N/A");
        v.setFont(Font.font(13));
        row.getChildren().addAll(k, v);
        parent.getChildren().add(row);
    }

    private void changeOrderStatus(Long orderId, String newStatus, VBox content, String statusName) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar cambio");
        confirm.setHeaderText("Cambiar estado de orden");
        confirm.setContentText("¿Estás seguro de que deseas marcar esta orden como " + statusName + "?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        new Thread(() -> {
            var resp = ApiClient.put("/orders/" + orderId + "/status/" + newStatus, null);
            Platform.runLater(() -> {
                if (!resp.isSuccess()) {
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.setTitle("Error");
                    err.setHeaderText("No se pudo actualizar el estado");
                    err.setContentText(resp.errorMessage());
                    err.showAndWait();
                    return;
                }
                Alert suc = new Alert(Alert.AlertType.INFORMATION);
                suc.setTitle("Éxito");
                suc.setHeaderText("Estado actualizado");
                suc.setContentText("Estado actualizado a: " + statusName);
                suc.showAndWait();
                // Recargar la orden
                content.getChildren().clear();
                content.getChildren().add(UiHelper.spinner());
                // Re-ejecutar la carga inicial
                new Thread(() -> {
                    var newResp = ApiClient.get("/orders/" + orderId);
                    Platform.runLater(() -> {
                        content.getChildren().clear();
                        if (!newResp.isSuccess()) {
                            content.getChildren().add(UiHelper.emptyState("❌ " + newResp.errorMessage()));
                            return;
                        }
                        // Reconstruir la escena desde cero
                        OrderDetailController ctrl = new OrderDetailController(orderId);
                        NavigationManager.navigateTo("order-detail");
                    });
                }).start();
            });
        }).start();
    }
}
