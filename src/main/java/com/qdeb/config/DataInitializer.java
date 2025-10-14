package com.qdeb.config;

import com.qdeb.entity.Role;
import com.qdeb.entity.User;
import com.qdeb.entity.Gender;
import com.qdeb.repository.RoleRepository;
import com.qdeb.repository.UserRepository;
import com.qdeb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    
    @Override
    public void run(String... args) throws Exception {
        // Создаем роли, если они не существуют
        Role userRole = null;
        Role adminRole = null;
        
        if (!roleRepository.findByName(Role.RoleName.ROLE_USERS).isPresent()) {
            userRole = new Role();
            userRole.setName(Role.RoleName.ROLE_USERS);
            roleRepository.save(userRole);
            System.out.println("Роль ROLE_USERS создана");
        } else {
            userRole = roleRepository.findByName(Role.RoleName.ROLE_USERS).get();
        }
        
        if (!roleRepository.findByName(Role.RoleName.ROLE_ADMIN).isPresent()) {
            adminRole = new Role();
            adminRole.setName(Role.RoleName.ROLE_ADMIN);
            roleRepository.save(adminRole);
            System.out.println("Роль ROLE_ADMIN создана");
        } else {
            adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN).get();
        }
        
        // Создаем админский аккаунт, если он не существует
        if (!userRepository.findByUsername("admin").isPresent()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@qdeb.com");
            admin.setPassword(userService.encodePassword("admin"));
            admin.setFullName("System Administrator");
            admin.setGender(Gender.O);
            admin.setPhone("+0000000000");
            admin.setDescription("System administrator account");
            admin.setProfilePicture(null);
            
            // Назначаем роль администратора
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(adminRole);
            admin.setRoles(adminRoles);
            
            userRepository.save(admin);
            System.out.println("Админский аккаунт создан: username=admin, password=admin");
        } else {
            System.out.println("Админский аккаунт уже существует");
        }
    }
}
