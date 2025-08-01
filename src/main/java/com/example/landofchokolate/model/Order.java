package com.example.landofchokolate.model;

import com.example.landofchokolate.enums.DeliveryMethod;
import com.example.landofchokolate.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "orders")
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal totalAmount;

    private String email;

    private String phoneNumber;

    private String customerName;

    private String someNotes;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    private DeliveryMethod deliveryMethod;



    @Column(name = "tracking_number", length = 50)
    private String trackingNumber;

    @Column(name = "recipient_city_ref", length = 100)
    private String recipientCityRef;

    @Column(name = "recipient_address_ref", length = 100)
    private String recipientAddressRef;

    @Column(name = "delivery_status", length = 50)
    private String deliveryStatus;



    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = OrderStatus.NEW;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Payment payment;



}