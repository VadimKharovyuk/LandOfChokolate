package com.example.landofchokolate.config;

import com.example.landofchokolate.enums.AdminType;
import com.example.landofchokolate.model.User;
import com.example.landofchokolate.repository.UserRepository;
import com.example.landofchokolate.util.PasswordEncoder;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        createDefaultSuperAdmin();
    }

    private void createDefaultSuperAdmin() {
        // Проверяем, есть ли уже SUPER_ADMIN
        if (userRepository.countByAdminType(AdminType.SUPER_ADMIN) == 0) {
            User superAdmin = new User();
            superAdmin.setUsername("admin");
            superAdmin.setPassword(passwordEncoder.encode("admin"));
            superAdmin.setAdminType(AdminType.SUPER_ADMIN);

            userRepository.save(superAdmin);
            log.info("Создан первоначальный SUPER_ADMIN: username=admin, password=admin");
            log.warn("ВАЖНО: Смените пароль по умолчанию после первого входа!");
        }
    }
}
