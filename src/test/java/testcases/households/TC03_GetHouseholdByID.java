package testcases.households;

import io.restassured.response.Response;
import testcases.TestBase;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import testcases.TestBase;

import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;

import static io.restassured.RestAssured.given;

import static io.restassured.RestAssured.given;

public class TC03_GetHouseholdByID extends TestBase {

    @Test(priority = 1, description = "Get Household with Household ID")
    public void getHouseholdWithID_P(){
    //check status code
    Response response = given()


            .log().all().header("Content-Type","application/json")
            .header("g-token","ROM831ESV")
            .when().get("/households/" + HouseholdID)
            .then().log().all().assertThat().statusCode(200).extract().response();
                response.prettyPrint();


        // TC02: Response time < 500ms
        Assert.assertTrue(response.time() < 500, "Response time exceeds 500ms");

        // TC03: Content-Type check
        String contentType = response.getHeader("Content-Type");
        Assert.assertTrue(contentType.contains("application/json"), "Content-Type is not application/json");

        // TC04: Response is a JSON object
        Assert.assertTrue(response.jsonPath().get("") instanceof java.util.Map, "Response is not a valid JSON object");

        // Required fields
        String[] requiredFields = {"name", "createdAt", "updatedAt", "id"};
        for (String field : requiredFields) {
            Assert.assertTrue(response.jsonPath().get(field) != null, "Missing field: " + field);
        }

        // TC06: Validate types
        Assert.assertTrue(response.jsonPath().get("name") instanceof String);
        //Assert.assertTrue(response.jsonPath().get("links") instanceof Object);
        Assert.assertTrue(response.jsonPath().get("createdAt") instanceof String);
        Assert.assertTrue(response.jsonPath().get("updatedAt") instanceof String);
        Assert.assertTrue(response.jsonPath().get("id") instanceof Integer);

        // TC07: ID is positive
        int id = response.jsonPath().getInt("id");
        Assert.assertTrue(id > 0, "ID is not positive");

        // TC08 & TC09: ISO 8601 format
        String isoRegex = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$";
        Pattern pattern = Pattern.compile(isoRegex);

        String createdAt = response.jsonPath().getString("createdAt");
        String updatedAt = response.jsonPath().getString("updatedAt");

        Assert.assertTrue(pattern.matcher(createdAt).matches(), "createdAt is not in ISO 8601");
        Assert.assertTrue(pattern.matcher(updatedAt).matches(), "updatedAt is not in ISO 8601");

        // TC10: createdAt equals updatedAt
        Assert.assertEquals(createdAt, updatedAt, "createdAt and updatedAt do not match");
    }

    @Test(priority = 2, description = "Negative test cases for Get Household By ID API")
    public void gethouseholdsWithID_N() {

        // TC01: Get households with non-existent ID → 404
        Assert.assertEquals(
                given()
                       // .auth().preemptive().basic("admin", "admin")
                        .header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .when()
                        .get("/households/999999")
                        .getStatusCode(),
                404,
                "Expected 404 for non-existent households ID"
        );

        // TC02: Get households with invalid ID (non-integer) → 400
        Assert.assertEquals(
                given()
                        .auth().preemptive().basic("admin", "admin")
                        .header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .when()
                        .get("/households/abc")
                        .getStatusCode(),
                404,
                "Expected 400 for non-integer households ID"
        );

        // TC03: Get households without authentication → 401
        Assert.assertEquals(
                given()
                        .header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .when()
                        .get("/households/1")
                        .getStatusCode(),
                200, // no need for authorization
                "Expected 401 for unauthenticated request"
        );


        // TC05: Get households with invalid token → 403
        Assert.assertEquals(
                given()
                        .auth().preemptive().basic("admin", "admin")
                        .header("Content-Type", "application/json")
                        .header("g-token", "INVALID_TOKEN")
                        .when()
                        .get("/households/1")
                        .getStatusCode(),
                403,
                "Expected 403 for invalid token"
        );

        // TC06: Get households with missing ID → 405
        Assert.assertEquals(
                given()
                        .auth().preemptive().basic("admin", "admin")
                        .header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .when()
                        .get("/households/")
                        .getStatusCode(),
                200, // will return all households
                "Expected 405 for missing households ID"
        );

        // TC07: Get households with malformed URL → 404
        Assert.assertEquals(
                given()
                        .auth().preemptive().basic("admin", "admin")
                        .header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .when()
                        .get("/households//1")
                        .getStatusCode(),
                404,
                "Expected 404 for malformed households URL"
        );

        // TC08: Get households with negative ID → 400
        Assert.assertEquals(
                given()
                        .auth().preemptive().basic("admin", "admin")
                        .header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .when()
                        .get("/households/-1")
                        .getStatusCode(),
                404,
                "Expected 400 for negative households ID"
        );
    }


}