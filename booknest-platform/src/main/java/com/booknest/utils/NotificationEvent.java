package com.booknest.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationEvent {
    private String email;    // Who gets the email?
    private String type;     // e.g., "PAYMENT_SUCCESS", "ORDER_CONFIRMED"
    private String message;  // The actual text of the email
}