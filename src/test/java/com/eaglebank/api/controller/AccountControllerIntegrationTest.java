package com.eaglebank.api.controller;

import com.eaglebank.api.dto.CreateAccountRequest;
import com.eaglebank.api.dto.UpdateAccountRequest;
import com.eaglebank.api.enums.AccountType;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;

import static com.eaglebank.api.enums.AccountType.PERSONAL;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class AccountControllerIntegrationTest extends BaseIntegrationTest {

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
            .body("accountType", equalTo(PERSONAL.getValue()))
            .body("currency", equalTo("GBP"))
            .body("balance", comparesEqualTo(0))
            .body("accountNumber", notNullValue())
            .body("sortCode", notNullValue());

    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void whenNameIsBlankOrNull_thenReturnsBadRequest(String invalidName) {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setName(invalidName);
        request.setAccountType(AccountType.PERSONAL);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(request)
                .when()
                .post("/v1/accounts")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", containsString("Invalid details supplied"));
    }

    @Test
    void whenAccountTypeIsNull_thenReturnsBadRequest() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setName("Test Account");
        request.setAccountType(null);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(request)
                .when()
                .post("/v1/accounts")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", containsString("Invalid details supplied"));
    }

    @Test
    void whenNameExceedsMaxLength_thenReturnsBadRequest() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setName("A".repeat(101)); // Assuming max length is 100
        request.setAccountType(AccountType.PERSONAL);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(request)
                .when()
                .post("/v1/accounts")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", containsString("Invalid details supplied"));
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
    void whenNoAccountFound_thenReturn404() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get("/v1/accounts/" + "inavlidAccount")
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
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
                .body("accountType", equalTo(PERSONAL.getValue()))
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
    void whenUpdateAccountNonExistentAccount_thenReturn404() {
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setName("Updated Account Name");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(request)
                .when()
                .patch("/v1/accounts/" + "invalidAccountNumber")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
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

