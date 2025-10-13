package com.qdeb.service;

import com.qdeb.dto.UserProfileResponse;
import com.qdeb.entity.Gender;
import com.qdeb.entity.Role;
import com.qdeb.entity.Team;
import com.qdeb.entity.User;
import com.qdeb.repository.RoleRepository;
import com.qdeb.repository.TeamRepository;
import com.qdeb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TabbycatService tabbycatService;
    private final TeamRepository teamRepository;
    
    public User createUser(String username, String email, String password, String fullName, 
                          Gender gender, String phone, String description, String profilePicturePath) {
        // Сначала создаем пользователя в нашей базе данных
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setGender(gender);
        user.setPhone(phone);
        user.setDescription(description);
        user.setProfilePicture(profilePicturePath);
        
        // Добавляем роль ROLE_USERS по умолчанию
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USERS)
                .orElseThrow(() -> new RuntimeException("Роль ROLE_USERS не найдена"));
        roles.add(userRole);
        user.setRoles(roles);
        
        // Сохраняем пользователя в нашей базе
        User savedUser = userRepository.save(user);
        
        // Создаем пользователя в Tabbycat
        try {
            boolean tabbycatSuccess = tabbycatService.createUser(username, email, password);
            if (!tabbycatSuccess) {
                System.err.println("Предупреждение: Tabbycat - пользователь " + username + " не создан");
            }
        } catch (Exception e) {
            System.err.println("Ошибка Tabbycat: " + e.getMessage());
        }
        
        return savedUser;
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    public User updateProfilePicture(Long userId, String profilePicturePath) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setProfilePicture(profilePicturePath);
        return userRepository.save(user);
    }
    
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }
    
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }
    
    public UserProfileResponse getUserProfile(Long userId) {
        User user = getUserById(userId);
        
        // Находим команду пользователя (как лидера или как участника)
        Optional<Team> team = findUserTeam(userId);
        
        return new UserProfileResponse(user, team.orElse(null));
    }
    
    public UserProfileResponse getUserProfileByUsername(String username) {
        User user = getUserByUsername(username);
        
        // Находим команду пользователя (как лидера или как участника)
        Optional<Team> team = findUserTeam(user.getId());
        
        return new UserProfileResponse(user, team.orElse(null));
    }
    
    private Optional<Team> findUserTeam(Long userId) {
        // Сначала проверяем, является ли пользователь лидером команды
        Optional<Team> leaderTeam = teamRepository.findByLeaderId(userId);
        if (leaderTeam.isPresent()) {
            return leaderTeam;
        }
        
        // Если не лидер, проверяем, является ли участником команды
        return teamRepository.findByUserId(userId);
    }
}
