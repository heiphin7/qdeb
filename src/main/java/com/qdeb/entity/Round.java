package com.qdeb.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "rounds")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Round {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false, length = 40)
    private String name;
    
    @NotBlank
    @Column(nullable = false, length = 10)
    private String abbreviation;
    
    @NotNull
    @Column(nullable = false)
    private Integer seq;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoundStage stage;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DrawType drawType;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DrawStatus drawStatus;
    
    @Column(name = "break_category")
    private String breakCategory;
    
    @Column(name = "starts_at")
    private LocalDateTime startsAt;
    
    @Column(nullable = false)
    private boolean completed;
    
    @Column(name = "feedback_weight")
    private Double feedbackWeight;
    
    @Column(nullable = false)
    private boolean silent;
    
    @Column(name = "motions_released", nullable = false)
    private boolean motionsReleased;
    
    @Column(nullable = false)
    private Integer weight;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;
}
