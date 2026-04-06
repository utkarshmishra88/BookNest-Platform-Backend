package com.booknest.notification.repository;

import com.booknest.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    
    // Fetch a user's entire inbox, sorting newest to oldest
    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId);
    
    // Fetch only unread messages (useful for a quick-view dropdown)
    List<Notification> findByUserIdAndIsRead(Integer userId, boolean isRead);
    
    // Calculate the number for the red notification badge on the UI
    int countByUserIdAndIsRead(Integer userId, boolean isRead);
    
    // Delete a specific notification
    void deleteByNotificationId(Integer notificationId);
}