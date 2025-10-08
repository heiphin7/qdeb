package com.qdeb.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignInRequest {
    
    @NotBlank
    private String username;
    
    @NotBlank
    private String password;
}
