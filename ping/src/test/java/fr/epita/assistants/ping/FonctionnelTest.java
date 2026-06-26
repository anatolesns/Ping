package fr.epita.assistants.ping;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import io.restassured.http.ContentType;



@QuarkusTest
public class FonctionnelTest {

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
    void loginSuccess() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"login\": \"admin.admin\", \"password\": \"admin\"}")
        .when()
            .post("/api/user/login")
        .then()
            .statusCode(200)
            .body("token", notNullValue());
    }

    @Test
    void loginWrongPassword() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"login\": \"admin.admin\", \"password\": \"invalid\"}")
        .when()
            .post("/api/user/login")
        .then()
            .statusCode(401);
    }

    @Test
    void loginInvalidFormat() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"login\": \"invalidformat\", \"password\": \"invalid\"}")
        .when()
            .post("/api/user/login")
        .then()
            .statusCode(400);
    }

    @Test
    void loginUserNotFound() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"login\": \"no.user\", \"password\": \"test\"}")
        .when()
            .post("/api/user/login")
        .then()
            .statusCode(401);
    }

    @Test
    void getAllUsers() {
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/user/all")
        .then()
            .statusCode(200);
    }

    @Test
    void getNoAuth() {
        given()
        .when()
            .get("/api/user/all")
        .then()
            .statusCode(401);
    }

    @Test
    void createUser() {
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"login\": \"test.test\", \"password\": \"password\", \"isAdmin\": false}")
        .when()
            .post("/api/user")
        .then()
            .statusCode(200)
            .body("login", equalTo("test.test"));
    }

    @Test
    void createUserNoAuth() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"login\": \"test.test\", \"password\": \"password\", \"isAdmin\": false}")
        .when()
            .post("/api/user")
        .then()
            .statusCode(401);
    }

    @Test
    void createUserInvalidLogin() {
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"login\": \"invalidformat\", \"password\": \"password\", \"isAdmin\": false}")
        .when()
            .post("/api/user")
        .then()
            .statusCode(400);
    }

    @Test
    void createUserNull() {
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/user")
        .then()
            .statusCode(400);
    }

    @Test
    void createUserDuplicate() {
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"login\": \"admin.admin\", \"password\": \"password\", \"isAdmin\": false}")
        .when()
            .post("/api/user")
        .then()
            .statusCode(409);
    }

    @Test
    void refreshToken() {
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/user/refresh")
        .then()
            .statusCode(200)
            .body("token", notNullValue());
    }

    @Test
    void refreshTokenNoAuth() {
        given()
        .when()
            .get("/api/user/refresh")
        .then()
            .statusCode(401);
    }

    @Test
    void getUserById() {
        String id = given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/user/all")
        .then()
            .statusCode(200)
            .extract()
            .path("[0].id");

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/user/" + id)
        .then()
            .statusCode(200)
            .body("login", notNullValue());
    }

    @Test
    void getUserNotFound() {
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/user/abcdefgh-abcd-abcd-abcd-abcdefghijkl")                
        .then()
            .statusCode(404);
    }

    @Test
    void getUserNoAuth() {
        given()
        .when()
            .get("/api/user/abcdefgh-abcd-abcd-abcd-abcdefghijkl")
        .then()
            .statusCode(401);
    }

    @Test
    void updateUser() {
        String id = given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/user/all")
        .then()
            .statusCode(200)
            .extract()
            .path("[0].id");

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"displayName\": \"New Name\"}")
        .when()
            .put("/api/user/" + id)
        .then()
            .statusCode(200)
            .body("displayName", equalTo("New Name"));
    }

    @Test
    void updateUserNotFound() {
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"displayName\": \"Test\"}")
        .when()
            .put("/api/user/abcdefgh-abcd-abcd-abcd-abcdefghijkl")
        .then()
            .statusCode(404);
    }

    @Test
    void updateUserNoAuth() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"displayName\": \"Test\"}")
        .when()
            .put("/api/user/abcdefgh-abcd-abcd-abcd-abcdefghijkl")
        .then()
            .statusCode(401);
    }

    @Test
    void deleteUserNotFound() {
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/api/user/abcdefgh-abcd-abcd-abcd-abcdefghijkl")
        .then()
            .statusCode(404);
    }

    @Test
    void deleteUserNoAuth() {
        given()
        .when()
            .delete("/api/user/abcdefgh-abcd-abcd-abcd-abcdefghijkl")
        .then()
            .statusCode(401);
    }

    
    @Test
    void deleteUser() {
        String id = given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"login\": \"test.test\", \"password\": \"password\", \"isAdmin\": false}")
        .when()
            .post("/api/user")
        .then()
            .statusCode(200)
            .extract()
            .path("id");

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/api/user/" + id)
        .then()
            .statusCode(404);
    }
}
