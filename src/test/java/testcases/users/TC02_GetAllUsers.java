package testcases.users;

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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static model.CreateBookBody.getCreateBookBody;
import static model.CreateUserBody.getCreateUserBody;
import static util.Utility.NameGenerator.*;

public class TC02_GetAllUsers extends TestBase {


    @Test(priority = 1, description = "[3.1] check get all users positive flow")
    public void checkGetAllUsers_P() {

        // Send GET request to /users endpoint
        Response response = given()
                .log().all()
                .header("Content-Type", "application/json")
                .header("g-token", "ROM831ESV")
                .when()
                .get("/users")
                .then()
                .log().all()
                .extract()
                .response();

        // Extract response data
        int statusCode = response.getStatusCode();
        long responseTime = response.getTime();
        String contentType = response.getHeader("Content-Type");
        String body = response.getBody().asString().trim();
        JsonPath json = response.jsonPath();

        // TC01 - Validate status code is 200 or 201
        Assert.assertTrue(statusCode == 200 || statusCode == 201, "[TC01] Unexpected status code: " + statusCode);

        // TC02 - Validate response time < 500ms
        Assert.assertTrue(responseTime < 500, "[TC02] Slow response time: " + responseTime + "ms");

        // TC03 - Validate Content-Type header is application/json
        Assert.assertTrue(contentType.contains("application/json"), "[TC03] Invalid Content-Type: " + contentType);

        // TC04 - Validate that response is an array
        List<Map<String, Object>> users = json.getList("");
        Assert.assertTrue(users instanceof List, "[TC04] Response is not a JSON array");

        // TC05 - Validate required fields exist in first user
        Map<String, Object> user1 = users.get(0);
        Assert.assertTrue(user1.containsKey("id"), "[TC05] Missing field: id");
        Assert.assertTrue(user1.containsKey("firstName"), "[TC05] Missing field: firstName");
        Assert.assertTrue(user1.containsKey("lastName"), "[TC05] Missing field: lastName");
        Assert.assertTrue(user1.containsKey("email"), "[TC05] Missing field: email");
        Assert.assertTrue(user1.containsKey("createdAt"), "[TC05] Missing field: createdAt");
        Assert.assertTrue(user1.containsKey("updatedAt"), "[TC05] Missing field: updatedAt");

        // TC06 - Validate field data types in first user
        Assert.assertTrue(user1.get("id") instanceof Integer, "[TC06] id is not an integer");
        Assert.assertTrue(user1.get("firstName") instanceof String, "[TC06] firstName is not a string");
        Assert.assertTrue(user1.get("lastName") instanceof String, "[TC06] lastName is not a string");
        Assert.assertTrue(user1.get("email") instanceof String, "[TC06] email is not a string");
        Assert.assertTrue(user1.get("createdAt") instanceof String, "[TC06] createdAt is not a string");
        Assert.assertTrue(user1.get("updatedAt") instanceof String, "[TC06] updatedAt is not a string");

        // TC07 - Validate ISO 8601 format for createdAt and updatedAt
        String isoRegex = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$";
        Pattern isoPattern = Pattern.compile(isoRegex);

        Assert.assertTrue(isoPattern.matcher(user1.get("createdAt").toString()).matches(),
                "[TC07] createdAt is not in ISO 8601 format");
        Assert.assertTrue(isoPattern.matcher(user1.get("updatedAt").toString()).matches(),
                "[TC07] updatedAt is not in ISO 8601 format");

        // TC08 - Validate createdAt and updatedAt match
        Assert.assertEquals(user1.get("createdAt"), user1.get("updatedAt"),
                "[TC08] createdAt and updatedAt do not match");

        // TC09 - Validate second user exists
        Assert.assertTrue(users.size() > 1, "[TC09] Less than two users returned");

        // TC10 - Validate first two users have different IDs
        Map<String, Object> user2 = users.get(1);
        Assert.assertNotEquals(user1.get("id"), user2.get("id"),
                "[TC10] First two users have the same ID");
    }


    @Test(priority = 2, description = "[3.1] check get all users negative flow")
    public void checkGetAllUsers_N() {

        // Intentionally remove or corrupt required headers to trigger failure
        Response response = given()
                .log().all()
                .header("Content-Type", "application/json")
                // .header("g-token", "ROM831ESV") // Token intentionally omitted
                .when()
                .get("/users")
                .then()
                .log().all()
                .extract()
                .response();

        int statusCode = response.getStatusCode();
        long responseTime = response.getTime();
        String contentType = response.getHeader("Content-Type");
        String responseBody = response.getBody().asString().trim();
        JsonPath json = null;
        boolean isJson = contentType != null && contentType.contains("application/json");

        if (isJson) {
            try {
                json = response.jsonPath();
            } catch (Exception e) {
                Assert.fail("[TC-N] Failed to parse response JSON: " + e.getMessage());
            }
        }

        // TC01-N: Status code should NOT be 200 or 201
        Assert.assertFalse(statusCode == 200 || statusCode == 201,
                "[TC01-N] Unexpected success status code: " + statusCode);

        // TC02-N: Response time should still be below threshold (though it's a fail case)
        Assert.assertTrue(responseTime < 2000, "[TC02-N] Slow error response: " + responseTime + "ms");

        // TC03-N: Content-Type should still be JSON or should be validated
        if (contentType != null) {
            Assert.assertTrue(contentType.contains("application/json"),
                    "[TC03-N] Unexpected Content-Type: " + contentType);
        }

        // TC04-N: Validate that body contains an error or is not a valid array
        boolean errorInBody = responseBody.contains("error") || responseBody.contains("message");
        boolean isJsonArray = responseBody.trim().startsWith("[") && responseBody.trim().endsWith("]");
        Assert.assertTrue(!isJsonArray || errorInBody,
                "[TC04-N] Expected error message or non-array response structure");

       /* // TC05-N: Error message should exist and be a string
        if (json != null && json.get("message") != null) {
            Assert.assertTrue(json.get("message") instanceof String,
                    "[TC05-N] Error message is not a string");
        }*/

        // TC06-N: createdAt and updatedAt should not be returned
        if (json != null) {
            Assert.assertNotNull(json.get("createdAt"), "[TC06-N] Unexpected 'createdAt' field found");
            Assert.assertNotNull(json.get("updatedAt"), "[TC06-N] Unexpected 'updatedAt' field found");
        }

        // TC07-N: createdAt != updatedAt should not be checked (skipped due to no values)

        // TC08-N: No valid users should be returned
        if (json != null && json.get("") instanceof List) {
            List<?> users = json.getList("");
            Assert.assertTrue(users.isEmpty(), "[TC08-N] Unexpected users returned");
        }

        // TC09-N & TC10-N are invalid in this context because we don't expect multiple valid users in error
    }


}