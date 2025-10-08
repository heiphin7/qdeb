package com.qdeb.dto;

import com.qdeb.entity.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TeamResponse {
    
    private Long id;
    private String name;
    private User leader;
    private String joinCode;
    private List<TeamMemberResponse> members;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int memberCount;
    private boolean isFull;
    
    public TeamResponse(Long id, String name, User leader, String joinCode, 
                       List<TeamMemberResponse> members, LocalDateTime createdAt, 
                       LocalDateTime updatedAt, int memberCount, boolean isFull) {
        this.id = id;
        this.name = name;
        this.leader = leader;
        this.joinCode = joinCode;
        this.members = members;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.memberCount = memberCount;
        this.isFull = isFull;
    }
}
