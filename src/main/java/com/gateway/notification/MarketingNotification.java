package com.gateway.notification;

import com.gateway.sender.MessageSender;

public class MarketingNotification extends Notification {

    private static final String TITLE = "Special offer";
    private static final String FOOTER = " Reply STOP to unsubscribe.";

    private final String campaign;

    public MarketingNotification(MessageSender sender, String campaign) {
        super(sender);
        this.campaign = campaign;
    }

    @Override
    public void send(String recipient, String message) {
        String marketingMessage = "[" + campaign + "] " + message + FOOTER;
        if (!sender.sendMessage(recipient, TITLE, marketingMessage)) {
            System.out.println("[Marketing] delivery failed via " + sender.getChannelName());
        }
    }
}
