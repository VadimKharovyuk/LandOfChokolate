package com.example.landofchokolate.service.serviceImpl;

import com.example.landofchokolate.dto.category.CategoryEditData;
import com.example.landofchokolate.dto.category.CategoryResponseDto;
import com.example.landofchokolate.dto.category.CreateCategoryDto;
import com.example.landofchokolate.mapper.CategoryMapper;
import com.example.landofchokolate.model.Category;
import com.example.landofchokolate.repository.CategoryRepository;
import com.example.landofchokolate.repository.ProductRepository;
import com.example.landofchokolate.service.CategoryService;
import com.example.landofchokolate.service.SlugService;
import com.example.landofchokolate.util.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final SlugService slugService;
    private final ProductRepository productRepository;
    private final StorageService storageService;

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

        // 🆕 Обрабатываем изображение если оно есть
        handleImageUpload(createCategoryDto, category);
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
        // 🆕 Обрабатываем изображение если оно есть
        handleImageUpdate(updateCategoryDto, existingCategory);

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
        // 🆕 Удаляем изображение перед удалением категории
        deleteImageIfExists(category);
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
     * Обрабатывает загрузку изображения при создании категории
     */
    private void handleImageUpload(CreateCategoryDto dto, Category category) {
        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            try {
                log.info("Uploading image for category: {}", dto.getName());

                // Загружаем изображение через StorageService
                StorageService.StorageResult result = storageService.uploadImage(dto.getImage());

                category.setImageUrl(result.getUrl());
                category.setImageId(result.getImageId());

                log.info("Image uploaded successfully. URL: {}, ID: {}", result.getUrl(), result.getImageId());
            } catch (Exception e) {
                log.error("Error uploading image for category: {}", dto.getName(), e);
                throw new RuntimeException("Помилка завантаження зображення: " + e.getMessage());
            }
        } else {
            log.debug("No image provided for category: {}", dto.getName());
        }
    }



    /**
     * Обрабатывает обновление изображения при редактировании категории
     */
    private void handleImageUpdate(CreateCategoryDto dto, Category existingCategory) {
        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            try {
                log.info("Updating image for category id: {}", existingCategory.getId());

                // Удаляем старое изображение если оно есть
                deleteImageIfExists(existingCategory);

                // Загружаем новое изображение через StorageService
                StorageService.StorageResult result = storageService.uploadImage(dto.getImage());

                existingCategory.setImageUrl(result.getUrl());
                existingCategory.setImageId(result.getImageId());

                log.info("Image updated successfully. New URL: {}, ID: {}", result.getUrl(), result.getImageId());
            } catch (Exception e) {
                log.error("Error updating image for category id: {}", existingCategory.getId(), e);
                throw new RuntimeException("Помилка оновлення зображення: " + e.getMessage());
            }
        } else {
            log.debug("No new image provided for category id: {}", existingCategory.getId());
        }
    }

    /**
     * Удаляет изображение если оно существует
     */
    private void deleteImageIfExists(Category category) {
        if (category.getImageId() != null && !category.getImageId().isEmpty()) {
            try {
                log.info("Deleting image for category id: {}, imageId: {}",
                        category.getId(), category.getImageId());

                boolean deleted = storageService.deleteImage(category.getImageId());

                if (deleted) {
                    log.info("Image deleted successfully for category id: {}", category.getId());
                } else {
                    log.warn("Image deletion returned false for category id: {}, imageId: {}",
                            category.getId(), category.getImageId());
                }
            } catch (Exception e) {
                log.warn("Error deleting image for category id: {}, imageId: {}. Error: {}",
                        category.getId(), category.getImageId(), e.getMessage());
                // Не бросаем исключение, так как это не критично для основной операции
            }
        }
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

    /**
     * Подготавливает данные для редактирования категории
     */
    @Override
    public CategoryEditData prepareEditData(Long id) {
        log.info("Preparing edit data for category with id: {}", id);

        // Получаем категорию
        CategoryResponseDto category = getCategoryById(id);

        // Преобразуем через mapper
        CategoryEditData editData = categoryMapper.toCategoryEditData(category);

        log.debug("Prepared edit data for category id={}, name={}", id, category.getName());
        return editData;
    }


}