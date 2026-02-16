package com.smartmove.dto.response;

import lombok.*;

import java.util.Map;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String error;
    private Map<String, String> fieldErrors;
}
