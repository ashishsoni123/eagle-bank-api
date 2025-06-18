package com.eaglebank.api.controller;

import com.eaglebank.api.dto.*;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

import static com.eaglebank.api.service.TransactionService.DEPOSIT;
import static com.eaglebank.api.service.TransactionService.WITHDRAWAL;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class TransactionControllerIntegrationTest extends BaseIntegrationTest {
    private String authToken;
    private String userId;
    private String accountNumber;

    @BeforeEach
    void setup() {
        // Create test user
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setEmail("transaction.test@example.com");
        createRequest.setPassword("Test@123");
        createRequest.setName("Transaction Test User");
        createRequest.setPhoneNumber("+44123456789");

        Address address = new Address();
        address.setLine1("123 Test St");
        address.setTown("London");
        address.setCounty("UK");
        createRequest.setAddress(address);

        // Register user
        userId = given()
            .contentType(ContentType.JSON)
            .body(createRequest)
        .when()
            .post("/v1/auth/register")
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .path("id");

        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("transaction.test@example.com");
        loginRequest.setPassword("Test@123");

        authToken = given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post("/v1/auth/login")
        .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .path("token");

        // Create a test account
        CreateAccountRequest accountRequest = new CreateAccountRequest();
        accountRequest.setName("Transaction Test Account");
        accountRequest.setAccountType("CURRENT");

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

    @Test
    void whenDeposit_thenBalanceIncreases() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("GBP");
        request.setType(DEPOSIT);
        request.setReference("Test deposit");

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request)
        .when()
            .post("/v1/accounts/" + accountNumber + "/transactions")
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("amount", comparesEqualTo(100.00f))
            .body("type", equalTo("DEPOSIT"))
            .body("id", startsWith("tan-"));

        // Verify account balance
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get("/v1/accounts/" + accountNumber)
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("balance", comparesEqualTo(100.00f));
    }

    @Test
    void whenWithdraw_thenBalanceDecreases() {
        // First deposit
        CreateTransactionRequest depositRequest = new CreateTransactionRequest();
        depositRequest.setAmount(new BigDecimal("200.00"));
        depositRequest.setCurrency("GBP");
        depositRequest.setType(DEPOSIT);
        depositRequest.setReference("Initial deposit");

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(depositRequest)
        .when()
            .post("/v1/accounts/" + accountNumber + "/transactions")
        .then()
            .statusCode(HttpStatus.CREATED.value());

        // Then withdraw
        CreateTransactionRequest withdrawRequest = new CreateTransactionRequest();
        withdrawRequest.setAmount(new BigDecimal("50.00"));
        withdrawRequest.setCurrency("GBP");
        withdrawRequest.setType(WITHDRAWAL);
        withdrawRequest.setReference("Test withdrawal");

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(withdrawRequest)
        .when()
            .post("/v1/accounts/" + accountNumber + "/transactions")
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("amount", comparesEqualTo(50.00f))
            .body("type", equalTo("WITHDRAWAL"));

        // Verify final balance
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get("/v1/accounts/" + accountNumber)
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("balance", comparesEqualTo(150.00f));
    }

    @Test
    void whenWithdrawInsufficientFunds_thenReturns400() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(new BigDecimal("1000.00"));
        request.setCurrency("GBP");
        request.setType(WITHDRAWAL);
        request.setReference("Insufficient funds test");

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request)
        .when()
            .post("/v1/accounts/" + accountNumber + "/transactions")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void whenGetTransactions_thenReturnsTransactionsList() {
        // Create a transaction first
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(new BigDecimal("75.00"));
        request.setCurrency("GBP");
        request.setType(DEPOSIT);
        request.setReference("List test transaction");

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request)
        .when()
            .post("/v1/accounts/" + accountNumber + "/transactions")
        .then()
            .statusCode(HttpStatus.CREATED.value());

        // Get transactions list
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get("/v1/accounts/" + accountNumber + "/transactions")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("transactions", hasSize(greaterThanOrEqualTo(1)))
            .body("transactions[0].id", startsWith("tan-"))
            .body("transactions[0].amount", notNullValue())
            .body("transactions[0].type", notNullValue());
    }
}
