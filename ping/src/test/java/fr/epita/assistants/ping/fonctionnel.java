package fr.epita.assistants.ping;


import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;



@QuarkusTest
public class fonctionnel {
    
    @Test
    void testStart(){
        given().contentType(ContentType.JSON)
        .body("{\"login\": \"noe.blp\",\"password\": \"belpe\"}")
        .when().post("/start").then().statusCode(200);
    }   
}
