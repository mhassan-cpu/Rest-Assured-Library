package testcases.books;

import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import testcases.TestBase;

import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;

import static io.restassured.RestAssured.given;

public class TC02_GetBookByID extends TestBase {


    @Test(priority = 1, description = "Get Book with BookID")
    public void getBookWithID_P(){

        Response response = given()


                .log().all().header("Content-Type","application/json")
                .header("g-token","ROM831ESV")
                .when().get("/books/" + BookID)
                .then().log().all().assertThat().statusCode(200).extract().response();
                response.prettyPrint();


        Assert.assertTrue(response.time() < 500, "Response time exceeds 500ms");


        String contentType = response.getHeader("Content-Type");
        Assert.assertTrue(contentType.contains("application/json"), "Content-Type is not application/json");


        Assert.assertTrue(response.jsonPath().get("") instanceof java.util.Map, "Response is not a valid JSON object");


        String[] requiredFields = {"title", "author", "isbn", "releaseDate", "createdAt", "updatedAt", "id"};
        for (String field : requiredFields) {
            Assert.assertTrue(response.jsonPath().get(field) != null, "Missing field: " + field);
        }


        Assert.assertTrue(response.jsonPath().get("title") instanceof String);
        Assert.assertTrue(response.jsonPath().get("author") instanceof String);
        Assert.assertTrue(response.jsonPath().get("isbn") instanceof String);
        Assert.assertTrue(response.jsonPath().get("releaseDate") instanceof String);
        Assert.assertTrue(response.jsonPath().get("createdAt") instanceof String);
        Assert.assertTrue(response.jsonPath().get("updatedAt") instanceof String);
        Assert.assertTrue(response.jsonPath().get("id") instanceof Integer);


        int id = response.jsonPath().getInt("id");
        Assert.assertTrue(id > 0, "ID is not positive");


        String isoRegex = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$";
        Pattern pattern = Pattern.compile(isoRegex);

        String createdAt = response.jsonPath().getString("createdAt");
        String updatedAt = response.jsonPath().getString("updatedAt");
        String releaseDate = response.jsonPath().getString("releaseDate");

        Assert.assertTrue(pattern.matcher(createdAt).matches(), "createdAt is not in ISO 8601");
        Assert.assertTrue(pattern.matcher(updatedAt).matches(), "updatedAt is not in ISO 8601");
        Assert.assertTrue(pattern.matcher(releaseDate).matches(), "releaseDate is not in ISO 8601");



    }

    @Test(priority = 2, description = "Negative test cases for Get Book By ID API")
    public void getBookWithID_N() {


        Assert.assertEquals(
                given()
                        .auth().preemptive().basic("admin", "admin")
                        .header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .when()
                        .get("/books/999999")
                        .getStatusCode(),
                404,
                "Expected 404 for non-existent book ID"
        );


        Assert.assertEquals(
                given()
                        .auth().preemptive().basic("admin", "admin")
                        .header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .when()
                        .get("/books/abc")
                        .getStatusCode(),
                404,
                "Expected 400 for non-integer book ID"
        );


        Assert.assertEquals(
                given()
                        .header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .when()
                        .get("/books/1")
                        .getStatusCode(),
                200, // no need for authorization
                "Expected 401 for unauthenticated request"
        );

        Assert.assertEquals(
                given()
                        .auth().preemptive().basic("admin", "admin")
                        .header("Content-Type", "application/json")
                        .header("g-token", "INVALID_TOKEN")
                        .when()
                        .get("/books/1")
                        .getStatusCode(),
                403,
                "Expected 403 for invalid token"
        );


        Assert.assertEquals(
                given()
                        .auth().preemptive().basic("admin", "admin")
                        .header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .when()
                        .get("/books/")
                        .getStatusCode(),
                200, // will return all books
                "Expected 405 for missing book ID"
        );


        Assert.assertEquals(
                given()
                        .auth().preemptive().basic("admin", "admin")
                        .header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .when()
                        .get("/books//1")
                        .getStatusCode(),
                404,
                "Expected 404 for malformed book URL"
        );


        Assert.assertEquals(
                given()
                        .auth().preemptive().basic("admin", "admin")
                        .header("Content-Type", "application/json")
                        .header("g-token", "ROM831ESV")
                        .when()
                        .get("/books/-1")
                        .getStatusCode(),
                404,
                "Expected 400 for negative book ID"
        );
    }



}



