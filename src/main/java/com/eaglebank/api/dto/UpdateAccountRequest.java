package com.eaglebank.api.dto;

import com.eaglebank.api.enums.AccountType;
import lombok.Data;

@Data
public class UpdateAccountRequest {
    private String name;
    private AccountType accountType;
}
