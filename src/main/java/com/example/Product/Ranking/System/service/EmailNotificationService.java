package com.example.Product.Ranking.System.service;

import org.springframework.stereotype.Service;

@Service("emailNotification") // ⭐ Naming the bean
public class EmailNotificationService implements NotificationService {
    @Override
    public void sendNotification(String message) {
        System.out.println("📧 EMAIL SENT: " + message);
    }
}