package testcases.books;

import io.restassured.response.Response;
import org.testng.annotations.Test;
import testcases.TestBase;

import static io.restassured.RestAssured.given;
//import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
//import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;

public class TC06_TestSchema extends TestBase {

    @Test(priority = 1, description = "test schema")
    public void testSchemaValidation_P() {
        given()
                .auth().basic("admin", "admin")
                .header("Content-Type", "application/json")
                .header("g-token", "ROM831ESV")
                .when().get("/books/" + BookID)
                .then()
                .log().all()
                .assertThat();
//                .body(matchesJsonSchemaInClasspath("schemas/book-schema.json"));
    }
    }


