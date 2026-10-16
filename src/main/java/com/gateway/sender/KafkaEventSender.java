package com.gateway.sender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KafkaEventSender implements MessageSender {

    private static final String TOPIC = "notifications.events";

    private boolean available = true;
    private long offset = 0;
    private final List<String> publishedEvents = new ArrayList<>();

    @Override
    public boolean sendMessage(String recipient, String title, String body) {
        if (!available || recipient == null || recipient.isBlank()) {
            return false;
        }
        String event = "{\"topic\":\"" + TOPIC + "\",\"key\":\"" + recipient
                + "\",\"offset\":" + offset++ + ",\"title\":\"" + title
                + "\",\"payload\":\"" + body + "\"}";
        publishedEvents.add(event);
        System.out.println("[KAFKA] published event -> " + event);
        return true;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String getChannelName() {
        return "Kafka";
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public List<String> getPublishedEvents() {
        return Collections.unmodifiableList(publishedEvents);
    }
}
