package com.qdeb.dto;

import com.qdeb.entity.Team;
import com.qdeb.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfileResponse {
    
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String description;
    private String profilePicture;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private TeamInfo team;
    
    public UserProfileResponse(User user, Team team) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.phone = user.getPhone();
        this.description = user.getDescription();
        this.profilePicture = buildProfilePictureUrl(user.getProfilePicture());
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        
        if (team != null) {
            this.team = new TeamInfo(team, user);
        } else {
            this.team = null;
        }
    }
    
    private String buildProfilePictureUrl(String profilePicture) {
        if (profilePicture == null || profilePicture.isEmpty()) {
            return null;
        }
        // Возвращаем полный URL для доступа к изображению
        return "/api/files/" + profilePicture;
    }
    
    @Data
    public static class TeamInfo {
        private Long id;
        private String name;
        private String joinCode;
        private String role; // "LEADER" or "MEMBER"
        private int memberCount;
        private boolean isFull;
        private LocalDateTime joinedAt;
        private Long memberId; // ID второго участника команды (если есть)
        
        public TeamInfo(Team team, User user) {
            this.id = team.getId();
            this.name = team.getName();
            this.joinCode = team.getJoinCode();
            this.memberCount = team.getMemberCount();
            this.isFull = team.isFull();
            
            // Определяем роль пользователя в команде
            if (team.getLeader().getId().equals(user.getId())) {
                this.role = "LEADER";
                this.joinedAt = team.getCreatedAt(); // Лидер присоединился при создании команды
                
                // Если пользователь лидер, ищем ID второго участника
                this.memberId = team.getMembers().stream()
                        .filter(member -> !member.getUser().getId().equals(user.getId()))
                        .findFirst()
                        .map(member -> member.getUser().getId())
                        .orElse(null);
            } else {
                this.role = "MEMBER";
                // Найдем дату присоединения из TeamMember
                this.joinedAt = team.getMembers().stream()
                        .filter(member -> member.getUser().getId().equals(user.getId()))
                        .findFirst()
                        .map(member -> member.getJoinedAt())
                        .orElse(null);
                
                // Если пользователь участник, то второй участник - это лидер
                this.memberId = team.getLeader().getId();
            }
        }
    }
}