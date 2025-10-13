package com.qdeb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TabbycatTeamRequest {
    
    private String institution;
    private List<String> breakCategories;
    private List<String> institutionConflicts;
    private List<VenueConstraint> venueConstraints;
    private List<Answer> answers;
    private String reference;
    private String shortReference;
    private String codeName;
    private Boolean useInstitutionPrefix;
    private Integer seed;
    private String emoji;
    private List<Speaker> speakers;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VenueConstraint {
        private String category;
        private Integer priority;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Answer {
        private String question;
        private Integer answer;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Speaker {
        private String name;
        private List<String> categories;
        private String barcode;
        private List<Answer> answers;
        private String lastName;
        private String email;
        private String phone;
        private Boolean anonymous;
        private String codeName;
        private String urlKey;
        private String gender;
        private String pronoun;
    }
}
