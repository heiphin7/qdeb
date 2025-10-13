package com.qdeb.dto;

import lombok.Data;

@Data
public class TournamentApplicationResponse {
    private String message;
    private Long applicationId;
    
    public TournamentApplicationResponse(String message, Long applicationId) {
        this.message = message;
        this.applicationId = applicationId;
    }
}
