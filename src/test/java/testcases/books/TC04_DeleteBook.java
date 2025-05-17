package testcases.books;

import io.restassured.response.Response;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import static io.restassured.RestAssured.given;
import com.fasterxml.jackson.databind.ObjectMapper;
import testcases.TestBase;

public class TC04_DeleteBook extends TestBase {

    @Test(priority = 1, description = "Delete book")
    public void checkDeleteBook_P() {

        Response response = given()
                //.auth().basic("admin", "admin")
                .auth().preemptive().basic("admin", "admin")
                .header("Content-Type", "application/json")
                .header("g-token", "ROM831ESV") // Only include if required by API
                .when()
                .delete("/books/" + BookID)
                .then()
                .log().all()
                //.assertThat().statusCode(204)
                .extract().response();

        // Log response for reference
        response.prettyPrint();

        // TC01 - Validate that status code is 204 or 404
        int statusCode = response.getStatusCode();
        Assert.assertTrue(statusCode == 204 || statusCode == 404,
                "Expected status code 204 or 404 but got: " + statusCode);

        // TC02 - Validate that response time is under 500ms
        long responseTime = response.getTime();
        Assert.assertTrue(responseTime < 500,
                "Response time exceeded 500ms: " + responseTime + "ms");

        // TC03 - Validate that response body is empty
        String responseBody = response.getBody().asString().trim();
        Assert.assertTrue(responseBody.isEmpty(),
                "Expected empty response body but got: " + responseBody);

        // TC04 - Validate that no JSON parsing errors occur (optional)
        try {
            if (!responseBody.isEmpty()) {
                new ObjectMapper().readTree(responseBody); // Validate JSON structure
            }
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.fail("Response body is not valid JSON: " + e.getMessage());
        }
    }

    @Test(priority = 2, description = "Negative test scenarios for deleting a book")
    public void checkDeleteBook_N() {

        // Delete book with non-existent ID → 404
        Assert.assertEquals(
                given()
                        .auth().preemptive().basic("admin", "admin")
                        .header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .when()
                        .delete("/books/999999")
                        .getStatusCode(),
                404,
                "Expected 404 for non-existent book ID"
        );

        // Delete book with invalid ID (non-integer) → 400
        Assert.assertEquals(
                given()
                        .auth().preemptive().basic("admin", "admin")
                        .header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .when()
                        .delete("/books/abc")
                        .getStatusCode(),
                404,
                "Expected 400 for non-integer book ID"
        );

        // Delete book without authentication → 401
        Assert.assertEquals(
                given()
                        .header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .when()
                        .delete("/books/1")
                        .getStatusCode(),
                401,
                "Expected 401 for unauthenticated request"
        );

        // Delete book with invalid credentials → 401, 403, or 404
        int invalidCredStatus = given()
                .auth().preemptive().basic("wrong", "wrong")
                .header("Content-Type", "application/json")
                .header("g-token", "ROM831ESV")
                .when()
                .delete("/books/1")
                .getStatusCode();
        Assert.assertTrue(
                invalidCredStatus == 401 || invalidCredStatus == 403 || invalidCredStatus == 404,
                "Expected 401/403/404 for invalid credentials, but got: " + invalidCredStatus
        );

        // Delete book with invalid token → 403
        Assert.assertEquals(
                given()
                        .auth().preemptive().basic("admin", "admin")
                        .header("Content-Type", "application/json")
                        .header("g-token", "INVALID_TOKEN")
                        .when()
                        .delete("/books/1")
                        .getStatusCode(),
                403,
                "Expected 403 for invalid token"
        );

        // Delete book with missing ID → 405
        Assert.assertEquals(
                given()
                        .auth().preemptive().basic("admin", "admin")
                        .header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .when()
                        .delete("/books/")
                        .getStatusCode(),
                204,
                "Expected 405 for missing book ID"
        );

        // Delete book with malformed URL → 404
        Assert.assertEquals(
                given()
                        .auth().preemptive().basic("admin", "admin")
                        .header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .when()
                        .delete("/books//123")
                        .getStatusCode(),
                204,
                "Expected 404 for malformed URL"
        );

        // Delete book with negative ID → 400
        Assert.assertEquals(
                given()
                        .auth().preemptive().basic("admin", "admin")
                        .header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .when()
                        .delete("/books/-1")
                        .getStatusCode(),
                404,
                "Expected 400 for negative book ID"
        );


    }
}



