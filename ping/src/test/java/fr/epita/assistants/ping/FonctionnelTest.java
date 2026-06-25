package fr.epita.assistants.ping;


import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;



@QuarkusTest
public class FonctionnelTest {
    
    @Test
    void testLoginSuccess() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"login\": \"admin.admin\", \"password\": \"admin\"}")
        .when()
            .post("/api/user/login")
        .then()
            .statusCode(200);
    }

    String token = given()
            .contentType(ContentType.JSON)
            .body("{\"login\": \"admin.admin\", \"password\": \"admin\"}")
        .when()
            .post("/api/user/login")
        .then()
            .statusCode(200)
            .extract()
            .path("token");
    
    @Test
    void Get(){
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/user/all")
        .then()
            .log().body()
            .statusCode(200);
    }
}
