package com.gateway.notification;

import com.gateway.sender.MessageSender;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EncryptedNotification extends Notification {

    private static final String TITLE = "Encrypted message";

    private final String key;

    public EncryptedNotification(MessageSender sender, String key) {
        super(sender);
        this.key = key;
    }

    @Override
    public void send(String recipient, String message) {
        String encrypted = encrypt(message, key);
        if (!sender.sendMessage(recipient, TITLE, encrypted)) {
            System.out.println("[Encrypted] delivery failed via " + sender.getChannelName());
        }
    }

    public static String encrypt(String text, String key) {
        return Base64.getEncoder().encodeToString(xor(text.getBytes(StandardCharsets.UTF_8), key));
    }

    public static String decrypt(String encoded, String key) {
        byte[] decoded = Base64.getDecoder().decode(encoded);
        return new String(xor(decoded, key), StandardCharsets.UTF_8);
    }

    private static byte[] xor(byte[] data, String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ keyBytes[i % keyBytes.length]);
        }
        return result;
    }
}
