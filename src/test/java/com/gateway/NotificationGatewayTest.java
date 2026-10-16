package com.gateway;

import com.gateway.notification.BatchSummary;
import com.gateway.notification.EncryptedNotification;
import com.gateway.notification.MarketingNotification;
import com.gateway.notification.Notification;
import com.gateway.notification.ScheduledBatchNotification;
import com.gateway.notification.UrgentNotification;
import com.gateway.sender.EmailMessageSender;
import com.gateway.sender.KafkaEventSender;
import com.gateway.sender.PushNotificationSender;
import com.gateway.sender.SmsMessageSender;
import com.gateway.sender.TelegramMessageSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationGatewayTest {

    private TelegramMessageSender telegram;
    private EmailMessageSender email;
    private SmsMessageSender sms;
    private KafkaEventSender kafka;

    @BeforeEach
    void setUp() {
        telegram = new TelegramMessageSender();
        email = new EmailMessageSender();
        sms = new SmsMessageSender();
        kafka = new KafkaEventSender();
    }

    @Test
    void urgentNotificationDeliversMessageWithUrgentPrefix() {
        Notification notification = new UrgentNotification(telegram);

        notification.send("@gulsezim", "Server is down");

        assertEquals(1, telegram.getSentMessages().size());
        assertTrue(telegram.getSentMessages().get(0).contains("[URGENT] Server is down"));
    }

    @Test
    void unavailableProviderDoesNotDeliverMessage() {
        email.setAvailable(false);

        boolean result = email.sendMessage("user@example.com", "Title", "Body");

        assertFalse(email.isAvailable());
        assertFalse(result);
        assertTrue(email.getSentMessages().isEmpty());
    }

    @Test
    void urgentNotificationFallsBackToSmsWhenTelegramIsUnavailable() {
        telegram.setAvailable(false);
        UrgentNotification notification = new UrgentNotification(telegram, sms);

        notification.send("+77011234567", "Database CPU at 99%");

        assertTrue(telegram.getSentMessages().isEmpty());
        assertEquals(1, sms.getSentMessages().size());
        assertEquals("SMS", notification.getLastUsedChannel());
    }

    @Test
    void smsRejectsMessagesLongerThanLimit() {
        String exactLimit = "a".repeat(SmsMessageSender.MAX_LENGTH);
        String tooLong = "a".repeat(SmsMessageSender.MAX_LENGTH + 1);

        assertTrue(sms.sendMessage("+77010000001", "Title", exactLimit));
        assertFalse(sms.sendMessage("+77010000001", "Title", tooLong));
        assertEquals(1, sms.getSentMessages().size());
    }

    @Test
    void encryptedNotificationSendsTransformedMessage() {
        Notification notification = new EncryptedNotification(email, "secret");

        notification.send("student@example.com", "Your grade is 100");

        String sent = email.getSentMessages().get(0);
        String encrypted = EncryptedNotification.encrypt("Your grade is 100", "secret");
        assertTrue(sent.contains(encrypted));
        assertFalse(sent.contains("Your grade is 100"));
        assertNotEquals("Your grade is 100", encrypted);
        assertEquals("Your grade is 100", EncryptedNotification.decrypt(encrypted, "secret"));
    }

    @Test
    void batchNotificationReportsSuccessfulAndFailedDeliveries() {
        List<String> recipients = List.of("+77010000001", "+77010000002", "+77010000003", "invalid", "87010000005");
        ScheduledBatchNotification notification = new ScheduledBatchNotification(sms, recipients);

        notification.send("group", "Lecture moved");

        BatchSummary summary = notification.getLastSummary();
        assertEquals(5, summary.total());
        assertEquals(3, summary.successful());
        assertEquals(2, summary.failed());
        assertEquals("Total: 5\nSuccessful: 3\nFailed: 2", summary.toString());
    }

    @Test
    void runtimeSenderSwitchingUsesTheSameNotificationObject() {
        Notification notification = new UrgentNotification(telegram);

        notification.send("user@example.com", "Deploy finished");
        notification.setSender(email);
        notification.send("user@example.com", "Deploy finished");

        assertEquals(1, telegram.getSentMessages().size());
        assertEquals(1, email.getSentMessages().size());
    }

    @Test
    void newPushSenderWorksWithExistingNotificationClasses() {
        PushNotificationSender push = new PushNotificationSender();

        new UrgentNotification(push).send("device-1", "Alert");
        new EncryptedNotification(push, "key").send("device-1", "Secret");

        assertEquals(2, push.getPushes().size());
        assertTrue(push.getPushes().get(0).contains("[URGENT] Alert"));
    }

    @Test
    void newMarketingNotificationWorksWithExistingSenders() {
        MarketingNotification notification = new MarketingNotification(telegram, "SALE");

        notification.send("@gulsezim", "50% off");
        notification.setSender(email);
        notification.send("user@example.com", "50% off");

        assertTrue(telegram.getSentMessages().get(0).contains("[SALE] 50% off"));
        assertTrue(email.getSentMessages().get(0).contains("[SALE] 50% off"));
    }

    @Test
    void urgentNotificationFailsGracefullyWhenAllSendersAreUnavailable() {
        telegram.setAvailable(false);
        sms.setAvailable(false);
        UrgentNotification notification = new UrgentNotification(telegram, sms);

        notification.send("+77011234567", "Anything");

        assertNull(notification.getLastUsedChannel());
        assertTrue(telegram.getSentMessages().isEmpty());
        assertTrue(sms.getSentMessages().isEmpty());
    }

    @Test
    void kafkaSenderPublishesEventWithRecipientAsKey() {
        new UrgentNotification(kafka).send("order-service", "Payment failed");

        assertEquals(1, kafka.getPublishedEvents().size());
        assertTrue(kafka.getPublishedEvents().get(0).contains("\"key\":\"order-service\""));
        assertTrue(kafka.getPublishedEvents().get(0).contains("[URGENT] Payment failed"));
    }

    @Test
    void blankRecipientIsRejectedByAllSenders() {
        assertFalse(telegram.sendMessage(" ", "Title", "Body"));
        assertFalse(email.sendMessage(null, "Title", "Body"));
        assertFalse(kafka.sendMessage("", "Title", "Body"));
        assertFalse(sms.sendMessage("no-plus", "Title", "Body"));
    }

    @Test
    void urgentNotificationSkipsPrimaryThatFailsToDeliver() {
        UrgentNotification notification = new UrgentNotification(sms, email);

        notification.send("user@example.com", "Check inbox");

        assertTrue(sms.getSentMessages().isEmpty());
        assertEquals(1, email.getSentMessages().size());
        assertEquals("Email", notification.getLastUsedChannel());
    }
}
