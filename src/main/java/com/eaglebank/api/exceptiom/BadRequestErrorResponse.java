package com.eaglebank.api.exceptiom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BadRequestErrorResponse {
    private String message;
    private List<Detail> details;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detail {
        private String field;
        private String message;
        private String type;
    }
}
