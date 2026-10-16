package com.gateway.sender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EmailMessageSender implements MessageSender {

    private static final String SIGNATURE = "-- \nBest regards,\nNotification Gateway Team";

    private boolean available = true;
    private final List<String> sentMessages = new ArrayList<>();

    @Override
    public boolean sendMessage(String recipient, String title, String body) {
        if (!available || recipient == null || recipient.isBlank()) {
            return false;
        }
        String email = "Subject: " + title + "\n\n" + body + "\n\n" + SIGNATURE;
        sentMessages.add(email);
        System.out.println("[EMAIL] to " + recipient + " -> " + email.replace("\n", " | "));
        return true;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String getChannelName() {
        return "Email";
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public List<String> getSentMessages() {
        return Collections.unmodifiableList(sentMessages);
    }
}
