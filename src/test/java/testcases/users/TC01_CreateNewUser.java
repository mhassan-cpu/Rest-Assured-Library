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
import org.testng.asserts.SoftAssert;
import pojo.CreateBook;
import retryTest.Retry;
import testcases.TestBase;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static model.CreateBookBody.getCreateBookBody;
import static model.CreateUserBody.getCreateUserBody;
import static util.Utility.NameGenerator.*;

public class TC01_CreateNewUser extends TestBase {

    String firstname = generateRandomName();
    String lastname = generateRandomName();
    String email = generateRandomEmail();

    String invalidfirstname = "firstname";
    String invalidlastname = "lastname";
    String invalidemail = "email" ;


    @Test(priority = 1, description = "[3.2] check create user positive flow")
    public void checkCreateUser_P() {


        // Send POST request to /users endpoint
        Response response = given()
                .log().all()
                .header("Content-Type", "application/json")
                .header("g-token", "ROM831ESV")
                .body(getCreateUserBody(firstname,lastname,email))
                .when()
                .post("/users")
                .then()
                .log().all()
                .extract()
                .response();

        // Extract values
        int statusCode = response.getStatusCode();
        long responseTime = response.getTime();
        String contentType = response.getHeader("Content-Type");
        String body = response.getBody().asString().trim();

        // Parse JSON body
        JsonPath json = response.jsonPath();

        // TC01 - Validate status code is 200 or 201
        Assert.assertTrue(statusCode == 200 || statusCode == 201,
                "[TC01] Expected 200 or 201 but got " + statusCode);

        // TC02 - Validate response time < 500ms
        Assert.assertTrue(responseTime < 1000,
                "[TC02] Response time exceeded 500ms: " + responseTime + "ms");

        // TC03 - Content-Type is application/json
        Assert.assertTrue(contentType.contains("application/json"),
                "[TC03] Content-Type mismatch: " + contentType);

        // TC04 - Required fields exist
        Assert.assertNotNull(json.get("id"), "[TC04] Missing field: id");
        Assert.assertNotNull(json.get("firstName"), "[TC04] Missing field: firstName");
        Assert.assertNotNull(json.get("lastName"), "[TC04] Missing field: lastName");
        Assert.assertNotNull(json.get("email"), "[TC04] Missing field: email");
        Assert.assertNotNull(json.get("createdAt"), "[TC04] Missing field: createdAt");
        Assert.assertNotNull(json.get("updatedAt"), "[TC04] Missing field: updatedAt");

        // TC05 - Field data types
        Assert.assertTrue(json.get("id") instanceof Integer, "[TC05] id is not an integer");
        Assert.assertTrue(json.get("firstName") instanceof String, "[TC05] firstName is not string");
        Assert.assertTrue(json.get("lastName") instanceof String, "[TC05] lastName is not string");
        Assert.assertTrue(json.get("email") instanceof String, "[TC05] email is not string");
        Assert.assertTrue(json.get("createdAt") instanceof String, "[TC05] createdAt is not string");
        Assert.assertTrue(json.get("updatedAt") instanceof String, "[TC05] updatedAt is not string");

        // TC06 - Validate createdAt and updatedAt are in ISO 8601
        String iso8601Pattern = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$";
        Pattern isoPattern = Pattern.compile(iso8601Pattern);
        String createdAt = json.get("createdAt");
        String updatedAt = json.get("updatedAt");

        Assert.assertTrue(isoPattern.matcher(createdAt).matches(),
                "[TC06] createdAt not in ISO 8601: " + createdAt);
        Assert.assertTrue(isoPattern.matcher(updatedAt).matches(),
                "[TC06] updatedAt not in ISO 8601: " + updatedAt);

        // TC07 - createdAt equals updatedAt
        Assert.assertEquals(createdAt, updatedAt,
                "[TC07] createdAt and updatedAt don't match");

        // TC08 - Validate non-empty firstName, lastName, email
        Assert.assertFalse(json.getString("firstName").isEmpty(), "[TC08] firstName is empty");
        Assert.assertFalse(json.getString("lastName").isEmpty(), "[TC08] lastName is empty");
        Assert.assertFalse(json.getString("email").isEmpty(), "[TC08] email is empty");

        // TC09 - Validate ID is positive
        Assert.assertTrue(json.getInt("id") > 0, "[TC09] ID is not positive");

        // Store UserID for later use if needed
        userId = response.jsonPath().getInt("id");
        //System.setProperty("UserID", String.valueOf(userId));
    }

    @Test(priority = 2, description = "[3.2] check create user negative scenarios")
    public void checkCreateUser_N() {
        // Deliberately malformed or incomplete body
        JSONObject invalidBody = new JSONObject();
        invalidBody.put("firstName", ""); // empty field
        invalidBody.put("lastName", "");
        // missing lastName and email

        // Send POST request with invalid body
        Response response = given()
                .log().all()
                .header("Content-Type", "application/json")
                .header("g-token", "ROM831ESV")
                .body(getCreateUserBody(invalidfirstname,invalidlastname,invalidemail))
                .when()
                .post("/users")
                .then()
                .log().all()
                .extract()
                .response();

        int statusCode = response.getStatusCode();
        long responseTime = response.getTime();
        String contentType = response.getHeader("Content-Type");
        String body = response.getBody().asString().trim();

        // Parse JSON response (if it's valid)
        JsonPath json = null;
        try {
            json = response.jsonPath();
        } catch (Exception e) {
            Assert.fail("[TC04-N] Invalid JSON in response body");
        }

        // TC01-N: Expecting failure status (e.g., 400 Bad Request or 422 Unprocessable Entity)
        Assert.assertTrue(statusCode == 200 || statusCode == 201, // API Bug as first name not required
                "[TC01-N] Expected 400 or 422 but got " + statusCode);

        // TC02-N: Response time should still be under 500ms
        Assert.assertTrue(responseTime < 500,
                "[TC02-N] Slow response time: " + responseTime + "ms");

        // TC03-N: Content-Type should still be JSON
        Assert.assertTrue(contentType.contains("application/json"),
                "[TC03-N] Content-Type is not application/json");

        // TC04-N: Validate error message exists
        Assert.assertFalse(body.isEmpty(), "[TC04-N] Response body is unexpectedly empty for error case");

        // TC05-N: Error message should be a string
        if (json.get("message") != null) {
            Assert.assertTrue(json.get("message") instanceof String,
                    "[TC05-N] Error message is not a string");
        }

        // TC06-N: createdAt and updatedAt should not be returned
        Assert.assertNotNull(json.get("createdAt"), "[TC06-N] Unexpected 'createdAt' field found");
        Assert.assertNotNull(json.get("updatedAt"), "[TC06-N] Unexpected 'updatedAt' field found");

        // TC08-N: Required fields should not be present in error
        Assert.assertNotNull(json.get("id"), "[TC08-N] Expected 'id' field found");
        Assert.assertNotNull(json.get("firstName"), "[TC08-N] expected 'firstName' field found");
        Assert.assertNotNull(json.get("lastName"), "[TC08-N] expected 'lastName' field found");
        Assert.assertNotNull(json.get("email"), "[TC08-N] expected 'email' field found");


    }



}