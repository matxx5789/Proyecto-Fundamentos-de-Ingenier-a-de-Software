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
import java.util.Map;

public class BookDetailController {

    private final Long bookId;

    public BookDetailController(Long bookId) {
        this.bookId = bookId;
    }

    public Scene buildScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UiHelper.SURFACE + ";");

        // NavBar
        HBox nav = UiHelper.navBar(SessionManager.getUserRole());
        Button backBtn = UiHelper.outlineBtn("← Volver al catálogo");
        backBtn.setOnAction(e -> NavigationManager.navigateTo("catalog"));
        nav.getChildren().add(1, backBtn);
        root.setTop(nav);

        // Contenido principal
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));

        ProgressIndicator spinner = UiHelper.spinner();
        content.getChildren().add(spinner);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: " + UiHelper.SURFACE + ";");
        root.setCenter(scroll);

        // Cargar datos
        new Thread(() -> {
            var resp = ApiClient.getPublic("/books/" + bookId);
            Platform.runLater(() -> {
                content.getChildren().remove(spinner);
                if (!resp.isSuccess()) {
                    content.getChildren().add(UiHelper.emptyState("❌ No se pudo cargar el libro."));
                    return;
                }
                Models.BookResponse book = resp.as(Models.BookResponse.class);
                if (book == null) return;
                renderBook(content, book);
                loadReviews(content, book);
            });
        }).start();

        return new Scene(root, 1280, 800);
    }

    private void renderBook(VBox content, Models.BookResponse book) {
        HBox mainRow = new HBox(24);

        // Portada
        VBox coverBox = new VBox(12);
        coverBox.setAlignment(Pos.TOP_CENTER);
        coverBox.setPrefWidth(220);
        var cover = UiHelper.bookCover(book.coverImageUrl, 220, 260, "📖");

        Button addCartBtn = UiHelper.primaryBtn("🛒 Agregar al Carrito");
        addCartBtn.setMaxWidth(Double.MAX_VALUE);
        addCartBtn.setOnAction(e -> addToCart(book.id, book.title, addCartBtn));

        Button wishlistBtn = UiHelper.outlineBtn("❤️ Guardar en Favoritos");
        wishlistBtn.setMaxWidth(Double.MAX_VALUE);
        wishlistBtn.setOnAction(e -> addToWishlist(book.id, wishlistBtn));

        coverBox.getChildren().addAll(cover, addCartBtn, wishlistBtn);
        HBox.setHgrow(coverBox, Priority.NEVER);

        // Detalles
        VBox detailBox = new VBox(12);
        HBox.setHgrow(detailBox, Priority.ALWAYS);

        Label titleLbl = UiHelper.title(book.title);
        Label authorLbl = new Label("por " + book.author);
        authorLbl.setFont(Font.font("System", FontWeight.BOLD, 15));
        authorLbl.setTextFill(Color.web(UiHelper.PRIMARY));

        HBox metaRow = new HBox(12);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        if (book.category != null) metaRow.getChildren().add(UiHelper.badge(book.category, UiHelper.PRIMARY));
        metaRow.getChildren().add(UiHelper.badge(book.displayPrice(), UiHelper.SUCCESS));
        metaRow.getChildren().add(new Label(book.displayRating()));
        if (book.language != null) metaRow.getChildren().add(UiHelper.badge(book.language.toUpperCase(), UiHelper.WARNING));

        if (book.tags != null && !book.tags.isEmpty()) {
            HBox tagRow = new HBox(6);
            tagRow.setAlignment(Pos.CENTER_LEFT);
            for (String tag : book.tags) tagRow.getChildren().add(UiHelper.badge("#" + tag, UiHelper.TEXT_MUTED));
            metaRow.getChildren().add(tagRow);
        }

        Label isbnLbl = UiHelper.muted("ISBN: " + (book.isbn != null ? book.isbn : "N/A"));
        Label yearLbl = UiHelper.muted("Año: " + (book.publishedYear != null ? book.publishedYear : "N/A"));
        Label dlLbl   = UiHelper.muted("Descargas: " + (book.downloadCount != null ? book.downloadCount : 0));

        Label descTitle = UiHelper.subtitle("Descripción");
        Label descLbl = new Label(book.description != null ? book.description : "Sin descripción disponible.");
        descLbl.setWrapText(true);
        descLbl.setFont(Font.font(14));
        descLbl.setTextFill(Color.web(UiHelper.TEXT_DARK));

        detailBox.getChildren().addAll(titleLbl, authorLbl, metaRow,
                UiHelper.separator(), isbnLbl, yearLbl, dlLbl,
                UiHelper.separator(), descTitle, descLbl);

        mainRow.getChildren().addAll(coverBox, detailBox);
        content.getChildren().add(mainRow);
    }

    private void loadReviews(VBox content, Models.BookResponse book) {
        content.getChildren().add(UiHelper.separator());
        content.getChildren().add(UiHelper.subtitle("⭐ Reseñas"));

        VBox reviewsBox = new VBox(10);
        content.getChildren().add(reviewsBox);
        reviewsBox.getChildren().add(UiHelper.spinner());

        new Thread(() -> {
            var resp = ApiClient.getPublic("/books/" + bookId + "/reviews");
            Platform.runLater(() -> {
                reviewsBox.getChildren().clear();
                if (!resp.isSuccess()) {
                    reviewsBox.getChildren().add(UiHelper.muted("No se pudieron cargar las reseñas."));
                    return;
                }
                try {
                    List<Models.ReviewResponse> reviews = ApiClient.mapper.readValue(resp.body(), new TypeReference<>() {});
                    if (reviews.isEmpty()) {
                        reviewsBox.getChildren().add(UiHelper.emptyState("Aún no hay reseñas para este libro."));
                    }
                    for (var review : reviews) {
                        reviewsBox.getChildren().add(buildReviewCard(review));
                    }
                } catch (Exception ex) {
                    reviewsBox.getChildren().add(UiHelper.muted("Error al cargar reseñas."));
                }

                // Formulario de reseña (solo si es buyer con libro adquirido - el backend valida)
                if (SessionManager.isLoggedIn() && !SessionManager.isAdmin() && !SessionManager.isSeller()) {
                    content.getChildren().add(UiHelper.separator());
                    content.getChildren().add(buildReviewForm(book.id, reviewsBox));
                }
            });
        }).start();
    }

    private VBox buildReviewCard(Models.ReviewResponse review) {
        VBox card = UiHelper.card();
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        String stars = "★".repeat(review.rating != null ? review.rating : 0) +
                       "☆".repeat(5 - (review.rating != null ? review.rating : 0));
        Label starsLbl = new Label(stars);
        starsLbl.setTextFill(Color.web(UiHelper.WARNING));
        starsLbl.setFont(Font.font(16));

        Label nameLbl = UiHelper.muted(review.reviewerName != null ? review.reviewerName : "Anónimo");
        Label dateLbl = UiHelper.muted(review.createdAt != null ? review.createdAt.substring(0, 10) : "");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        header.getChildren().addAll(starsLbl, nameLbl, sp, dateLbl);

        if (review.title != null && !review.title.isBlank()) {
            Label titleLbl = new Label(review.title);
            titleLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
            card.getChildren().addAll(header, titleLbl);
        } else {
            card.getChildren().add(header);
        }

        if (review.body != null && !review.body.isBlank()) {
            Label bodyLbl = new Label(review.body);
            bodyLbl.setWrapText(true);
            card.getChildren().add(bodyLbl);
        }

        return card;
    }

    private VBox buildReviewForm(Long bookId, VBox reviewsBox) {
        VBox form = UiHelper.card();
        form.setMaxWidth(600);
        form.getChildren().add(UiHelper.subtitle("✍️ Deja tu reseña"));
        form.getChildren().add(UiHelper.muted("Solo disponible si ya adquiriste este libro."));

        ComboBox<Integer> ratingBox = new ComboBox<>();
        ratingBox.getItems().addAll(5, 4, 3, 2, 1);
        ratingBox.setValue(5);
        ratingBox.setStyle("-fx-font-size: 14px;");

        TextField titleField = UiHelper.styledField("Título de tu reseña");
        TextArea bodyArea    = UiHelper.styledTextArea("Comparte tu opinión sobre el libro...");
        Label errorLbl       = UiHelper.errorLabel();
        Button submitBtn     = UiHelper.primaryBtn("Publicar Reseña");

        submitBtn.setOnAction(e -> {
            errorLbl.setText("");
            int rating = ratingBox.getValue();
            String rtitle = titleField.getText().trim();
            String rbody  = bodyArea.getText().trim();
            if (rbody.isEmpty()) { errorLbl.setText("El cuerpo de la reseña es obligatorio."); return; }

            submitBtn.setDisable(true);
            new Thread(() -> {
                var resp = ApiClient.post("/books/" + bookId + "/reviews",
                        Map.of("rating", rating, "title", rtitle, "body", rbody));
                Platform.runLater(() -> {
                    submitBtn.setDisable(false);
                    if (resp.isSuccess()) {
                        UiHelper.alert("¡Gracias!", "Tu reseña fue publicada exitosamente.", Alert.AlertType.INFORMATION);
                        titleField.clear(); bodyArea.clear();
                    } else {
                        errorLbl.setText("❌ " + resp.errorMessage());
                    }
                });
            }).start();
        });

        form.getChildren().addAll(
                new Label("Calificación:"), ratingBox,
                new Label("Título:"), titleField,
                new Label("Comentario:"), bodyArea,
                errorLbl, submitBtn
        );
        return form;
    }

    private void addToCart(Long bid, String title, Button btn) {
        if (!SessionManager.isLoggedIn()) {
            UiHelper.alert("Sesión requerida", "Inicia sesión para agregar al carrito.", Alert.AlertType.WARNING);
            return;
        }
        btn.setDisable(true);
        new Thread(() -> {
            var r = ApiClient.post("/cart", Map.of("bookId", bid));
            Platform.runLater(() -> {
                btn.setDisable(false);
                if (r.isSuccess()) UiHelper.alert("¡Listo!", "\"" + title + "\" agregado al carrito.", Alert.AlertType.INFORMATION);
                else UiHelper.alert("Error", r.errorMessage(), Alert.AlertType.ERROR);
            });
        }).start();
    }

    private void addToWishlist(Long bid, Button btn) {
        if (!SessionManager.isLoggedIn()) {
            UiHelper.alert("Sesión requerida", "Inicia sesión para guardar en favoritos.", Alert.AlertType.WARNING);
            return;
        }
        btn.setDisable(true);
        new Thread(() -> {
            var r = ApiClient.post("/wishlist", Map.of("bookId", bid));
            Platform.runLater(() -> {
                btn.setDisable(false);
                if (r.isSuccess()) UiHelper.alert("¡Guardado!", "Libro agregado a tus favoritos.", Alert.AlertType.INFORMATION);
                else UiHelper.alert("Error", r.errorMessage(), Alert.AlertType.ERROR);
            });
        }).start();
    }
}
