package com.openlib.fx.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.openlib.fx.OpenLibApp;
import com.openlib.fx.model.Models;
import com.openlib.fx.util.SessionManager;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

/**
 * Capa de acceso a datos real integrada con el Backend a través de HTTP.
 * Se conecta a Spring Boot (por defecto http://localhost:8080/api) y adapta los formatos
 * JSON transparentemente para mantener el frontend 100% intacto y funcional.
 */
public class ApiClient {

    public static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static String getBaseUrl() {
        return OpenLibApp.API_BASE != null && !OpenLibApp.API_BASE.contains("local://")
                ? OpenLibApp.API_BASE
                : "http://localhost:8080/api";
    }

    public static ApiResponse getPublic(String path)               { return handle(path,"GET",null,false); }
    public static ApiResponse get(String path)                     { return handle(path,"GET",null,true); }
    public static ApiResponse postPublic(String path, Object body) { return handle(path,"POST",body,false); }
    public static ApiResponse post(String path, Object body)       { return handle(path,"POST",body,true); }
    public static ApiResponse put(String path, Object body)        { return handle(path,"PUT",body,true); }
    public static ApiResponse delete(String path)                  { return handle(path,"DELETE",null,true); }

    private static ApiResponse handle(String path, String method, Object body, boolean auth) {
        try {
            String baseUrl = getBaseUrl();
            
            // ── ADAPTACIÓN DE PETICIONES (REQUESTS) ──

            // 1. Wishlist POST mapping: El front hace POST /wishlist con body {"bookId": 123}
            //    El backend espera POST /wishlist/{bookId}
            if (path.equals("/wishlist") && "POST".equals(method)) {
                if (body instanceof Map<?,?> r) {
                    Object bId = r.get("bookId");
                    if (bId != null) {
                        path = "/wishlist/" + bId.toString();
                        body = null; // No lleva body
                    }
                }
            }

            // 2. Publicación de libros POST /seller/books mapping:
            //    El front envía "categoryName" y el backend espera "categoryId" en el BookRequest.
            if (path.equals("/seller/books") && "POST".equals(method)) {
                if (body instanceof Map<?,?> r) {
                    Map<String, Object> adapted = new HashMap<>();
                    for (Map.Entry<?, ?> entry : r.entrySet()) {
                        adapted.put(entry.getKey().toString(), entry.getValue());
                    }

                    // Resolver dinámicamente categoryName -> categoryId haciendo un GET sincrónico
                    String categoryName = (String) r.get("categoryName");
                    long categoryId = 1L; // default
                    if (categoryName != null && !categoryName.isBlank()) {
                        try {
                            ApiResponse catResp = getPublic("/books/categories");
                            if (catResp.isSuccess()) {
                                JsonNode cats = mapper.readTree(catResp.body());
                                if (cats.isArray()) {
                                    for (JsonNode cat : cats) {
                                        if (categoryName.equalsIgnoreCase(cat.path("name").asText())) {
                                            categoryId = cat.path("id").asLong();
                                            break;
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            System.err.println("ApiClient: Error al mapear categoryName a ID: " + ex.getMessage());
                        }
                    }
                    adapted.put("categoryId", categoryId);
                    adapted.put("tagIds", List.of());
                    adapted.putIfAbsent("pages", 120); // Default requerido por el backend
                    adapted.putIfAbsent("coverUrl", ""); // Default
                    body = adapted;
                }
            }

            // 3. Checkout POST /orders mapping:
            //    El front envía billingName, billingEmail, billingAddress, paymentMethod.
            //    El backend espera billingFullName, billingEmail, billingAddress, billingCity, billingCountry, paymentMethod.
            //    Además, se debe remapear de POST /orders/checkout a POST /orders.
            if (path.equals("/orders/checkout") && "POST".equals(method)) {
                path = "/orders";
            }
            if (path.equals("/orders") && "POST".equals(method)) {
                if (body instanceof Map<?,?> r) {
                    Map<String, Object> adapted = new HashMap<>();
                    adapted.put("billingFullName", r.get("billingName") != null ? r.get("billingName") : "");
                    adapted.put("billingEmail", r.get("billingEmail") != null ? r.get("billingEmail") : "");
                    
                    String address = r.get("billingAddress") != null ? r.get("billingAddress").toString() : "";
                    adapted.put("billingAddress", address);
                    
                    // Separar ciudad y país si la dirección los incluye, o poner fallback
                    String city = "Ciudad";
                    String country = "País";
                    if (address.contains(",")) {
                        String[] parts = address.split(",");
                        if (parts.length >= 2) {
                            country = parts[parts.length - 1].trim();
                            city = parts[parts.length - 2].trim();
                        }
                    }
                    adapted.put("billingCity", city);
                    adapted.put("billingCountry", country);
                    adapted.put("paymentMethod", r.get("paymentMethod") != null ? r.get("paymentMethod") : "FREE");
                    adapted.put("notes", "Pedido desde el cliente JavaFX");
                    body = adapted;
                }
            }

            // ── CONSTRUCCIÓN DE LA PETICIÓN HTTP ──
            String fullUrl = baseUrl + path;
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .header("Accept", "application/json");

            // Si requiere autenticación, inyectar el header Bearer
            if (auth && SessionManager.isLoggedIn()) {
                builder.header("Authorization", SessionManager.getBearerHeader());
            }

            // Método y cuerpo
            if ("GET".equalsIgnoreCase(method)) {
                builder.GET();
            } else if ("DELETE".equalsIgnoreCase(method)) {
                builder.DELETE();
            } else {
                builder.header("Content-Type", "application/json");
                String jsonBody = body != null ? mapper.writeValueAsString(body) : "";
                builder.method(method.toUpperCase(), HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8));
            }

            // Enviar petición de forma sincrónica
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String responseBody = response.body();

            // ── ADAPTACIÓN DE RESPUESTAS (RESPONSES) ──
            if (status >= 200 && status < 300) {
                // A. Login / Register: Promover user.* a la raíz
                if ((path.equals("/auth/login") || path.equals("/auth/register")) && "POST".equals(method)) {
                    responseBody = adaptAuthResponse(responseBody);
                }
                
                // B. Carrito: Convertir List<BookResponse> del backend en List<CartItemResponse> del front
                else if (path.equals("/cart") && "GET".equals(method)) {
                    responseBody = adaptCartResponse(responseBody);
                }

                // C. Reseñas: Extraer content array del Page de reseñas para que coincida con el List esperado
                else if (path.matches("/books/\\d+/reviews") && "GET".equals(method)) {
                    responseBody = extractPageContent(responseBody);
                }

                // D. Adaptación de formato BookResponse en general (Catalog, Wishlist, Library, Seller, Admin)
                else {
                    responseBody = adaptBooksInJson(responseBody);
                }
            }

            return new ApiResponse(status, responseBody);

        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Error de comunicación HTTP";
            return err(500, msg);
        }
    }

    // Promover campos del objeto user anidado al nivel de raíz para AuthResponse
    private static String adaptAuthResponse(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            if (root.isObject() && root.has("user")) {
                ObjectNode obj = (ObjectNode) root;
                JsonNode user = root.get("user");
                if (user.isObject()) {
                    if (user.has("id")) obj.put("userId", user.get("id").asLong());
                    if (user.has("email")) obj.put("email", user.get("email").asText());
                    if (user.has("fullName")) obj.put("fullName", user.get("fullName").asText());
                    if (user.has("role")) obj.put("role", user.get("role").asText());
                }
                return mapper.writeValueAsString(obj);
            }
        } catch (Exception e) {
            System.err.println("Error adaptando AuthResponse: " + e.getMessage());
        }
        return json;
    }

    // Convertir List<BookResponse> a List<CartItemResponse>
    private static String adaptCartResponse(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            if (root.isArray()) {
                ArrayNode list = mapper.createArrayNode();
                for (JsonNode book : root) {
                    ObjectNode item = mapper.createObjectNode();
                    long id = book.path("id").asLong();
                    item.put("id", id);
                    item.put("bookId", id);
                    item.put("bookTitle", book.path("title").asText());
                    item.put("bookAuthor", book.path("author").asText());
                    item.put("bookCover", book.path("coverUrl").asText());
                    item.put("price", book.path("price").asDouble());
                    list.add(item);
                }
                return mapper.writeValueAsString(list);
            }
        } catch (Exception e) {
            System.err.println("Error adaptando CartResponse: " + e.getMessage());
        }
        return json;
    }

    // Extraer la lista "content" de un objeto de paginación Spring
    private static String extractPageContent(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            if (root.isObject() && root.has("content")) {
                return mapper.writeValueAsString(root.get("content"));
            }
        } catch (Exception e) {
            System.err.println("Error extrayendo content: " + e.getMessage());
        }
        return json;
    }

    // Transformar BookResponse para mapear las propiedades simplificadas del front
    private static String adaptBooksInJson(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            if (root.isObject()) {
                ObjectNode obj = (ObjectNode) root;
                if (obj.has("content") && obj.get("content").isArray()) {
                    ArrayNode arr = (ArrayNode) obj.get("content");
                    for (JsonNode item : arr) {
                        if (item.isObject()) transformBookNode((ObjectNode) item);
                    }
                } else {
                    transformBookNode(obj);
                }
                return mapper.writeValueAsString(obj);
            } else if (root.isArray()) {
                ArrayNode arr = (ArrayNode) root;
                for (JsonNode item : arr) {
                    if (item.isObject()) transformBookNode((ObjectNode) item);
                }
                return mapper.writeValueAsString(arr);
            }
        } catch (Exception e) {
            System.err.println("Error adaptando BookResponse: " + e.getMessage());
        }
        return json;
    }

    private static void transformBookNode(ObjectNode book) {
        // coverUrl -> coverImageUrl
        if (book.has("coverUrl") && !book.has("coverImageUrl")) {
            book.set("coverImageUrl", book.get("coverUrl"));
        }
        // category (object) -> category (string name)
        if (book.has("category") && book.get("category").isObject()) {
            book.put("category", book.get("category").path("name").asText());
        }
        // tags (array of objects) -> tags (array of strings)
        if (book.has("tags") && book.get("tags").isArray()) {
            ArrayNode tagsArr = (ArrayNode) book.get("tags");
            ArrayNode newTags = mapper.createArrayNode();
            for (JsonNode t : tagsArr) {
                if (t.isObject() && t.has("name")) {
                    newTags.add(t.get("name").asText());
                } else {
                    newTags.add(t.asText());
                }
            }
            book.set("tags", newTags);
        }
        // seller (object) -> sellerName (string fullName)
        if (book.has("seller") && book.get("seller").isObject()) {
            book.put("sellerName", book.get("seller").path("fullName").asText());
        }
    }

    private static ApiResponse err(int code, String msg) {
        return new ApiResponse(code, "{\"detail\":\"" + msg.replace("\"", "'") + "\"}");
    }

    public record ApiResponse(int status, String body) {
        public boolean isSuccess() { return status >= 200 && status < 300; }
        public JsonNode json() { try { return mapper.readTree(body); } catch (Exception e) { return mapper.createObjectNode(); } }
        public <T> T as(Class<T> c) { try { return mapper.readValue(body, c); } catch (Exception e) { return null; } }
        public String errorMessage() {
            try {
                JsonNode n = json();
                if (n.has("detail")) return n.get("detail").asText();
                if (n.has("message")) return n.get("message").asText();
                if (n.has("error")) return n.get("error").asText();
            } catch (Exception ignored) {}
            return "Error (HTTP " + status + ")";
        }
    }
}
