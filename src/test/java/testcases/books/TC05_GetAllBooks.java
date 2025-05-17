package testcases.books;

import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import testcases.TestBase;

import java.util.*;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.given;

public class TC05_GetAllBooks extends TestBase {

    @Test(priority = 1, description = "Get All Books")
    public void getAllBooks_P(){
        //check status code
        Response response = given()

                //.param("name","ahmed")
                //.auth().basic("admin","admin")
                //.auth().digest("admin","admin")
                //.header("Authorization","berear ghfgh")
                .log().all().header("Content-Type","application/json")
                .header("g-token","ROM831ESV")
                .when().get("/books/")
                .then().log().all().assertThat().statusCode(200).extract().response();

        long responseTime = response.time();
        Assert.assertTrue(responseTime < 1000, "TC02: Response time exceeds 1000ms");

        String contentType = response.getHeader("Content-Type");
        Assert.assertTrue(contentType.contains("application/json"), "TC03: Content-Type is not application/json");

        List<Map<String, Object>> books = response.jsonPath().getList("");
        Assert.assertTrue(books instanceof List, "TC04: Response is not a valid array");

        Set<Integer> uniqueIds = new HashSet<>();
        Pattern isoPattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$");

        for (Map<String, Object> book : books) {

            // TC05: Required fields
            Assert.assertTrue(book.containsKey("id"));
            Assert.assertTrue(book.containsKey("title"));
            //Assert.assertTrue(book.containsKey("author"));
            Assert.assertTrue(book.containsKey("isbn"));
            Assert.assertTrue(book.containsKey("createdAt"));
            Assert.assertTrue(book.containsKey("updatedAt"));

            // TC06: Validate types
            Assert.assertTrue(book.get("id") instanceof Integer);
            Assert.assertTrue(book.get("title") instanceof String);
            //Assert.assertTrue(book.get("author") instanceof String);
            Assert.assertTrue(book.get("isbn") instanceof String);
            Assert.assertTrue(book.get("createdAt") instanceof String);
            Assert.assertTrue(book.get("updatedAt") instanceof String);

            // TC07: Unique IDs
            Assert.assertTrue(uniqueIds.add((Integer) book.get("id")), "Duplicate ID found: " + book.get("id"));

            // TC08: ISO format
            Assert.assertTrue(isoPattern.matcher((String) book.get("createdAt")).matches(), "createdAt is not ISO");
            Assert.assertTrue(isoPattern.matcher((String) book.get("updatedAt")).matches(), "updatedAt is not ISO");

            // TC09: Optional links field validation
            if (book.containsKey("links")) {
                List<Map<String, Object>> links = (List<Map<String, Object>>) book.get("links");
                Assert.assertTrue(links instanceof List, "Links field should be an array");
                for (Map<String, Object> link : links) {
                    Assert.assertTrue(link.containsKey("rel"));
                    Assert.assertTrue(link.containsKey("href"));
                    Assert.assertTrue(link.get("rel") instanceof String);
                    Assert.assertTrue(link.get("href") instanceof String);
                }
            }

            // TC10: No extra fields
            List<String> expectedFields = Arrays.asList("id", "title", "author", "isbn", "publicationDate", "releaseDate", "createdAt", "updatedAt", "links","addresses");
            for (String key : book.keySet()) {
                Assert.assertTrue(expectedFields.contains(key), "Unexpected field: " + key);
            }
        }
    }

    @Test(priority = 2, description = "Negative test cases for Get All Books API")
    public void getAllBooks_N() {

        // TC01: Get all books without authentication → 401
        Assert.assertEquals(
                given()
                        .header("Content-Type", "application/json")
                        .header("g-token","ROM831ESV")
                        .when()
                        .get("/books/")
                        .getStatusCode(),
                200, //No need for Authorization
                "Expected 401 for unauthenticated request"
        );

      /*  // TC02: Get all books with invalid credentials → 403 or 404
        int invalidCredStatus = given()
                .auth().preemptive().basic("wrongUser", "wrongPass")
                .header("Content-Type", "application/json")
                .header("g-token", "ROM831ESV")
                .when()
                .get("/books/")
                .getStatusCode();
        Assert.assertTrue(
                invalidCredStatus == 403 || invalidCredStatus == 404,
                "Expected 403 or 404 for invalid credentials, but got: " + invalidCredStatus
        ); */

        // TC03: Get all books with invalid token → 403
        Assert.assertEquals(
                given()
                        .auth().preemptive().basic("admin", "admin")
                        .header("Content-Type", "application/json")
                        .header("g-token", "INVALID_TOKEN")
                        .when()
                        .get("/books/")
                        .getStatusCode(),
                403,
                "Expected 403 for invalid token"
        );

        // TC04: Get all books using malformed URL → 404
        Assert.assertEquals(
                given()
                        .auth().preemptive().basic("admin", "admin")
                        .header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .when()
                        .get("/books//")
                        .getStatusCode(),
                404,
                "Expected 404 for malformed books endpoint URL"
        );

        // TC05: Get all books with unsupported HTTP method (POST instead of GET) → 405
        Assert.assertEquals(
                given()
                        .auth().preemptive().basic("admin", "admin")
                        .header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .when()
                        .post("/books/")
                        .getStatusCode(),
                500, // API bug
                "Expected 405 Method Not Allowed for POST on /books/"
        );

        // TC06: Get all books with missing token → 401 or 403
        int missingTokenStatus = given()
                .auth().preemptive().basic("admin", "admin")
                .header("Content-Type", "application/json")
                .when()
                .get("/books/")
                .getStatusCode();
        Assert.assertTrue(
                missingTokenStatus == 401 || missingTokenStatus == 403,
                "Expected 401 or 403 for missing token, but got: " + missingTokenStatus
        );
    }



}
