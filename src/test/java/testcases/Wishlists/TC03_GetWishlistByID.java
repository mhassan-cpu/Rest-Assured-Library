package testcases.Wishlists;

import testcases.TestBase;

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

public class TC03_GetWishlistByID extends TestBase {


    @Test(priority = 1, description = "[4.3] check get wishlist by ID positive flow")
    public void checkGetWishlistByID_P() {
        // Assume wishlistID is already created and available for use
        Assert.assertTrue(wishlistID > 0, "[Setup] Invalid wishlist ID for retrieval");

        // Send GET request
        Response response = given()
                .header("Content-Type", "application/json")
                .header("g-token", "ROM831ESV")
                .when()
                .get("/wishlists/" + wishlistID)
                .then()
                .log().all()
                .extract().response();

        int statusCode = response.getStatusCode();
        long responseTime = response.getTime();
        String contentType = response.getHeader("Content-Type");
        JsonPath json = response.jsonPath();

        // TC01 - Status code is 200 or 201
        Assert.assertTrue(statusCode == 200 || statusCode == 201,
                "[TC01] Expected 200 or 201 but got " + statusCode);

        // TC02 - Response time < 500ms
        Assert.assertTrue(responseTime < 500,
                "[TC02] Response time exceeded 500ms: " + responseTime + "ms");

        // TC03 - Content-Type is application/json
        Assert.assertTrue(contentType.contains("application/json"),
                "[TC03] Invalid Content-Type: " + contentType);

        // TC04 - Response is a valid object
        Map<String, Object> jsonData = json.getMap("");
        Assert.assertNotNull(jsonData, "[TC04] Response is not a valid JSON object");

        // TC05 - Required fields exist
        Assert.assertTrue(jsonData.containsKey("name"), "[TC05] Missing field: name");
        Assert.assertTrue(jsonData.containsKey("books"), "[TC05] Missing field: books");
        Assert.assertTrue(jsonData.containsKey("createdAt"), "[TC05] Missing field: createdAt");
        Assert.assertTrue(jsonData.containsKey("updatedAt"), "[TC05] Missing field: updatedAt");
        Assert.assertTrue(jsonData.containsKey("id"), "[TC05] Missing field: id");

        // TC06 - Field data types
        Assert.assertTrue(jsonData.get("name") instanceof String, "[TC06] name is not a string");
        Assert.assertTrue(jsonData.get("books") instanceof List, "[TC06] books is not an array");
        Assert.assertTrue(jsonData.get("createdAt") instanceof String, "[TC06] createdAt is not a string");
        Assert.assertTrue(jsonData.get("updatedAt") instanceof String, "[TC06] updatedAt is not a string");
        Assert.assertTrue(jsonData.get("id") instanceof Integer, "[TC06] id is not a number");

        // TC07 - Validate createdAt and updatedAt are in ISO 8601 format
        String isoRegex = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$";
        Pattern isoPattern = Pattern.compile(isoRegex);
        String createdAt = jsonData.get("createdAt").toString();
        String updatedAt = jsonData.get("updatedAt").toString();
        Assert.assertTrue(isoPattern.matcher(createdAt).matches(),
                "[TC07] createdAt is not in ISO 8601 format: " + createdAt);
        Assert.assertTrue(isoPattern.matcher(updatedAt).matches(),
                "[TC07] updatedAt is not in ISO 8601 format: " + updatedAt);

        // TC08 - createdAt and updatedAt are equal
        Assert.assertEquals(createdAt, updatedAt,
                "[TC08] createdAt and updatedAt do not match");

        // TC09 - Name is not empty
        Assert.assertFalse(jsonData.get("name").toString().trim().isEmpty(),
                "[TC09] name is empty");

        // TC10 - ID is a positive integer
        int id = (int) jsonData.get("id");
        Assert.assertTrue(id > 0, "[TC10] ID is not positive");
    }


    @Test(priority = 2, description = "[4.3] check get wishlist by ID negative flow")
    public void checkGetWishlistByID_N() {
        int invalidId = 999999; // Assumed to not exist

        // Send GET request with invalid wishlist ID
        Response response = given()
                .header("Content-Type", "application/json")
                .header("g-token", "ROM831ESV")
                .when()
                .get("/wishlists/" + invalidId)
                .then()
                .log().all()
                .extract().response();

        int statusCode = response.getStatusCode();
        long responseTime = response.getTime();
        String contentType = response.getHeader("Content-Type");
        String responseBody = response.getBody().asString().trim();

        JsonPath json = null;
        try {
            json = response.jsonPath();
        } catch (Exception e) {
            // Gracefully handle invalid JSON
            System.out.println("[TC04-N] Response is not valid JSON");
        }

        // TC01-N - Validate error status code (400, 404, 422, 500, etc.)
        Assert.assertTrue(
                Arrays.asList(400, 404, 422, 500).contains(statusCode),
                "[TC01-N] Expected error status code but got: " + statusCode
        );

        // TC02-N - Validate response time still under 500ms
        Assert.assertTrue(
                responseTime < 500,
                "[TC02-N] Response took too long: " + responseTime + "ms"
        );

        // TC03-N - Content-Type should still be JSON or informative
        Assert.assertTrue(
                contentType != null && (contentType.contains("application/json") || contentType.contains("text/html")),
                "[TC03-N] Unexpected Content-Type: " + contentType
        );

        // TC04-N - Validate response is not an object or has no data
        Object root = null;
        try {
            root = json.get("");
            Assert.assertTrue(root instanceof Map, "[TC04-N] Expected JSON object structure in error response");
        } catch (Exception e) {
            System.out.println("[TC04-N] Skipping structure check – response is not a valid JSON object.");
        }


        // TC05-N - Expected error message exists
        Assert.assertTrue(
                responseBody.contains("error") ||
                        (json != null && json.get("message") != null),
                "[TC05-N] Missing expected error message or field"
        );
    }


}