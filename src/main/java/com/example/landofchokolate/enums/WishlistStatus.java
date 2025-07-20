package com.example.landofchokolate.enums;

import lombok.Getter;

@Getter
public enum WishlistStatus {
    ACTIVE("Активный"),
    EXPIRED("Истёкший");

    private final String displayName;

    WishlistStatus(String displayName) {
        this.displayName = displayName;
    }
}