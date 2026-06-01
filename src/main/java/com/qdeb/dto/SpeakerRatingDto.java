package com.qdeb.dto;

public class SpeakerRatingDto {
    private Long speakerId;
    private String speakerName;
    private String teamName;
    private double rating;
    private int roundsPlayed;

    public SpeakerRatingDto(Long speakerId, String speakerName, String teamName, double rating, int roundsPlayed) {
        this.speakerId = speakerId;
        this.speakerName = speakerName;
        this.teamName = teamName;
        this.rating = rating;
        this.roundsPlayed = roundsPlayed;
    }

    public Long getSpeakerId() { return speakerId; }
    public String getSpeakerName() { return speakerName; }
    public String getTeamName() { return teamName; }
    public double getRating() { return rating; }
    public int getRoundsPlayed() { return roundsPlayed; }
}
