package com.example.landofchokolate.mapper;
import com.example.landofchokolate.dto.wishlis.WishlistDto;
import com.example.landofchokolate.dto.wishlis.WishlistItemDto;
import com.example.landofchokolate.model.Product;
import com.example.landofchokolate.model.ProductImage;
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
        List<WishlistItemDto> itemDtos = new ArrayList<>();
        if (wishlist.getItems() != null) {
            itemDtos = wishlist.getItems().stream()
                    .map(this::toItemDto)
                    .filter(Objects::nonNull) // Исключаем null элементы
                    .collect(Collectors.toList());
        }
        dto.setItems(itemDtos);

        // Устанавливаем базовые поля
        dto.setIsEmpty(itemDtos.isEmpty());
        dto.setTotalItems(itemDtos.size());

        // Вычисляем метрики для доступных товаров
        calculateWishlistMetrics(dto, itemDtos);

        log.debug("Mapped wishlist: id={}, totalItems={}, availableItems={}, total={}",
                dto.getId(), dto.getTotalItems(), dto.getAvailableItems(), dto.getTotal());

        return dto;
    }

    /**
     * Вычисление метрик wishlist (availableItems, total)
     */
    private void calculateWishlistMetrics(WishlistDto dto, List<WishlistItemDto> itemDtos) {
        if (itemDtos == null || itemDtos.isEmpty()) {
            dto.setAvailableItems(0);
            dto.setTotal(BigDecimal.ZERO);
            return;
        }

        int availableCount = 0;
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (WishlistItemDto item : itemDtos) {
            if (item.getProduct() != null && item.getProduct().isAvailable()) {
                availableCount++;
                BigDecimal itemPrice = item.getProduct().getSafeCurrentPrice();
                if (itemPrice != null) {
                    totalPrice = totalPrice.add(itemPrice);
                }
            }
        }

        dto.setAvailableItems(availableCount);
        dto.setTotal(totalPrice);

        log.debug("Calculated metrics: availableItems={}, totalPrice={}", availableCount, totalPrice);
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

            // Безопасное преобразование Product
            if (item.getProduct() != null) {
                WishlistItemDto.ProductInfo productInfo = toProductInfo(item.getProduct());
                dto.setProduct(productInfo);
            }

            return dto;

        } catch (LazyInitializationException e) {
            log.warn("LazyInitializationException при преобразовании WishlistItem: {}", e.getMessage());
            return null; // Исключаем проблемный элемент
        } catch (Exception e) {
            log.error("Unexpected error при преобразовании WishlistItem: {}", e.getMessage(), e);
            return null;
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

            log.debug("Mapped product: id={}, name={}, price={}, active={}, stock={}",
                    productInfo.getId(), productInfo.getName(), productInfo.getCurrentPrice(),
                    productInfo.isActive(), productInfo.getStockQuantity());

            return productInfo;

        } catch (Exception e) {
            log.error("Ошибка при преобразовании Product в ProductInfo: {}", e.getMessage());
            // Возвращаем минимальную информацию
            return createFallbackProductInfo(product);
        }
    }

    /**
     * Создание fallback ProductInfo при ошибках
     */
    private WishlistItemDto.ProductInfo createFallbackProductInfo(Product product) {
        WishlistItemDto.ProductInfo productInfo = new WishlistItemDto.ProductInfo();
        productInfo.setId(safeGetProductId(product));
        productInfo.setName("Товар недоступен");
        productInfo.setCurrentPrice(BigDecimal.ZERO);
        productInfo.setIsActive(false);
        productInfo.setStockQuantity(0);
        productInfo.setSlug("");
        productInfo.setImageUrl("");
        return productInfo;
    }

    // Безопасные методы получения данных продукта
    private Long safeGetProductId(Product product) {
        try {
            return product.getId();
        } catch (LazyInitializationException e) {
            log.warn("Не удалось получить ID продукта: {}", e.getMessage());
            return null;
        }
    }

    private String safeGetProductName(Product product) {
        try {
            String name = product.getName();
            return name != null ? name : "Товар недоступен";
        } catch (LazyInitializationException e) {
            log.warn("Не удалось получить название продукта: {}", e.getMessage());
            return "Товар недоступен";
        }
    }

    private String safeGetProductSlug(Product product) {
        try {
            String slug = product.getSlug();
            return slug != null ? slug : "";
        } catch (LazyInitializationException e) {
            log.warn("Не удалось получить slug продукта: {}", e.getMessage());
            return "";
        }
    }

    private BigDecimal safeGetProductPrice(Product product) {
        try {
            BigDecimal price = product.getPrice();
            return price != null ? price : BigDecimal.ZERO;
        } catch (LazyInitializationException e) {
            log.warn("Не удалось получить цену продукта: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * 🔄 ОБНОВЛЕНО: Безопасное получение URL главного изображения продукта
     */
    private String safeGetProductImageUrl(Product product) {
        try {
            // Проверяем наличие изображений
            if (product.getImages() == null || product.getImages().isEmpty()) {
                return "";
            }

            // Ищем главное изображение
            String mainImageUrl = product.getImages().stream()
                    .filter(img -> Boolean.TRUE.equals(img.getIsMain()))
                    .findFirst()
                    .map(ProductImage::getImageUrl)
                    .orElse(null);

            // Если главного нет, берем первое доступное
            if (mainImageUrl == null && !product.getImages().isEmpty()) { // ← 🔥 Добавить проверку
                mainImageUrl = product.getImages().get(0).getImageUrl();
            }

            return mainImageUrl != null ? mainImageUrl : "";

        } catch (LazyInitializationException e) {
            log.warn("Не удалось получить изображения продукта: {}", e.getMessage());
            return "";
        } catch (Exception e) {
            log.error("Ошибка при получении изображения продукта: {}", e.getMessage(), e);
            return "";
        }
    }

    private Integer safeGetProductStock(Product product) {
        try {
            Integer stock = product.getStockQuantity();
            return stock != null ? stock : 0;
        } catch (LazyInitializationException e) {
            log.warn("Не удалось получить количество на складе: {}", e.getMessage());
            return 0;
        }
    }

    private Boolean safeGetProductIsActive(Product product) {
        try {
            Boolean isActive = product.getIsActive();
            return isActive != null ? isActive : false;
        } catch (LazyInitializationException e) {
            log.warn("Не удалось получить статус активности продукта: {}", e.getMessage());
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
        dto.setAvailableItems(0);
        dto.setTotal(BigDecimal.ZERO);
        return dto;
    }

    /**
     * Метод для обновления только метрик без полного пересоздания DTO
     * Полезно при частых обновлениях статистики
     */
    public void updateWishlistMetrics(WishlistDto dto) {
        if (dto == null || dto.getItems() == null) {
            return;
        }

        calculateWishlistMetrics(dto, dto.getItems());
    }

    /**
     * Быстрая проверка доступности товара без полного маппинга
     */
    public boolean isProductAvailable(Product product) {
        if (product == null) {
            return false;
        }

        try {
            Boolean isActive = safeGetProductIsActive(product);
            Integer stock = safeGetProductStock(product);
            return Boolean.TRUE.equals(isActive) && stock != null && stock > 0;
        } catch (Exception e) {
            log.warn("Ошибка при проверке доступности товара: {}", e.getMessage());
            return false;
        }
    }
}