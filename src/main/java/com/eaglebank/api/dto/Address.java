package com.eaglebank.api.dto;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @NotBlank(message = "Line 1 cannot be blank")
    private String line1;

    private String line2;

    private String line3;

    @NotBlank(message = "Town cannot be blank")
    private String town;

    @NotBlank(message = "County cannot be blank")
    private String county;

    @NotBlank(message = "Postcode cannot be blank")
    private String postcode;
}
