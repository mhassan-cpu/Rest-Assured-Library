package testcases.books;

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

import static io.restassured.RestAssured.given;
import static model.CreateBookBody.getCreateBookBody;
import static util.Utility.NameGenerator.*;

public class TC01_CreateNewBook extends TestBase {

    //String author = faker.name().fullName();
    //String author = generateRandomName();
    //String author = getSingleJsonData(System.getProperty("user.dir")+"/src/test/resources/data/BookTestData.json","author");
    //String author = getExcelData(0,0,"ورقة1");
    String Title = generateRandomName();
    String Author = generateRandomName();
    String ISBN = generateRandomIsbn();
    String ReleaseDate = "2015-03-21T00:00:00.000Z";

    SimpleDateFormat secondFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");


    public TC01_CreateNewBook() throws IOException, ParseException, org.json.simple.parser.ParseException {
    }


    // define base url

    @Test(priority = 1, description = "Create new book with valid data" , retryAnalyzer = Retry.class)
    public void checkCreateNewBook_P() throws JsonProcessingException {



       /*JSONObject CreateRequestBody = new JSONObject();
        CreateRequestBody.put("title",generateRandomName());
        CreateRequestBody.put("author",generateRandomName());
        CreateRequestBody.put("isbn","test isbn");
        CreateRequestBody.put("releaseDate","11-11-2025");

        JSONArray arr = new JSONArray();
        JSONObject address1 = new JSONObject();
        JSONObject address2 = new JSONObject();
        address1.put("address","address 1");
        address2.put("address","address 2");
        arr.add(address1);
        arr.add(address2);
        CreateRequestBody.put("addresses",arr);*/

        //CreateBook CreateBookBody = new CreateBook();
        //CreateBookBody.setTitle(generateRandomName()).setAuthor(generateRandomName()).setIsbn("test isbn").setReleaseDate("2010-04-15T00:00:00.000Z");

        //ObjectMapper mapper = new ObjectMapper();


        //check status code
        Response response = given()

                //.param("name","ahmed")
                //.auth().basic("admin","admin")
                //.auth().digest("admin","admin")
                //.header("Authorization", "berear ghfgh")
                .log().all().header("Content-Type", "application/json")
                .header("g-token", "ROM831ESV")
                .body(getCreateBookBody(Title, Author, ISBN, ReleaseDate))
                .when().post("/books")
                .then().log().all().assertThat().statusCode(201).extract().response();
        response.prettyPrint();


        // check response time
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

        //Soft assert


       /* SoftAssert softassert = new SoftAssert();
        softassert.assertNotNull(title, "Title is missing");
        softassert.assertTrue(!title.isEmpty());
        softassert.assertTrue(!title.equals(null));
        softassert.assertEquals(response.jsonPath().get("title"), Title);

*/

        /////

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

            Assert.assertEquals(updatedAtTrimmed, createdAtTrimmed,
                    "createdAt and updatedAt should be equal up to seconds on creation");

            Assert.assertFalse(release.after(now), "releaseDate should not be in the future");

            // Optional: assert full precision if needed
            // Assert.assertEquals(updatedAt, createdAt, "createdAt and updatedAt should be equal upon creation");

        } catch (ParseException e) {
            e.printStackTrace();
            Assert.fail("Date parsing failed: " + e.getMessage());
        }

        // get BookID
        BookID = response.jsonPath().getInt("id");

    }

    @Test(priority = 2, description = "Negative test cases for creating a new book")
    public void checkCreateNewBook_N() {

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

      /*  // TC09: Missing Content-Type header
        Assert.assertEquals(
                given()
                        .header("g-token", "ROM831ESV")
                        .body(String.format(basePayload, generateRandomName(), generateRandomName(), generateRandomIsbn(), ReleaseDate))
                        .when().post("/books")
                        .getStatusCode(),
                415, // API Bug
                "TC09: Expected 415 Unsupported Media Type (missing Content-Type)"
        );  */
    }

}
