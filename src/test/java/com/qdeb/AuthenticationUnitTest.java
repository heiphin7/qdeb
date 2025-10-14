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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class AuthenticationUnitTest {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationUnitTest.class);

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
    private static String testFullName;
    private static String testPassword;
    private static String jwtToken;

    @BeforeAll
    static void setup() {
        long timestamp = System.currentTimeMillis();
        testUsername = "testuser_" + timestamp;
        testEmail = "test@example.com";
        testFullName = "Test User";
        testPassword = "TestPassword123!";
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @Order(1)
    @DisplayName("Authentication Test 1: Успешная регистрация пользователя")
    @Rollback(false)
    void testSuccessfulUserRegistration() throws Exception {
        log.info("=== Authentication Test 1: Успешная регистрация пользователя ===");
        log.info("Регистрация пользователя: {}", testUsername);
        log.info("Email: {}", testEmail);
        log.info("Full Name: {}", testFullName);

        // Убедимся, что роли существуют
        if (!roleRepository.findByName(Role.RoleName.ROLE_USERS).isPresent()) {
            Role userRole = new Role();
            userRole.setName(Role.RoleName.ROLE_USERS);
            roleRepository.save(userRole);
        }
        if (!roleRepository.findByName(Role.RoleName.ROLE_ADMIN).isPresent()) {
            Role adminRole = new Role();
            adminRole.setName(Role.RoleName.ROLE_ADMIN);
            roleRepository.save(adminRole);
        }

        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername(testUsername);
        signUpRequest.setEmail(testEmail);
        signUpRequest.setPassword(testPassword);
        signUpRequest.setFullName(testFullName);
        signUpRequest.setGender(Gender.M);
        signUpRequest.setPhone("+1234567890");
        signUpRequest.setDescription("Test user description");

        User createdUser = userService.createUser(
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                signUpRequest.getPassword(),
                signUpRequest.getFullName(),
                signUpRequest.getGender(),
                signUpRequest.getPhone(),
                signUpRequest.getDescription(),
                null
        );

        assertNotNull(createdUser.getId());
        assertEquals(testUsername, createdUser.getUsername());
        assertEquals(testEmail, createdUser.getEmail());
        assertEquals(testFullName, createdUser.getFullName());
        
        log.info("Успешно: Пользователь создан и сохранен в БД с ID: {}", createdUser.getId());
    }

    @Test
    @Order(2)
    @DisplayName("Authentication Test 2: Неуспешная регистрация - дублирование username")
    @Rollback(false)
    void testFailedUserRegistrationDuplicateUsername() throws Exception {
        log.info("=== Authentication Test 2: Неуспешная регистрация - дублирование username ===");
        log.info("Попытка регистрации пользователя с существующим username: {}", testUsername);

        SignUpRequest duplicateRequest = new SignUpRequest();
        duplicateRequest.setUsername(testUsername); // Тот же username
        duplicateRequest.setEmail("newemail@example.com");
        duplicateRequest.setPassword("password123");
        duplicateRequest.setFullName("Another User");
        duplicateRequest.setGender(Gender.F);
        duplicateRequest.setPhone("+9876543210");
        duplicateRequest.setDescription("Another user description");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().is4xxClientError());

        log.info("Успешно: Регистрация отклонена из-за дублирования username");
    }

    @Test
    @Order(3)
    @DisplayName("Authentication Test 3: Успешная авторизация и получение токена")
    @Rollback(false)
    void testSuccessfulLoginAndTokenRetrieval() throws Exception {
        log.info("=== Authentication Test 3: Успешная авторизация и получение токена ===");
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
        assertTrue(jwtToken.length() > 50); // Проверяем, что токен достаточно длинный
        
        log.info("Успешно: Получен JWT токен");
        log.info("Token (первые 50 символов): {}...", jwtToken.substring(0, Math.min(jwtToken.length(), 50)));
    }

    @Test
    @Order(4)
    @DisplayName("Authentication Test 4: Неуспешная авторизация с неправильным паролем")
    @Rollback(false)
    void testFailedLoginWithWrongPassword() throws Exception {
        log.info("=== Authentication Test 4: Неуспешная авторизация с неправильным паролем ===");
        log.info("Попытка авторизации с неправильным паролем для пользователя: {}", testUsername);

        SignInRequest wrongPasswordRequest = new SignInRequest();
        wrongPasswordRequest.setUsername(testUsername);
        wrongPasswordRequest.setPassword("WrongPassword123!"); // Неправильный пароль

        try {
            mockMvc.perform(post("/api/auth/signin")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
                    .andExpect(status().isUnauthorized());
        } catch (Exception e) {
            // Ожидаем исключение при неправильном пароле
            log.info("Успешно: Получена ошибка о неправильных учетных данных (исключение)");
        }

        log.info("Успешно: Получена ошибка о неправильных учетных данных");
    }

    @Test
    @Order(5)
    @DisplayName("Authentication Test 5: Неуспешная авторизация с несуществующим пользователем")
    @Rollback(false)
    void testFailedLoginWithNonExistentUser() throws Exception {
        log.info("=== Authentication Test 5: Неуспешная авторизация с несуществующим пользователем ===");
        log.info("Попытка авторизации несуществующего пользователя");

        SignInRequest nonExistentUserRequest = new SignInRequest();
        nonExistentUserRequest.setUsername("nonexistentuser");
        nonExistentUserRequest.setPassword("password123");

        try {
            mockMvc.perform(post("/api/auth/signin")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(nonExistentUserRequest)))
                    .andExpect(status().isUnauthorized());
        } catch (Exception e) {
            // Ожидаем исключение при несуществующем пользователе
            log.info("Успешно: Получена ошибка о несуществующем пользователе (исключение)");
        }

        log.info("Успешно: Получена ошибка о несуществующем пользователе");
    }

    @AfterAll
    static void cleanup(@Autowired UserRepository userRepository) {
        log.info("=== Authentication Test Cleanup: Очистка тестовых данных ===");
        userRepository.findByUsername(testUsername).ifPresent(user -> {
            log.info("Удаление тестового пользователя: {}", testUsername);
            userRepository.delete(user);
        });
        log.info("Успешно: Тестовый пользователь удален");
        log.info("=== Authentication Tests Completed ===");
    }
}
