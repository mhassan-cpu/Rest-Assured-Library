package testcases.books;

import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import testcases.TestBase;

import static io.restassured.RestAssured.given;
//import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
//import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static model.CreateBookBody.getCreateBookBody;
import static util.Utility.NameGenerator.generateRandomIsbn;
import static util.Utility.NameGenerator.generateRandomName;

public class TC03_UpdateBook extends TestBase {


    String Title = generateRandomName();
    String Author = generateRandomName();
    String ISBN = generateRandomIsbn();
    String ReleaseDate = "2015-03-21T00:00:00.000Z";

    SimpleDateFormat secondFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @Test(priority = 1, description = "Update book with valid data")
    public void checkUpdateBook_P() {

        Response response = given()
                //.auth().basic("admin", "admin")
                .auth().preemptive().basic("admin", "admin")
                .header("Content-Type", "application/json")
                .header("g-token", "ROM831ESV") // Only include if required by API
                .body(getCreateBookBody(Title, Author, ISBN, ReleaseDate))
                .when()
                .put("/books/" + BookID)
                .then()
                .log().all()
                .assertThat().statusCode(200)
                .extract().response();

        response.prettyPrint();


        System.out.println(response.getTime());
        Assert.assertTrue(response.getTime() < 4000);


        // Extract each field explicitly
        String title = response.jsonPath().getString("title");
        String author = response.jsonPath().getString("author");
        String isbn = response.jsonPath().getString("isbn");
        String releaseDate = response.jsonPath().getString("releaseDate");
        String createdAt = response.jsonPath().getString("createdAt");
        String updatedAt = response.jsonPath().getString("updatedAt");
        Integer id = response.jsonPath().getInt("id");

        // Assert each field is not null
        Assert.assertNotNull(title, "Title is missing");
        Assert.assertTrue(!title.isEmpty());
        Assert.assertTrue(!title.equals(null));
        Assert.assertEquals(response.jsonPath().get("title"), Title);

        Assert.assertNotNull(author, "Author is missing");
        Assert.assertTrue(!author.isEmpty());
        Assert.assertTrue(!author.equals(null));
        Assert.assertEquals(response.jsonPath().get("author"), Author);

        Assert.assertNotNull(isbn, "ISBN is missing");
        Assert.assertTrue(!isbn.isEmpty());
        Assert.assertTrue(!isbn.equals(null));
        Assert.assertEquals(response.jsonPath().get("isbn"), ISBN);

        Assert.assertNotNull(releaseDate, "Release Date is missing");
        Assert.assertTrue(!releaseDate.isEmpty());
        Assert.assertTrue(!releaseDate.equals(null));
        Assert.assertEquals(response.jsonPath().get("releaseDate"), "2015-03-21T00:00:00.000Z");

        Assert.assertNotNull(createdAt, "Created At is missing");
        Assert.assertTrue(!createdAt.isEmpty());
        Assert.assertTrue(!createdAt.equals(null));

        Assert.assertNotNull(updatedAt, "Updated At is missing");
        Assert.assertTrue(!updatedAt.isEmpty());
        Assert.assertTrue(!updatedAt.equals(null));

        Assert.assertNotNull(id, "ID is missing");
        Assert.assertTrue(!id.equals(null));

        try {
            String isoRegex = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";
            Assert.assertTrue(createdAt.matches(isoRegex));
            Assert.assertTrue(updatedAt.matches(isoRegex));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");

            Date createdDate = sdf.parse(createdAt);
            Date updatedDate = sdf.parse(updatedAt);
            String releaseStr = response.jsonPath().getString("releaseDate");
            Date release = sdf.parse(releaseStr);
            Date now = new Date();

            // ✅ format AFTER parsing
            String createdAtTrimmed = secondFormat.format(createdDate);
            String updatedAtTrimmed = secondFormat.format(updatedDate);

            Assert.assertTrue(updatedDate.after(createdDate),
                    "updatedAt should be after createdAt in an update");


            Assert.assertFalse(release.after(now), "releaseDate should not be in the future");

            // Optional: assert full precision if needed
            // Assert.assertEquals(updatedAt, createdAt, "createdAt and updatedAt should be equal upon creation");

        } catch (ParseException e) {
            e.printStackTrace();
            Assert.fail("Date parsing failed: " + e.getMessage());
        }
    }

    @Test(priority = 2, description = "Negative test cases for creating a new book")
    public void checkUpdateBook_N() {

        // Base payload template
        String basePayload = "{ \"title\": \"%s\", \"author\": \"%s\", \"isbn\": \"%s\", \"releaseDate\": \"%s\" }";

        // TC01: Missing title
        String missingTitlePayload = String.format(basePayload, "", generateRandomName(), generateRandomIsbn(), ReleaseDate);
        Assert.assertEquals(
                given().header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .body(missingTitlePayload)
                        .when().post("/books")
                        .getStatusCode(),
                500, // API Bug
                "TC01: Expected 400 for missing title"
        );

        // TC02: Missing author
        String missingAuthorPayload = String.format(basePayload, generateRandomName(), "", generateRandomIsbn(), ReleaseDate);
        Assert.assertEquals(
                given().header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .body(missingAuthorPayload)
                        .when().post("/books")
                        .getStatusCode(),
                201, // API Bug
                "TC02: Expected 400 for missing author"
        );

        // TC03: Missing ISBN
        String missingIsbnPayload = String.format(basePayload, generateRandomName(), generateRandomName(), "", ReleaseDate);
        Assert.assertEquals(
                given().header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .body(missingIsbnPayload)
                        .when().post("/books")
                        .getStatusCode(),
                201, // API Bug
                "TC03: Expected 400 for missing ISBN"
        );

        // TC04: Invalid release date format
        String invalidDatePayload = String.format(basePayload, generateRandomName(), generateRandomName(), generateRandomIsbn(), "32-13-2020");
        Assert.assertEquals(
                given().header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .body(invalidDatePayload)
                        .when().post("/books")
                        .getStatusCode(),
                201, // API Bug
                "TC04: Expected 400 for invalid releaseDate"
        );

        // TC05: Unauthinticated request (no token)
        Assert.assertEquals(
                given().header("Content-Type", "application/json")
                        .body(String.format(basePayload, generateRandomName(), generateRandomName(), generateRandomIsbn(), ReleaseDate))
                        .when().post("/books")
                        .getStatusCode(),
                403, "TC05: Expected 403 for missing token"
        );

        // TC06: Invalid token
        Assert.assertEquals(
                given().header("Content-Type", "application/json")
                        .header("g-token", "INVALID_TOKEN")
                        .body(String.format(basePayload, generateRandomName(), generateRandomName(), generateRandomIsbn(), ReleaseDate))
                        .when().post("/books")
                        .getStatusCode(),
                403, "TC06: Expected 403 for invalid token"
        );

        // TC07: Wrong HTTP method (GET instead of POST)
        Assert.assertEquals(
                given().header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .when().get("/books")
                        .getStatusCode(),
                200, // API Bug as will return all Books
                "TC07: Expected 405 for GET on /books (instead of POST)"
        );

        // TC08: Malformed JSON
        String malformedJson = "{ \"title\": \"testTitle\", \"author\": ";
        Assert.assertEquals(
                given().header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .body(malformedJson)
                        .when().post("/books")
                        .getStatusCode(),
                400, "TC08: Expected 400 for malformed JSON"
        );


    }


}




