package com.qdeb.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tournaments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tournament {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    @NotBlank
    @Column(nullable = false, unique = true)
    private String slug;
    
    @NotBlank
    @Column(name = "organizer_name", nullable = false)
    private String organizerName;
    
    @NotBlank
    @Column(name = "organizer_contact", nullable = false)
    private String organizerContact;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @NotNull
    @Column(nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(nullable = false)
    private LocalDate endDate;
    
    @Column(nullable = false)
    private boolean active;
    
    @Column(nullable = false)
    private Integer fee;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TournamentLevel level;
    
    @NotBlank
    @Column(nullable = false)
    private String format;
    
    @NotNull
    @Column(nullable = false)
    private Integer seq;
    
    @Column(name = "tournament_picture")
    private String tournamentPicture;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Метод для получения полного URL изображения
    public String getImageURL() {
        if (tournamentPicture == null || tournamentPicture.isEmpty()) {
            return null;
        }
        return "/api/files/" + tournamentPicture;
    }
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RegistrationField> registrationFields = new ArrayList<>();
    
    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Round> rounds = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
