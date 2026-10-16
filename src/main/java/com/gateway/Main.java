package com.gateway;

import com.gateway.notification.EncryptedNotification;
import com.gateway.notification.MarketingNotification;
import com.gateway.notification.Notification;
import com.gateway.notification.ScheduledBatchNotification;
import com.gateway.notification.UrgentNotification;
import com.gateway.sender.EmailMessageSender;
import com.gateway.sender.PushNotificationSender;
import com.gateway.sender.SmsMessageSender;
import com.gateway.sender.TelegramMessageSender;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        TelegramMessageSender telegram = new TelegramMessageSender();
        EmailMessageSender email = new EmailMessageSender();
        SmsMessageSender sms = new SmsMessageSender();
        PushNotificationSender push = new PushNotificationSender();

        title("1. UrgentNotification + Telegram");
        Notification urgent = new UrgentNotification(telegram);
        urgent.send("@gulsezim", "Server is down");

        title("2. EncryptedNotification + Email");
        Notification encrypted = new EncryptedNotification(email, "secret");
        encrypted.send("student@example.com", "Your grade is 100");

        title("3. ScheduledBatchNotification + SMS");
        List<String> phones = List.of(
                "+77010000001", "+77010000002", "+77010000003", "+77010000004",
                "+77010000005", "+77010000006", "+77010000007", "+77010000008",
                "invalid-number", "87010000010");
        Notification batch = new ScheduledBatchNotification(sms, phones);
        batch.send("evening-group", "Lecture moved to 15:00");

        title("4. Runtime switching: Telegram -> Email");
        Notification switching = new UrgentNotification(telegram);
        switching.send("user@example.com", "Deploy finished");
        switching.setSender(email);
        switching.send("user@example.com", "Deploy finished");

        title("5. Telegram unavailable -> fallback to SMS");
        telegram.setAvailable(false);
        UrgentNotification withFallback = new UrgentNotification(telegram, sms);
        withFallback.send("+77011234567", "Database CPU at 99%");
        System.out.println("Delivered via: " + withFallback.getLastUsedChannel());
        telegram.setAvailable(true);

        title("6. MarketingNotification + PushNotificationSender");
        Notification marketing = new MarketingNotification(push, "SALE");
        marketing.send("device-token-42", "50% off all courses this week!");
    }

    private static void title(String text) {
        System.out.println("\n=== " + text + " ===");
    }
}
