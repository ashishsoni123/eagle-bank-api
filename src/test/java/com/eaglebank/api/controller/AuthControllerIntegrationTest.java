package com.eaglebank.api.controller;

import com.eaglebank.api.dto.Address;
import com.eaglebank.api.dto.CreateUserRequest;
import com.eaglebank.api.dto.LoginRequest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void whenValidLogin_thenReturns200AndToken() {
        // First create a user
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setEmail("test@example.com");
        createRequest.setPassword("Test@123");
        createRequest.setName("Test User");
        createRequest.setPhoneNumber("+44123456789");

        Address address = new Address();
        address.setLine1("123 Test St");
        address.setTown("London");
        address.setCounty("UK");
        createRequest.setAddress(address);

        // Register the user
        given()
            .contentType(ContentType.JSON)
            .body(createRequest)
        .when()
            .post("/v1/auth/register")
        .then()
            .statusCode(HttpStatus.CREATED.value());

        // Try to login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("test@example.com");
        loginRequest.setPassword("Test@123");

        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post("/v1/auth/login")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("token", notNullValue())
            .body("token", not(emptyString()));
    }

    @Test
    void whenInvalidLogin_thenReturns401() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistent@example.com");
        loginRequest.setPassword("wrongpassword");

        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post("/v1/auth/login")
        .then()
            .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void whenRegisterWithExistingEmail_thenReturns400() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("duplicate@example.com");
        request.setPassword("Test@123");
        request.setName("Test User");
        request.setPhoneNumber("+44123456789");

        Address address = new Address();
        address.setLine1("123 Test St");
        address.setTown("London");
        address.setCounty("UK");
        request.setAddress(address);

        // First registration
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/v1/auth/register")
        .then()
            .statusCode(HttpStatus.CREATED.value());

        // Second registration with same email
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/v1/auth/register")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}
