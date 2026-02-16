package com.smartmove.dto.response;

import lombok.*;

import java.util.Map;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private Long timestamp;
    private Map<String, String> fieldErrors;
}
