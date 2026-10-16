package com.gateway.sender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SmsMessageSender implements MessageSender {

    public static final int MAX_LENGTH = 160;

    private boolean available = true;
    private final List<String> sentMessages = new ArrayList<>();

    @Override
    public boolean sendMessage(String recipient, String title, String body) {
        if (!available || recipient == null || !recipient.startsWith("+")) {
            return false;
        }
        if (body == null || body.length() > MAX_LENGTH) {
            System.out.println("[SMS] rejected: message exceeds " + MAX_LENGTH + " characters");
            return false;
        }
        sentMessages.add(body);
        System.out.println("[SMS] to " + recipient + " -> " + body);
        return true;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String getChannelName() {
        return "SMS";
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public List<String> getSentMessages() {
        return Collections.unmodifiableList(sentMessages);
    }
}
