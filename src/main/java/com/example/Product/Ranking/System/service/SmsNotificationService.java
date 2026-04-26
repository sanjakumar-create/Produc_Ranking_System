package com.example.Product.Ranking.System.service;

import org.springframework.stereotype.Service;

@Service("smsNotification") // ⭐ Naming the bean
public class SmsNotificationService implements NotificationService {
    @Override
    public void sendNotification(String message) {
        System.out.println("📱 SMS SENT: " + message);
    }
}