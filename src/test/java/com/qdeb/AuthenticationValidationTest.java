package com.qdeb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qdeb.dto.SignUpRequest;
import com.qdeb.entity.Gender;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class AuthenticationValidationTest {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationValidationTest.class);

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @Order(1)
    @DisplayName("Validation Test 1: Регистрация с пустым username")
    void testRegistrationWithEmptyUsername() throws Exception {
        log.info("=== Validation Test 1: Регистрация с пустым username ===");

        SignUpRequest request = new SignUpRequest();
        request.setUsername(""); // Пустой username
        request.setEmail("test@example.com");
        request.setPassword("Password123!");
        request.setFullName("Test User");
        request.setGender(Gender.M);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());

        log.info("Успешно: Регистрация отклонена из-за пустого username");
    }

    @Test
    @Order(2)
    @DisplayName("Validation Test 2: Регистрация с некорректным email")
    void testRegistrationWithInvalidEmail() throws Exception {
        log.info("=== Validation Test 2: Регистрация с некорректным email ===");

        SignUpRequest request = new SignUpRequest();
        request.setUsername("testuser");
        request.setEmail("invalid-email"); // Некорректный email
        request.setPassword("Password123!");
        request.setFullName("Test User");
        request.setGender(Gender.M);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());

        log.info("Успешно: Регистрация отклонена из-за некорректного email");
    }

    @Test
    @Order(3)
    @DisplayName("Validation Test 3: Регистрация со слабым паролем")
    void testRegistrationWithWeakPassword() throws Exception {
        log.info("=== Validation Test 3: Регистрация со слабым паролем ===");

        SignUpRequest request = new SignUpRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("123"); // Слабый пароль
        request.setFullName("Test User");
        request.setGender(Gender.M);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());

        log.info("Успешно: Регистрация отклонена из-за слабого пароля");
    }

    @Test
    @Order(4)
    @DisplayName("Validation Test 4: Регистрация с пустым fullName")
    void testRegistrationWithEmptyFullName() throws Exception {
        log.info("=== Validation Test 4: Регистрация с пустым fullName ===");

        SignUpRequest request = new SignUpRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("Password123!");
        request.setFullName(""); // Пустое имя
        request.setGender(Gender.M);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());

        log.info("Успешно: Регистрация отклонена из-за пустого fullName");
    }

    @Test
    @Order(5)
    @DisplayName("Validation Test 5: Регистрация с null gender")
    void testRegistrationWithNullGender() throws Exception {
        log.info("=== Validation Test 5: Регистрация с null gender ===");

        SignUpRequest request = new SignUpRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("Password123!");
        request.setFullName("Test User");
        request.setGender(null); // Null gender

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());

        log.info("Успешно: Регистрация отклонена из-за null gender");
    }
}
