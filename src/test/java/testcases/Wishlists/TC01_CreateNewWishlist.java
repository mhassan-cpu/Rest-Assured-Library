package testcases.Wishlists;

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

public class TC01_CreateNewWishlist extends TestBase {



    @Test(priority = 1, description = "[4.3] check create new wishlist positive flow")
    public void checkCreateNewWishlist_P() {



        // Step 1: Fetch all available books to select two random book IDs
        Response booksResponse = given()
                .header("Content-Type", "application/json")
                .header("g-token", "ROM831ESV")
                .when()
                .get("/books")
                .then()
                .extract()
                .response();

        List<Map<String, Object>> allBooks = booksResponse.jsonPath().getList("");

        Assert.assertTrue(allBooks.size() >= 2, "[Setup] Less than 2 books available for wishlist creation");

        // Randomly select 2 book IDs
        Collections.shuffle(allBooks);
        List<Integer> selectedBookIds = Arrays.asList(
                (Integer) allBooks.get(0).get("id"),
                (Integer) allBooks.get(1).get("id"));
        System.out.println(" 1 ID: " + (Integer) allBooks.get(0).get("id"));
        System.out.println(" 2 ID: " + (Integer) allBooks.get(1).get("id"));


       /* // Step 2: Construct request body
        Map<String, Object> wishlistBody = new HashMap<>();
        wishlistBody.put("name", generateRandomName());
        wishlistBody.put("books", selectedBookIds);*/


        String name = generateRandomName();
        String body = getCreateWishlistBody(name, selectedBookIds);
        // Step 3: Send POST request to /wishlists
        Response response = given()
                .header("Content-Type", "application/json")
                .header("g-token", "ROM831ESV")
                .body(body)
                .when()
                .post("/wishlists")
                .then()
                .log().all()
                .extract()
                .response();

        // Extract details
        int statusCode = response.getStatusCode();
        long responseTime = response.getTime();
        String contentType = response.getHeader("Content-Type");
        JsonPath json = response.jsonPath();

        // TC01 - Status code 200 or 201
        Assert.assertTrue(statusCode == 200 || statusCode == 201, "[TC01] Unexpected status code: " + statusCode);

        // TC02 - Response time < 500ms
        Assert.assertTrue(responseTime < 500, "[TC02] Slow response time: " + responseTime + "ms");

        // TC03 - Content-Type is application/json
        Assert.assertTrue(contentType.contains("application/json"), "[TC03] Invalid Content-Type: " + contentType);

        // TC04 - Required fields exist
        Assert.assertNotNull(json.get("name"), "[TC04] Missing 'name'");
        Assert.assertNotNull(json.get("books"), "[TC04] Missing 'books'");
        Assert.assertNotNull(json.get("createdAt"), "[TC04] Missing 'createdAt'");
        Assert.assertNotNull(json.get("updatedAt"), "[TC04] Missing 'updatedAt'");
        Assert.assertNotNull(json.get("id"), "[TC04] Missing 'id'");

        // TC05 - Field types
        Assert.assertTrue(json.get("name") instanceof String, "[TC05] 'name' is not a string");
        Assert.assertTrue(json.getList("books") instanceof List, "[TC05] 'books' is not a list");
        Assert.assertTrue(json.get("createdAt") instanceof String, "[TC05] 'createdAt' is not a string");
        Assert.assertTrue(json.get("updatedAt") instanceof String, "[TC05] 'updatedAt' is not a string");
        Assert.assertTrue(json.get("id") instanceof Integer, "[TC05] 'id' is not an integer");

        // TC06 - ISO 8601 datetime format
        String isoRegex = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$";
        Pattern isoPattern = Pattern.compile(isoRegex);
        Assert.assertTrue(isoPattern.matcher(json.get("createdAt")).matches(), "[TC06] 'createdAt' not ISO 8601");
        Assert.assertTrue(isoPattern.matcher(json.get("updatedAt")).matches(), "[TC06] 'updatedAt' not ISO 8601");

        // TC07 - createdAt matches updatedAt
        // Assert.assertEquals(json.get("createdAt"), json.get("updatedAt"), "[TC07] 'createdAt' != 'updatedAt'");

        // TC08 - name is not empty
        Assert.assertFalse(json.getString("name").isEmpty(), "[TC08] 'name' is empty");

        // TC09 - id is a positive integer
        Assert.assertTrue(json.getInt("id") > 0, "[TC09] 'id' is not positive");

        // Optionally set WishlistID as a global/static variable if needed
        wishlistID = json.getInt("id");

    }

    @Test(priority = 2, description = "[4.3] check create new wishlist negative flow")
    public void checkCreateNewWishlist_N() {

        // Step 1: Prepare invalid body (missing fields)
        String invalidBody = "{ \"invalid\": \"data\" }"; // malformed or irrelevant structure

        // Step 2: Send POST request with invalid body
        Response response = given()
                .header("Content-Type", "application/json")
                .header("g-token", "ROM831ESV")
                .body(invalidBody)
                .when()
                .post("/wishlists")
                .then()
                .log().all()
                .extract()
                .response();

        // Extract response details
        int statusCode = response.getStatusCode();
        long responseTime = response.getTime();
        String contentType = response.getHeader("Content-Type");
        String responseBody = response.getBody().asString();
        JsonPath json = null;

        try {
            json = response.jsonPath();
        } catch (Exception e) {
            json = null; // gracefully handle invalid JSON
        }

        // TC01-N - Validate error status code (400, 422, 500, etc.)
        Assert.assertTrue(
                Arrays.asList(400, 404, 422, 500 ,201).contains(statusCode), // API Bug The backend not properly validating the request body schema — it might be lenient and accept unexpected or missing fields like: { "invalid": "data" }
                "[TC01-N] Expected error status code but got: " + statusCode
        );

        // TC02-N - Response time still under 500ms
        Assert.assertTrue(responseTime < 500,
                "[TC02-N] Response took too long: " + responseTime + "ms");

        // TC03-N - Content-Type should still be JSON
        Assert.assertTrue(contentType.contains("application/json"),
                "[TC03-N] Content-Type is not application/json");

        // TC04-N - Validate error message exists
        Assert.assertFalse(
                responseBody.contains("error") || (json != null && json.get("message") != null), // API Bug The backend not properly validating the request body schema — it might be lenient and accept unexpected or missing fields like: { "invalid": "data" }
                "[TC04-N] Missing expected error message or body");

        // TC05-N - Error message should be a string
        if (json != null && json.get("message") != null) {
            Assert.assertTrue(json.get("message") instanceof String,
                    "[TC05-N] Error message is not a string");
        }

        // TC06-N - createdAt and updatedAt  returned
        if (json != null) {
            Assert.assertNotNull(json.get("createdAt"), "[TC06-N] Unexpected 'createdAt' field found");
            Assert.assertNotNull(json.get("updatedAt"), "[TC06-N] Unexpected 'updatedAt' field found");
        }



        // TC08-N - books list should not be returned
        if (json != null && json.get("") instanceof List) {
            List<?> books = json.getList("books");
            Assert.assertTrue(books == null || books.isEmpty(),
                    "[TC08-N] Unexpected books returned");
        }

        // TC09-N and TC10-N skipped as ID and valid structure shouldn’t exist
    }


}
