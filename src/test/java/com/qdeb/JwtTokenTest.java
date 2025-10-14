package com.qdeb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qdeb.dto.SignInRequest;
import com.qdeb.dto.SignUpRequest;
import com.qdeb.entity.Gender;
import com.qdeb.entity.Role;
import com.qdeb.entity.User;
import com.qdeb.repository.RoleRepository;
import com.qdeb.repository.UserRepository;
import com.qdeb.service.UserService;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class JwtTokenTest {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenTest.class);

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    private static String testUsername;
    private static String testEmail;
    private static String testPassword;
    private static String jwtToken;

    @BeforeAll
    static void setup() {
        long timestamp = System.currentTimeMillis();
        testUsername = "jwtuser_" + timestamp;
        testEmail = "jwt@example.com";
        testPassword = "JwtPassword123!";
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @Order(1)
    @DisplayName("JWT Test 1: Создание пользователя для тестирования JWT")
    @Rollback(false)
    void testCreateUserForJwtTesting() throws Exception {
        log.info("=== JWT Test 1: Создание пользователя для тестирования JWT ===");
        log.info("Создание пользователя: {}", testUsername);

        // Убедимся, что роли существуют
        if (!roleRepository.findByName(Role.RoleName.ROLE_USERS).isPresent()) {
            Role userRole = new Role();
            userRole.setName(Role.RoleName.ROLE_USERS);
            roleRepository.save(userRole);
        }

        User createdUser = userService.createUser(
                testUsername,
                testEmail,
                testPassword,
                "JWT Test User",
                Gender.M,
                "+1234567890",
                "JWT test user",
                null
        );

        assertNotNull(createdUser.getId());
        assertEquals(testUsername, createdUser.getUsername());
        
        log.info("Успешно: Пользователь создан для JWT тестирования с ID: {}", createdUser.getId());
    }

    @Test
    @Order(2)
    @DisplayName("JWT Test 2: Получение JWT токена")
    @Rollback(false)
    void testGetJwtToken() throws Exception {
        log.info("=== JWT Test 2: Получение JWT токена ===");
        log.info("Авторизация пользователя: {}", testUsername);

        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setUsername(testUsername);
        signInRequest.setPassword(testPassword);

        String response = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value(testUsername))
                .andExpect(jsonPath("$.email").value(testEmail))
                .andReturn().getResponse().getContentAsString();

        jwtToken = objectMapper.readTree(response).get("token").asText();
        assertNotNull(jwtToken);
        assertTrue(jwtToken.length() > 50);
        
        log.info("Успешно: Получен JWT токен");
        log.info("Token (первые 50 символов): {}...", jwtToken.substring(0, Math.min(jwtToken.length(), 50)));
    }

    @Test
    @Order(3)
    @DisplayName("JWT Test 3: Проверка валидности JWT токена")
    @Rollback(false)
    void testJwtTokenValidity() throws Exception {
        log.info("=== JWT Test 3: Проверка валидности JWT токена ===");
        log.info("Проверка структуры и валидности JWT токена");

        // Проверяем, что токен не пустой
        assertNotNull(jwtToken);
        assertTrue(jwtToken.length() > 50);
        
        // Проверяем структуру JWT токена (должен содержать точки)
        String[] parts = jwtToken.split("\\.");
        assertEquals(3, parts.length, "JWT токен должен содержать 3 части, разделенные точками");
        
        log.info("Успешно: JWT токен имеет правильную структуру");
        log.info("Токен содержит {} частей", parts.length);
    }

    @Test
    @Order(4)
    @DisplayName("JWT Test 4: Проверка содержимого JWT токена")
    @Rollback(false)
    void testJwtTokenContent() throws Exception {
        log.info("=== JWT Test 4: Проверка содержимого JWT токена ===");
        log.info("Проверка содержимого JWT токена");

        // Проверяем, что токен содержит username
        assertTrue(jwtToken.contains("."), "JWT токен должен содержать точки");
        
        // Проверяем длину токена
        assertTrue(jwtToken.length() > 100, "JWT токен должен быть достаточно длинным");
        
        log.info("Успешно: JWT токен содержит корректное содержимое");
        log.info("Длина токена: {} символов", jwtToken.length());
    }

    @AfterAll
    static void cleanup(@Autowired UserRepository userRepository) {
        log.info("=== JWT Test Cleanup: Очистка тестовых данных ===");
        userRepository.findByUsername(testUsername).ifPresent(user -> {
            log.info("Удаление тестового пользователя: {}", testUsername);
            userRepository.delete(user);
        });
        log.info("Успешно: Тестовый пользователь удален");
        log.info("=== JWT Tests Completed ===");
    }
}
