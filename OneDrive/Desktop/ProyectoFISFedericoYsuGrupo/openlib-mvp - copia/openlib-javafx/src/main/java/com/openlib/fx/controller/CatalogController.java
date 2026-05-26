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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

public class CatalogController {

    private FlowPane booksGrid;
    private Label statusLbl;
    private TextField searchField;
    private ComboBox<String> categoryBox;
    private ComboBox<String> sortBox;
    private int currentPage = 0;
    private List<Models.CategoryResponse> categories = new ArrayList<>();

    public Scene buildScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UiHelper.SURFACE + ";");

        // ── NavBar ──
        VBox topArea = new VBox();
        topArea.getChildren().add(UiHelper.navBar("BUYER"));

        // ── Barra de herramientas ──
        HBox toolbar = new HBox(12);
        toolbar.setPadding(new Insets(16, 24, 12, 24));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: white; -fx-border-color: " + UiHelper.BORDER + "; -fx-border-width: 0 0 1 0;");

        Label titleLbl = UiHelper.subtitle("📖 Catálogo");

        searchField = UiHelper.styledField("Buscar por título, autor o ISBN...");
        searchField.setPrefWidth(300);

        categoryBox = new ComboBox<>();
        categoryBox.setPromptText("Categoría");
        categoryBox.setStyle("-fx-font-size: 13px;");
        categoryBox.setPrefWidth(160);

        sortBox = new ComboBox<>();
        sortBox.getItems().addAll("Más reciente", "Más descargado", "Mejor calificado", "Título A-Z");
        sortBox.setValue("Más reciente");
        sortBox.setStyle("-fx-font-size: 13px;");

        Button searchBtn = UiHelper.primaryBtn("🔍 Buscar");
        searchBtn.setOnAction(e -> { currentPage = 0; loadBooks(); });
        searchField.setOnAction(e -> { currentPage = 0; loadBooks(); });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Botones de navegación
        Button cartBtn     = UiHelper.outlineBtn("🛒 Carrito");
        Button libraryBtn  = UiHelper.outlineBtn("📚 Biblioteca");
        Button ordersBtn   = UiHelper.outlineBtn("📋 Órdenes");
        Button wishlistBtn = UiHelper.outlineBtn("❤️ Favoritos");
        Button reportsBtn  = UiHelper.outlineBtn("📊 Resumen");

        cartBtn.setOnAction(e     -> NavigationManager.navigateTo("cart"));
        libraryBtn.setOnAction(e  -> NavigationManager.navigateTo("library"));
        ordersBtn.setOnAction(e   -> NavigationManager.navigateTo("orders"));
        wishlistBtn.setOnAction(e -> NavigationManager.navigateTo("wishlist"));
        reportsBtn.setOnAction(e  -> NavigationManager.navigateTo("buyer_reports"));

        toolbar.getChildren().addAll(
                titleLbl, searchField, categoryBox, sortBox, searchBtn,
                spacer, reportsBtn, wishlistBtn, ordersBtn, libraryBtn, cartBtn
        );
        topArea.getChildren().add(toolbar);

        VBox instructions = UiHelper.infoCard(
                "Qué puedes hacer como comprador",
                "Busca libros por título, autor o ISBN. Filtra por categoría, añade los mejores títulos a tu carrito, guarda favoritos y revisa tu biblioteca cuando quieras."
        );
        instructions.setPadding(new Insets(0, 24, 12, 24));
        topArea.getChildren().add(instructions);

        root.setTop(topArea);

        // ── Grid de libros ──
        booksGrid = new FlowPane(16, 16);
        booksGrid.setPadding(new Insets(20, 24, 20, 24));

        statusLbl = new Label("Cargando catálogo...");
        statusLbl.setFont(Font.font(15));
        statusLbl.setTextFill(Color.web(UiHelper.TEXT_MUTED));

        VBox centerBox = new VBox(12, statusLbl, booksGrid);
        centerBox.setPadding(new Insets(4));

        ScrollPane scroll = new ScrollPane(centerBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: " + UiHelper.SURFACE + ";");
        root.setCenter(scroll);

        // ── Paginación ──
        HBox pagination = new HBox(12);
        pagination.setAlignment(Pos.CENTER);
        pagination.setPadding(new Insets(12));
        pagination.setStyle("-fx-background-color: white; -fx-border-color: " + UiHelper.BORDER + "; -fx-border-width: 1 0 0 0;");

        Button prevBtn = UiHelper.outlineBtn("← Anterior");
        Button nextBtn = UiHelper.outlineBtn("Siguiente →");
        Label  pageLbl = new Label("Página 1");
        pageLbl.setFont(Font.font(13));

        prevBtn.setOnAction(e -> { if (currentPage > 0) { currentPage--; loadBooks(pageLbl, prevBtn, nextBtn); } });
        nextBtn.setOnAction(e -> { currentPage++; loadBooks(pageLbl, prevBtn, nextBtn); });

        pagination.getChildren().addAll(prevBtn, pageLbl, nextBtn);
        root.setBottom(pagination);

        // Cargar categorías y luego libros
        new Thread(() -> {
            var catResp = ApiClient.getPublic("/books/categories");
            if (catResp.isSuccess()) {
                try {
                    categories = ApiClient.mapper.readValue(catResp.body(), new TypeReference<>() {});
                } catch (Exception ignored) {}
            }
            Platform.runLater(() -> {
                categoryBox.getItems().add("Todas");
                for (var cat : categories) categoryBox.getItems().add(cat.name);
                categoryBox.setValue("Todas");
                loadBooks(pageLbl, prevBtn, nextBtn);
            });
        }).start();

        return new Scene(root, 1280, 800);
    }

    private void loadBooks() {
        loadBooks(null, null, null);
    }

    private void loadBooks(Label pageLbl, Button prev, Button next) {
        booksGrid.getChildren().clear();
        statusLbl.setText("⏳ Cargando...");
        statusLbl.setVisible(true);

        String q    = searchField.getText().trim();
        String sort = mapSort(sortBox.getValue());

        // Construir URL
        StringBuilder url = new StringBuilder("/books?page=" + currentPage + "&size=12&sort=" + sort);
        if (!q.isEmpty()) url.append("&q=").append(URLEncoder.encode(q, StandardCharsets.UTF_8));

        // Categoría
        String selectedCat = categoryBox.getValue();
        if (selectedCat != null && !selectedCat.equals("Todas")) {
            categories.stream()
                    .filter(c -> c.name.equals(selectedCat))
                    .findFirst()
                    .ifPresent(c -> url.append("&categoryId=").append(c.id));
        }

        final String finalUrl = url.toString();

        new Thread(() -> {
            var resp = ApiClient.getPublic(finalUrl);
            Platform.runLater(() -> {
                statusLbl.setVisible(false);
                if (!resp.isSuccess()) {
                    statusLbl.setText("❌ Error al cargar: " + resp.errorMessage());
                    statusLbl.setVisible(true);
                    return;
                }
                try {
                    // Intentar parsear como paginado
                    var node = resp.json();
                    List<Models.BookResponse> books;
                    int totalPages = 1;

                    if (node.has("content")) {
                        books = ApiClient.mapper.readValue(node.get("content").toString(), new TypeReference<>() {});
                        totalPages = node.path("totalPages").asInt(1);
                    } else {
                        books = ApiClient.mapper.readValue(resp.body(), new TypeReference<>() {});
                    }

                    if (books.isEmpty()) {
                        statusLbl.setText("No se encontraron libros.");
                        statusLbl.setVisible(true);
                    }

                    for (var book : books) {
                        booksGrid.getChildren().add(buildBookCard(book));
                    }

                    if (pageLbl != null) {
                        int tp = totalPages;
                        pageLbl.setText("Página " + (currentPage + 1) + " / " + Math.max(tp, 1));
                        prev.setDisable(currentPage == 0);
                        next.setDisable(currentPage >= tp - 1);
                    }

                } catch (Exception ex) {
                    statusLbl.setText("Error al procesar respuesta: " + ex.getMessage());
                    statusLbl.setVisible(true);
                }
            });
        }).start();
    }

    private VBox buildBookCard(Models.BookResponse book) {
        VBox card = UiHelper.card();
        card.setPrefWidth(220);
        card.setMaxWidth(220);
        card.setSpacing(8);

        var cover = UiHelper.bookCover(book.coverImageUrl, 220, 150, "📖");

        Label titleLbl = new Label(book.title);
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        titleLbl.setWrapText(true);
        titleLbl.setTextFill(Color.web(UiHelper.TEXT_DARK));

        Label authorLbl = UiHelper.muted(book.author);
        authorLbl.setFont(Font.font(12));

        Label ratingLbl = new Label(book.displayRating());
        ratingLbl.setFont(Font.font(11));
        ratingLbl.setTextFill(Color.web(UiHelper.WARNING));

        HBox priceRow = new HBox(8);
        priceRow.setAlignment(Pos.CENTER_LEFT);
        Label priceLbl = UiHelper.badge(book.displayPrice(), UiHelper.SUCCESS);

        if (book.category != null) {
            Label catLbl = UiHelper.badge(book.category, UiHelper.PRIMARY);
            priceRow.getChildren().addAll(priceLbl, catLbl);
        } else {
            priceRow.getChildren().add(priceLbl);
        }

        HBox actions = new HBox(6);
        actions.setAlignment(Pos.CENTER);

        Button detailBtn = UiHelper.outlineBtn("Ver más");
        detailBtn.setOnAction(e -> NavigationManager.navigateTo("book_detail", book.id));
        detailBtn.setStyle(detailBtn.getStyle() + " -fx-font-size: 11px; -fx-padding: 5 10;");

        Button addCartBtn = UiHelper.primaryBtn("🛒");
        addCartBtn.setFont(Font.font(12));
        addCartBtn.setStyle(addCartBtn.getStyle() + " -fx-padding: 5 10;");
        addCartBtn.setTooltip(new Tooltip("Agregar al carrito"));
        addCartBtn.setOnAction(e -> addToCart(book.id, book.title, addCartBtn));

        actions.getChildren().addAll(detailBtn, addCartBtn);
        card.getChildren().addAll(cover, titleLbl, authorLbl, ratingLbl, priceRow, actions);
        return card;
    }

    private void addToCart(Long bookId, String title, Button btn) {
        if (!SessionManager.isLoggedIn()) {
            UiHelper.alert("Sesión requerida", "Debes iniciar sesión para agregar al carrito.", Alert.AlertType.WARNING);
            NavigationManager.navigateTo("login");
            return;
        }
        btn.setDisable(true);
        new Thread(() -> {
            var resp = ApiClient.post("/cart", java.util.Map.of("bookId", bookId));
            Platform.runLater(() -> {
                btn.setDisable(false);
                if (resp.isSuccess()) {
                    UiHelper.alert("¡Agregado!", "\"" + title + "\" fue agregado al carrito.", Alert.AlertType.INFORMATION);
                } else {
                    UiHelper.alert("Error", resp.errorMessage(), Alert.AlertType.ERROR);
                }
            });
        }).start();
    }

    private String mapSort(String display) {
        return switch (display) {
            case "Más descargado" -> "downloadCount,desc";
            case "Mejor calificado" -> "averageRating,desc";
            case "Título A-Z" -> "title,asc";
            default -> "createdAt,desc";
        };
    }
}
