package com.booknest.notification.resource;

import com.booknest.notification.entity.Notification;
import com.booknest.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationResource {

    private final NotificationService notificationService;

    // View entire inbox
    @GetMapping
    public ResponseEntity<List<Notification>> getMyInbox(Principal principal) {
        return ResponseEntity.ok(notificationService.getMyNotifications(principal.getName()));
    }

    // Fetch the number for the red UI badge
    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadCount(Principal principal) {
        return ResponseEntity.ok(notificationService.getUnreadCount(principal.getName()));
    }

    // Mark a specific message as read
    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Integer id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }
    
    // Clear the badge count completely
    @PutMapping("/mark-all-read")
    public ResponseEntity<String> markAllAsRead(Principal principal) {
        notificationService.markAllAsRead(principal.getName());
        return ResponseEntity.ok("All notifications marked as read.");
    }

    // Delete a notification from the inbox
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNotification(@PathVariable Integer id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok("Notification deleted successfully.");
    }

    // Simulates an internal system event (like an order completing)
    @PostMapping("/simulate")
    public ResponseEntity<Notification> simulateSystemAlert(
            Principal principal,
            @RequestParam String type,
            @RequestParam String message) {
        
        return ResponseEntity.ok(notificationService.sendNotification(principal.getName(), type, message));
    }
}