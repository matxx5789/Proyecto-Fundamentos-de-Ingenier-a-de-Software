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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class SellerReportsController {

    private VBox reportArea;

    public Scene buildScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UiHelper.SURFACE + ";");

        VBox top = new VBox();
        top.getChildren().add(UiHelper.navBar("SELLER"));

        HBox toolbar = new HBox(12);
        toolbar.setPadding(new Insets(16, 24, 12, 24));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: white; -fx-border-color: " + UiHelper.BORDER + "; -fx-border-width: 0 0 1 0;");
        toolbar.getChildren().add(UiHelper.subtitle("📊 Reportes de vendedor"));

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Button backBtn = UiHelper.outlineBtn("← Mis Publicaciones");
        backBtn.setOnAction(e -> NavigationManager.navigateTo("seller_books"));
        toolbar.getChildren().addAll(sp, backBtn);
        top.getChildren().add(toolbar);

        VBox instructions = UiHelper.infoCard(
                "Control de ventas y publicaciones",
                "Revisa cuántos libros tienes publicados, cuántos están aprobados y cuánto has vendido hasta ahora."
        );
        instructions.setPadding(new Insets(0, 24, 12, 24));
        top.getChildren().add(instructions);

        top.getChildren().add(createSellerMenu());

        reportArea = new VBox(16);
        reportArea.setPadding(new Insets(24));

        ScrollPane scroll = new ScrollPane(reportArea);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        root.setCenter(scroll);

        loadReport();
        return new Scene(root, 1280, 800);
    }

    private HBox createSellerMenu() {
        HBox menu = new HBox(10);
        menu.setPadding(new Insets(0, 24, 12, 24));
        menu.setAlignment(Pos.CENTER_LEFT);

        Button myBooksBtn = UiHelper.outlineBtn("Mis Publicaciones");
        Button reportsBtn = UiHelper.outlineBtn("Reportes");

        myBooksBtn.setOnAction(e -> NavigationManager.navigateTo("seller_books"));
        reportsBtn.setOnAction(e -> NavigationManager.navigateTo("seller_reports"));

        menu.getChildren().addAll(myBooksBtn, reportsBtn);
        return menu;
    }

    private void loadReport() {
        reportArea.getChildren().clear();
        reportArea.getChildren().add(UiHelper.spinner());

        new Thread(() -> {
            var resp = ApiClient.get("/reports/seller");
            Platform.runLater(() -> {
                reportArea.getChildren().clear();
                if (!resp.isSuccess()) {
                    reportArea.getChildren().add(UiHelper.emptyState("No se pudo cargar el informe: " + resp.errorMessage()));
                    return;
                }
                try {
                    Models.SellerReportResponse report = ApiClient.mapper.readValue(resp.body(), Models.SellerReportResponse.class);
                    reportArea.getChildren().add(createStatsRow(report));
                } catch (Exception e) {
                    reportArea.getChildren().add(UiHelper.emptyState("Error cargando informe: " + e.getMessage()));
                }
            });
        }).start();
    }

    private HBox createStatsRow(Models.SellerReportResponse report) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER);
        row.setPrefHeight(140);

        row.getChildren().addAll(
                createStatCard("Libros publicados", report.totalBooksPublished != null ? report.totalBooksPublished.toString() : "0", "Número total de libros subidos."),
                createStatCard("Aprobados", report.totalApprovedBooks != null ? report.totalApprovedBooks.toString() : "0", "Libros que ya están disponibles para compradores."),
                createStatCard("En revisión", report.totalPendingBooks != null ? report.totalPendingBooks.toString() : "0", "Libros esperando moderación."),
                createStatCard("Ventas completadas", report.totalCompletedSales != null ? report.totalCompletedSales.toString() : "0", "Total de artículos vendidos."),
                createStatCard("Ingresos", report.displayTotalRevenue(), "Monto bruto generado por tus libros.")
        );
        return row;
    }

    private VBox createStatCard(String title, String value, String subtitle) {
        VBox card = UiHelper.card();
        card.setPrefWidth(240);
        Label titleLbl = UiHelper.muted(title);
        Label valueLbl = new Label(value);
        valueLbl.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 22));
        Label subtitleLbl = new Label(subtitle);
        subtitleLbl.setFont(javafx.scene.text.Font.font(13));
        subtitleLbl.setTextFill(javafx.scene.paint.Color.web(UiHelper.TEXT_MUTED));
        card.getChildren().addAll(titleLbl, valueLbl, subtitleLbl);
        return card;
    }
}
