package com.example.landofchokolate.service;

import com.example.landofchokolate.dto.category.*;
import com.example.landofchokolate.model.Category;
import org.springframework.ui.Model;

import java.util.List;

public interface CategoryService {

    /**
     * Создает новую категорию
     */
    CategoryResponseDto createCategory(CreateCategoryDto createCategoryDto);

    /**
     * Обновляет существующую категорию
     */
    CategoryResponseDto updateCategory(Long id, CreateCategoryDto updateCategoryDto);

    /**
     * Удаляет категорию по ID
     */
    void deleteCategory(Long id);

    /**
     * Получает категорию по ID
     */
    CategoryResponseDto getCategoryById(Long id);

    /**
     * Получает все категории
     */
    List<CategoryResponseDto> getAllCategories();

    /**
     * Поиск категорий по имени (содержит подстроку, игнорируя регистр)
     */
    List<CategoryResponseDto> getCategoriesByName(String name);


     void generateMissingSlugForAllCategories() ;


    CategoryEditData prepareEditData(Long id);


    CategoryListPublicDto getPublicCategories(int page, int size);

    /**
     * Получает топ категории для главной страницы
     */
    List<CategoryPublicDto> getTopCategories(int limit);


    Category findBySlug(String categorySlug);
}