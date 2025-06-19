package com.eaglebank.api.controller;

import com.eaglebank.api.dto.LoginRequest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void whenValidLogin_thenReturns200AndToken() {
        // First create a user
        createTestUserAndAuthenticate();
        assert authToken != null : "Auth token should not be null after login";
    }

    @Test
    void whenInvalidLogin_thenReturns403() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistent@example.com");
        loginRequest.setPassword("wrongpassword");

        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post("/v1/auth/login")
        .then()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void whenInvalidUsername_thenReturns403() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistent@example.com");
        loginRequest.setPassword(TEST_PASSWORD);

        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post("/v1/auth/login")
        .then()
            .statusCode(HttpStatus.FORBIDDEN.value())
            .body("message", equalTo("Invalid username or password"));
    }

  /*  @Test
    void whenInvalidPassword_thenReturns403() {
        // First create a user with known credentials
        String testEmail = generateRandomEmail();
        createTestUserWithEmail(testEmail);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(testEmail);
        loginRequest.setPassword("WrongPassword123!");

        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post("/v1/auth/login")
        .then()
            .statusCode(HttpStatus.FORBIDDEN.value())
            .body("message", equalTo("Invalid username or password"));
    }

    @Test
    void whenEmptyUsername_thenReturns400() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("");
        loginRequest.setPassword(TEST_PASSWORD);

        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post("/v1/auth/login")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void whenEmptyPassword_thenReturns400() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(generateRandomEmail());
        loginRequest.setPassword("");

        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post("/v1/auth/login")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }*/
}
