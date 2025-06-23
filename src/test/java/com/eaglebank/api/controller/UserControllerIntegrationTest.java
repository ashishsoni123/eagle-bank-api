package com.eaglebank.api.controller;

import com.eaglebank.api.dto.Address;
import com.eaglebank.api.dto.CreateUserRequest;
import com.eaglebank.api.dto.UpdateUserRequest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class UserControllerIntegrationTest extends BaseIntegrationTest {

    @BeforeEach
    void setup() {
        createTestUserAndAuthenticate();
    }

    @Test
    void whenGetUser_thenReturnsUserDetails() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get("/v1/users/" + userId)
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("id", equalTo(userId))
            .body("email", equalTo(TEST_EMAIL))
            .body("name", equalTo("Test User"))
            .body("phoneNumber", equalTo("+447438390300"));
    }

    @Test
    void whenCreateUserWithExistingEmail_thenReturns400() {
        CreateUserRequest createRequest = createUserRequest();
        given()
                .contentType(ContentType.JSON)
                .body(createRequest)
                .when()
                .post("/v1/users")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", equalTo("User with this email already exists."));
    }


    @Test
    void whenUpdateUser_thenReturnsUpdatedDetails() {
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setPhoneNumber("+44987654321");

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(updateRequest)
        .when()
            .patch("/v1/users/" + userId)
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("name", equalTo("Updated Name"))
            .body("phoneNumber", equalTo("+44987654321"));
    }

    @Test
    void whenUpdateUser_withOtherUserProfile_thenReturns403() {
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setPhoneNumber("+44987654321");

        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setEmail("user.test2@example.com");
        createRequest.setPassword("test1234");
        createRequest.setName("Test User");
        createRequest.setPhoneNumber("+447438390300");

        Address address = new Address();
        address.setLine1("123 Test St");
        address.setTown("London");
        address.setCounty("UK");
        address.setPostcode("SL");
        createRequest.setAddress(address);

        // Register user and extract ID
        String otherUserId = given()
                .contentType(ContentType.JSON)
                .body(createRequest)
                .when()
                .post("/v1/users")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .path("id");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(updateRequest)
                .when()
                .patch("/v1/users/" + otherUserId)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void whenGetUserWithoutAuth_thenReturns403() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/v1/users/" + userId)
        .then()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void whenAccessOtherUserProfile_thenReturns403() {
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setEmail("user.test1@example.com");
        createRequest.setPassword("test1234");
        createRequest.setName("Test User");
        createRequest.setPhoneNumber("+447438390300");

        Address address = new Address();
        address.setLine1("123 Test St");
        address.setTown("London");
        address.setCounty("UK");
        address.setPostcode("SL");
        createRequest.setAddress(address);

        // Register user and extract ID
        String otherUserId = given()
                .contentType(ContentType.JSON)
                .body(createRequest)
                .when()
                .post("/v1/users")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .path("id");


        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get("/v1/users/" + otherUserId)
        .then()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @AfterEach
    void cleanup() {
        cleanupTestUser();
    }


}
