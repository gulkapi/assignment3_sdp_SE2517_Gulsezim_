package com.gateway.notification;

import com.gateway.sender.MessageSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScheduledBatchNotification extends Notification {

    private static final String TITLE = "Scheduled notification";

    private final List<String> recipients = new ArrayList<>();
    private BatchSummary lastSummary;

    public ScheduledBatchNotification(MessageSender sender, List<String> recipients) {
        super(sender);
        this.recipients.addAll(recipients);
    }

    public void addRecipient(String recipient) {
        recipients.add(recipient);
    }

    public List<String> getRecipients() {
        return Collections.unmodifiableList(recipients);
    }

    @Override
    public void send(String batchName, String message) {
        int successful = 0;
        int failed = 0;
        for (String recipient : recipients) {
            if (sender.isAvailable() && sender.sendMessage(recipient, TITLE, message)) {
                successful++;
            } else {
                failed++;
            }
        }
        lastSummary = new BatchSummary(recipients.size(), successful, failed);
        System.out.println("[Batch: " + batchName + " via " + sender.getChannelName() + "]\n" + lastSummary);
    }

    public BatchSummary getLastSummary() {
        return lastSummary;
    }
}
