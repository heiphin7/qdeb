package com.qdeb.service;

import com.qdeb.dto.CreateTournamentRequest;
import com.qdeb.dto.RegistrationFieldDto;
import com.qdeb.entity.*;
import com.qdeb.repository.RegistrationFieldRepository;
import com.qdeb.repository.RoundRepository;
import com.qdeb.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final FileStorageService fileStorageService;
    
    @Transactional
    public Tournament createTournament(CreateTournamentRequest request, MultipartFile tournamentPicture) {
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
        
        // Обрабатываем фотографию турнира
        if (tournamentPicture != null && !tournamentPicture.isEmpty()) {
            try {
                String fileName = fileStorageService.storeFile(tournamentPicture);
                tournament.setTournamentPicture(fileName);
                log.info("Фотография турнира сохранена: {}", fileName);
            } catch (Exception e) {
                log.error("Ошибка при сохранении фотографии турнира: {}", e.getMessage());
                throw new RuntimeException("Ошибка при сохранении фотографии турнира: " + e.getMessage());
            }
        }
        
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
        
        // РАУНДЫ БОЛЬШЕ НЕ СОЗДАЕМ НА ЭТОМ ЭТАПЕ
        
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
        
        // РАУНДЫ В TABBYCAT БОЛЬШЕ НЕ СОЗДАЕМ НА ЭТОМ ЭТАПЕ
        
        log.info("Создание турнира '{}' завершено успешно", request.getName());
        return savedTournament;
    }
    
    public List<Tournament> getAllTournaments() {
        log.info("Получение списка всех турниров");
        List<Tournament> tournaments = tournamentRepository.findAll();
        log.info("Найдено {} турниров", tournaments.size());
        return tournaments;
    }
    
    public Tournament getTournamentBySlug(String slug) {
        log.info("Поиск турнира по slug: {}", slug);
        return tournamentRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Турнир с slug '" + slug + "' не найден"));
    }
    
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
    }
}
