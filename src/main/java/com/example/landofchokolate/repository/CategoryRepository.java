package com.example.landofchokolate.repository;

import com.example.landofchokolate.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Проверяет существование категории по имени
     */
    boolean existsByName(String name);

    /**
     * Находит категорию по точному имени
     */
    Optional<Category> findByName(String name);

    /**
     * Поиск категорий по имени (содержит подстроку, игнорируя регистр)
     */
    List<Category> findByNameContainingIgnoreCase(String name);

    /**
     * Поиск категорий по имени, игнорируя регистр
     */
    List<Category> findByNameIgnoreCase(String name);


    // 🆕 Добавить этот метод:
    boolean existsBySlug(String slug);

    // 🆕 И этот для поиска по slug:
    Optional<Category> findBySlug(String slug);


    List<Category> findBySlugIsNull();


    /**
     * Находит активные категории с пагинацией
     */
    Page<Category> findByIsActiveTrue(Pageable pageable);

    /**
     * Находит активные категории по имени с пагинацией
     */
    Page<Category> findByIsActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Получить топовые категории с лимитом
     */
    List<Category> findByIsActiveTrueAndIsFeaturedTrueOrderByNameAsc(Pageable pageable);



    Optional<Category> findBySlugAndIsActiveTrue(String slug);


}