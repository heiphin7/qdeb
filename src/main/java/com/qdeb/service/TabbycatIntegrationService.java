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
        request.setEmoji(null);
        
        // Поля, которые оставляем null
        request.setInstitution(null);
        request.setBreakCategories(null);
        request.setInstitutionConflicts(null);
        request.setVenueConstraints(null);
        request.setAnswers(null);
        
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
        
        log.info("Построен запрос для Tabbycat: команда={}, спикеров={}", 
                teamName, speakers.size());
        
        return request;
    }
    
    private TabbycatTeamRequest.Speaker createSpeakerFromUser(User user) {
        TabbycatTeamRequest.Speaker speaker = new TabbycatTeamRequest.Speaker();
        
        // Разбираем fullName на имя и фамилию
        String[] nameParts = user.getFullName().split(" ");
        String firstName = nameParts.length > 1 ? nameParts[1] : nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[0] : "";
        
        speaker.setName(firstName);
        speaker.setLastName(lastName);
        speaker.setEmail(user.getEmail());
        speaker.setPhone(user.getPhone());
        speaker.setCodeName(user.getUsername());
        speaker.setGender("O"); // По умолчанию "Other"
        speaker.setPronoun(null);
        speaker.setAnonymous(true);
        speaker.setBarcode(null);
        speaker.setUrlKey(null);
        speaker.setCategories(new ArrayList<>());
        speaker.setAnswers(new ArrayList<>());
        
        log.info("Создан спикер: name={}, lastName={}, email={}, username={}", 
                firstName, lastName, user.getEmail(), user.getUsername());
        
        return speaker;
    }
}
