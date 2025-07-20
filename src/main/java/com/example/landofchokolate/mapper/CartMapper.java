package com.example.landofchokolate.mapper;

import com.example.landofchokolate.dto.card.CartDto;
import com.example.landofchokolate.dto.card.CartItemDto;
import com.example.landofchokolate.model.Cart;
import com.example.landofchokolate.model.CartItem;
import com.example.landofchokolate.model.Product;
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
public class CartMapper {

    /**
     * Безопасное преобразование Cart в CartDto
     */
    public CartDto toDto(Cart cart) {
        if (cart == null) {
            return createEmptyCartDto();
        }

        CartDto dto = new CartDto();
        dto.setCartUuid(cart.getCartUuid());
        dto.setStatus(cart.getStatus());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());
        dto.setLastActivityAt(cart.getLastActivityAt());
        dto.setExpiresAt(cart.getExpiresAt());

        // Безопасное преобразование элементов корзины
        if (cart.getItems() != null) {
            List<CartItemDto> itemDtos = cart.getItems().stream()
                    .map(this::toItemDto)
                    .filter(Objects::nonNull) // Исключаем null элементы
                    .collect(Collectors.toList());
            dto.setItems(itemDtos);
        } else {
            dto.setItems(new ArrayList<>());
        }

        return dto;
    }

    /**
     * Безопасное преобразование CartItem в CartItemDto
     */
    private CartItemDto toItemDto(CartItem item) {
        if (item == null) {
            return null;
        }

        try {
            CartItemDto dto = new CartItemDto();
            dto.setQuantity(item.getQuantity());
            dto.setPriceAtTime(item.getPriceAtTime());
            dto.setAddedAt(item.getAddedAt());
            dto.setUpdatedAt(item.getUpdatedAt());

            // Безопасное преобразование Product
            if (item.getProduct() != null) {
                CartItemDto.ProductInfo productInfo = toProductInfo(item.getProduct());
                dto.setProduct(productInfo);
            }

            return dto;

        } catch (LazyInitializationException e) {
            log.warn("LazyInitializationException при преобразовании CartItem: {}", e.getMessage());
            return null; // Исключаем проблемный элемент
        }
    }

    /**
     * Безопасное преобразование Product в ProductInfo
     */
    private CartItemDto.ProductInfo toProductInfo(Product product) {
        try {
            CartItemDto.ProductInfo productInfo = new CartItemDto.ProductInfo();

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
            CartItemDto.ProductInfo productInfo = new CartItemDto.ProductInfo();
            productInfo.setId(safeGetProductId(product));
            productInfo.setName("Товар недоступен");
            productInfo.setCurrentPrice(BigDecimal.ZERO);
            productInfo.setIsActive(false);
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
            return "";
        }
    }

    private BigDecimal safeGetProductPrice(Product product) {
        try {
            return product.getPrice();
        } catch (LazyInitializationException e) {
            return BigDecimal.ZERO;
        }
    }

    private String safeGetProductImageUrl(Product product) {
        try {
            return product.getImageUrl();
        } catch (LazyInitializationException e) {
            return "";
        }
    }

    private Integer safeGetProductStock(Product product) {
        try {
            return product.getStockQuantity();
        } catch (LazyInitializationException e) {
            return 0;
        }
    }

    private Boolean safeGetProductIsActive(Product product) {
        try {
            return product.getIsActive();
        } catch (LazyInitializationException e) {
            return false;
        }
    }

    /**
     * Создать пустую CartDto
     */
    private CartDto createEmptyCartDto() {
        CartDto dto = new CartDto();
        dto.setCartUuid("");
        dto.setItems(new ArrayList<>());
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        return dto;
    }
}