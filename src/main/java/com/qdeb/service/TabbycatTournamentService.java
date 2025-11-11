package com.qdeb.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TabbycatTournamentService {
    
    private final RestTemplate restTemplate;
    
    @Value("${tabbycat.api.url}")
    private String tabbycatApiUrl;
    
    @Value("${tabbycat.api.key}")
    private String tabbycatApiKey;
    
    public boolean createTournament(String name, String shortName, Integer seq, String slug, boolean active) {
        try {
            String url = tabbycatApiUrl + "/tournaments";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Пробуем разные форматы авторизации
            headers.set("Authorization", "Token " + tabbycatApiKey);
            // Альтернативно: headers.set("Authorization", "Bearer " + tabbycatApiKey);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", name);
            requestBody.put("short_name", shortName);
            requestBody.put("seq", seq);
            requestBody.put("slug", slug);
            requestBody.put("active", active);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            log.info("Отправка запроса на создание турнира в Tabbycat: {}", url);
            log.info("API ключ: {}", tabbycatApiKey);
            log.info("Данные запроса: {}", requestBody);
            log.info("Заголовки: {}", headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Турнир успешно создан в Tabbycat. Ответ: {}", response.getBody());
                return true;
            } else {
                log.error("Ошибка при создании турнира в Tabbycat. Статус: {}, Ответ: {}", 
                         response.getStatusCode(), response.getBody());
                return false;
            }
            
        } catch (Exception e) {
            log.error("Исключение при создании турнира в Tabbycat: {}", e.getMessage(), e);
            if (e.getMessage().contains("401")) {
                log.error("Ошибка авторизации - проверьте API ключ: {}", tabbycatApiKey);
            }
            return false;
        }
    }
    
    public boolean createRound(String tournamentSlug, Map<String, Object> roundData) {
        try {
            String url = tabbycatApiUrl + "/tournaments/" + tournamentSlug + "/rounds";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Token " + tabbycatApiKey);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(roundData, headers);
            
            log.info("Отправка запроса на создание раунда в Tabbycat: {}", url);
            log.info("Данные раунда: {}", roundData);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Раунд успешно создан в Tabbycat. Ответ: {}", response.getBody());
                return true;
            } else {
                log.error("Ошибка при создании раунда в Tabbycat. Статус: {}, Ответ: {}", 
                         response.getStatusCode(), response.getBody());
                return false;
            }
            
        } catch (Exception e) {
            log.error("Исключение при создании раунда в Tabbycat: {}", e.getMessage(), e);
            return false;
        }
    }
}
