package com.qdeb.controller;

import com.qdeb.dto.SpeakerRatingDto;
import com.qdeb.service.EloRatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rating")
@RequiredArgsConstructor
public class RatingController {

    private final EloRatingService eloRatingService;

    @GetMapping("/speakers")
    public List<SpeakerRatingDto> getSpeakerRatings() {
        return eloRatingService.calculateSpeakerRatings();
    }
}
