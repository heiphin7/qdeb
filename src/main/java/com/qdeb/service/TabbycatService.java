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
    
    public boolean createUser(String username, String email, String password) {
        try {
            String url = tabbycatApiUrl + "/users";
            
            // Создаем запрос для Tabbycat
            TabbycatUserRequest tabbycatUser = new TabbycatUserRequest(username, password, email);
            
            // Логируем данные, которые отправляем (компактно)
            log.info("Отправляем запрос в Tabbycat: username={}, email={}", username, email);
            
            // Логируем JSON, который отправляем
            String requestBody = objectMapper.writeValueAsString(tabbycatUser);
            log.info("JSON запрос: {}", requestBody);
            
            // Настраиваем заголовки
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Token " + tabbycatApiKey);
            
            // Создаем HTTP entity
            HttpEntity<TabbycatUserRequest> request = new HttpEntity<>(tabbycatUser, headers);
            
            // Отправляем запрос
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            // Логируем только статус и краткий ответ
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Tabbycat: Успешно создан пользователь {}", username);
                return true;
            } else {
                String responseBody = response.getBody();
                // Обрезаем длинный HTML ответ
                if (responseBody != null && responseBody.length() > 200) {
                    responseBody = responseBody.substring(0, 200) + "...";
                }
                log.error("Tabbycat: Ошибка {} - {}", response.getStatusCode(), responseBody);
                return false;
            }
            
        } catch (Exception e) {
            log.error("Ошибка при интеграции с Tabbycat: {}", e.getMessage());
            // Логируем только краткую информацию об ошибке
            if (e.getMessage().contains("500")) {
                log.error("Tabbycat вернул ошибку 500 - возможно, проблема на стороне сервера Tabbycat");
            } else if (e.getMessage().contains("Connection refused")) {
                log.error("Tabbycat недоступен - проверьте, что сервер запущен на localhost:8000");
            }
            return false;
        }
    }
}
