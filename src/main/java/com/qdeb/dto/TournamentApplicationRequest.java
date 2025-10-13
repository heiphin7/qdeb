package com.qdeb.dto;

import lombok.Data;

import java.util.List;

@Data
public class TournamentApplicationRequest {
    private Long teamId;
    private List<ApplicationFieldDto> fields;
}
