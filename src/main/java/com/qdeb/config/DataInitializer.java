package com.qdeb.config;

import com.qdeb.entity.Role;
import com.qdeb.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final RoleRepository roleRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // Создаем роли, если они не существуют
        if (!roleRepository.findByName(Role.RoleName.ROLE_USERS).isPresent()) {
            Role userRole = new Role();
            userRole.setName(Role.RoleName.ROLE_USERS);
            roleRepository.save(userRole);
            System.out.println("Роль ROLE_USERS создана");
        }
        
        if (!roleRepository.findByName(Role.RoleName.ROLE_ADMIN).isPresent()) {
            Role adminRole = new Role();
            adminRole.setName(Role.RoleName.ROLE_ADMIN);
            roleRepository.save(adminRole);
            System.out.println("Роль ROLE_ADMIN создана");
        }
    }
}
