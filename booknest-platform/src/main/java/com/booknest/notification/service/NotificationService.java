package com.booknest.notification.service;

import com.booknest.auth.entity.User;
import com.booknest.auth.repository.UserRepository;
import com.booknest.notification.entity.Notification;
import com.booknest.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final JavaMailSender emailSender; // Mail integration

    // Securely resolve the JWT email to a User ID
    private Integer resolveUserId(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getUserId();
    }

    // Helper method to send the actual email
    private void sendEmailAlert(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("alerts@booknest.com"); 
            
            emailSender.send(message);
            System.out.println("✅ Real email successfully sent to: " + toEmail);
        } catch (Exception e) {
            // Catching the exception ensures that if the email server is down, 
            // the database transaction doesn't fail and crash the checkout process!
            System.err.println("❌ Failed to send real email: " + e.getMessage());
        }
    }

    // 1. Dispatch a new Notification (Saves to DB + Sends Email)
    public Notification sendNotification(String email, String type, String message) {
        Integer userId = resolveUserId(email);
        
        // Save to Database for the In-App Inbox
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        Notification savedNotification = notificationRepository.save(notification);

        // Fire the real email alert asynchronously
        String subject = "BookNest Alert: " + type;
        sendEmailAlert(email, subject, message);

        return savedNotification;
    }

    // 2. Load the User's Inbox
    public List<Notification> getMyNotifications(String email) {
        Integer userId = resolveUserId(email);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // 3. Get Unread Badge Count
    public int getUnreadCount(String email) {
        Integer userId = resolveUserId(email);
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    // 4. Mark Single Message as Read
    @Transactional
    public Notification markAsRead(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        notification.setRead(true);
        return notificationRepository.save(notification);
    }
    
    // 5. Mark All as Read (Bulk Action)
    @Transactional
    public void markAllAsRead(String email) {
        Integer userId = resolveUserId(email);
        List<Notification> unreadList = notificationRepository.findByUserIdAndIsRead(userId, false);
        
        unreadList.forEach(notif -> notif.setRead(true));
        notificationRepository.saveAll(unreadList);
    }

    // 6. Delete a notification
    @Transactional
    public void deleteNotification(Integer notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}