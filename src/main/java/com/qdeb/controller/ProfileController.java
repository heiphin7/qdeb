package com.qdeb.controller;

import com.qdeb.dto.UserProfileResponse;
import com.qdeb.entity.User;
import com.qdeb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    
    private final UserService userService;
    
    @GetMapping
    public ResponseEntity<?> getMyProfile() {
        try {
            User currentUser = getCurrentUser();
            UserProfileResponse profile = userService.getUserProfile(currentUser.getId());
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при получении профиля: " + e.getMessage());
        }
    }
    
    @GetMapping("/{username}")
    public ResponseEntity<?> getUserProfile(@PathVariable String username) {
        try {
            UserProfileResponse profile = userService.getUserProfileByUsername(username);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при получении профиля: " + e.getMessage());
        }
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}
