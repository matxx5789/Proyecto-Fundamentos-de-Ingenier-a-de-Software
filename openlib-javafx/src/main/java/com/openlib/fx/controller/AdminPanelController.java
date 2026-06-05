package com.openlib.fx.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.openlib.fx.model.Models;
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

import java.util.List;
import java.util.Map;

public class AdminPanelController {

    private TabPane tabPane;

    public Scene buildScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UiHelper.SURFACE + ";");
        root.setTop(UiHelper.navBar("ADMIN"));

        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-tab-min-width: 140px;");

        Tab dashTab   = new Tab("📊 Dashboard",    buildDashboardTab());
        Tab metricsTab= new Tab("📈 Métricas",    buildMetricsTab());
        Tab ordersTab = new Tab("📋 Órdenes",     buildOrdersTab());
        Tab booksTab  = new Tab("📖 Libros",        buildBooksTab());
        Tab usersTab  = new Tab("👥 Usuarios",      buildUsersTab());
        Tab catsTab   = new Tab("🏷️ Categorías",   buildCategoriesTab());
        Tab reviewTab = new Tab("⭐ Reseñas",       buildReviewsTab());

        tabPane.getTabs().addAll(dashTab, metricsTab, ordersTab, booksTab, usersTab, catsTab, reviewTab);
        root.setCenter(tabPane);
        return new Scene(root, 1280, 800);
    }

    // ── Dashboard ─────────────────────────────────────────────────

    private VBox buildDashboardTab() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        page.getChildren().add(UiHelper.infoCard(
                "Qué puedes hacer como administrador",
                "Supervisa usuarios, aprueba y rechaza libros publicados por sellers, revisa reseñas y gestiona categorías para mantener la plataforma ordenada y segura."
        ));
        page.getChildren().add(UiHelper.subtitle("Resumen de la Plataforma"));
        page.getChildren().add(UiHelper.spinner());

        new Thread(() -> {
            var resp = ApiClient.get("/admin/dashboard");
            Platform.runLater(() -> {
                page.getChildren().clear();
                page.getChildren().add(UiHelper.subtitle("Resumen de la Plataforma"));

                if (!resp.isSuccess()) {
                    page.getChildren().add(UiHelper.emptyState("❌ " + resp.errorMessage()));
                    return;
                }
                Models.DashboardResponse dash = resp.as(Models.DashboardResponse.class);
                if (dash == null) return;

                // Cards de métricas
                HBox metricsRow = new HBox(16);
                metricsRow.setAlignment(Pos.CENTER_LEFT);
                metricsRow.getChildren().addAll(
                        metricCard("👥 Usuarios",    dash.totalUsers,     UiHelper.PRIMARY),
                        metricCard("📚 Libros",      dash.totalBooks,     UiHelper.SUCCESS),
                        metricCard("📋 Órdenes",     dash.totalOrders,    UiHelper.WARNING),
                        metricCard("⬇️ Descargas",  dash.totalDownloads, "#8B5CF6"),
                        metricCard("⏳ Pendientes",  dash.pendingBooks,   UiHelper.DANGER)
                );
                page.getChildren().add(metricsRow);

                if (dash.topDownloaded != null && !dash.topDownloaded.isEmpty()) {
                    page.getChildren().add(UiHelper.subtitle("🏆 Top Libros Descargados"));
                    VBox topList = new VBox(8);
                    int i = 1;
                    for (var book : dash.topDownloaded) {
                        HBox row = new HBox(12);
                        row.setAlignment(Pos.CENTER_LEFT);
                        row.setPadding(new Insets(10, 14, 10, 14));
                        row.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);");
                        Label rank = new Label("#" + i++);
                        rank.setFont(Font.font("System", FontWeight.BOLD, 16));
                        rank.setTextFill(Color.web(UiHelper.WARNING));
                        rank.setMinWidth(36);
                        Label title = new Label(book.title);
                        title.setFont(Font.font("System", FontWeight.BOLD, 13));
                        HBox.setHgrow(title, Priority.ALWAYS);
                        Label dl = UiHelper.muted("⬇️ " + (book.downloadCount != null ? book.downloadCount : 0));
                        row.getChildren().addAll(rank, title, dl);
                        topList.getChildren().add(row);
                    }
                    page.getChildren().add(topList);
                }
            });
        }).start();

        return page;
    }

    private VBox metricCard(String label, Long value, String color) {
        VBox card = UiHelper.card();
        card.setPrefWidth(200);
        card.setAlignment(Pos.CENTER);
        Label valLbl = new Label(value != null ? value.toString() : "0");
        valLbl.setFont(Font.font("System", FontWeight.BOLD, 36));
        valLbl.setTextFill(Color.web(color));
        Label nameLbl = new Label(label);
        nameLbl.setFont(Font.font(13));
        nameLbl.setTextFill(Color.web(UiHelper.TEXT_MUTED));
        card.getChildren().addAll(valLbl, nameLbl);
        return card;
    }

    // ── Métricas ──────────────────────────────────────────────────

    private VBox buildMetricsTab() {
        VBox page = new VBox(12);
        page.setPadding(new Insets(20));
        page.getChildren().add(UiHelper.subtitle("📊 Métricas de Ventas del Sistema"));

        Button metricsBtn = UiHelper.primaryBtn("Abrir Panel de Métricas Completo");
        metricsBtn.setStyle(metricsBtn.getStyle() + "; -fx-font-size: 13;");
        metricsBtn.setOnAction(e -> NavigationManager.navigateTo("metrics"));

        HBox btnBox = new HBox();
        btnBox.setAlignment(Pos.CENTER);
        btnBox.getChildren().add(metricsBtn);

        VBox info = UiHelper.infoCard(
                "Panel de Métricas Completo",
                "Accede a un panel detallado con análisis de ventas, órdenes por estado y tasa de conversión. Puedes filtrar por período de fechas."
        );

        page.getChildren().addAll(btnBox, info);
        return page;
    }

    // ── Órdenes ───────────────────────────────────────────────────

    private VBox buildOrdersTab() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(20));

        HBox filterBox = new HBox(12);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        Label filterLbl = new Label("Filtrar por estado:");
        filterLbl.setFont(Font.font("System", FontWeight.BOLD, 12));

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("TODOS", "PENDING", "CONFIRMED", "COMPLETED", "CANCELLED");
        statusFilter.setValue("TODOS");
        statusFilter.setPrefWidth(150);

        filterBox.getChildren().addAll(filterLbl, statusFilter);
        page.getChildren().addAll(UiHelper.subtitle("📋 Gestión de Órdenes"), filterBox);

        VBox content = new VBox(12);
        content.setPadding(new Insets(0, 0, 0, 0));
        page.getChildren().add(content);

        // Cargar órdenes
        loadOrdersForAdmin(content, statusFilter.getValue(), () -> loadOrdersForAdmin(content, statusFilter.getValue(), () -> {}));

        // Filtro
        statusFilter.setOnAction(e -> loadOrdersForAdmin(content, statusFilter.getValue(), () -> loadOrdersForAdmin(content, statusFilter.getValue(), () -> {})));

        return page;
    }

    private void loadOrdersForAdmin(VBox content, String statusFilter, Runnable onComplete) {
        content.getChildren().clear();
        content.getChildren().add(UiHelper.spinner());

        new Thread(() -> {
            var resp = ApiClient.get("/admin/orders?page=0&size=50");
            Platform.runLater(() -> {
                content.getChildren().clear();

                if (!resp.isSuccess()) {
                    content.getChildren().add(UiHelper.emptyState("❌ Error cargando órdenes: " + resp.errorMessage()));
                    return;
                }

                try {
                    var node = resp.json();
                    Map<String, Object> pageResp = ApiClient.mapper.readValue(
                            node.has("content") ? node.toString() : resp.body(), new TypeReference<>() {});
                    if (pageResp == null || !pageResp.containsKey("content")) {
                        content.getChildren().add(UiHelper.emptyState("No hay órdenes"));
                        return;
                    }

                    List<Map<String, Object>> orders = (List<Map<String, Object>>) pageResp.get("content");
                    if (orders == null || orders.isEmpty()) {
                        content.getChildren().add(UiHelper.emptyState("No hay órdenes"));
                        return;
                    }

                    for (var orderMap : orders) {
                        String status = (String) orderMap.get("status");

                        // Filtrar si es necesario
                        if (!statusFilter.equals("TODOS") && !status.equals(statusFilter)) {
                            continue;
                        }

                        Number id = (Number) orderMap.get("id");
                        String total = String.format("$%.2f", ((Number) orderMap.get("totalAmount")).doubleValue());
                        String created = ((String) orderMap.get("createdAt")).substring(0, 10);

                        HBox orderCard = createOrderCard(id.longValue(), status, total, created, () -> onComplete.run());
                        content.getChildren().add(orderCard);
                    }

                    if (content.getChildren().isEmpty()) {
                        content.getChildren().add(UiHelper.emptyState("No hay órdenes con ese estado"));
                    }
                } catch (Exception e) {
                    content.getChildren().add(UiHelper.emptyState("❌ Error procesando órdenes: " + e.getMessage()));
                }
            });
        }).start();
    }

    private HBox createOrderCard(Long orderId, String status, String total, String created, Runnable onStatusChange) {
        HBox card = new HBox(16);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);");
        card.setAlignment(Pos.CENTER_LEFT);

        // Estado con color
        Label statusBadge = new Label("●");
        statusBadge.setFont(Font.font(16));
        statusBadge.setTextFill(getStatusColor(status));

        Label orderInfo = new Label("Orden #" + orderId + " • " + total + " • " + created);
        orderInfo.setFont(Font.font(13));
        HBox.setHgrow(orderInfo, Priority.ALWAYS);

        // ComboBox para cambiar estado
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("PENDING", "CONFIRMED", "COMPLETED", "CANCELLED");
        statusBox.setValue(status);
        statusBox.setPrefWidth(140);

        statusBox.setOnAction(e -> {
            String newStatus = statusBox.getValue();
            if (!newStatus.equals(status)) {
                changeAdminOrderStatus(orderId, newStatus, card, onStatusChange);
            }
        });

        card.getChildren().addAll(statusBadge, orderInfo, statusBox);
        return card;
    }

    private Color getStatusColor(String status) {
        return switch (status) {
            case "PENDING" -> Color.web(UiHelper.WARNING);
            case "CONFIRMED" -> Color.web(UiHelper.PRIMARY);
            case "COMPLETED" -> Color.web(UiHelper.SUCCESS);
            case "CANCELLED" -> Color.web(UiHelper.DANGER);
            default -> Color.web(UiHelper.TEXT_MUTED);
        };
    }

    private void changeAdminOrderStatus(Long orderId, String newStatus, HBox card, Runnable onComplete) {
        new Thread(() -> {
            var resp = ApiClient.put("/admin/orders/" + orderId + "/status/" + newStatus, null);
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
                suc.setContentText("Estado actualizado a: " + newStatus);
                suc.showAndWait();
                onComplete.run();
            });
        }).start();
    }

    // ── Libros ────────────────────────────────────────────────────

    private VBox buildBooksTab() {
        VBox page = new VBox(12);
        page.setPadding(new Insets(20));

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(UiHelper.subtitle("Gestión de Libros"));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        ComboBox<String> filterBox = new ComboBox<>();
        filterBox.getItems().addAll("Todos", "PENDING", "APPROVED", "REJECTED", "ARCHIVED");
        filterBox.setValue("PENDING");

        Button filterBtn = UiHelper.primaryBtn("Filtrar");
        header.getChildren().addAll(sp, new Label("Estado:"), filterBox, filterBtn);
        page.getChildren().add(header);

        VBox listBox = new VBox(8);
        ScrollPane scroll = new ScrollPane(listBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        page.getChildren().add(scroll);

        Runnable loadBooks = () -> {
            listBox.getChildren().clear();
            listBox.getChildren().add(UiHelper.spinner());
            String status = filterBox.getValue().equals("Todos") ? "" : "&status=" + filterBox.getValue();

            new Thread(() -> {
                var resp = ApiClient.get("/admin/books?page=0&size=50" + status);
                Platform.runLater(() -> {
                    listBox.getChildren().clear();
                    if (!resp.isSuccess()) { listBox.getChildren().add(UiHelper.emptyState("❌ " + resp.errorMessage())); return; }
                    try {
                        var node = resp.json();
                        List<Models.BookResponse> books = ApiClient.mapper.readValue(
                                node.has("content") ? node.get("content").toString() : resp.body(), new TypeReference<>() {});
                        if (books.isEmpty()) { listBox.getChildren().add(UiHelper.emptyState("No hay libros con ese estado.")); return; }
                        for (var b : books) listBox.getChildren().add(buildAdminBookRow(b, listBox, () -> filterBtn.fire()));
                    } catch (Exception ex) { listBox.getChildren().add(UiHelper.emptyState("Error: " + ex.getMessage())); }
                });
            }).start();
        };

        filterBtn.setOnAction(e -> loadBooks.run());
        loadBooks.run();
        return page;
    }

    private HBox buildAdminBookRow(Models.BookResponse book, VBox parent, Runnable refresh) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);");

        Label icon = new Label("📖"); icon.setFont(Font.font(24));

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label t = new Label(book.title); t.setFont(Font.font("System", FontWeight.BOLD, 13));
        Label a = UiHelper.muted("por " + book.author + (book.sellerName != null ? " | Seller: " + book.sellerName : ""));
        info.getChildren().addAll(t, a);

        String sc = switch (book.status != null ? book.status : "") {
            case "APPROVED" -> UiHelper.SUCCESS; case "PENDING" -> UiHelper.WARNING;
            case "REJECTED" -> UiHelper.DANGER; default -> UiHelper.TEXT_MUTED;
        };
        Label badge = UiHelper.badge(book.status != null ? book.status : "?", sc);

        HBox btns = new HBox(6);
        if ("PENDING".equals(book.status)) {
            Button approveBtn = UiHelper.successBtn("✅ Aprobar");
            approveBtn.setOnAction(e -> {
                approveBtn.setDisable(true);
                new Thread(() -> {
                    ApiClient.put("/admin/books/" + book.id + "/approve", null);
                    Platform.runLater(refresh::run);
                }).start();
            });
            Button rejectBtn = UiHelper.dangerBtn("❌ Rechazar");
            rejectBtn.setOnAction(e -> {
                rejectBtn.setDisable(true);
                new Thread(() -> {
                    ApiClient.put("/admin/books/" + book.id + "/reject", null);
                    Platform.runLater(refresh::run);
                }).start();
            });
            btns.getChildren().addAll(approveBtn, rejectBtn);
        }

        row.getChildren().addAll(icon, info, badge, btns);
        return row;
    }

    // ── Usuarios ──────────────────────────────────────────────────

    private VBox buildUsersTab() {
        VBox page = new VBox(12);
        page.setPadding(new Insets(20));
        page.getChildren().add(UiHelper.subtitle("Gestión de Usuarios"));

        VBox listBox = new VBox(8);
        listBox.getChildren().add(UiHelper.spinner());
        ScrollPane scroll = new ScrollPane(listBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        page.getChildren().add(scroll);

        new Thread(() -> {
            var resp = ApiClient.get("/admin/users?page=0&size=50");
            Platform.runLater(() -> {
                listBox.getChildren().clear();
                if (!resp.isSuccess()) { listBox.getChildren().add(UiHelper.emptyState("❌ " + resp.errorMessage())); return; }
                try {
                    var node = resp.json();
                    List<Models.UserResponse> users = ApiClient.mapper.readValue(
                            node.has("content") ? node.get("content").toString() : resp.body(), new TypeReference<>() {});
                    for (var u : users) listBox.getChildren().add(buildUserRow(u, listBox));
                } catch (Exception ex) { listBox.getChildren().add(UiHelper.emptyState("Error: " + ex.getMessage())); }
            });
        }).start();

        return page;
    }

    private HBox buildUserRow(Models.UserResponse user, VBox parent) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 4, 0, 0, 1);");

        Label icon = new Label("👤"); icon.setFont(Font.font(22));

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label((user.fullName != null ? user.fullName : user.username)); name.setFont(Font.font("System", FontWeight.BOLD, 13));
        Label email = UiHelper.muted(user.email + " | @" + user.username);
        info.getChildren().addAll(name, email);

        String rc = switch (user.role != null ? user.role : "") {
            case "ADMIN" -> UiHelper.DANGER; case "SELLER" -> UiHelper.WARNING; default -> UiHelper.PRIMARY;
        };
        Label roleBadge = UiHelper.badge(user.role != null ? user.role : "?", rc);
        Label statusBadge = UiHelper.badge(Boolean.TRUE.equals(user.isActive) ? "Activo" : "Inactivo",
                Boolean.TRUE.equals(user.isActive) ? UiHelper.SUCCESS : UiHelper.DANGER);

        Button toggleBtn = new Button(Boolean.TRUE.equals(user.isActive) ? "🔴 Desactivar" : "🟢 Activar");
        toggleBtn.setStyle("-fx-background-color: " + UiHelper.BORDER + "; -fx-padding: 5 12; -fx-background-radius: 6; -fx-cursor: hand;");
        toggleBtn.setOnAction(e -> {
            toggleBtn.setDisable(true);
            new Thread(() -> {
                ApiClient.put("/admin/users/" + user.id + "/toggle-active", null);
                Platform.runLater(() -> {
                    // Refresh haciendo un request
                    var resp = ApiClient.get("/admin/users?page=0&size=50");
                    parent.getChildren().clear();
                    if (resp.isSuccess()) {
                        try {
                            var node = resp.json();
                            List<Models.UserResponse> users2 = ApiClient.mapper.readValue(
                                    node.has("content") ? node.get("content").toString() : resp.body(), new TypeReference<>() {});
                            for (var u2 : users2) parent.getChildren().add(buildUserRow(u2, parent));
                        } catch (Exception ex) { parent.getChildren().add(UiHelper.emptyState("Error")); }
                    }
                });
            }).start();
        });

        row.getChildren().addAll(icon, info, roleBadge, statusBadge, toggleBtn);
        return row;
    }

    // ── Categorías ────────────────────────────────────────────────

    private VBox buildCategoriesTab() {
        VBox page = new VBox(12);
        page.setPadding(new Insets(20));

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(UiHelper.subtitle("Gestión de Categorías"));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button newBtn = UiHelper.primaryBtn("+ Nueva Categoría");
        header.getChildren().addAll(sp, newBtn);
        page.getChildren().add(header);

        VBox listBox = new VBox(8);
        listBox.getChildren().add(UiHelper.spinner());
        ScrollPane scroll = new ScrollPane(listBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        page.getChildren().add(scroll);

        Runnable loadCats = () -> {
            listBox.getChildren().clear();
            listBox.getChildren().add(UiHelper.spinner());
            new Thread(() -> {
                var resp = ApiClient.getPublic("/books/categories");
                Platform.runLater(() -> {
                    listBox.getChildren().clear();
                    if (!resp.isSuccess()) { listBox.getChildren().add(UiHelper.emptyState("❌ " + resp.errorMessage())); return; }
                    try {
                        List<Models.CategoryResponse> cats = ApiClient.mapper.readValue(resp.body(), new TypeReference<>() {});
                        for (var cat : cats) listBox.getChildren().add(buildCatRow(cat));
                    } catch (Exception ex) { listBox.getChildren().add(UiHelper.emptyState("Error: " + ex.getMessage())); }
                });
            }).start();
        };

        newBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Nueva Categoría");
            dialog.setHeaderText("Nombre de la nueva categoría:");
            dialog.showAndWait().ifPresent(name -> {
                if (!name.isBlank()) {
                    new Thread(() -> {
                        ApiClient.post("/admin/categories", Map.of("name", name));
                        Platform.runLater(loadCats::run);
                    }).start();
                }
            });
        });

        loadCats.run();
        return page;
    }

    private HBox buildCatRow(Models.CategoryResponse cat) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 4, 0, 0, 1);");

        Label icon = new Label("🏷️"); icon.setFont(Font.font(20));
        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label(cat.name); name.setFont(Font.font("System", FontWeight.BOLD, 13));
        Label slug = UiHelper.muted("/" + cat.slug + (cat.description != null ? " — " + cat.description : ""));
        info.getChildren().addAll(name, slug);

        row.getChildren().addAll(icon, info);
        return row;
    }

    // ── Reseñas ───────────────────────────────────────────────────

    private VBox buildReviewsTab() {
        VBox page = new VBox(12);
        page.setPadding(new Insets(20));
        page.getChildren().add(UiHelper.subtitle("Moderación de Reseñas"));

        VBox listBox = new VBox(8);
        listBox.getChildren().add(UiHelper.spinner());
        ScrollPane scroll = new ScrollPane(listBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        page.getChildren().add(scroll);

        new Thread(() -> {
            var resp = ApiClient.get("/admin/reviews?page=0&size=50");
            Platform.runLater(() -> {
                listBox.getChildren().clear();
                if (!resp.isSuccess()) { listBox.getChildren().add(UiHelper.emptyState("❌ " + resp.errorMessage())); return; }
                try {
                    var node = resp.json();
                    List<Models.ReviewResponse> reviews = ApiClient.mapper.readValue(
                            node.has("content") ? node.get("content").toString() : resp.body(), new TypeReference<>() {});
                    if (reviews.isEmpty()) { listBox.getChildren().add(UiHelper.emptyState("No hay reseñas para moderar.")); return; }
                    for (var r : reviews) listBox.getChildren().add(buildReviewRow(r, listBox));
                } catch (Exception ex) { listBox.getChildren().add(UiHelper.emptyState("Error: " + ex.getMessage())); }
            });
        }).start();

        return page;
    }

    private HBox buildReviewRow(Models.ReviewResponse review, VBox parent) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 4, 0, 0, 1);");

        Label stars = new Label("★".repeat(review.rating != null ? review.rating : 0));
        stars.setTextFill(Color.web(UiHelper.WARNING));
        stars.setFont(Font.font(16));

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label titleLbl = new Label(review.title != null ? review.title : "(sin título)");
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        Label bodyLbl = UiHelper.muted(review.body != null ? review.body.substring(0, Math.min(100, review.body.length())) + "..." : "");
        Label meta = UiHelper.muted("Por: " + (review.reviewerName != null ? review.reviewerName : "?") + " | " + (review.createdAt != null ? review.createdAt.substring(0, 10) : ""));
        info.getChildren().addAll(titleLbl, bodyLbl, meta);

        Label visibilityBadge = UiHelper.badge(Boolean.TRUE.equals(review.isVisible) ? "Visible" : "Oculta",
                Boolean.TRUE.equals(review.isVisible) ? UiHelper.SUCCESS : UiHelper.DANGER);

        Button toggleBtn = new Button(Boolean.TRUE.equals(review.isVisible) ? "Ocultar" : "Mostrar");
        toggleBtn.setStyle("-fx-background-color: " + UiHelper.BORDER + "; -fx-padding: 5 12; -fx-background-radius: 6; -fx-cursor: hand;");
        toggleBtn.setOnAction(e -> {
            toggleBtn.setDisable(true);
            new Thread(() -> {
                ApiClient.put("/admin/reviews/" + review.id + "/hide", null);
                Platform.runLater(() -> {
                    // Simple refresh
                    var resp = ApiClient.get("/admin/reviews?page=0&size=50");
                    parent.getChildren().clear();
                    if (resp.isSuccess()) {
                        try {
                            var node = resp.json();
                            List<Models.ReviewResponse> reviews = ApiClient.mapper.readValue(
                                    node.has("content") ? node.get("content").toString() : resp.body(), new TypeReference<>() {});
                            for (var r : reviews) parent.getChildren().add(buildReviewRow(r, parent));
                        } catch (Exception ex) { parent.getChildren().add(UiHelper.emptyState("Error")); }
                    }
                });
            }).start();
        });

        row.getChildren().addAll(stars, info, visibilityBadge, toggleBtn);
        return row;
    }
}
