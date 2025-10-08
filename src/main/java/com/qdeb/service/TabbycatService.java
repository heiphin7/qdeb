package com.qdeb.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qdeb.dto.TabbycatUserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class TabbycatService {
    
    @Value("${tabbycat.api.url}")
    private String tabbycatApiUrl;
    
    @Value("${tabbycat.api.key}")
    private String tabbycatApiKey;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public boolean createUser(String fullName, String email, String password) {
        try {
            String url = tabbycatApiUrl + "/users";
            
            // Создаем запрос для Tabbycat
            TabbycatUserRequest tabbycatUser = new TabbycatUserRequest(fullName, email, password);
            
            // Настраиваем заголовки
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Token " + tabbycatApiKey);
            
            // Создаем HTTP entity
            HttpEntity<TabbycatUserRequest> request = new HttpEntity<>(tabbycatUser, headers);
            
            // Отправляем запрос
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Пользователь успешно создан в Tabbycat: {}", fullName);
                return true;
            } else {
                log.error("Ошибка при создании пользователя в Tabbycat: {}", response.getStatusCode());
                return false;
            }
            
        } catch (Exception e) {
            log.error("Ошибка при интеграции с Tabbycat: {}", e.getMessage());
            return false;
        }
    }
}
