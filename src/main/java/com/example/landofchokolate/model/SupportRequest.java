package com.example.landofchokolate.model;
import com.example.landofchokolate.enums.SupportTopic;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private SupportTopic topic;


    @Column(nullable = false)
    private String message;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
