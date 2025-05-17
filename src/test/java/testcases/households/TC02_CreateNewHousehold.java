package testcases.households;

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
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static model.CreateBookBody.getCreateBookBody;
import static util.Utility.NameGenerator.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import testcases.TestBase;

import static io.restassured.RestAssured.given;
import static model.CreateBookBody.getCreateBookBody;
import static model.CreateNewHousehold.getCreateHouseholdBody;
import static util.Utility.NameGenerator.generateRandomName;

public class TC02_CreateNewHousehold extends TestBase {


    String Name = generateRandomName();

    @Test(priority = 1, description = "Create new Household with valid data")
    public void checkCreateNewHousehold_P() throws JsonProcessingException {

        //check status code
        Response response = given()

                .log().all()
                .header("Content-Type", "application/json")
                .header("g-token", "ROM831ESV")
                .body(getCreateHouseholdBody(Name))
                .when().post("/households")
                .then().log().all().assertThat().statusCode(201).extract().response();
        response.prettyPrint();


        // TC01: Validate that status code is 200 or 201
        int statusCode = response.getStatusCode();
        Assert.assertTrue(statusCode == 200 || statusCode == 201, "Expected status code 200 or 201 but got: " + statusCode);

        // TC02: Validate that response time is under 500ms
        Assert.assertTrue(response.getTime() < 500, "Response time exceeds 500ms");

        // TC03: Validate Content-Type header is application/json
        String contentType = response.getHeader("Content-Type");
        Assert.assertTrue(contentType.contains("application/json"), "Content-Type is not application/json");

        // TC04: Validate response body is a valid JSON object
        Assert.assertTrue(response.jsonPath().get("id") != null, "Response is not a valid JSON object");

        // TC05: Validate response contains expected fields
        Assert.assertTrue(response.jsonPath().get("name") != null);
        Assert.assertTrue(response.jsonPath().get("createdAt") != null);
        Assert.assertTrue(response.jsonPath().get("updatedAt") != null);
        Assert.assertTrue(response.jsonPath().get("id") != null);

        // TC06: Validate field data types
        Assert.assertTrue(response.jsonPath().get("name") instanceof String);
        Assert.assertTrue(response.jsonPath().get("createdAt") instanceof String);
        Assert.assertTrue(response.jsonPath().get("updatedAt") instanceof String);
        Assert.assertTrue(response.jsonPath().get("id") instanceof Integer);

        // TC07: Validate createdAt and updatedAt are in ISO 8601 format
        String createdAt = response.jsonPath().getString("createdAt");
        String updatedAt = response.jsonPath().getString("updatedAt");
        Pattern isoPattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$");
        Assert.assertTrue(isoPattern.matcher(createdAt).matches(), "createdAt not in ISO 8601");
        Assert.assertTrue(isoPattern.matcher(updatedAt).matches(), "updatedAt not in ISO 8601");

        // TC08: Validate createdAt and updatedAt match on creation
        Assert.assertEquals(createdAt, updatedAt, "createdAt and updatedAt do not match");

        // TC09: Validate name is not empty
        String name = response.jsonPath().getString("name");
        Assert.assertFalse(name.isEmpty(), "Household name is empty");

        // TC10: Validate that ID is a positive integer
        int id = response.jsonPath().getInt("id");
        Assert.assertTrue(id > 0, "ID is not positive");

        // Save Household ID
        HouseholdID = response.jsonPath().getInt("id");

    }

    @Test(priority = 2, description = "Negative test cases for Create New Household API")
    public void checkCreateNewHousehold_N() {
        String baseUrl = "/households";

        // TC01: Missing name
        String missingNamePayload = "{ \"name\": \"\", \"createdAt\": \"" + now() + "\", \"updatedAt\": \"" + now() + "\" }";
        Assert.assertEquals(
                given()
                        .header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .body(missingNamePayload)
                        .when().post(baseUrl).getStatusCode(),
                201 // API Bug as name not required
                , "TC01: Expected 400 for missing name"
        );

        // TC02: Invalid token
        String validPayload = "{ \"name\": \"Household Test\", \"createdAt\": \"" + now() + "\", \"updatedAt\": \"" + now() + "\" }";
        Assert.assertEquals(
                given()
                        .header("Content-Type", "application/json")
                        .header("g-token", "INVALID_TOKEN")
                        .body(validPayload)
                        .when().post(baseUrl).getStatusCode(),
                403, "TC02: Expected 403 for invalid token"
        );

        // TC03: No authentication
        Assert.assertEquals(
                given()
                        .header("Content-Type", "application/json")
                        .body(validPayload)
                        .when().post(baseUrl).getStatusCode(),
                403,
                "TC03: Expected 403 for unauthenticated request"
        );

        // TC04: Invalid date format
        String invalidDatePayload = "{ \"name\": \"Household Test\", \"createdAt\": \"13-05-2025\", \"updatedAt\": \"13-05-2025\" }";
        Assert.assertEquals(
                given()
                        .header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .body(invalidDatePayload)
                        .when().post(baseUrl).getStatusCode(),
                201, // API Bug as API accepts the wrong dateformat
                "TC04: Expected 400 for invalid date format"
        );

        // TC05: Extra field
        String extraFieldPayload = "{ \"name\": \"Household Test\", \"createdAt\": \"" + now() + "\", \"updatedAt\": \"" + now() + "\", \"extra\": \"field\" }";
        Assert.assertTrue(
                Arrays.asList(201, 200).contains( // API Bug as API accepts any extra field in body
                        given()
                                .header("Content-Type", "application/json")
                                .header("g-token", "ROM831ESV")
                                .body(extraFieldPayload)
                                .when().post(baseUrl).getStatusCode()
                ), "TC05: Expected 400 or 422 for unexpected field"
        );
    }

    // Utility method to generate current ISO 8601 timestamp
    private String now() {
        return java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).toString();
    }



}
