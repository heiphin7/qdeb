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
    @DisplayName("JWT Test 3: Доступ к защищенному endpoint с валидным токеном")
    @Rollback(false)
    void testAccessProtectedEndpointWithValidToken() throws Exception {
        log.info("=== JWT Test 3: Доступ к защищенному endpoint с валидным токеном ===");
        log.info("Попытка доступа к защищенному endpoint с валидным токеном");

        mockMvc.perform(get("/api/test/user")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Привет, " + testUsername + "! Это защищенный endpoint для пользователей."));

        log.info("Успешно: Доступ к защищенному endpoint разрешен с валидным токеном");
    }

    @Test
    @Order(4)
    @DisplayName("JWT Test 4: Доступ к защищенному endpoint с невалидным токеном")
    @Rollback(false)
    void testAccessProtectedEndpointWithInvalidToken() throws Exception {
        log.info("=== JWT Test 4: Доступ к защищенному endpoint с невалидным токеном ===");
        log.info("Попытка доступа к защищенному endpoint с невалидным токеном");

        mockMvc.perform(get("/api/test/user")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());

        log.info("Успешно: Доступ к защищенному endpoint отклонен с невалидным токеном");
    }

    @Test
    @Order(5)
    @DisplayName("JWT Test 5: Доступ к защищенному endpoint без токена")
    @Rollback(false)
    void testAccessProtectedEndpointWithoutToken() throws Exception {
        log.info("=== JWT Test 5: Доступ к защищенному endpoint без токена ===");
        log.info("Попытка доступа к защищенному endpoint без токена");

        mockMvc.perform(get("/api/test/user"))
                .andExpect(status().isUnauthorized());

        log.info("Успешно: Доступ к защищенному endpoint отклонен без токена");
    }

    @Test
    @Order(6)
    @DisplayName("JWT Test 6: Получение профиля с валидным токеном")
    @Rollback(false)
    void testGetProfileWithValidToken() throws Exception {
        log.info("=== JWT Test 6: Получение профиля с валидным токеном ===");
        log.info("Попытка получения профиля с валидным токеном");

        mockMvc.perform(get("/api/profile")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(testUsername))
                .andExpect(jsonPath("$.email").value(testEmail));

        log.info("Успешно: Профиль пользователя получен с валидным токеном");
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
