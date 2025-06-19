package com.eaglebank.api.controller;

import com.eaglebank.api.dto.CreateAccountRequest;
import com.eaglebank.api.dto.UpdateAccountRequest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class AccountControllerIntegrationTest extends BaseIntegrationTest {

    public static String PERSONAL = "personal";

    @BeforeEach
    void setup() {
       createTestUserAndAuthenticate();
        // Create a test account
        createTestAccount();
    }



    @Test
    void whenCreateAccount_thenReturnsNewAccount() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setName("Second Account");
        request.setAccountType(PERSONAL);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request)
        .when()
            .post("/v1/accounts")
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("name", equalTo("Second Account"))
            .body("accountType", equalTo(PERSONAL))
            .body("currency", equalTo("GBP"))
            .body("balance", comparesEqualTo(0))
            .body("accountNumber", notNullValue())
            .body("sortCode", notNullValue());

    }

    @Test
    void whenGetAllAccounts_thenReturnsAccountsList() {

        setUp();
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get("/v1/accounts")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("accounts", hasSize(greaterThanOrEqualTo(1)))
            .body("accounts[0].accountNumber", notNullValue())
            .body("accounts[0].name", notNullValue())
            .body("accounts[0].balance", notNullValue());
    }

    @Test
    void whenGetAccountByNumber_thenReturnsAccount() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get("/v1/accounts/" + accountNumber)
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("accountNumber", equalTo(accountNumber))
            .body("name", equalTo("Test Account"))
            .body("accountType", equalTo(PERSONAL))
            .body("currency", equalTo("GBP"));
    }

    @Test
    void whenUpdateAccount_thenReturnsUpdatedAccount() {
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setName("Updated Account Name");

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request)
        .when()
            .patch("/v1/accounts/" + accountNumber)
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("name", equalTo("Updated Account Name"))
            .body("accountNumber", equalTo(accountNumber));
    }

    @Test
    void whenDeleteAccount_thenReturns204() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
        .when()
            .delete("/v1/accounts/" + accountNumber)
        .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // Verify account is deleted
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get("/v1/accounts/" + accountNumber)
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }



}

