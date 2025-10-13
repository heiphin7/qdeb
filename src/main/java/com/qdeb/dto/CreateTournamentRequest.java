package com.qdeb.dto;

import com.qdeb.entity.TournamentLevel;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateTournamentRequest {
    private String name;
    private String slug;
    private String organizerName;
    private String organizerContact;
    private String description;
    private LocalDate date;
    private boolean active;
    private Integer fee;
    private TournamentLevel level;
    private String format;
    private Integer seq;
    private List<RegistrationFieldDto> registrationFields;
}
