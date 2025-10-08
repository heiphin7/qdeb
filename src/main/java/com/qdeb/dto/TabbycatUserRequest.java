package com.qdeb.dto;

import lombok.Data;

@Data
public class TabbycatUserRequest {
    
    private String username;
    private String password;
    private boolean is_superuser = false;
    private boolean is_staff = false;
    private String email;
    private boolean is_active = true;
    private Object[] tournaments = new Object[0];
    
    public TabbycatUserRequest(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
}
