package com.qdeb.dto;

import com.qdeb.entity.ApplicationStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TournamentApplicationDetailResponse {
    private Long id;
    private Long tournamentId;
    private String tournamentName;
    private String tournamentSlug;
    private TeamInfo team;
    private UserInfo submittedBy;
    private ApplicationStatus status;
    private List<ApplicationFieldResponse> fields;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    public static class TeamInfo {
        private Long id;
        private String name;
        private String joinCode;
        private UserInfo leader;
        private UserInfo member;
        private int memberCount;
        private boolean isFull;
        private LocalDateTime createdAt;
        
        public TeamInfo(Long id, String name, String joinCode, UserInfo leader, UserInfo member, 
                       int memberCount, boolean isFull, LocalDateTime createdAt) {
            this.id = id;
            this.name = name;
            this.joinCode = joinCode;
            this.leader = leader;
            this.member = member;
            this.memberCount = memberCount;
            this.isFull = isFull;
            this.createdAt = createdAt;
        }
    }
    
    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private String phone;
        private String description;
        private String profilePicture;
        private String imageURL;
        private LocalDateTime createdAt;
        
        public UserInfo(Long id, String username, String email, String fullName, String phone, 
                       String description, String profilePicture, LocalDateTime createdAt) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.fullName = fullName;
            this.phone = phone;
            this.description = description;
            this.profilePicture = profilePicture;
            this.imageURL = profilePicture != null ? "/api/files/" + profilePicture : null;
            this.createdAt = createdAt;
        }
    }
    
    @Data
    public static class ApplicationFieldResponse {
        private Long id;
        private String name;
        private String value;
        
        public ApplicationFieldResponse(Long id, String name, String value) {
            this.id = id;
            this.name = name;
            this.value = value;
        }
    }
}
