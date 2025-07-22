package com.example.landofchokolate.repository;

import com.example.landofchokolate.enums.AdminType;
import com.example.landofchokolate.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    int countByAdminType(AdminType adminType);


    Optional<User> findByUsername(@NotBlank(message = "Имя пользователя не может быть пустым") @Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов") String username);
}
