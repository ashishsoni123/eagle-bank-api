package com.eaglebank.api.controller;

import com.eaglebank.api.dto.Address;
import com.eaglebank.api.dto.CreateAccountRequest;
import com.eaglebank.api.dto.CreateUserRequest;
import com.eaglebank.api.dto.LoginRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static com.eaglebank.api.enums.AccountType.PERSONAL;
import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @LocalServerPort
    private int port;

    protected String authToken;
    protected String userId;
    protected String accountNumber;
    protected  String TEST_EMAIL = "test." + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    protected static final String TEST_PASSWORD = "Test1234";

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "";
    }

    protected void createTestUserAndAuthenticate() {
        // Create a test user and get authentication token
        CreateUserRequest createRequest = createUserRequest();

        // Register user and extract ID
        userId = given()
                .contentType(ContentType.JSON)
                .body(createRequest)
                .when()
                .post("/v1/users")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .path("id");

        // Login and get token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);

        authToken = given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/v1/auth/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("jwt");
    }

    protected void createTestAccount() {
        CreateAccountRequest accountRequest = new CreateAccountRequest();
        accountRequest.setName("Test Account");
        accountRequest.setAccountType(PERSONAL);

        accountNumber = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(accountRequest)
                .when()
                .post("/v1/accounts")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .path("accountNumber");
    }

    protected @NotNull CreateUserRequest createUserRequest() {
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setEmail(TEST_EMAIL);
        createRequest.setPassword(TEST_PASSWORD);
        createRequest.setName("Test User");
        createRequest.setPhoneNumber("+447438390300");

        Address address = new Address();
        address.setLine1("123 Test St");
        address.setTown("London");
        address.setCounty("UK");
        address.setPostcode("SL");
        createRequest.setAddress(address);
        return createRequest;
    }

    protected void cleanupTestUser() {
        if (userId != null && authToken != null) {
            given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + authToken)
                    .when()
                    .delete("/v1/users/" + userId)
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }
    }
}
