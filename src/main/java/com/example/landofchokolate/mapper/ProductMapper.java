package com.example.landofchokolate.mapper;

import com.example.landofchokolate.dto.product.*;
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
        product.setIsRecommendation(createProductDto.getIsRecommendation());
        log.debug("Mapped CreateProductDto to Product: {}", createProductDto.getName());
        return product;
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
        dto.setSlug(product.getSlug());
        dto.setIsRecommendation(product.getIsRecommendation());

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


         dto.setCreatedAt(product.getCreatedAt());
         dto.setUpdatedAt(product.getUpdatedAt());

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
        dto.setSlug(product.getSlug());
        dto.setIsRecommendation(product.getIsRecommendation());



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
     * Конвертация Product в ProductDetailDto для детальной страницы
     */
    public ProductDetailDto toDetailDto(Product product) {
        if (product == null) {
            return null;
        }

        ProductDetailDto dto = new ProductDetailDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSlug(product.getSlug());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setImageUrl(product.getImageUrl());
        dto.setImageId(product.getImageId());
        dto.setIsActive(product.getIsActive());

        // Маппинг категории
        if (product.getCategory() != null) {
            ProductDetailDto.CategoryInfo categoryInfo = new ProductDetailDto.CategoryInfo();
            categoryInfo.setId(product.getCategory().getId());
            categoryInfo.setName(product.getCategory().getName());
            categoryInfo.setSlug(product.getCategory().getSlug());
            // Добавьте другие поля категории при необходимости
            if (product.getCategory().getImageUrl() != null) {
                categoryInfo.setImageUrl(product.getCategory().getImageUrl());
            }
            if (product.getCategory().getShortDescription() != null) {
                categoryInfo.setDescription(product.getCategory().getShortDescription());
            }
            dto.setCategory(categoryInfo);
        }

        // Маппинг бренда
        if (product.getBrand() != null) {
            ProductDetailDto.BrandInfo brandInfo = new ProductDetailDto.BrandInfo();
            brandInfo.setId(product.getBrand().getId());
            brandInfo.setName(product.getBrand().getName());
            // Добавьте другие поля бренда при необходимости
            if (product.getBrand().getImageUrl() != null) {
                brandInfo.setImageUrl(product.getBrand().getImageUrl());
            }
            if (product.getBrand().getDescription() != null) {
                brandInfo.setDescription(product.getBrand().getDescription());
            }
            dto.setBrand(brandInfo);
        }

        return dto;
    }


    ///похожие товары для дейтайлс
    public List<RelatedProductDto> toRelatedDtoList(List<Product> relatedProducts) {
        if (relatedProducts == null || relatedProducts.isEmpty()) {
            return List.of();
        }

        return relatedProducts.stream()
                .map(this::toRelatedDto)
                .collect(Collectors.toList());
    }

    /**
     * Конвертация Product в RelatedProductDto
     */
    public RelatedProductDto toRelatedDto(Product product) {
        if (product == null) {
            return null;
        }

        RelatedProductDto dto = new RelatedProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSlug(product.getSlug());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setImageUrl(product.getImageUrl());

        return dto;
    }
}