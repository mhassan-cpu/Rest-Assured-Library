package testcases.households;

import testcases.TestBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import testcases.TestBase;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import testcases.TestBase;

import static io.restassured.RestAssured.given;
//import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Pattern;

//import static io.restassured.RestAssured.given;
//import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static model.CreateBookBody.getCreateBookBody;
import static model.CreateNewHousehold.getCreateHouseholdBody;
import static util.Utility.NameGenerator.generateRandomIsbn;
import static util.Utility.NameGenerator.generateRandomName;

public class TC05_DeleteHousehold extends TestBase {

    @Test(priority = 1, description = "[2.5] check delete household positive flow")
    public void checkDeleteHousehold_P() {
        // Send DELETE request
        Response response =
                given()
                        //.auth().basic("admin", "admin")
                        .auth().preemptive().basic("admin", "admin")
                        .header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV") // Only include if required by API
                        .when()
                        .delete("/households/" + HouseholdID)
                        .then()
                        .log().all()
                        //.assertThat().statusCode(204)
                        .extract().response();

        // Log response for reference
        response.prettyPrint();


        int statusCode = response.getStatusCode();
        long responseTime = response.getTime();
        String body = response.getBody().asString().trim();

        // [2.5] TC01 - Validate that status code is 204 or 404
        Assert.assertTrue(
                statusCode == 204 || statusCode == 404,
                "[TC01] Expected status code 204 or 404 but got " + statusCode
        );

        // [2.5] TC02 - Validate that response time is under 500ms
        Assert.assertTrue(
                responseTime < 500,
                "[TC02] Response time exceeded 500ms: " + responseTime + "ms"
        );

        // [2.5] TC03 - Validate response body is empty
        Assert.assertTrue(
                body.isEmpty(),
                "[TC03] Expected empty response body but got: " + body
        );

        // [2.5] TC04 - Validate that no JSON parsing errors occur
        if (!body.isEmpty()) {
            try {
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(body);
            } catch (Exception e) {
                Assert.fail("[TC04] Response body is not valid JSON: " + e.getMessage());
            }
        }
    }


    @Test(priority = 2, description = "[2.5] check delete household negative flow")
    public void checkDeleteHousehold_N() {
        // Intentionally use an invalid or non-existent household ID to simulate failure
        String invalidHouseholdId = "nonexistent-id-xyz";

        Response response =
                given()
                        .auth().preemptive().basic("admin", "admin")
                        .header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV") // Adjust if optional/required
                        .when()
                        .delete("/households/" + invalidHouseholdId)
                        .then()
                        .log().all()
                        .extract().response();

        int statusCode = response.getStatusCode();
        long responseTime = response.getTime();
        String body = response.getBody().asString().trim();

        // [2.5] TC01 - Validate that status code is NOT 204 (expecting 404 or 400)
        Assert.assertNotEquals(
                statusCode,
                204,
                "[TC01-N] Unexpected success with 204 for invalid ID"
        );

        // [2.5] TC02 - Validate response time is still under 500ms
        Assert.assertTrue(
                responseTime < 500,
                "[TC02-N] Response time exceeded 500ms: " + responseTime + "ms"
        );

        // [2.5] TC03 - Validate response body is NOT empty (expecting error message)
        Assert.assertFalse(
                body.isEmpty(),
                "[TC03-N] Expected non-empty error response body but found empty"
        );

        // [2.5] TC04 - Validate JSON parsing still works on error response
        try {
            new com.fasterxml.jackson.databind.ObjectMapper().readTree(body);
        } catch (Exception e) {
            Assert.fail("[TC04-N] Response body is not valid JSON: " + e.getMessage());
        }
    }

}
