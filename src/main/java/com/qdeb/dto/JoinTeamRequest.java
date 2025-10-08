package com.qdeb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JoinTeamRequest {
    
    @NotBlank
    @Size(min = 8, max = 8)
    private String joinCode;
}
