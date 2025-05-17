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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static model.CreateBookBody.getCreateBookBody;
import static model.CreateUserBody.getCreateUserBody;
import static util.Utility.NameGenerator.*;

public class TC05_DeleteWishlist extends TestBase {

    @Test(priority = 1, description = "[2.5] check delete wishlist positive flow")
    public void checkDeleteWishlist_P() {
        // Send DELETE request
        Response response =
                given()
                        //.auth().basic("admin", "admin")
                        .auth().preemptive().basic("admin", "admin")
                        .header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV") // Only include if required by API
                        .when()
                        .delete("/wishlists/" + wishlistID)
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


    @Test(priority = 2, description = "[2.5] check delete wishlist negative flow")
    public void checkDeleteWishlist_N() {
        // Intentionally use an invalid or non-existent household ID to simulate failure
        String invalidWishlistId = "nonexistent-id-xyz";

        Response response =
                given()
                        .auth().preemptive().basic("admin", "admin")
                        .header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV") // Adjust if optional/required
                        .when()
                        .delete("/wishlists/" + invalidWishlistId)
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
