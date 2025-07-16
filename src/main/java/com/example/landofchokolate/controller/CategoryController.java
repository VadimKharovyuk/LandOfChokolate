package com.example.landofchokolate.controller;

import com.example.landofchokolate.dto.category.CategoryResponseDto;
import com.example.landofchokolate.dto.category.CreateCategoryDto;
import com.example.landofchokolate.service.CategoryService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/category")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public String categoryCreateForm(Model model) {
        model.addAttribute("category", new CreateCategoryDto());
        model.addAttribute("isEdit", false);
        return "admin/category/category-form";
    }

    @PostMapping
    public String categoryCreate(@ModelAttribute("category") @Valid CreateCategoryDto createCategoryDto,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors occurred while creating category: {}", bindingResult.getAllErrors());
            model.addAttribute("isEdit", false);
            return "admin/category/category-form";
        }

        try {
            CategoryResponseDto createdCategory = categoryService.createCategory(createCategoryDto);
            log.info("Category created successfully with id: {}", createdCategory.getId());


            redirectAttributes.addFlashAttribute("successMessage",
                    "Категория '" + createdCategory.getName() + "' успешно создана!");

        } catch (Exception e) {
            log.error("Error creating category: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при создании категории: " + e.getMessage());
        }

        return "redirect:/admin/category/list";
    }

    @GetMapping("/list")
    public String categoryList(Model model) {
        List<CategoryResponseDto> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "admin/category/category-list";
    }

    @GetMapping("/edit/{id}")
    public String categoryEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            CategoryResponseDto category = categoryService.getCategoryById(id);
            CreateCategoryDto editDto = new CreateCategoryDto();

            editDto.setName(category.getName());
            editDto.setShortDescription(category.getShortDescription());
            editDto.setIsActive(category.getIsActive());
            editDto.setMetaDescription(category.getMetaDescription());
            editDto.setMetaTitle(category.getMetaTitle());

            model.addAttribute("category", editDto);
            model.addAttribute("categoryId", id);
            model.addAttribute("isEdit", true);

            // 🆕 Добавляем данные об изображении для отображения в форме
            if (category.getImageUrl() != null) {
                model.addAttribute("currentImageUrl", category.getImageUrl());
            }

            return "admin/category/category-form";

        } catch (Exception e) {
            log.error("Error loading category for edit with id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Категория с ID " + id + " не найдена!");
            return "redirect:/admin/category/list";
        }
    }

    @PostMapping("/edit/{id}")
    public String categoryUpdate(@PathVariable Long id,
                                 @ModelAttribute("category") @Valid CreateCategoryDto updateCategoryDto,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors occurred while updating category with id: {}", id);
            model.addAttribute("categoryId", id);
            model.addAttribute("isEdit", true);
            return "admin/category/category-form";
        }

        try {
            CategoryResponseDto updatedCategory = categoryService.updateCategory(id, updateCategoryDto);
            log.info("Category updated successfully with id: {}", updatedCategory.getId());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Категория '" + updatedCategory.getName() + "' успешно обновлена!");

        } catch (Exception e) {
            log.error("Error updating category with id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при обновлении категории: " + e.getMessage());
        }

        return "redirect:/admin/category/list";
    }

    @GetMapping("/delete/{id}")
    public String categoryDelete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            CategoryResponseDto category = categoryService.getCategoryById(id);
            categoryService.deleteCategory(id);

            log.info("Category deleted successfully with id: {}", id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Категория '" + category.getName() + "' успешно удалена!");

        } catch (Exception e) {
            log.error("Error deleting category with id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении категории: " + e.getMessage());
        }

        return "redirect:/admin/category/list";
    }

    @GetMapping("/search")
    public String categorySearch(@RequestParam(name = "name", required = false) String name, Model model) {
        if (name == null || name.trim().isEmpty()) {
            return "redirect:/admin/category/list";
        }

        try {
            List<CategoryResponseDto> categories = categoryService.getCategoriesByName(name.trim());
            model.addAttribute("categories", categories);
            model.addAttribute("searchQuery", name);
            model.addAttribute("isSearch", true);

            log.info("Found {} categories for search query: {}", categories.size(), name);

            return "admin/category/category-list";

        } catch (Exception e) {
            log.error("Error searching categories with name: {}", name, e);
            model.addAttribute("errorMessage", "Ошибка при поиске категорий: " + e.getMessage());
            return "admin/category/category-list";
        }
    }


    // Добавить в контроллер кнопку или вызвать один раз:
    @PostConstruct
    public void init() {
        categoryService.generateMissingSlugForAllCategories();
    }
}