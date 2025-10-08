package com.qdeb.dto;

import com.qdeb.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamMemberResponse {
    
    private Long id;
    private User user;
    private LocalDateTime joinedAt;
    
    public TeamMemberResponse(Long id, User user, LocalDateTime joinedAt) {
        this.id = id;
        this.user = user;
        this.joinedAt = joinedAt;
    }
}
