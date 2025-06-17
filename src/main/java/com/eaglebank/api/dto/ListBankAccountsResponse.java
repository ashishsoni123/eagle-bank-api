package com.eaglebank.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListBankAccountsResponse {
    private List<BankAccountResponse> accounts;
}
