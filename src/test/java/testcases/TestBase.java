package testcases;

import com.aventstack.chaintest.plugins.ChainTestListener;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Parameters;
import org.testng.asserts.SoftAssert;


@Listeners(ChainTestListener.class)
public class TestBase {


    public static int BookID;

    public static int HouseholdID;

    public static int userId;

    public static int wishlistID;

    //protected Faker faker = new Faker();
    SoftAssert softassert ;


    @Parameters ("env")
    @BeforeTest public void setBaseURL(String env){

        //RestAssured.baseURI = "http://localhost:3000";
        RestAssured.baseURI = env ;


    }
}
