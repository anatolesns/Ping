package fr.epita.assistants.ping;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import io.restassured.http.ContentType;



@QuarkusTest
public class CreneauNotifTest {

    String token;

    @BeforeEach
    void setup() {
        token = given()
            .contentType(ContentType.JSON)
            .body("{\"login\": \"admin.admin\", \"password\": \"admin\"}")
        .when()
            .post("/api/user/login")
        .then()
            .statusCode(200)
            .extract()
            .path("token");
    }

    @Test
    void getMyNotifications() {
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/notification/me")
        .then()
            .statusCode(200);
    }

    @Test
    void getUnread() {
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/notification/me/unread-count")
        .then()
            .statusCode(200)
            .body("count", greaterThanOrEqualTo(0));
    }

    @Test
    void getMyNotificationsNoAuth() {
        given()
        .when()
            .get("/api/notification/me")
        .then()
            .statusCode(401);
    }

    @Test
    void createCreneau() {
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"jour\": \"Samedi\", \"heureDebut\": \"10:00\", \"heureFin\": \"12:00\", \"poste\": \"billetterie\"}")
        .when()
            .post("/api/creneau")
        .then()
            .statusCode(200)
            .body("poste", equalTo("billetterie"));
    }

    @Test
    void createCreneauMissing() {
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"jour\": \"Samedi\"}")
        .when()
            .post("/api/creneau")
        .then()
            .statusCode(400);
    }

    @Test
    void createCreneauNoAuth() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"jour\": \"Samedi\", \"heureDebut\": \"10:00\", \"heureFin\": \"12:00\", \"poste\": \"billetterie\"}")
        .when()
            .post("/api/creneau")
        .then()
            .statusCode(401);
    }

    @Test
    void getAllCreneaux() {
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/creneau/all")
        .then()
            .statusCode(200);
    }
}
