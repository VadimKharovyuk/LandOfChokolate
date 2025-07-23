package com.example.landofchokolate.service.serviceImpl;

import com.example.landofchokolate.dto.category.*;
import com.example.landofchokolate.exception.CategoryNotFoundException;
import com.example.landofchokolate.mapper.CategoryMapper;
import com.example.landofchokolate.model.Category;
import com.example.landofchokolate.repository.CategoryRepository;
import com.example.landofchokolate.repository.ProductRepository;
import com.example.landofchokolate.service.CategoryService;
import com.example.landofchokolate.service.SlugService;
import com.example.landofchokolate.util.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        // Возвращаем ResponseDto
        return categoryMapper.toResponseDto(savedCategory);
    }

    @Override
    public CategoryResponseDto updateCategory(Long id, CreateCategoryDto updateCategoryDto) {

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


        return categoryMapper.toResponseDto(savedCategory);
    }

    @Override
    public void deleteCategory(Long id) {
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
    }

    @Override
    public CategoryResponseDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        return categoryMapper.toResponseDto(category);
    }

    @Override
    public List<CategoryResponseDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categoryMapper.toResponseDtoList(categories);
    }

    @Override
    public List<CategoryResponseDto> getCategoriesByName(String name) {
        List<Category> categories = categoryRepository.findByNameContainingIgnoreCase(name);
        return categoryMapper.toResponseDtoList(categories);
    }

    /**
     * Обрабатывает загрузку изображения при создании категории
     */
    private void handleImageUpload(CreateCategoryDto dto, Category category) {
        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            try {

                // Загружаем изображение через StorageService
                StorageService.StorageResult result = storageService.uploadImage(dto.getImage());

                category.setImageUrl(result.getUrl());
                category.setImageId(result.getImageId());

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

                // Удаляем старое изображение если оно есть
                deleteImageIfExists(existingCategory);

                // Загружаем новое изображение через StorageService
                StorageService.StorageResult result = storageService.uploadImage(dto.getImage());

                existingCategory.setImageUrl(result.getUrl());
                existingCategory.setImageId(result.getImageId());

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
        }
    }

    /**
     * Подготавливает данные для редактирования категории
     */
    @Override
    public CategoryEditData prepareEditData(Long id) {

        // Получаем категорию
        CategoryResponseDto category = getCategoryById(id);

        // Преобразуем через mapper
        CategoryEditData editData = categoryMapper.toCategoryEditData(category);

        log.debug("Prepared edit data for category id={}, name={}", id, category.getName());
        return editData;
    }

    @Override
    public CategoryListPublicDto getPublicCategories(int page, int size) {
        // Создаем Pageable с сортировкой по названию
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        // Получаем только активные категории
        Page<Category> categoryPage = categoryRepository.findByIsActiveTrue(pageable);

        // Преобразуем в PublicDto с ценовой информацией
        List<CategoryPublicDto> publicDtos = categoryPage.getContent().stream()
                .map(this::enrichCategoryWithPriceInfo)
                .collect(Collectors.toList());

        // Генерируем номера страниц для навигации
        List<Integer> pageNumbers = generatePageNumbers(page, categoryPage.getTotalPages());

        // Создаем результирующий DTO
        CategoryListPublicDto result = CategoryListPublicDto.builder()
                .categories(publicDtos)
                .totalCount((int) categoryPage.getTotalElements())
                .currentPage(page)
                .pageSize(size)
                .totalPages(categoryPage.getTotalPages())
                .hasNext(categoryPage.hasNext())
                .hasPrevious(categoryPage.hasPrevious())
                .nextPage(categoryPage.hasNext() ? page + 1 : null)
                .previousPage(categoryPage.hasPrevious() ? page - 1 : null)
                .pageNumbers(pageNumbers)
                .build();

        return result;
    }

    @Override
    public List<CategoryPublicDto> getTopCategories(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Category> featuredCategories = categoryRepository
                .findByIsActiveTrueAndIsFeaturedTrueOrderByNameAsc(pageable);

        // Обогащаем ценовой информацией
        List<CategoryPublicDto> result = featuredCategories.stream()
                .map(this::enrichCategoryWithPriceInfo)
                .collect(Collectors.toList());

        return result;
    }

    @Override
    public Category findBySlug(String slug) {
        return categoryRepository.findBySlugAndIsActiveTrue(slug)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + slug));
    }

    /**
     * Обогащает категорию ценовой информацией
     */
    private CategoryPublicDto enrichCategoryWithPriceInfo(Category category) {
        try {
            // Получаем статистику цен для категории
            Map<String, Object> priceStats = productRepository.findPriceStatsByCategoryId(category.getId());

            BigDecimal minPrice = (BigDecimal) priceStats.get("minPrice");
            BigDecimal maxPrice = (BigDecimal) priceStats.get("maxPrice");
            Integer productCount = ((Number) priceStats.get("productCount")).intValue();

            return categoryMapper.toPublicDto(category, minPrice, maxPrice, productCount);

        } catch (Exception e) {
            log.warn("Error getting price info for category id={}: {}", category.getId(), e.getMessage());
            // Возвращаем категорию без ценовой информации
            return categoryMapper.toPublicDto(category, null, null, 0);
        }
    }

    /**
     * Генерирует список номеров страниц для пагинации
     */
    private List<Integer> generatePageNumbers(int currentPage, int totalPages) {
        List<Integer> pageNumbers = new ArrayList<>();

        if (totalPages <= 0) {
            return pageNumbers;
        }

        // Определяем диапазон страниц для отображения (например, показываем 5 страниц)
        int maxPagesToShow = 5;
        int startPage = Math.max(0, currentPage - maxPagesToShow / 2);
        int endPage = Math.min(totalPages - 1, startPage + maxPagesToShow - 1);

        // Корректируем startPage если endPage достиг максимума
        if (endPage - startPage < maxPagesToShow - 1) {
            startPage = Math.max(0, endPage - maxPagesToShow + 1);
        }

        // Добавляем номера страниц
        for (int i = startPage; i <= endPage; i++) {
            pageNumbers.add(i);
        }

        return pageNumbers;
    }


}