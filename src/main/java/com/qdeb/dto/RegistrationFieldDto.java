package com.qdeb.dto;

import com.qdeb.entity.RegistrationFieldType;
import lombok.Data;

@Data
public class RegistrationFieldDto {
    private String name;
    private RegistrationFieldType type;
    private boolean required;
}
