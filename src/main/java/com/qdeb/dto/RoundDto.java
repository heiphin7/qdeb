package com.qdeb.dto;

import com.qdeb.entity.DrawStatus;
import com.qdeb.entity.DrawType;
import com.qdeb.entity.RoundStage;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoundDto {
    private String breakCategory;
    private LocalDateTime startsAt;
    private Integer seq;
    private boolean completed;
    private String name;
    private String abbreviation;
    private RoundStage stage;
    private DrawType drawType;
    private DrawStatus drawStatus;
    private Double feedbackWeight;
    private boolean silent;
    private boolean motionsReleased;
    private Integer weight;
}
