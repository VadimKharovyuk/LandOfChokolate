package com.example.landofchokolate.mapper;

import com.example.landofchokolate.dto.product.CreateProductDto;
import com.example.landofchokolate.dto.product.ProductListDto;
import com.example.landofchokolate.dto.product.ProductResponseDto;
import com.example.landofchokolate.model.Brand;
import com.example.landofchokolate.model.Category;
import com.example.landofchokolate.model.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ProductMapper {

    /**
     * Преобразует CreateProductDto в Entity
     * Категория и бренд должны быть установлены отдельно в сервисе
     */
    public Product toEntity(CreateProductDto createProductDto) {
        if (createProductDto == null) {
            log.warn("CreateProductDto is null, returning null");
            return null;
        }

        Product product = new Product();
        product.setName(createProductDto.getName());
        product.setPrice(createProductDto.getPrice());
        product.setStockQuantity(createProductDto.getStockQuantity());
        // imageUrl и imageId будут установлены в сервисе после загрузки файла
        // category и brand будут установлены в сервисе по ID

        log.debug("Mapped CreateProductDto to Product: {}", createProductDto.getName());
        return product;
    }

    /**
     * Обновляет существующую Entity из DTO
     */
    public void updateEntityFromDto(CreateProductDto dto, Product existingProduct) {
        if (dto == null || existingProduct == null) {
            log.warn("DTO or existing product is null, skipping update");
            return;
        }

        existingProduct.setName(dto.getName());
        existingProduct.setPrice(dto.getPrice());
        existingProduct.setStockQuantity(dto.getStockQuantity());
        // category, brand, imageUrl обновляются отдельно в сервисе

        log.debug("Updated product entity with name: {}", dto.getName());
    }

    /**
     * Преобразует Entity в полный ResponseDto
     */
    public ProductResponseDto toResponseDto(Product product) {
        if (product == null) {
            log.warn("Product entity is null, returning null");
            return null;
        }

        ProductResponseDto dto = new ProductResponseDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setImageUrl(product.getImageUrl());

        // Маппинг категории
        if (product.getCategory() != null) {
            ProductResponseDto.CategoryInfo categoryInfo = new ProductResponseDto.CategoryInfo();
            categoryInfo.setId(product.getCategory().getId());
            categoryInfo.setName(product.getCategory().getName());
            dto.setCategory(categoryInfo);
        }

        // Маппинг бренда
        if (product.getBrand() != null) {
            ProductResponseDto.BrandInfo brandInfo = new ProductResponseDto.BrandInfo();
            brandInfo.setId(product.getBrand().getId());
            brandInfo.setName(product.getBrand().getName());
            brandInfo.setImageUrl(product.getBrand().getImageUrl());
            dto.setBrand(brandInfo);
        }

        // Если у Product есть поля createdAt/updatedAt, добавьте их
        // dto.setCreatedAt(product.getCreatedAt());
        // dto.setUpdatedAt(product.getUpdatedAt());

        log.debug("Mapped Product entity to ResponseDto: id={}, name={}",
                product.getId(), product.getName());
        return dto;
    }

    /**
     * Преобразует Entity в упрощенный ListDto для списков
     */
    public ProductListDto toListDto(Product product) {
        if (product == null) {
            log.warn("Product entity is null, returning null");
            return null;
        }

        ProductListDto dto = new ProductListDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setImageUrl(product.getImageUrl());

        // Простые поля для отображения
        dto.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : "Без категории");
        dto.setBrandName(product.getBrand() != null ? product.getBrand().getName() : "Без бренда");

        // Статусы товара
        dto.setInStock(product.getStockQuantity() > 0);
        dto.setLowStock(product.getStockQuantity() < 10);

        log.debug("Mapped Product entity to ListDto: id={}, name={}",
                product.getId(), product.getName());
        return dto;
    }

    /**
     * Преобразует список Entity в список ResponseDto
     */
    public List<ProductResponseDto> toResponseDtoList(List<Product> products) {
        if (products == null || products.isEmpty()) {
            log.debug("Products list is null or empty, returning empty list");
            return new ArrayList<>();
        }

        List<ProductResponseDto> responseDtos = products.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());

        log.debug("Mapped {} products to ResponseDto list", responseDtos.size());
        return responseDtos;
    }

    /**
     * Преобразует список Entity в список ListDto
     */
    public List<ProductListDto> toListDtoList(List<Product> products) {
        if (products == null || products.isEmpty()) {
            log.debug("Products list is null or empty, returning empty list");
            return new ArrayList<>();
        }

        List<ProductListDto> listDtos = products.stream()
                .map(this::toListDto)
                .collect(Collectors.toList());

        log.debug("Mapped {} products to ListDto list", listDtos.size());
        return listDtos;
    }

    /**
     * Устанавливает связи с категорией и брендом
     */
    public void setRelations(Product product, Category category, Brand brand) {
        if (product == null) {
            log.warn("Product is null, cannot set relations");
            return;
        }

        if (category != null) {
            product.setCategory(category);
            log.debug("Set category {} for product {}", category.getName(), product.getName());
        }

        if (brand != null) {
            product.setBrand(brand);
            log.debug("Set brand {} for product {}", brand.getName(), product.getName());
        }
    }

    /**
     * Создает краткую информацию о продукте (для отображения в других местах)
     */
    public String getProductSummary(Product product) {
        if (product == null) {
            return "Продукт не найден";
        }

        return String.format("%s (%s) - %s руб. (в наличии: %d шт.)",
                product.getName(),
                product.getBrand() != null ? product.getBrand().getName() : "Без бренда",
                product.getPrice(),
                product.getStockQuantity());
    }

    /**
     * Вспомогательный метод для проверки доступности товара
     */
    public boolean isProductAvailable(Product product) {
        return product != null && product.getStockQuantity() > 0;
    }
}