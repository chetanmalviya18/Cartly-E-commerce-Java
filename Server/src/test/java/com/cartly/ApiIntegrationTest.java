package com.cartly;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.cartly.util.HibernateUtil;

/**
 * End-to-end API integration tests for the Cartly backend.
 *
 * Prerequisites:
 *   - Tomcat must be running at http://localhost:8080/Cartly
 *   - MySQL database must be accessible
 *
 * Each test method hits a real HTTP endpoint and validates the response.
 * DB is cleaned before all tests start and after all tests finish.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiIntegrationTest {

    private static final String BASE_URL = "http://localhost:8080/Cartly";
    private static HttpClient httpClient;

    // Shared state across ordered tests
    private static Long createdCategoryId;
    private static Long createdProductId;

    // ─────────────────────────────────────────────
    // Lifecycle: Before & After All Tests
    // ─────────────────────────────────────────────

    @BeforeAll
    static void setup() {
        // Build HttpClient with CookieManager for automatic JSESSIONID management
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        httpClient = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .build();

        cleanDatabase();
        System.out.println("\n✅ [SETUP] Database cleaned. HttpClient initialized.\n");
    }

    @AfterAll
    static void teardown() {
        cleanDatabase();
        System.out.println("\n✅ [TEARDOWN] Database cleaned after all tests.\n");
    }

    private static void cleanDatabase() {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createNativeQuery("DELETE FROM order_items",   void.class).executeUpdate();
            session.createNativeQuery("DELETE FROM orders",        void.class).executeUpdate();
            session.createNativeQuery("DELETE FROM cart_items",    void.class).executeUpdate();
            session.createNativeQuery("DELETE FROM carts",         void.class).executeUpdate();
            session.createNativeQuery("DELETE FROM products",      void.class).executeUpdate();
            session.createNativeQuery("DELETE FROM categories",    void.class).executeUpdate();
            session.createNativeQuery("DELETE FROM addresses",     void.class).executeUpdate();
            session.createNativeQuery("DELETE FROM users",         void.class).executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Database cleanup failed", e);
        }
    }

    // ─────────────────────────────────────────────
    // Helper: Build POST / PUT JSON request
    // ─────────────────────────────────────────────

    private HttpRequest postJson(String path, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(body))
                .build();
    }

    private HttpRequest putJson(String path, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .PUT(BodyPublishers.ofString(body))
                .build();
    }

    private HttpRequest get(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .GET()
                .build();
    }

    private HttpRequest delete(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .DELETE()
                .build();
    }

    private HttpResponse<String> send(HttpRequest request) throws Exception {
        return httpClient.send(request, BodyHandlers.ofString());
    }

    // ─────────────────────────────────────────────
    // Test 1: Public access to protected endpoint - no session
    // ─────────────────────────────────────────────

    @Test
    @Order(1)
    void test_GetMe_WithoutLogin_Returns401() throws Exception {
        HttpResponse<String> response = send(get("/api/users/me"));

        System.out.println("[1] GET /api/users/me (no session) → " + response.statusCode());

        assertEquals(401, response.statusCode(),
                "Expected 401 Unauthorized when accessing /api/users/me without login");
    }

    // ─────────────────────────────────────────────
    // Test 2: Register
    // ─────────────────────────────────────────────

    @Test
    @Order(2)
    void test_Register_Returns201() throws Exception {
        HttpResponse<String> response = send(postJson("/api/auth/register",
                "{\"name\":\"API Test User\",\"email\":\"apitest@cartly.com\",\"password\":\"password123\"}"));

        System.out.println("[2] POST /api/auth/register → " + response.statusCode() + " | " + response.body());

        assertEquals(201, response.statusCode(), "Expected 201 Created on registration");
        assertTrue(response.body().contains("apitest@cartly.com"), "Response should contain registered email");
        assertTrue(!response.body().contains("password"), "Response must NOT expose password hash");
    }

    // ─────────────────────────────────────────────
    // Test 3: Register duplicate email → 400
    // ─────────────────────────────────────────────

    @Test
    @Order(3)
    void test_Register_DuplicateEmail_Returns400() throws Exception {
        HttpResponse<String> response = send(postJson("/api/auth/register",
                "{\"name\":\"Duplicate\",\"email\":\"apitest@cartly.com\",\"password\":\"password123\"}"));

        System.out.println("[3] POST /api/auth/register (duplicate) → " + response.statusCode());

        assertEquals(400, response.statusCode(), "Expected 400 Bad Request for duplicate email");
    }

    // ─────────────────────────────────────────────
    // Test 4: Login
    // ─────────────────────────────────────────────

    @Test
    @Order(4)
    void test_Login_Returns200_AndSetsSession() throws Exception {
        HttpResponse<String> response = send(postJson("/api/auth/login",
                "{\"email\":\"apitest@cartly.com\",\"password\":\"password123\"}"));

        System.out.println("[4] POST /api/auth/login → " + response.statusCode() + " | " + response.body());

        assertEquals(200, response.statusCode(), "Expected 200 OK on login");
        assertTrue(response.body().contains("apitest@cartly.com"), "Response should contain user email");
        assertNotNull(response.headers().firstValue("Set-Cookie").orElse(null),
                "Login must set session cookie (JSESSIONID)");
    }

    // ─────────────────────────────────────────────
    // Test 5: Get current user (after login)
    // ─────────────────────────────────────────────

    @Test
    @Order(5)
    void test_GetMe_AfterLogin_Returns200() throws Exception {
        HttpResponse<String> response = send(get("/api/users/me"));

        System.out.println("[5] GET /api/users/me (logged in) → " + response.statusCode() + " | " + response.body());

        assertEquals(200, response.statusCode(), "Expected 200 OK for authenticated user");
        assertTrue(response.body().contains("apitest@cartly.com"), "Response should contain logged-in user's email");
    }

    // ─────────────────────────────────────────────
    // Test 6: Create Category
    // ─────────────────────────────────────────────

    @Test
    @Order(6)
    void test_CreateCategory_Returns201() throws Exception {
        HttpResponse<String> response = send(postJson("/api/categories",
                "{\"name\":\"Electronics\",\"description\":\"Electronic devices and accessories\"}"));

        System.out.println("[6] POST /api/categories → " + response.statusCode() + " | " + response.body());

        assertEquals(201, response.statusCode(), "Expected 201 Created for new category");
        assertTrue(response.body().contains("Electronics"), "Response should contain category name");
        assertTrue(response.body().contains("Electronic devices"), "Response should contain description");

        // Extract created category ID
        String body = response.body();
        int idStart = body.indexOf("\"id\":") + 5;
        int idEnd = body.indexOf(",", idStart);
        createdCategoryId = Long.parseLong(body.substring(idStart, idEnd).trim());
        System.out.println("   → Created category ID: " + createdCategoryId);
    }

    // ─────────────────────────────────────────────
    // Test 7: Get All Categories
    // ─────────────────────────────────────────────

    @Test
    @Order(7)
    void test_GetAllCategories_Returns200() throws Exception {
        HttpResponse<String> response = send(get("/api/categories"));

        System.out.println("[7] GET /api/categories → " + response.statusCode() + " | " + response.body());

        assertEquals(200, response.statusCode(), "Expected 200 OK for categories list");
        assertTrue(response.body().contains("Electronics"), "Response should contain created category");
    }

    // ─────────────────────────────────────────────
    // Test 8: Get Category by ID
    // ─────────────────────────────────────────────

    @Test
    @Order(8)
    void test_GetCategoryById_Returns200() throws Exception {
        assertNotNull(createdCategoryId, "Category ID must be set by previous test");

        HttpResponse<String> response = send(get("/api/categories/" + createdCategoryId));

        System.out.println("[8] GET /api/categories/" + createdCategoryId + " → " + response.statusCode() + " | " + response.body());

        assertEquals(200, response.statusCode(), "Expected 200 OK for category by ID");
        assertTrue(response.body().contains("Electronics"), "Response should contain category name");
    }

    // ─────────────────────────────────────────────
    // Test 9: Update Category
    // ─────────────────────────────────────────────

    @Test
    @Order(9)
    void test_UpdateCategory_Returns200() throws Exception {
        assertNotNull(createdCategoryId, "Category ID must be set by previous test");

        HttpResponse<String> response = send(putJson("/api/categories/" + createdCategoryId,
                "{\"name\":\"Electronics & Gadgets\",\"description\":\"Updated description\"}"));

        System.out.println("[9] PUT /api/categories/" + createdCategoryId + " → " + response.statusCode() + " | " + response.body());

        assertEquals(200, response.statusCode(), "Expected 200 OK for updated category");
        assertTrue(response.body().contains("Electronics & Gadgets"), "Response should contain updated name");
    }

    // ─────────────────────────────────────────────
    // Test 10: Create Product
    // ─────────────────────────────────────────────

    @Test
    @Order(10)
    void test_CreateProduct_Returns201() throws Exception {
        assertNotNull(createdCategoryId, "Category ID must be set by previous test");

        String body = "{\"name\":\"iPhone 16 Pro\",\"description\":\"Apple flagship phone\","
                + "\"price\":134900.00,\"stock\":50,\"categoryId\":" + createdCategoryId
                + ",\"imageUrl\":\"https://example.com/iphone.jpg\"}";

        HttpResponse<String> response = send(postJson("/api/products", body));

        System.out.println("[10] POST /api/products → " + response.statusCode() + " | " + response.body());

        assertEquals(201, response.statusCode(), "Expected 201 Created for new product");
        assertTrue(response.body().contains("iPhone 16 Pro"), "Response should contain product name");
        assertTrue(response.body().contains("134900"), "Response should contain price");

        // Extract created product ID
        String responseBody = response.body();
        int idStart = responseBody.indexOf("\"id\":") + 5;
        int idEnd = responseBody.indexOf(",", idStart);
        createdProductId = Long.parseLong(responseBody.substring(idStart, idEnd).trim());
        System.out.println("   → Created product ID: " + createdProductId);
    }

    // ─────────────────────────────────────────────
    // Test 11: Get All Products
    // ─────────────────────────────────────────────

    @Test
    @Order(11)
    void test_GetAllProducts_Returns200() throws Exception {
        HttpResponse<String> response = send(get("/api/products"));

        System.out.println("[11] GET /api/products → " + response.statusCode() + " | " + response.body());

        assertEquals(200, response.statusCode(), "Expected 200 OK for products list");
        assertTrue(response.body().contains("iPhone 16 Pro"), "Response should contain created product");
    }

    // ─────────────────────────────────────────────
    // Test 12: Get Products by Category
    // ─────────────────────────────────────────────

    @Test
    @Order(12)
    void test_GetProductsByCategory_Returns200() throws Exception {
        assertNotNull(createdCategoryId, "Category ID must be set by previous test");

        HttpResponse<String> response = send(get("/api/products?categoryId=" + createdCategoryId));

        System.out.println("[12] GET /api/products?categoryId=" + createdCategoryId + " → " + response.statusCode() + " | " + response.body());

        assertEquals(200, response.statusCode(), "Expected 200 OK for products by category");
        assertTrue(response.body().contains("iPhone 16 Pro"), "Response should contain product in category");
    }

    // ─────────────────────────────────────────────
    // Test 13: Search Products
    // ─────────────────────────────────────────────

    @Test
    @Order(13)
    void test_SearchProducts_Returns200() throws Exception {
        HttpResponse<String> response = send(get("/api/products?search=iPhone"));

        System.out.println("[13] GET /api/products?search=iPhone → " + response.statusCode() + " | " + response.body());

        assertEquals(200, response.statusCode(), "Expected 200 OK for product search");
        assertTrue(response.body().contains("iPhone 16 Pro"), "Search should return matching product");
    }

    // ─────────────────────────────────────────────
    // Test 14: Get Product by ID
    // ─────────────────────────────────────────────

    @Test
    @Order(14)
    void test_GetProductById_Returns200() throws Exception {
        assertNotNull(createdProductId, "Product ID must be set by previous test");

        HttpResponse<String> response = send(get("/api/products/" + createdProductId));

        System.out.println("[14] GET /api/products/" + createdProductId + " → " + response.statusCode() + " | " + response.body());

        assertEquals(200, response.statusCode(), "Expected 200 OK for product by ID");
        assertTrue(response.body().contains("iPhone 16 Pro"), "Response should contain product name");
    }

    // ─────────────────────────────────────────────
    // Test 15: Update Product
    // ─────────────────────────────────────────────

    @Test
    @Order(15)
    void test_UpdateProduct_Returns200() throws Exception {
        assertNotNull(createdProductId, "Product ID must be set by previous test");

        String body = "{\"name\":\"iPhone 16 Pro Max\",\"description\":\"Updated flagship\","
                + "\"price\":159900.00,\"stock\":30,\"categoryId\":" + createdCategoryId
                + ",\"imageUrl\":\"https://example.com/iphone-max.jpg\"}";

        HttpResponse<String> response = send(putJson("/api/products/" + createdProductId, body));

        System.out.println("[15] PUT /api/products/" + createdProductId + " → " + response.statusCode() + " | " + response.body());

        assertEquals(200, response.statusCode(), "Expected 200 OK for updated product");
        assertTrue(response.body().contains("iPhone 16 Pro Max"), "Response should contain updated product name");
    }

    // ─────────────────────────────────────────────
    // Test 16: Deactivate (soft-delete) Product
    // ─────────────────────────────────────────────

    @Test
    @Order(16)
    void test_DeactivateProduct_Returns204() throws Exception {
        assertNotNull(createdProductId, "Product ID must be set by previous test");

        HttpResponse<String> response = send(delete("/api/products/" + createdProductId));

        System.out.println("[16] DELETE /api/products/" + createdProductId + " → " + response.statusCode());

        assertEquals(204, response.statusCode(), "Expected 204 No Content for deactivated product");
    }

    // ─────────────────────────────────────────────
    // Test 17: Logout
    // ─────────────────────────────────────────────

    @Test
    @Order(17)
    void test_Logout_Returns204() throws Exception {
        HttpResponse<String> response = send(postJson("/api/auth/logout", ""));

        System.out.println("[17] POST /api/auth/logout → " + response.statusCode());

        assertEquals(204, response.statusCode(), "Expected 204 No Content on logout");
    }

    // ─────────────────────────────────────────────
    // Test 18: Get Me after logout - should be 401
    // ─────────────────────────────────────────────

    @Test
    @Order(18)
    void test_GetMe_AfterLogout_Returns401() throws Exception {
        HttpResponse<String> response = send(get("/api/users/me"));

        System.out.println("[18] GET /api/users/me (after logout) → " + response.statusCode());

        assertEquals(401, response.statusCode(),
                "Expected 401 Unauthorized after session is invalidated by logout");
    }
}
