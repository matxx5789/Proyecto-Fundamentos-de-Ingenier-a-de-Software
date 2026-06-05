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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Controlador para mostrar métricas de ventas del sistema
 */
public class MetricsController {

    private VBox metricsArea;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;

    public Scene buildScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UiHelper.SURFACE + ";");

        VBox top = new VBox();
        top.getChildren().add(UiHelper.navBar("ADMIN"));

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setPadding(new Insets(16, 24, 12, 24));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: white; -fx-border-color: " + UiHelper.BORDER + "; -fx-border-width: 0 0 1 0;");
        toolbar.getChildren().add(UiHelper.subtitle("📊 Métricas del Sistema"));

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Button backBtn = UiHelper.outlineBtn("← Volver");
        backBtn.setOnAction(e -> NavigationManager.navigateTo("admin_panel"));
        toolbar.getChildren().addAll(sp, backBtn);
        top.getChildren().add(toolbar);

        // Info card
        VBox instructions = UiHelper.infoCard(
                "Análisis de ventas",
                "Visualiza métricas detalladas: libros vendidos, órdenes por estado, tasa de conversión del carrito a venta."
        );
        instructions.setPadding(new Insets(0, 24, 12, 24));
        top.getChildren().add(instructions);

        // Filtro de fechas
        top.getChildren().add(createDateFilterPanel());

        root.setTop(top);

        metricsArea = new VBox(16);
        metricsArea.setPadding(new Insets(24));

        ScrollPane scroll = new ScrollPane(metricsArea);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        root.setCenter(scroll);

        loadMetrics();
        return new Scene(root, 1280, 800);
    }

    private VBox createDateFilterPanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(0, 24, 12, 24));
        panel.setStyle("-fx-background-color: white; -fx-border-color: " + UiHelper.BORDER + "; -fx-border-width: 0 0 1 0;");

        Label filterLabel = new Label("Filtrar por período:");
        filterLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        HBox dateBox = new HBox(12);
        dateBox.setAlignment(Pos.CENTER_LEFT);

        // Fecha inicial (por defecto hace 30 días)
        Label startLabel = new Label("Desde:");
        startDatePicker = new DatePicker(LocalDate.now().minusDays(30));
        startDatePicker.setPrefWidth(150);

        // Fecha final (hoy)
        Label endLabel = new Label("Hasta:");
        endDatePicker = new DatePicker(LocalDate.now());
        endDatePicker.setPrefWidth(150);

        Button applyBtn = UiHelper.primaryBtn("Aplicar filtro");
        applyBtn.setOnAction(e -> loadMetrics());

        Button resetBtn = UiHelper.outlineBtn("Restaurar");
        resetBtn.setOnAction(e -> {
            startDatePicker.setValue(LocalDate.now().minusDays(30));
            endDatePicker.setValue(LocalDate.now());
            loadMetrics();
        });

        dateBox.getChildren().addAll(
                startLabel, startDatePicker,
                endLabel, endDatePicker,
                applyBtn, resetBtn
        );

        panel.getChildren().addAll(filterLabel, dateBox);
        return panel;
    }

    private void loadMetrics() {
        metricsArea.getChildren().clear();
        metricsArea.getChildren().add(UiHelper.spinner());

        new Thread(() -> {
            // Convertir fechas a LocalDateTime
            LocalDateTime startDateTime = LocalDateTime.of(startDatePicker.getValue(), LocalTime.MIDNIGHT);
            LocalDateTime endDateTime = LocalDateTime.of(endDatePicker.getValue(), LocalTime.of(23, 59, 59));

            // Construir URL con parámetros
            String url = String.format("/reports/metrics?startDate=%s&endDate=%s",
                    startDateTime.toString().replace(" ", "T"),
                    endDateTime.toString().replace(" ", "T"));

            var resp = ApiClient.get(url);

            Platform.runLater(() -> {
                metricsArea.getChildren().clear();
                if (!resp.isSuccess()) {
                    metricsArea.getChildren().add(UiHelper.emptyState("No se pudo cargar las métricas: " + resp.errorMessage()));
                    return;
                }
                try {
                    Models.MetricsResponse metrics = ApiClient.mapper.readValue(resp.body(), Models.MetricsResponse.class);
                    metricsArea.getChildren().addAll(
                            createMetricsHeader(metrics),
                            createMainMetricsRow(metrics),
                            createOrdersStatusPanel(metrics),
                            createConversionRateCard(metrics)
                    );
                } catch (Exception e) {
                    metricsArea.getChildren().add(UiHelper.emptyState("Error cargando métricas: " + e.getMessage()));
                }
            });
        }).start();
    }

    private VBox createMetricsHeader(Models.MetricsResponse metrics) {
        VBox header = new VBox(8);
        Label periodLabel = new Label("Período: " + metrics.getPeriodDisplay());
        periodLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        periodLabel.setTextFill(Color.web(UiHelper.TEXT_MUTED));
        header.getChildren().add(periodLabel);
        return header;
    }

    private HBox createMainMetricsRow(Models.MetricsResponse metrics) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.TOP_CENTER);

        row.getChildren().addAll(
                createMetricCard("📚 Libros vendidos", metrics.booksSoldInPeriod.toString(), "Total de títulos únicos vendidos en el período"),
                createMetricCard("🛒 Items en carrito", metrics.totalCartItems.toString(), "Total de items agregados al carrito"),
                createMetricCard("✅ Items vendidos", metrics.totalItemsSold.toString(), "Total de items comprados/descargados")
        );
        return row;
    }

    private VBox createOrdersStatusPanel(Models.MetricsResponse metrics) {
        VBox panel = new VBox(12);
        panel.getChildren().add(UiHelper.subtitle("📋 Órdenes por estado"));

        HBox statusRow = new HBox(16);
        statusRow.setAlignment(Pos.TOP_CENTER);

        if (metrics.ordersByStatus != null) {
            for (String status : metrics.ordersByStatus.keySet()) {
                Long count = metrics.ordersByStatus.get(status);
                String statusName = formatStatusName(status);
                statusRow.getChildren().add(createStatusCard(statusName, count.toString(), getStatusColor(status)));
            }
        }

        panel.getChildren().add(statusRow);
        return panel;
    }

    private VBox createConversionRateCard(Models.MetricsResponse metrics) {
        VBox container = new VBox(12);
        container.getChildren().add(UiHelper.subtitle("📊 Tasa de conversión"));

        VBox card = UiHelper.card();
        card.setPrefHeight(150);
        card.setAlignment(Pos.CENTER);
        card.setStyle(card.getStyle() + "; -fx-background-color: linear-gradient(to bottom, #e8f5e9, #f1f8f6);");

        Label conversionLabel = new Label(metrics.displayConversionRate());
        conversionLabel.setFont(Font.font("System", FontWeight.BOLD, 48));
        conversionLabel.setTextFill(Color.web("#2e7d32"));

        Label descriptionLabel = new Label("Items vendidos de los agregados al carrito");
        descriptionLabel.setFont(Font.font("System", 13));
        descriptionLabel.setTextFill(Color.web(UiHelper.TEXT_MUTED));

        Label formulaLabel = new Label(String.format("Fórmula: (%d / %d) × 100", metrics.totalItemsSold, metrics.totalCartItems));
        formulaLabel.setFont(Font.font("System", 11));
        formulaLabel.setTextFill(Color.web("#757575"));

        card.getChildren().addAll(conversionLabel, descriptionLabel, formulaLabel);
        container.getChildren().add(card);
        return container;
    }

    private VBox createMetricCard(String title, String value, String description) {
        VBox card = UiHelper.card();
        card.setPrefWidth(250);
        card.setPrefHeight(120);

        Label titleLabel = UiHelper.muted(title);
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        valueLabel.setTextFill(Color.web(UiHelper.PRIMARY));

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font(12));
        descLabel.setTextFill(Color.web(UiHelper.TEXT_MUTED));
        descLabel.setWrapText(true);

        card.getChildren().addAll(titleLabel, valueLabel, descLabel);
        return card;
    }

    private VBox createStatusCard(String status, String count, String color) {
        VBox card = UiHelper.card();
        card.setPrefWidth(200);
        card.setPrefHeight(100);
        card.setStyle(card.getStyle() + "; -fx-border-color: " + color + "; -fx-border-width: 2;");

        Label statusLabel = new Label(status);
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        statusLabel.setTextFill(Color.web(color));

        Label countLabel = new Label(count);
        countLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        countLabel.setTextFill(Color.web(color));

        card.getChildren().addAll(statusLabel, countLabel);
        return card;
    }

    private String formatStatusName(String status) {
        return switch (status) {
            case "PENDING" -> "⏳ Pendiente";
            case "CONFIRMED" -> "✓ Confirmada";
            case "COMPLETED" -> "✓✓ Completada";
            case "CANCELLED" -> "✗ Cancelada";
            default -> status;
        };
    }

    private String getStatusColor(String status) {
        return switch (status) {
            case "PENDING" -> "#FF9800";
            case "CONFIRMED" -> "#2196F3";
            case "COMPLETED" -> "#4CAF50";
            case "CANCELLED" -> "#F44336";
            default -> "#9E9E9E";
        };
    }
}
