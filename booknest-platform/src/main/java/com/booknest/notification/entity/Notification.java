package com.booknest.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer notificationId; // Matches the case study diagram

    @Column(nullable = false)
    private Integer userId; // Decoupled: We only store the ID, not the User object

    @Column(nullable = false)
    private String type; // e.g., "ORDER", "PAYMENT", "SYSTEM_ALERT"

    @Column(nullable = false, length = 500)
    private String message; // The actual alert text

    private boolean isRead = false; // Starts as unread to trigger the UI badge count

    private LocalDateTime createdAt;
}