package com.example.landofchokolate.mapper;

import com.example.landofchokolate.dto.brend.ProductBrandClientDto;
import com.example.landofchokolate.dto.category.CategoryProductDto;
import com.example.landofchokolate.dto.product.*;
import com.example.landofchokolate.model.Brand;
import com.example.landofchokolate.model.Category;
import com.example.landofchokolate.model.Product;
import com.example.landofchokolate.model.ProductImage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ProductMapper {


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

        product.setDescription(createProductDto.getDescription());
        product.setMetaDescription(createProductDto.getMetaDescription());
        product.setMetaTitle(createProductDto.getMetaTitle());
        product.setPriceUnit(createProductDto.getPriceUnit());

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
        dto.setPriceUnit(product.getPriceUnit());

        // 🔄 Обновлено: маппинг изображений вместо одного imageUrl
        dto.setImages(mapProductImages(product.getImages()));

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

        dto.setDescription(product.getDescription());
        dto.setMetaDescription(product.getMetaDescription());
        dto.setMetaTitle(product.getMetaTitle());

        return dto;
    }


    /**
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

        // 🆕 ОБНОВЛЕНО: устанавливаем список изображений
        dto.setImages(mapProductImages(product.getImages()));

        dto.setSlug(product.getSlug());

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

        // 🔄 Обновлено: получаем главное изображение

        dto.setImages(mapProductImages(product.getImages()));

        dto.setIsActive(product.getIsActive());

        dto.setMetaTitle(product.getMetaTitle());
        dto.setMetaDescription(product.getMetaDescription());
        dto.setDescription(product.getDescription());

        // Маппинг категории
        if (product.getCategory() != null) {
            ProductDetailDto.CategoryInfo categoryInfo = new ProductDetailDto.CategoryInfo();
            categoryInfo.setId(product.getCategory().getId());
            categoryInfo.setName(product.getCategory().getName());
            categoryInfo.setSlug(product.getCategory().getSlug());
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

        // 🔄 Обновлено: получаем URL главного изображения
        dto.setImageUrl(getMainImageUrl(product));

        return dto;
    }


//список кликов
public ProductListClickDto toListDtoClick(Product product) {
    if (product == null) {
        return null;
    }

    ProductListClickDto dto = new ProductListClickDto();
    dto.setId(product.getId());
    dto.setName(product.getName());
    dto.setPrice(product.getPrice());
    dto.setStockQuantity(product.getStockQuantity());

    // 🔄 Обновлено: получаем URL главного изображения
    dto.setImageUrl(getMainImageUrl(product));

    dto.setSlug(product.getSlug());
    dto.setClickCount(product.getClickCount());

    return dto;
}




    /**
     * Конвертація Product в DTO для відображення в контексті бренду
     */
    public ProductBrandClientDto toProductBrandClientDto(Product product) {
        if (product == null) {
            return null;
        }

        ProductBrandClientDto dto = new ProductBrandClientDto();

        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());

        // 🔄 Обновлено: получаем URL главного изображения
        dto.setImageUrl(getMainImageUrl(product));

        dto.setSlug(product.getSlug());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setIsActive(product.getIsActive());

        // Встановлюємо назву категорії
        dto.setCategoryName(product.getCategory() != null ?
                product.getCategory().getName() : null);

        // Обчислюємо статус наявності
        dto.setInStock(product.getStockQuantity() != null &&
                product.getStockQuantity() > 0);

        // Обчислюємо статус низького залишку
        dto.setLowStock(product.getStockQuantity() != null &&
                product.getStockQuantity() < 10 &&
                product.getStockQuantity() > 0);

        return dto;
    }

    /**
     * Конвертація списку Product в список DTO для бренду
     */
    public List<ProductBrandClientDto> toProductBrandClientDtoList(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return new ArrayList<>();
        }

        return products.stream()
                .map(this::toProductBrandClientDto)
                .collect(Collectors.toList());
    }


    /**
     * Получить главное изображение продукта
     */
    private ProductImage getMainImage(Product product) {
        if (product == null || product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }

        return product.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsMain()))
                .findFirst()
                .orElse(product.getImages().get(0));
    }
    /**
     * Получить URL главного изображения
     */
    private String getMainImageUrl(Product product) {
        ProductImage mainImage = getMainImage(product);
        return mainImage != null ? mainImage.getImageUrl() : null;
    }


    public CategoryProductDto toCardDtoCategoryList(Product product) {
        ProductImage mainImage = product.getMainImage();

        return new CategoryProductDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getSlug(),
                mainImage != null ? mainImage.getImageUrl() : null,
                mainImage != null ? mainImage.getAltText() : null,
                product.getStockQuantity()
        );
    }



    // Теперь маппинг станет проще - один метод для всех DTO:
    private List<ProductImageInfo> mapProductImages(List<ProductImage> images) {
        if (images == null || images.isEmpty()) {
            return new ArrayList<>();
        }

        return images.stream()
                .map(this::mapProductImage)
                .collect(Collectors.toList());
    }

    private ProductImageInfo mapProductImage(ProductImage image) {
        ProductImageInfo imageInfo = new ProductImageInfo();
        imageInfo.setId(image.getId());
        imageInfo.setImageUrl(image.getImageUrl());
        imageInfo.setImageId(image.getImageId());
        imageInfo.setSortOrder(image.getSortOrder());
        imageInfo.setIsMain(image.getIsMain());
        imageInfo.setAltText(image.getAltText());
        return imageInfo;
    }
}