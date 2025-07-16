package com.example.landofchokolate.service;

import com.example.landofchokolate.repository.BrandRepository;
import com.example.landofchokolate.repository.CategoryRepository;
import com.example.landofchokolate.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlugService {
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    /**
     * Генерирует уникальный slug для категории
     */
    public String generateUniqueSlugForCategory(String name) {
        String baseSlug = generateSlug(name);
        String uniqueSlug = baseSlug;
        int counter = 1;

        // Проверяем уникальность и добавляем цифру если нужно
        while (categoryRepository.existsBySlug(uniqueSlug)) {
            uniqueSlug = baseSlug + "-" + counter;
            counter++;
        }

        log.debug("Generated unique category slug: {} for name: {}", uniqueSlug, name);
        return uniqueSlug;
    }

    /**
     * Генерирует уникальный slug для бренда
     */
    public String generateUniqueSlugForBrand(String name) {
        String baseSlug = generateSlug(name);
        String uniqueSlug = baseSlug;
        int counter = 1;

        // Проверяем уникальность и добавляем цифру если нужно
        while (brandRepository.existsBySlug(uniqueSlug)) {
            uniqueSlug = baseSlug + "-" + counter;
            counter++;
        }

        log.debug("Generated unique brand slug: {} for name: {}", uniqueSlug, name);
        return uniqueSlug;
    }

    /**
     * Генерирует уникальный slug для продукта
     */
    public String generateUniqueSlugForProduct(String name) {
        String baseSlug = generateSlug(name);
        String uniqueSlug = baseSlug;
        int counter = 1;

        // Проверяем уникальность и добавляем цифру если нужно
        while (productRepository.existsBySlug(uniqueSlug)) {
            uniqueSlug = baseSlug + "-" + counter;
            counter++;
        }

        log.debug("Generated unique product slug: {} for name: {}", uniqueSlug, name);
        return uniqueSlug;
    }

    /**
     * Генерирует slug из названия (базовый метод)
     */
    private String generateSlug(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "item";
        }

        return name.toLowerCase()
                .trim()
                // Транслитерация кириллицы
                .replaceAll("а", "a").replaceAll("б", "b").replaceAll("в", "v")
                .replaceAll("г", "g").replaceAll("д", "d").replaceAll("е", "e")
                .replaceAll("ё", "e").replaceAll("ж", "zh").replaceAll("з", "z")
                .replaceAll("и", "i").replaceAll("й", "y").replaceAll("к", "k")
                .replaceAll("л", "l").replaceAll("м", "m").replaceAll("н", "n")
                .replaceAll("о", "o").replaceAll("п", "p").replaceAll("р", "r")
                .replaceAll("с", "s").replaceAll("т", "t").replaceAll("у", "u")
                .replaceAll("ф", "f").replaceAll("х", "h").replaceAll("ц", "ts")
                .replaceAll("ч", "ch").replaceAll("ш", "sh").replaceAll("щ", "sch")
                .replaceAll("ъ", "").replaceAll("ы", "y").replaceAll("ь", "")
                .replaceAll("э", "e").replaceAll("ю", "yu").replaceAll("я", "ya")
                // Украинские буквы
                .replaceAll("і", "i").replaceAll("ї", "yi").replaceAll("є", "ye")
                .replaceAll("ґ", "g")
                // Удаляем все кроме букв, цифр и пробелов
                .replaceAll("[^a-z0-9\\s]", "")
                // Заменяем пробелы на дефисы
                .replaceAll("\\s+", "-")
                // Удаляем множественные дефисы
                .replaceAll("-+", "-")
                // Удаляем дефисы в начале и конце
                .replaceAll("^-|-$", "");
    }

    /**
     * Проверяет доступность slug для категории
     */
    public boolean isCategorySlugAvailable(String slug) {
        return !categoryRepository.existsBySlug(slug);
    }

    /**
     * Проверяет доступность slug для бренда
     */
    public boolean isBrandSlugAvailable(String slug) {
        return !brandRepository.existsBySlug(slug);
    }

    /**
     * Проверяет доступность slug для продукта
     */
    public boolean isProductSlugAvailable(String slug) {
        return !productRepository.existsBySlug(slug);
    }
}