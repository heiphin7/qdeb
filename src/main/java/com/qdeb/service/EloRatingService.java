package com.qdeb.service;

import com.qdeb.dto.SpeakerRatingDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EloRatingService {

    private static final double K = 32.0;
    private static final double INITIAL_RATING = 1000.0;

    private final JdbcTemplate tabbycatJdbcTemplate;

    public EloRatingService(@Qualifier("tabbycatJdbcTemplate") JdbcTemplate tabbycatJdbcTemplate) {
        this.tabbycatJdbcTemplate = tabbycatJdbcTemplate;
    }

    public List<SpeakerRatingDto> calculateSpeakerRatings() {
        String sql = """
                SELECT
                    ps.id          AS speaker_id,
                    ps.name        AS speaker_name,
                    pt.short_name  AS team_name,
                    SUM(ss.score)  AS total_score,
                    db.id          AS debate_id
                FROM results_speakerscore ss
                JOIN results_ballotsubmission bs ON ss.ballot_submission_id = bs.id
                JOIN draw_debateteam dt          ON ss.debate_team_id = dt.id
                JOIN draw_debate db              ON dt.debate_id = db.id
                JOIN tournaments_round r         ON db.round_id = r.id
                JOIN participants_speaker ps     ON ss.speaker_id = ps.id
                JOIN participants_team pt        ON ps.team_id = pt.id
                WHERE bs.confirmed = true
                  AND NOT COALESCE(ss.ghost, false)
                GROUP BY ps.id, ps.name, pt.short_name, db.id, r.tournament_id, r.seq
                ORDER BY r.tournament_id, r.seq, db.id
                """;

        Map<Long, List<SpeakerEntry>> byDebate = new LinkedHashMap<>();

        tabbycatJdbcTemplate.query(sql, rs -> {
            long debateId = rs.getLong("debate_id");
            SpeakerEntry entry = new SpeakerEntry(
                    rs.getLong("speaker_id"),
                    rs.getString("speaker_name"),
                    rs.getString("team_name"),
                    rs.getDouble("total_score")
            );
            byDebate.computeIfAbsent(debateId, k -> new ArrayList<>()).add(entry);
        });

        Map<Long, double[]> ratings = new HashMap<>();
        Map<Long, String> names = new HashMap<>();
        Map<Long, String> teams = new HashMap<>();

        for (List<SpeakerEntry> speakers : byDebate.values()) {
            for (SpeakerEntry s : speakers) {
                ratings.putIfAbsent(s.speakerId, new double[]{INITIAL_RATING, 0});
                names.put(s.speakerId, s.speakerName);
                teams.put(s.speakerId, s.teamName);
            }

            Map<Long, Double> deltas = new HashMap<>();
            for (SpeakerEntry s : speakers) {
                deltas.put(s.speakerId, 0.0);
            }

            for (int i = 0; i < speakers.size(); i++) {
                for (int j = i + 1; j < speakers.size(); j++) {
                    SpeakerEntry a = speakers.get(i);
                    SpeakerEntry b = speakers.get(j);
                    if (a.teamName.equals(b.teamName)) continue;

                    double ra = ratings.get(a.speakerId)[0];
                    double rb = ratings.get(b.speakerId)[0];

                    double ea = 1.0 / (1 + Math.pow(10, (rb - ra) / 400.0));
                    double eb = 1.0 - ea;

                    double sa, sb;
                    if (a.totalScore > b.totalScore) { sa = 1; sb = 0; }
                    else if (a.totalScore < b.totalScore) { sa = 0; sb = 1; }
                    else { sa = 0.5; sb = 0.5; }

                    deltas.merge(a.speakerId, K * (sa - ea), Double::sum);
                    deltas.merge(b.speakerId, K * (sb - eb), Double::sum);
                }
            }

            for (SpeakerEntry s : speakers) {
                double[] data = ratings.get(s.speakerId);
                data[0] += deltas.get(s.speakerId);
                data[1]++;
            }
        }

        return ratings.entrySet().stream()
                .map(e -> new SpeakerRatingDto(
                        e.getKey(),
                        names.get(e.getKey()),
                        teams.get(e.getKey()),
                        Math.round(e.getValue()[0] * 10.0) / 10.0,
                        (int) e.getValue()[1]
                ))
                .sorted(Comparator.comparingDouble(SpeakerRatingDto::getRating).reversed())
                .collect(Collectors.toList());
    }

    private record SpeakerEntry(long speakerId, String speakerName, String teamName, double totalScore) {}
}
