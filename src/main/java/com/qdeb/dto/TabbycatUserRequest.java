package com.qdeb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TabbycatUserRequest {
    
    private String username;
    private String password;
    
    @JsonProperty("is_superuser")
    private boolean is_superuser = false;
    
    @JsonProperty("is_staff")
    private boolean is_staff = false;
    
    private String email;
    
    @JsonProperty("is_active")
    private boolean is_active = true;
    
    public TabbycatUserRequest(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
    
    // Добавляем конструктор по умолчанию для Jackson
    public TabbycatUserRequest() {
    }
}
