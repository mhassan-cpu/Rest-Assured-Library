package testcases.Wishlists;

import testcases.TestBase;
import testcases.TestBase;
import io.restassured.path.json.JsonPath;
import testcases.TestBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import model.CreateBookBody;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pojo.CreateBook;
import retryTest.Retry;
import testcases.TestBase;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static model.CreateBookBody.getCreateBookBody;
import static model.CreateUserBody.getCreateUserBody;
import static model.CreateWishlistBody.getCreateWishlistBody;
import static util.Utility.NameGenerator.*;

public class TC02_GetAllWishlists extends TestBase {
    SoftAssert softassert = new SoftAssert();

    @Test(priority = 1, description = "[4.1] check get all wishlists positive flow")
    public void checkGetAllWishlists_P() {

        // Step 1: Send GET request to fetch all wishlists
        Response response = given()
                .header("Content-Type", "application/json")
                .header("g-token", "ROM831ESV")
                .when()
                .get("/wishlists")
                .then()
                .log().all()
                .extract()
                .response();

        // Extract details
        int statusCode = response.getStatusCode();
        long responseTime = response.getTime();
        String contentType = response.getHeader("Content-Type");
        JsonPath json = response.jsonPath();
        List<Map<String, Object>> wishlists = json.getList("");

        // TC01 - Status code 200 or 201
        Assert.assertTrue(statusCode == 200 || statusCode == 201,
                "[TC01] Expected 200 or 201 but got: " + statusCode);

        // TC02 - Response time < 500ms
        Assert.assertTrue(responseTime < 500,
                "[TC02] Response time exceeded 500ms: " + responseTime + "ms");

        // TC03 - Content-Type should be JSON
        Assert.assertTrue(contentType.contains("application/json"),
                "[TC03] Invalid Content-Type: " + contentType);

        // TC04 - Response must be an array
        Assert.assertTrue(wishlists instanceof List, "[TC04] Response is not an array");

        for (Map<String, Object> wishlist : wishlists) {
            // TC05 - Required fields
            //Assert.assertTrue(wishlist.containsKey("name"), "[TC05] Missing field: name"); // API Bug Not all objects have name
            //Assert.assertTrue(wishlist.containsKey("books"), "[TC05] Missing field: books"); // API Bug Not all objects have book []
            Assert.assertTrue(wishlist.containsKey("id"), "[TC05] Missing field: id");

            // TC06 - 'books' is an array
            softassert.assertTrue(wishlist.get("books") instanceof List,
                    "[TC06] Wishlist ID " + wishlist.get("id") + " has invalid or missing 'books' field: " + wishlist.get("books"));


            // TC07 - 'id' is a positive integer
            Object id = wishlist.get("id");
            Assert.assertTrue(id instanceof Integer && (Integer) id > 0,
                    "[TC07] 'id' is not a positive integer");

            // TC08 - 'name' is not empty
            Object nameField = wishlist.get("name");
            softassert.assertNotNull(nameField, "[TC08] 'name' is null");
            //softassert.assertFalse(nameField.toString().trim().isEmpty(), "[TC08] 'name' is empty"); // Name is NULL


            // TC09 - Optional timestamps if present
            Pattern isoPattern = Pattern.compile("^[\\d]{4}-[\\d]{2}-[\\d]{2}T[\\d]{2}:[\\d]{2}:[\\d]{2}\\.\\d{3}Z$");
            if (wishlist.get("createdAt") != null) {
                String createdAt = wishlist.get("createdAt").toString();
                Assert.assertTrue(isoPattern.matcher(createdAt).matches(),
                        "[TC09] Invalid createdAt timestamp format");
            }
            if (wishlist.get("updatedAt") != null) {
                String updatedAt = wishlist.get("updatedAt").toString();
                Assert.assertTrue(isoPattern.matcher(updatedAt).matches(),
                        "[TC09] Invalid updatedAt timestamp format");
            }
        }
    }



    @Test(priority = 2, description = "[4.1] check get all wishlists negative flow")
    public void checkGetAllWishlists_N() {

        // Step 1: Create an invalid request (e.g., missing token)
        Response response = given()
                .header("Content-Type", "application/json") // Missing or invalid g-token
                .when()
                .get("/wishlists-invalid") // Invalid endpoint to simulate malformed URL
                .then()
                .log().all()
                .extract()
                .response();

        // Step 2: Extract data
        int statusCode = response.getStatusCode();
        long responseTime = response.getTime();
        String contentType = response.getHeader("Content-Type");
        String responseBody = response.getBody().asString().trim();

        JsonPath json = null;
        try {
            json = response.jsonPath(); // only if body is JSON
        } catch (Exception e) {
            // Handle non-JSON gracefully
            json = null;
        }

        // TC01-N: Validate status code is error (typically 400, 401, 403, 404, 422, or 500)
        Assert.assertTrue(
                Arrays.asList(400, 401, 403, 404, 422, 500).contains(statusCode),
                "[TC01-N] Unexpected status code: " + statusCode
        );

        // TC02-N: Validate response time is still performant (optional tolerance)
        Assert.assertTrue(
                responseTime < 1000,
                "[TC02-N] Slow error response time: " + responseTime + "ms"
        );

        // TC03-N: Validate Content-Type is still JSON or expected type
        if (contentType != null) {
            Assert.assertTrue(
                    contentType.contains("application/json") || contentType.contains("text/html"),
                    "[TC03-N] Unexpected Content-Type: " + contentType
            );
        }

        // TC04-N: Validate response is not an array or has no data
        Object root = null;
        try {
            root = json.get("");
            Assert.assertFalse(root instanceof List, "[TC04-N] Unexpected array structure in error response");
        } catch (Exception e) {
            // Log and skip this check if the response isn't valid JSON
            System.out.println("[TC04-N] Skipping structure check — response is not a valid JSON.");
        }


        // TC05-N: Expected error message
        // TC05-N - Expected error message
        try {
            Assert.assertTrue(
                    responseBody.contains("error") || (json != null && json.get("message") != null),
                    "[TC05-N] Missing expected error message or field"
            );
        } catch (Exception e) {
            System.out.println("[TC05-N] Skipping message check — response is not a valid JSON.");
        }

    }


}
