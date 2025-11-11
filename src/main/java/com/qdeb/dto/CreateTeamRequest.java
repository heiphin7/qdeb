package com.qdeb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTeamRequest {
    
    @NotBlank
    @Size(max = 100)
    private String name;
}
