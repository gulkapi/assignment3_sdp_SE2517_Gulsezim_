package com.gateway.notification;

import com.gateway.sender.MessageSender;

import java.util.ArrayList;
import java.util.List;

public class UrgentNotification extends Notification {

    private static final String TITLE = "URGENT";
    private static final String PREFIX = "[URGENT] ";

    private final List<MessageSender> fallbacks = new ArrayList<>();
    private String lastUsedChannel;

    public UrgentNotification(MessageSender sender, MessageSender... fallbackSenders) {
        super(sender);
        for (MessageSender fallback : fallbackSenders) {
            fallbacks.add(fallback);
        }
    }

    public void addFallback(MessageSender fallback) {
        fallbacks.add(fallback);
    }

    @Override
    public void send(String recipient, String message) {
        String urgentMessage = PREFIX + message;
        lastUsedChannel = null;

        List<MessageSender> candidates = new ArrayList<>();
        candidates.add(sender);
        candidates.addAll(fallbacks);

        for (MessageSender candidate : candidates) {
            if (!candidate.isAvailable()) {
                System.out.println("[Urgent] " + candidate.getChannelName() + " is unavailable, trying next sender");
                continue;
            }
            if (candidate.sendMessage(recipient, TITLE, urgentMessage)) {
                lastUsedChannel = candidate.getChannelName();
                return;
            }
            System.out.println("[Urgent] " + candidate.getChannelName() + " failed to deliver, trying next sender");
        }
        System.out.println("[Urgent] delivery failed: no sender could deliver the message");
    }

    public String getLastUsedChannel() {
        return lastUsedChannel;
    }
}
