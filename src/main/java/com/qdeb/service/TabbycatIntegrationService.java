package com.qdeb.service;

import com.qdeb.dto.TabbycatTeamRequest;
import com.qdeb.dto.TournamentApplicationDetailResponse;
import com.qdeb.entity.TournamentApplication;
import com.qdeb.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TabbycatIntegrationService {
    
    private final RestTemplate restTemplate;
    
    @Value("${tabbycat.api.url}")
    private String tabbycatApiUrl;
    
    @Value("${tabbycat.api.key}")
    private String tabbycatApiKey;
    
    public void createTeamInTabbycat(TournamentApplication application) {
        try {
            log.info("Создание команды в Tabbycat для заявки {}", application.getId());
            
            String tournamentSlug = application.getTournament().getSlug();
            String url = tabbycatApiUrl + "/tournaments/" + tournamentSlug + "/teams";
            
            TabbycatTeamRequest request = buildTabbycatTeamRequest(application);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Token " + tabbycatApiKey);
            
            HttpEntity<TabbycatTeamRequest> entity = new HttpEntity<>(request, headers);
            
            log.info("Отправка запроса в Tabbycat: {}", url);
            log.info("Данные команды: reference={}, speakers={}", 
                    request.getReference(), 
                    request.getSpeakers() != null ? request.getSpeakers().size() : 0);
            
            // Логируем детали спикеров для отладки
            if (request.getSpeakers() != null) {
                for (int i = 0; i < request.getSpeakers().size(); i++) {
                    TabbycatTeamRequest.Speaker speaker = request.getSpeakers().get(i);
                    log.info("Спикер {}: name={}, lastName={}, email={}, gender={}", 
                            i + 1, speaker.getName(), speaker.getLastName(), 
                            speaker.getEmail(), speaker.getGender());
                }
            }
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Команда успешно создана в Tabbycat для заявки {}", application.getId());
            } else {
                log.warn("Неожиданный статус ответа от Tabbycat: {} для заявки {}", 
                        response.getStatusCode(), application.getId());
            }
            
        } catch (Exception e) {
            log.error("Ошибка при создании команды в Tabbycat для заявки {}: {}", 
                    application.getId(), e.getMessage(), e);
            // Не прерываем процесс принятия заявки, только логируем ошибку
        }
    }
    
    private TabbycatTeamRequest buildTabbycatTeamRequest(TournamentApplication application) {
        TabbycatTeamRequest request = new TabbycatTeamRequest();
        
        // Основные поля команды
        String teamName = application.getTeam().getName();
        request.setReference(teamName);
        request.setShortReference(teamName);
        request.setCodeName(teamName);
        request.setUseInstitutionPrefix(false);
        request.setSeed(null);
        request.setEmoji("");
        
        // Поля, которые не могут быть null - устанавливаем пустые значения
        request.setInstitution("");
        request.setBreakCategories(new ArrayList<>());
        request.setInstitutionConflicts(new ArrayList<>());
        request.setVenueConstraints(new ArrayList<>());
        request.setAnswers(new ArrayList<>());

        // Создаем спикеров из команды
        List<TabbycatTeamRequest.Speaker> speakers = new ArrayList<>();
        
        // Добавляем лидера команды
        User leader = application.getTeam().getLeader();
        speakers.add(createSpeakerFromUser(leader));
        
        // Добавляем участника команды
        User member = application.getTeam().getMembers().stream()
                .filter(tm -> !tm.getUser().getId().equals(leader.getId()))
                .findFirst()
                .map(tm -> tm.getUser())
                .orElse(null);
        
        if (member != null) {
            speakers.add(createSpeakerFromUser(member));
        }
        
        request.setSpeakers(speakers);
        
        // Логируем детали о гендерах спикеров
        StringBuilder genderInfo = new StringBuilder();
        for (int i = 0; i < speakers.size(); i++) {
            TabbycatTeamRequest.Speaker speaker = speakers.get(i);
            genderInfo.append("Спикер ").append(i + 1).append(": ").append(speaker.getGender());
            if (i < speakers.size() - 1) {
                genderInfo.append(", ");
            }
        }
        
        log.info("Построен запрос для Tabbycat: команда={}, спикеров={}, гендеры=[{}]", 
                teamName, speakers.size(), genderInfo.toString());
        
        return request;
    }
    
    private TabbycatTeamRequest.Speaker createSpeakerFromUser(User user) {
        TabbycatTeamRequest.Speaker speaker = new TabbycatTeamRequest.Speaker();
        
        // Разбираем fullName на имя и фамилию
        String[] nameParts = user.getFullName().split(" ");
        String firstName = nameParts.length > 1 ? nameParts[1] : nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[0] : "";
        
        // Получаем гендер пользователя
        String userGender = user.getGender() != null ? user.getGender().name() : "O";
        
        speaker.setName(firstName);
        speaker.setLastName(lastName);
        speaker.setEmail(user.getEmail());
        speaker.setPhone(user.getPhone() != null ? user.getPhone() : "");
        speaker.setCodeName(user.getUsername());
        speaker.setGender(userGender); // Используем реальный гендер пользователя
        speaker.setPronoun("");
        speaker.setAnonymous(true);
        speaker.setBarcode(null);
        speaker.setUrlKey("");
        speaker.setCategories(new ArrayList<>());
        speaker.setAnswers(new ArrayList<>());
        
        log.info("Создан спикер: name={}, lastName={}, email={}, username={}, gender={} (из профиля пользователя)", 
                firstName, lastName, user.getEmail(), user.getUsername(), userGender);
        
        return speaker;
    }
}
