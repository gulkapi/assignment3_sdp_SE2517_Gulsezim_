package com.gateway.sender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TelegramMessageSender implements MessageSender {

    private boolean available = true;
    private final List<String> sentMessages = new ArrayList<>();

    @Override
    public boolean sendMessage(String recipient, String title, String body) {
        if (!available || recipient == null || recipient.isBlank()) {
            return false;
        }
        String formatted = "*" + title + "*\n_" + body + "_";
        sentMessages.add(formatted);
        System.out.println("[TELEGRAM] to " + recipient + " -> " + formatted.replace("\n", " | "));
        return true;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String getChannelName() {
        return "Telegram";
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public List<String> getSentMessages() {
        return Collections.unmodifiableList(sentMessages);
    }
}
