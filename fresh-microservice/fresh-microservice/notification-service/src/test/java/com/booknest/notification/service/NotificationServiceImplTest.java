package com.booknest.notification.service;

import com.booknest.notification.entity.NotificationLog;
import com.booknest.notification.repository.NotificationLogRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private NotificationLogRepository repository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Mock
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testSendOtpEmail() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(repository.save(any(NotificationLog.class))).thenReturn(new NotificationLog());

        notificationService.sendOtpEmail("test@example.com", "123456", "+1234567890");

        verify(mailSender, times(1)).send(mimeMessage);
        verify(repository, times(1)).save(any(NotificationLog.class));
    }

    @Test
    void testSendUpdateEmail() {
        when(repository.save(any(NotificationLog.class))).thenReturn(new NotificationLog());

        notificationService.sendUpdateEmail("test@example.com", "Subject", "Message", "+1234567890");

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
        verify(repository, times(1)).save(any(NotificationLog.class));
    }

    @Test
    void testSendOrderDocumentsEmail_WithoutAttachments() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(repository.save(any(NotificationLog.class))).thenReturn(new NotificationLog());

        notificationService.sendOrderDocumentsEmail("test@example.com", "Subject", "<p>Body</p>", null, null, "+1234567890");

        verify(mailSender, times(1)).send(mimeMessage);
        verify(repository, times(1)).save(any(NotificationLog.class));
    }

    @Test
    void testSendOrderDocumentsEmail_WithAttachments() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(repository.save(any(NotificationLog.class))).thenReturn(new NotificationLog());

        byte[] invoice = new byte[]{1, 2, 3};
        byte[] receipt = new byte[]{4, 5, 6};

        notificationService.sendOrderDocumentsEmail("test@example.com", "Subject", "<p>Body</p>", invoice, receipt, "+1234567890");

        verify(mailSender, times(1)).send(mimeMessage);
        verify(repository, times(1)).save(any(NotificationLog.class));
    }
}
