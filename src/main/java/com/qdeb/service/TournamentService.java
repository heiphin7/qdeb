package com.qdeb.service;

import com.qdeb.dto.CreateTournamentRequest;
import com.qdeb.dto.RegistrationFieldDto;
import com.qdeb.dto.RoundDto;
import com.qdeb.entity.*;
import com.qdeb.repository.RegistrationFieldRepository;
import com.qdeb.repository.RoundRepository;
import com.qdeb.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentService {
    
    private final TournamentRepository tournamentRepository;
    private final RoundRepository roundRepository;
    private final RegistrationFieldRepository registrationFieldRepository;
    private final TabbycatTournamentService tabbycatTournamentService;
    
    @Transactional
    public Tournament createTournament(CreateTournamentRequest request) {
        log.info("Начало создания турнира: {}", request.getName());
        
        // Проверяем, что slug уникален
        if (tournamentRepository.existsBySlug(request.getSlug())) {
            throw new RuntimeException("Турнир с таким slug уже существует: " + request.getSlug());
        }
        
        // Создаем турнир в нашей базе данных
        Tournament tournament = new Tournament();
        tournament.setName(request.getName());
        tournament.setSlug(request.getSlug());
        tournament.setOrganizerName(request.getOrganizerName());
        tournament.setOrganizerContact(request.getOrganizerContact());
        tournament.setDescription(request.getDescription());
        tournament.setDate(request.getDate());
        tournament.setActive(request.isActive());
        tournament.setFee(request.getFee());
        tournament.setLevel(request.getLevel());
        tournament.setFormat(request.getFormat());
        tournament.setSeq(request.getSeq());
        
        // Сохраняем турнир
        Tournament savedTournament = tournamentRepository.save(tournament);
        log.info("Турнир сохранен в базе данных с ID: {}", savedTournament.getId());
        
        // Создаем поля регистрации
        if (request.getRegistrationFields() != null && !request.getRegistrationFields().isEmpty()) {
            List<RegistrationField> registrationFields = new ArrayList<>();
            for (RegistrationFieldDto fieldDto : request.getRegistrationFields()) {
                RegistrationField field = new RegistrationField();
                field.setName(fieldDto.getName());
                field.setType(fieldDto.getType());
                field.setRequired(fieldDto.isRequired());
                field.setTournament(savedTournament);
                registrationFields.add(field);
            }
            registrationFieldRepository.saveAll(registrationFields);
            savedTournament.setRegistrationFields(registrationFields);
            log.info("Сохранено {} полей регистрации", registrationFields.size());
        }
        
        // Создаем раунды
        if (request.getRounds() != null && !request.getRounds().isEmpty()) {
            List<Round> rounds = new ArrayList<>();
            for (RoundDto roundDto : request.getRounds()) {
                Round round = new Round();
                round.setName(roundDto.getName());
                round.setAbbreviation(roundDto.getAbbreviation());
                round.setSeq(roundDto.getSeq());
                round.setStage(roundDto.getStage());
                round.setDrawType(roundDto.getDrawType());
                round.setDrawStatus(roundDto.getDrawStatus());
                round.setBreakCategory(roundDto.getBreakCategory());
                round.setStartsAt(roundDto.getStartsAt());
                round.setCompleted(roundDto.isCompleted());
                round.setFeedbackWeight(roundDto.getFeedbackWeight());
                round.setSilent(roundDto.isSilent());
                round.setMotionsReleased(roundDto.isMotionsReleased());
                round.setWeight(roundDto.getWeight());
                round.setTournament(savedTournament);
                rounds.add(round);
            }
            roundRepository.saveAll(rounds);
            savedTournament.setRounds(rounds);
            log.info("Сохранено {} раундов", rounds.size());
        }
        
        // Создаем турнир в Tabbycat
        boolean tabbycatSuccess = tabbycatTournamentService.createTournament(
            request.getName(),
            request.getSlug(), // используем slug как short_name
            request.getSeq(),
            request.getSlug(),
            request.isActive()
        );
        
        if (!tabbycatSuccess) {
            log.warn("Не удалось создать турнир в Tabbycat, но турнир сохранен в нашей базе данных");
        } else {
            log.info("Турнир успешно создан в Tabbycat");
        }
        
        // Создаем раунды в Tabbycat
        if (request.getRounds() != null && !request.getRounds().isEmpty()) {
            for (RoundDto roundDto : request.getRounds()) {
                Map<String, Object> roundData = new HashMap<>();
                roundData.put("break_category", roundDto.getBreakCategory());
                roundData.put("motions", new ArrayList<>()); // всегда пустой массив
                roundData.put("starts_at", formatDateTime(roundDto.getStartsAt()));
                roundData.put("seq", roundDto.getSeq());
                roundData.put("completed", roundDto.isCompleted());
                roundData.put("name", roundDto.getName());
                roundData.put("abbreviation", roundDto.getAbbreviation());
                roundData.put("stage", roundDto.getStage().name());
                roundData.put("draw_type", roundDto.getDrawType().name());
                roundData.put("draw_status", roundDto.getDrawStatus().name());
                roundData.put("feedback_weight", roundDto.getFeedbackWeight());
                roundData.put("silent", roundDto.isSilent());
                roundData.put("motions_released", roundDto.isMotionsReleased());
                roundData.put("weight", roundDto.getWeight());
                
                boolean roundSuccess = tabbycatTournamentService.createRound(request.getSlug(), roundData);
                if (!roundSuccess) {
                    log.warn("Не удалось создать раунд '{}' в Tabbycat", roundDto.getName());
                } else {
                    log.info("Раунд '{}' успешно создан в Tabbycat", roundDto.getName());
                }
            }
        }
        
        log.info("Создание турнира '{}' завершено успешно", request.getName());
        return savedTournament;
    }
    
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
    }
}
