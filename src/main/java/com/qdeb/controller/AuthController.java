package com.qdeb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qdeb.dto.JwtResponse;
import com.qdeb.dto.SignInRequest;
import com.qdeb.dto.SignUpRequest;
import com.qdeb.entity.User;
import com.qdeb.repository.UserRepository;
import com.qdeb.service.FileStorageService;
import com.qdeb.service.UserService;
import com.qdeb.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    @GetMapping("/test")
    public String test() {
        return "PISKA!";
    }
    
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody SignInRequest signInRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(signInRequest.getUsername(), signInRequest.getPassword())
        );
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtil.generateToken(userDetails);
        
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        return ResponseEntity.ok(new JwtResponse(jwt, user.getUsername(), user.getEmail()));
    }
    
    /**
     * Регистрация с JSON и фото в multipart/form-data
     * - part "register" -> JSON RegisterRequest
     * - part "profilePicture" -> файл (optional)
     */
    @PostMapping(value = "/signup", consumes = "application/json")
    public ResponseEntity<?> registerUserJson(@Valid @RequestBody SignUpRequest signUpRequest) {
        try {
            // Валидация
            if (userService.existsByUsername(signUpRequest.getUsername())) {
                return ResponseEntity.badRequest()
                        .body("Ошибка: Имя пользователя уже используется!");
            }
            
            if (userService.existsByEmail(signUpRequest.getEmail())) {
                return ResponseEntity.badRequest()
                        .body("Ошибка: Email уже используется!");
            }
            
            // Создание пользователя
            User user = userService.createUser(
                    signUpRequest.getUsername(),
                    signUpRequest.getEmail(),
                    signUpRequest.getPassword(),
                    signUpRequest.getFullName(),
                    signUpRequest.getGender(),
                    signUpRequest.getPhone(),
                    signUpRequest.getDescription(),
                    null
            );
            
            return ResponseEntity.ok("Пользователь успешно зарегистрирован!");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Ошибка при регистрации: " + e.getMessage());
        }
    }
    
    @PostMapping(value = "/signup", consumes = "multipart/form-data")
    public ResponseEntity<?> registerUser(
            @RequestPart("register") String registerJson,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {
        
        try {
            // Парсим JSON
            SignUpRequest signUpRequest = objectMapper.readValue(registerJson, SignUpRequest.class);
            
            // Валидация
            if (userService.existsByUsername(signUpRequest.getUsername())) {
                return ResponseEntity.badRequest()
                        .body("Ошибка: Имя пользователя уже используется!");
            }
            
            if (userService.existsByEmail(signUpRequest.getEmail())) {
                return ResponseEntity.badRequest()
                        .body("Ошибка: Email уже используется!");
            }
            
            // Обработка файла изображения
            String profilePicturePath = null;
            if (profilePicture != null && !profilePicture.isEmpty()) {
                if (!isImageFile(profilePicture)) {
                    return ResponseEntity.badRequest()
                            .body("Поддерживаются только изображения (JPG, PNG, GIF, WebP)");
                }
                profilePicturePath = fileStorageService.storeFile(profilePicture);
            }
            
            // Создание пользователя
            User user = userService.createUser(
                    signUpRequest.getUsername(),
                    signUpRequest.getEmail(),
                    signUpRequest.getPassword(),
                    signUpRequest.getFullName(),
                    signUpRequest.getGender(),
                    signUpRequest.getPhone(),
                    signUpRequest.getDescription(),
                    profilePicturePath
            );
            
            return ResponseEntity.ok("Пользователь успешно зарегистрирован!");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Ошибка при регистрации: " + e.getMessage());
        }
    }
    
    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif") ||
                contentType.equals("image/webp")
        );
    }
}
