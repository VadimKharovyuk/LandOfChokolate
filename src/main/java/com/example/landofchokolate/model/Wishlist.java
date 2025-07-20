package com.example.landofchokolate.model;

import com.example.landofchokolate.enums.WishlistStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
// Модель Wishlist (избранное)
public class Wishlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Уникальный идентификатор для анонимных пользователей
    @Column(unique = true, length = 36)
    private String wishlistUuid;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WishlistStatus status = WishlistStatus.ACTIVE;

    @OneToMany(mappedBy = "wishlist", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WishlistItem> items = new ArrayList<>();

    // Для анонимных пользователей
    @Column(length = 45)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    // Время последней активности
    @Column(nullable = false)
    private LocalDateTime lastActivityAt = LocalDateTime.now();

    // Время истечения для анонимных wishlist
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();
    }

}