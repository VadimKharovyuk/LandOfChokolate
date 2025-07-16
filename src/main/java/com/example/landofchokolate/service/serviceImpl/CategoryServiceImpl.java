package com.example.landofchokolate.service.serviceImpl;

import com.example.landofchokolate.dto.category.CategoryResponseDto;
import com.example.landofchokolate.dto.category.CreateCategoryDto;
import com.example.landofchokolate.mapper.CategoryMapper;
import com.example.landofchokolate.model.Category;
import com.example.landofchokolate.repository.CategoryRepository;
import com.example.landofchokolate.repository.ProductRepository;
import com.example.landofchokolate.service.CategoryService;
import com.example.landofchokolate.service.SlugService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final SlugService slugService;
    private final ProductRepository productRepository;

    @Override
    public CategoryResponseDto createCategory(CreateCategoryDto createCategoryDto) {
        log.info("Creating category with name: {}", createCategoryDto.getName());

        // Проверяем, что категория с таким именем не существует
        if (categoryRepository.existsByName(createCategoryDto.getName())) {
            throw new RuntimeException("Category with name '" + createCategoryDto.getName() + "' already exists");
        }

        // Преобразуем DTO в Entity
        Category category = categoryMapper.toEntity(createCategoryDto);

        // 🆕 Генерируем slug через сервис
        category.setSlug(slugService.generateUniqueSlugForCategory(createCategoryDto.getName()));

        // Сохраняем в базе данных
        Category savedCategory = categoryRepository.save(category);

        log.info("Category created successfully with id: {}", savedCategory.getId());

        // Возвращаем ResponseDto
        return categoryMapper.toResponseDto(savedCategory);
    }

    @Override
    public CategoryResponseDto updateCategory(Long id, CreateCategoryDto updateCategoryDto) {
        log.info("Updating category with id: {}", id);

        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Проверяем, что новое имя не используется другой категорией
        if (!existingCategory.getName().equals(updateCategoryDto.getName()) &&
                categoryRepository.existsByName(updateCategoryDto.getName())) {
            throw new RuntimeException("Category with name '" + updateCategoryDto.getName() + "' already exists");
        }

        // Обновляем существующую entity
        categoryMapper.updateEntityFromDto(updateCategoryDto, existingCategory);

        // Сохраняем изменения
        Category savedCategory = categoryRepository.save(existingCategory);

        log.info("Category updated successfully with id: {}", savedCategory.getId());

        return categoryMapper.toResponseDto(savedCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        log.info("Attempting to delete category with id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Категорію з id " + id + " не знайдено"));

        // Считаем количество продуктов в категории
        long productCount = productRepository.countByCategoryId(id);

        if (productCount > 0) {
            throw new RuntimeException(
                    String.format("Неможливо видалити категорію '%s', оскільки вона містить %d товар(ів). " +
                                    "Будь ласка, спочатку перемістіть або видаліть товари.",
                            category.getName(), productCount)
            );
        }

        categoryRepository.deleteById(id);
        log.info("Category '{}' deleted successfully with id: {}", category.getName(), id);
    }

    @Override
    public CategoryResponseDto getCategoryById(Long id) {
        log.info("Getting category with id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        return categoryMapper.toResponseDto(category);
    }

    @Override
    public List<CategoryResponseDto> getAllCategories() {
        log.info("Getting all categories");

        List<Category> categories = categoryRepository.findAll();
        log.info("Found {} categories", categories.size());

        return categoryMapper.toResponseDtoList(categories);
    }

    @Override
    public List<CategoryResponseDto> getCategoriesByName(String name) {
        log.info("Searching categories by name: {}", name);

        List<Category> categories = categoryRepository.findByNameContainingIgnoreCase(name);
        log.info("Found {} categories matching name: {}", categories.size(), name);

        return categoryMapper.toResponseDtoList(categories);
    }


    /**
     * Генерирует slug для всех категорий где он null
     */
    @Override
    @Transactional
    public void generateMissingSlugForAllCategories() {
        List<Category> categoriesWithoutSlug = categoryRepository.findBySlugIsNull();

        for (Category category : categoriesWithoutSlug) {
            String slug = slugService.generateUniqueSlugForCategory(category.getName());
            category.setSlug(slug);
            categoryRepository.save(category);
            log.info("Generated slug '{}' for category id={} name='{}'",
                    slug, category.getId(), category.getName());
        }

        log.info("Generated slugs for {} categories", categoriesWithoutSlug.size());
    }
}