package testcases.users;

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
import pojo.CreateBook;
import testcases.TestBase;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static model.CreateBookBody.getCreateBookBody;
import static util.Utility.NameGenerator.*;

public class TC03_GetUserByID extends TestBase {

    @Test(priority = 1, description = "[3.3] check get user by ID positive flow")
    public void checkGetUserByID_P() {

        // Assume userId is set from a previous test like createUser
        //int validUserId = userId; // Replace with actual user ID if needed

        // Send GET request to /users/{id}
        Response response = given()
                .log().all()
                .header("Content-Type", "application/json")
                .header("g-token", "ROM831ESV")
                .when()
                .get("/users/" + userId)
                .then()
                .log().all()
                .extract().response();

        // Extract core response elements
        int statusCode = response.getStatusCode();
        long responseTime = response.getTime();
        String contentType = response.getHeader("Content-Type");
        JsonPath json = response.jsonPath();

        // TC01 - Validate status code is 200 or 201
        Assert.assertTrue(statusCode == 200 || statusCode == 201,
                "[TC01] Expected status code 200 or 201, but got " + statusCode);

        // TC02 - Validate response time is under 500ms
        Assert.assertTrue(responseTime < 500,
                "[TC02] Response time exceeded 500ms: " + responseTime + "ms");

        // TC03 - Validate Content-Type header is application/json
        Assert.assertTrue(contentType.contains("application/json"),
                "[TC03] Content-Type mismatch: " + contentType);

        // TC04 - Validate response body is a valid JSON object
        Assert.assertTrue(json.get("") instanceof Object,
                "[TC04] Response is not a valid JSON object");

        // TC05 - Validate required fields exist
        Assert.assertNotNull(json.get("id"), "[TC05] Missing field: id");
        Assert.assertNotNull(json.get("firstName"), "[TC05] Missing field: firstName");
        Assert.assertNotNull(json.get("lastName"), "[TC05] Missing field: lastName");
        Assert.assertNotNull(json.get("email"), "[TC05] Missing field: email");
        Assert.assertNotNull(json.get("createdAt"), "[TC05] Missing field: createdAt");
        Assert.assertNotNull(json.get("updatedAt"), "[TC05] Missing field: updatedAt");

        // TC06 - Validate data types of fields
        Assert.assertTrue(json.get("id") instanceof Integer, "[TC06] id is not a number");
        Assert.assertTrue(json.get("firstName") instanceof String, "[TC06] firstName is not a string");
        Assert.assertTrue(json.get("lastName") instanceof String, "[TC06] lastName is not a string");
        Assert.assertTrue(json.get("email") instanceof String, "[TC06] email is not a string");
        Assert.assertTrue(json.get("createdAt") instanceof String, "[TC06] createdAt is not a string");
        Assert.assertTrue(json.get("updatedAt") instanceof String, "[TC06] updatedAt is not a string");

        // TC07 - Validate createdAt and updatedAt are in ISO 8601 format
        String isoRegex = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$";
        Pattern isoPattern = Pattern.compile(isoRegex);
        Assert.assertTrue(isoPattern.matcher(json.getString("createdAt")).matches(),
                "[TC07] createdAt is not in ISO 8601 format");
        Assert.assertTrue(isoPattern.matcher(json.getString("updatedAt")).matches(),
                "[TC07] updatedAt is not in ISO 8601 format");

        // TC08 - Validate createdAt and updatedAt match
        Assert.assertEquals(json.getString("createdAt"), json.getString("updatedAt"),
                "[TC08] createdAt and updatedAt do not match");

        // TC09 - Validate firstName, lastName, and email are not empty
        Assert.assertFalse(json.getString("firstName").isEmpty(), "[TC09] firstName is empty");
        Assert.assertFalse(json.getString("lastName").isEmpty(), "[TC09] lastName is empty");
        Assert.assertFalse(json.getString("email").isEmpty(), "[TC09] email is empty");

        // TC10 - Validate ID is a positive integer
        Assert.assertTrue(json.getInt("id") > 0, "[TC10] ID is not positive");
    }

    @Test(priority = 2, description = "[3.3] check get user by ID negative flow")
    public void checkGetUserByID_N() {

        int invalidUserId = 999999; // Assumed non-existent ID

        // Send GET request with invalid ID
        Response response = given()
                .log().all()
                .header("Content-Type", "application/json")
                .header("g-token", "ROM831ESV")
                .when()
                .get("/users/" + invalidUserId)
                .then()
                .log().all()
                .extract().response();

        int statusCode = response.getStatusCode();
        long responseTime = response.getTime();
        String contentType = response.getHeader("Content-Type");
        String responseBody = response.getBody().asString().trim();

        JsonPath json = null;
        boolean isJson = false;
        try {
            json = response.jsonPath();
            isJson = true;
        } catch (Exception e) {
            isJson = false;
        }

        // TC01-N - Expect error code (e.g., 404 or 400)
        Assert.assertTrue(statusCode == 404 || statusCode == 400,
                "[TC01-N] Expected status code 404 or 400, but got " + statusCode);

        // TC02-N - Response time still under 500ms
        Assert.assertTrue(responseTime < 500,
                "[TC02-N] Slow response time: " + responseTime + "ms");

        // TC03-N - Content-Type should still be JSON or error type
        Assert.assertTrue(contentType.contains("application/json"),
                "[TC03-N] Content-Type is not application/json");

        // TC04-N - Response body should contain error message or valid structure
        Assert.assertTrue(isJson && (json.get("message") != null || responseBody.contains("error")),
                "[TC04-N] Expected error message or non-empty body");

        // TC05-N - Error message should be string
        if (json != null && json.get("message") != null) {
            Assert.assertTrue(json.get("message") instanceof String,
                    "[TC05-N] Error message is not a string");
        }

        // TC06-N - createdAt and updatedAt should NOT be returned
        if (json != null) {
            Assert.assertNull(json.get("createdAt"), "[TC06-N] Unexpected 'createdAt' field found");
            Assert.assertNull(json.get("updatedAt"), "[TC06-N] Unexpected 'updatedAt' field found");
        }

        // TC07-N skipped as not applicable (no matching timestamps)

        // TC08-N - Fields like firstName, lastName, email should NOT exist
        if (json != null) {
            Assert.assertNull(json.get("firstName"), "[TC08-N] Unexpected field 'firstName'");
            Assert.assertNull(json.get("lastName"), "[TC08-N] Unexpected field 'lastName'");
            Assert.assertNull(json.get("email"), "[TC08-N] Unexpected field 'email'");
        }


    }
}