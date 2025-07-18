package com.example.landofchokolate.mapper;

import com.example.landofchokolate.dto.card.CartDto;
import com.example.landofchokolate.dto.card.CartItemDto;
import com.example.landofchokolate.model.Cart;
import com.example.landofchokolate.model.CartItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartDto toDto(Cart cart) {
        if (cart == null) {
            return null;
        }

        CartDto dto = new CartDto();
        dto.setId(cart.getId());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());

        if (cart.getItems() != null) {
            dto.setItems(cart.getItems().stream()
                    .map(this::toDto)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public CartItemDto toDto(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }

        CartItemDto dto = new CartItemDto();
        dto.setId(cartItem.getId());
        dto.setCartId(cartItem.getCart().getId());
        dto.setQuantity(cartItem.getQuantity());
        dto.setPriceAtTime(cartItem.getPriceAtTime());
        dto.setAddedAt(cartItem.getAddedAt());
        dto.setUpdatedAt(cartItem.getUpdatedAt());

        // Маппинг информации о продукте
        if (cartItem.getProduct() != null) {
            CartItemDto.ProductInfo productInfo = new CartItemDto.ProductInfo();
            productInfo.setId(cartItem.getProduct().getId());
            productInfo.setName(cartItem.getProduct().getName());
            productInfo.setSlug(cartItem.getProduct().getSlug());
            productInfo.setCurrentPrice(cartItem.getProduct().getPrice());
            productInfo.setImageUrl(cartItem.getProduct().getImageUrl());
            productInfo.setStockQuantity(cartItem.getProduct().getStockQuantity());
            productInfo.setIsActive(cartItem.getProduct().getIsActive());
            dto.setProduct(productInfo);
        }

        return dto;
    }



    public List<CartDto> toDtoList(List<Cart> carts) {
        return carts.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}