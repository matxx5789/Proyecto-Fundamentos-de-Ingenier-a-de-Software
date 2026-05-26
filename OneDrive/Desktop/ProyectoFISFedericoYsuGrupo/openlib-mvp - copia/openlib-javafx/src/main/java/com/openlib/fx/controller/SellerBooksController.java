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

public class SellerBooksController {

    private VBox booksList;

    public Scene buildScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UiHelper.SURFACE + ";");

        // Nav
        VBox top = new VBox();
        top.getChildren().add(UiHelper.navBar("SELLER"));

        HBox toolbar = new HBox(12);
        toolbar.setPadding(new Insets(16, 24, 12, 24));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: white; -fx-border-color: " + UiHelper.BORDER + "; -fx-border-width: 0 0 1 0;");
        toolbar.getChildren().add(UiHelper.subtitle("📗 Mis Publicaciones"));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button newBtn = UiHelper.primaryBtn("+ Nueva Publicación");
        newBtn.setOnAction(e -> showPublishDialog());
        Button reportsBtn = UiHelper.outlineBtn("📊 Reportes");
        reportsBtn.setOnAction(e -> NavigationManager.navigateTo("seller_reports"));
        toolbar.getChildren().addAll(sp, reportsBtn, newBtn);
        top.getChildren().add(toolbar);

        VBox instructions = UiHelper.infoCard(
                "Qué puedes hacer como vendedor",
                "Publica nuevos libros para revisión, edita tus títulos existentes y archiva aquellos que ya no desees ofrecer en la plataforma."
        );
        instructions.setPadding(new Insets(0, 24, 0, 24));
        top.getChildren().add(instructions);

        root.setTop(top);

        // Lista
        booksList = new VBox(12);
        booksList.setPadding(new Insets(24));

        ScrollPane scroll = new ScrollPane(booksList);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        root.setCenter(scroll);

        loadMyBooks();
        return new Scene(root, 1280, 800);
    }

    private void loadMyBooks() {
        booksList.getChildren().clear();
        booksList.getChildren().add(UiHelper.spinner());

        new Thread(() -> {
            var resp = ApiClient.get("/seller/books");
            Platform.runLater(() -> {
                booksList.getChildren().clear();
                if (!resp.isSuccess()) {
                    booksList.getChildren().add(UiHelper.emptyState("❌ " + resp.errorMessage()));
                    return;
                }
                try {
                    var node = resp.json();
                    List<Models.BookResponse> books;
                    if (node.has("content")) {
                        books = ApiClient.mapper.readValue(node.get("content").toString(), new TypeReference<>() {});
                    } else {
                        books = ApiClient.mapper.readValue(resp.body(), new TypeReference<>() {});
                    }
                    if (books.isEmpty()) {
                        booksList.getChildren().add(UiHelper.emptyState("Aún no has publicado ningún libro.\nUsa el botón '+ Nueva Publicación' para comenzar."));
                        return;
                    }
                    for (var book : books) booksList.getChildren().add(buildBookRow(book));
                } catch (Exception ex) {
                    booksList.getChildren().add(UiHelper.emptyState("Error: " + ex.getMessage()));
                }
            });
        }).start();
    }

    private HBox buildBookRow(Models.BookResponse book) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);");

        var coverNode = UiHelper.bookCover(book.coverImageUrl, 80, 110, "📖");

        VBox info = new VBox(6);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label titleLbl = new Label(book.title);
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
        Label authorLbl = UiHelper.muted("por " + book.author);
        Label isbnLbl = UiHelper.muted("ISBN: " + (book.isbn != null ? book.isbn : "N/A"));
        Label priceLbl = new Label("Precio: " + book.displayPrice());
        priceLbl.setFont(Font.font(13));
        info.getChildren().addAll(titleLbl, authorLbl, isbnLbl, priceLbl);

        String statusColor = switch (book.status != null ? book.status : "") {
            case "APPROVED"  -> UiHelper.SUCCESS;
            case "PENDING"   -> UiHelper.WARNING;
            case "REJECTED"  -> UiHelper.DANGER;
            case "ARCHIVED"  -> UiHelper.TEXT_MUTED;
            default          -> UiHelper.TEXT_MUTED;
        };
        Label statusBadge = UiHelper.badge(book.status != null ? book.status : "?", statusColor);

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = UiHelper.outlineBtn("✏️ Editar");
        editBtn.setOnAction(e -> showEditDialog(book));

        Button archiveBtn = UiHelper.dangerBtn("📦 Archivar");
        archiveBtn.setOnAction(e -> archiveBook(book.id, archiveBtn));
        archiveBtn.setDisable("ARCHIVED".equals(book.status));

        actions.getChildren().addAll(statusBadge, editBtn, archiveBtn);
        row.getChildren().addAll(coverNode, info, actions);
        return row;
    }

    private void showPublishDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Nueva Publicación");
        dialog.setHeaderText("Publicar un nuevo libro");

        VBox form = new VBox(10);
        form.setPadding(new Insets(16));

        TextField titleField  = UiHelper.styledField("Título del libro *");
        TextField authorField = UiHelper.styledField("Autor(es) *");
        TextField isbnField   = UiHelper.styledField("ISBN");
        TextField yearField   = UiHelper.styledField("Año de publicación");
        TextField langField   = UiHelper.styledField("Idioma (es/en)");
        TextField priceField  = UiHelper.styledField("Precio (ej: 29.90)");
        TextField coverField  = UiHelper.styledField("URL de portada");
        TextArea  descArea    = UiHelper.styledTextArea("Descripción del libro");
        Label errorLbl        = UiHelper.errorLabel();

        form.getChildren().addAll(
                new Label("Título *:"), titleField,
                new Label("Autor *:"), authorField,
                new Label("ISBN:"), isbnField,
                new Label("Año:"), yearField,
                new Label("Idioma:"), langField,
                new Label("Precio:"), priceField,
                new Label("Portada (URL):"), coverField,
                new Label("Descripción:"), descArea,
                errorLbl
        );

        dialog.getDialogPane().setContent(new ScrollPane(form));
        dialog.getDialogPane().setPrefWidth(500);

        ButtonType publishType = new ButtonType("Publicar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType  = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(publishType, cancelType);

        Button publishBtn = (Button) dialog.getDialogPane().lookupButton(publishType);
        publishBtn.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            String title  = titleField.getText().trim();
            String author = authorField.getText().trim();
            if (title.isEmpty() || author.isEmpty()) {
                errorLbl.setText("Título y autor son obligatorios.");
                e.consume();
                return;
            }

            var body = new java.util.HashMap<String, Object>();
            body.put("title", title);
            body.put("author", author);
            if (!isbnField.getText().isBlank()) body.put("isbn", isbnField.getText().trim());
            if (!yearField.getText().isBlank()) { try { body.put("publishedYear", Integer.parseInt(yearField.getText().trim())); } catch (Exception ignored) {} }
            if (!langField.getText().isBlank()) body.put("language", langField.getText().trim());
            if (!priceField.getText().isBlank()) {
                try { body.put("price", Double.parseDouble(priceField.getText().trim())); }
                catch (Exception ignored) { body.put("price", 0.0); }
            } else {
                body.put("price", 0.0);
            }
            if (!coverField.getText().isBlank()) body.put("coverUrl", coverField.getText().trim());
            if (!descArea.getText().isBlank()) body.put("description", descArea.getText().trim());

            var resp = ApiClient.post("/seller/books", body);
            if (resp.isSuccess()) {
                UiHelper.alert("¡Publicado!", "Tu libro fue enviado para revisión. Estado: PENDING.", Alert.AlertType.INFORMATION);
                loadMyBooks();
            } else {
                errorLbl.setText("❌ " + resp.errorMessage());
                e.consume();
            }
        });

        dialog.showAndWait();
    }

    private void showEditDialog(Models.BookResponse book) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Editar Publicación");
        dialog.setHeaderText("Editar: " + book.title);

        VBox form = new VBox(10);
        form.setPadding(new Insets(16));

        TextField titleField  = UiHelper.styledField("Título *");
        titleField.setText(book.title != null ? book.title : "");
        TextField authorField = UiHelper.styledField("Autor *");
        authorField.setText(book.author != null ? book.author : "");
        TextField priceField  = UiHelper.styledField("Precio (ej: 29.90)");
        priceField.setText(book.price != null ? String.format("%.2f", book.price) : "");
        TextField coverField  = UiHelper.styledField("URL de portada");
        coverField.setText(book.coverImageUrl != null ? book.coverImageUrl : "");
        TextArea descArea = UiHelper.styledTextArea("Descripción");
        descArea.setText(book.description != null ? book.description : "");
        Label errorLbl = UiHelper.errorLabel();

        form.getChildren().addAll(
                new Label("Título *:"), titleField,
                new Label("Autor *:"), authorField,
                new Label("Precio:"), priceField,
                new Label("Portada (URL):"), coverField,
                new Label("Descripción:"), descArea,
                errorLbl
        );

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setPrefWidth(480);

        ButtonType saveType   = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, cancelType);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            String title  = titleField.getText().trim();
            String author = authorField.getText().trim();
            if (title.isEmpty() || author.isEmpty()) {
                errorLbl.setText("Título y autor son obligatorios.");
                e.consume();
                return;
            }
            var body = new java.util.HashMap<String, Object>();
            body.put("title", title);
            body.put("author", author);
            body.put("description", descArea.getText().trim());
            if (!priceField.getText().isBlank()) {
                try { body.put("price", Double.parseDouble(priceField.getText().trim())); }
                catch (Exception ignored) { }
            }
            if (!coverField.getText().isBlank()) body.put("coverUrl", coverField.getText().trim());
            var resp = ApiClient.put("/seller/books/" + book.id, body);
            if (resp.isSuccess()) {
                UiHelper.alert("Guardado", "Publicación actualizada correctamente.", Alert.AlertType.INFORMATION);
                loadMyBooks();
            } else {
                errorLbl.setText("❌ " + resp.errorMessage());
                e.consume();
            }
        });

        dialog.showAndWait();
    }

    private void archiveBook(Long bookId, Button btn) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Seguro que deseas archivar este libro? Dejará de aparecer en el catálogo.", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                btn.setDisable(true);
                new Thread(() -> {
                    var resp = ApiClient.put("/seller/books/" + bookId + "/archive", null);
                    Platform.runLater(() -> {
                        if (resp.isSuccess()) loadMyBooks();
                        else { btn.setDisable(false); UiHelper.alert("Error", resp.errorMessage(), Alert.AlertType.ERROR); }
                    });
                }).start();
            }
        });
    }
}
