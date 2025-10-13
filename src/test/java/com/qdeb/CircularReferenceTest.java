package com.qdeb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qdeb.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CircularReferenceTest {

    @Test
    void testTournamentSerializationWithoutCircularReference() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Создаем турнир
        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.setName("test3");
        tournament.setSlug("test3");
        tournament.setOrganizerName("someOrganizer");
        tournament.setOrganizerContact("some@gmail.com");
        tournament.setDescription("best tournament in the world");
        tournament.setDate(LocalDate.of(2025, 12, 31));
        tournament.setActive(true);
        tournament.setFee(500);
        tournament.setLevel(TournamentLevel.NATIONAL);
        tournament.setFormat("online");
        tournament.setSeq(1);
        tournament.setTournamentPicture("a836dc1f-e364-4482-9b56-dca579a8c590.png");
        tournament.setCreatedAt(LocalDateTime.parse("2025-10-13T22:43:20.488668"));
        tournament.setUpdatedAt(LocalDateTime.parse("2025-10-13T22:43:20.94508"));
        
        // Создаем поле регистрации
        RegistrationField field = new RegistrationField();
        field.setId(1L);
        field.setName("Full Name");
        field.setType(RegistrationFieldType.DESCRIPTION);
        field.setRequired(true);
        field.setTournament(tournament);
        
        // Добавляем поле в турнир
        List<RegistrationField> fields = new ArrayList<>();
        fields.add(field);
        tournament.setRegistrationFields(fields);
        
        // Сериализуем в JSON
        String json = objectMapper.writeValueAsString(tournament);
        
        // Проверяем, что JSON не содержит циклических ссылок
        assertNotNull(json);
        assertFalse(json.contains("\"tournament\":{"), "JSON не должен содержать циклическую ссылку на tournament в RegistrationField");
        
        // Проверяем, что основные поля турнира присутствуют
        assertTrue(json.contains("\"name\":\"test3\""));
        assertTrue(json.contains("\"slug\":\"test3\""));
        assertTrue(json.contains("\"registrationFields\""));
        
        // Проверяем, что поле регистрации содержит только нужные поля
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"name\":\"Full Name\""));
        assertTrue(json.contains("\"type\":\"DESCRIPTION\""));
        assertTrue(json.contains("\"required\":true"));
        
        System.out.println("Tournament JSON: " + json);
    }
}
