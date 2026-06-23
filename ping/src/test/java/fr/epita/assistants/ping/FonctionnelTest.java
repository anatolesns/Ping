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
            .body("{\"login\": \"admin.admin\", \"password\": \"belpe\"}")
        .when()
            .post("/api/user/login")
        .then()
            .statusCode(200);
    }
    
    @Test
    void Get(){
        given()
        .when()
            .get("/api/user/all")
        .then()
            .log().body()
            .statusCode(200);
    }
}
