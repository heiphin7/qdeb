package com.qdeb.dto;

import com.qdeb.entity.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignUpRequest {
    
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;
    
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    @Size(min = 6)
    private String password;
    
    @NotBlank
    @Size(max = 100)
    private String fullName;
    
    @NotNull
    private Gender gender;
    
    @Size(max = 20)
    private String phone;
    
    @Size(max = 500)
    private String description;
}
