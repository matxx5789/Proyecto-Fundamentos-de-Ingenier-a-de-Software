package com.openlib.fx.util;

import com.openlib.fx.controller.*;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestiona la navegación entre pantallas (vistas) de la app.
 */
public class NavigationManager {

    private static Stage primaryStage;
    private static final Map<String, Runnable> routes = new HashMap<>();

    public static void init(Stage stage) {
        primaryStage = stage;
        registerRoutes();
    }

    private static void registerRoutes() {
        routes.put("login",         () -> show(new LoginController().buildScene()));
        routes.put("register",      () -> show(new RegisterController().buildScene()));
        routes.put("catalog",       () -> show(new CatalogController().buildScene()));
        routes.put("cart",          () -> show(new CartController().buildScene()));
        routes.put("checkout",      () -> show(new CheckoutController().buildScene()));
        routes.put("library",       () -> show(new LibraryController().buildScene()));
        routes.put("orders",        () -> show(new OrdersController().buildScene()));
        routes.put("wishlist",      () -> show(new WishlistController().buildScene()));
        routes.put("seller_books",  () -> show(new SellerBooksController().buildScene()));
        routes.put("buyer_reports", () -> show(new BuyerReportsController().buildScene()));
        routes.put("seller_reports",() -> show(new SellerReportsController().buildScene()));
        routes.put("admin_panel",   () -> show(new AdminPanelController().buildScene()));
        routes.put("metrics",       () -> show(new MetricsController().buildScene()));
    }

    public static void navigateTo(String route) {
        Runnable r = routes.get(route);
        if (r != null) r.run();
        else System.err.println("Ruta desconocida: " + route);
    }

    public static void navigateTo(String route, Object data) {
        // Para rutas que necesitan datos (ej: detalle de libro)
        switch (route) {
            case "book_detail" -> show(new BookDetailController((Long) data).buildScene());
            case "order_detail" -> show(new OrderDetailController((Long) data).buildScene());
            default -> navigateTo(route);
        }
    }

    private static void show(Scene scene) {
        primaryStage.setScene(scene);
    }

    public static Stage getStage() { return primaryStage; }
}
