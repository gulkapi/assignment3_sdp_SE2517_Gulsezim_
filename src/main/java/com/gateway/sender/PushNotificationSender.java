package com.gateway.sender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PushNotificationSender implements MessageSender {

    private boolean available = true;
    private final List<String> pushes = new ArrayList<>();

    @Override
    public boolean sendMessage(String recipient, String title, String body) {
        if (!available || recipient == null || recipient.isBlank()) {
            return false;
        }
        String push = "[" + title + "] " + body;
        pushes.add(push);
        System.out.println("[PUSH] to device " + recipient + " -> " + push);
        return true;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String getChannelName() {
        return "Push";
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public List<String> getPushes() {
        return Collections.unmodifiableList(pushes);
    }
}
