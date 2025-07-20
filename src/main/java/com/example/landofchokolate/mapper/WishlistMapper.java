package com.example.landofchokolate.mapper;

import com.example.landofchokolate.dto.wishlis.WishlistDto;
import com.example.landofchokolate.dto.wishlis.WishlistItemDto;
import com.example.landofchokolate.model.Product;
import com.example.landofchokolate.model.Wishlist;
import com.example.landofchokolate.model.WishlistItem;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.LazyInitializationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class WishlistMapper {

    /**
     * Безопасное преобразование Wishlist в WishlistDto
     */
    public WishlistDto toDto(Wishlist wishlist) {
        if (wishlist == null) {
            return createEmptyWishlistDto();
        }

        WishlistDto dto = new WishlistDto();
        dto.setId(wishlist.getId());
        dto.setWishlistUuid(wishlist.getWishlistUuid());
        dto.setCreatedAt(wishlist.getCreatedAt());
        dto.setUpdatedAt(wishlist.getUpdatedAt());

        // Безопасное преобразование элементов избранного
        if (wishlist.getItems() != null) {
            List<WishlistItemDto> itemDtos = wishlist.getItems().stream()
                    .map(this::toItemDto)
                    .filter(Objects::nonNull) // Исключаем null элементы
                    .collect(Collectors.toList());
            dto.setItems(itemDtos);
        } else {
            dto.setItems(new ArrayList<>());
        }

        // Устанавливаем вычисляемые поля
        dto.setIsEmpty(dto.getItems().isEmpty());
        dto.setTotalItems(dto.getItems().size());

        return dto;
    }

    /**
     * Безопасное преобразование WishlistItem в WishlistItemDto
     */
    private WishlistItemDto toItemDto(WishlistItem item) {
        if (item == null) {
            return null;
        }

        try {
            WishlistItemDto dto = new WishlistItemDto();
            dto.setId(item.getId());
            dto.setAddedAt(item.getAddedAt());
            dto.setAddedFromPage(item.getAddedFromPage());

            // Безопасное преобразование Product
            if (item.getProduct() != null) {
                WishlistItemDto.ProductInfo productInfo = toProductInfo(item.getProduct());
                dto.setProduct(productInfo);
            }

            return dto;

        } catch (LazyInitializationException e) {
            log.warn("LazyInitializationException при преобразовании WishlistItem: {}", e.getMessage());
            return null; // Исключаем проблемный элемент
        }
    }

    /**
     * Безопасное преобразование Product в ProductInfo
     */
    private WishlistItemDto.ProductInfo toProductInfo(Product product) {
        try {
            WishlistItemDto.ProductInfo productInfo = new WishlistItemDto.ProductInfo();

            // Безопасно получаем каждое поле
            productInfo.setId(safeGetProductId(product));
            productInfo.setName(safeGetProductName(product));
            productInfo.setSlug(safeGetProductSlug(product));
            productInfo.setCurrentPrice(safeGetProductPrice(product));
            productInfo.setImageUrl(safeGetProductImageUrl(product));
            productInfo.setStockQuantity(safeGetProductStock(product));
            productInfo.setIsActive(safeGetProductIsActive(product));

            return productInfo;

        } catch (Exception e) {
            log.error("Ошибка при преобразовании Product в ProductInfo: {}", e.getMessage());
            // Возвращаем минимальную информацию
            WishlistItemDto.ProductInfo productInfo = new WishlistItemDto.ProductInfo();
            productInfo.setId(safeGetProductId(product));
            productInfo.setName("Товар недоступен");
            productInfo.setCurrentPrice(BigDecimal.ZERO);
            productInfo.setIsActive(false);
            productInfo.setStockQuantity(0);
            return productInfo;
        }
    }

    // Безопасные методы получения данных продукта
    private Long safeGetProductId(Product product) {
        try {
            return product.getId();
        } catch (LazyInitializationException e) {
            log.warn("Не удалось получить ID продукта");
            return null;
        }
    }

    private String safeGetProductName(Product product) {
        try {
            return product.getName();
        } catch (LazyInitializationException e) {
            log.warn("Не удалось получить название продукта");
            return "Товар недоступен";
        }
    }

    private String safeGetProductSlug(Product product) {
        try {
            return product.getSlug();
        } catch (LazyInitializationException e) {
            log.warn("Не удалось получить slug продукта");
            return "";
        }
    }

    private BigDecimal safeGetProductPrice(Product product) {
        try {
            return product.getPrice();
        } catch (LazyInitializationException e) {
            log.warn("Не удалось получить цену продукта");
            return BigDecimal.ZERO;
        }
    }

    private String safeGetProductImageUrl(Product product) {
        try {
            return product.getImageUrl();
        } catch (LazyInitializationException e) {
            log.warn("Не удалось получить изображение продукта");
            return "";
        }
    }

    private Integer safeGetProductStock(Product product) {
        try {
            return product.getStockQuantity();
        } catch (LazyInitializationException e) {
            log.warn("Не удалось получить количество на складе");
            return 0;
        }
    }

    private Boolean safeGetProductIsActive(Product product) {
        try {
            return product.getIsActive();
        } catch (LazyInitializationException e) {
            log.warn("Не удалось получить статус активности продукта");
            return false;
        }
    }

    /**
     * Создать пустой WishlistDto
     */
    private WishlistDto createEmptyWishlistDto() {
        WishlistDto dto = new WishlistDto();
        dto.setId(null);
        dto.setWishlistUuid("");
        dto.setItems(new ArrayList<>());
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        dto.setIsEmpty(true);
        dto.setTotalItems(0);
        return dto;
    }

    /**
     * Преобразовать список товаров в список ProductInfo для быстрого доступа
     */
    public List<WishlistItemDto.ProductInfo> toProductInfoList(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return new ArrayList<>();
        }

        return products.stream()
                .map(this::toProductInfo)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Создать минимальный WishlistItemDto для случаев, когда нужно показать только основную информацию
     */
    public WishlistItemDto createMinimalItemDto(Product product, LocalDateTime addedAt, String addedFromPage) {
        WishlistItemDto dto = new WishlistItemDto();
        dto.setAddedAt(addedAt != null ? addedAt : LocalDateTime.now());
        dto.setAddedFromPage(addedFromPage != null ? addedFromPage : "unknown");

        if (product != null) {
            dto.setProduct(toProductInfo(product));
        }

        return dto;
    }
}