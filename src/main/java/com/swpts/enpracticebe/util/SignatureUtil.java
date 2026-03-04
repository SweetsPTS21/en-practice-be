package com.swpts.enpracticebe.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class SignatureUtil {
    private static final String HMAC_ALGO = "HmacSHA256";

    // compute hex string HMAC-SHA256(message, secret)
    public static String computeHmacHex(String secret, String message) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
            byte[] raw = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(raw.length * 2);
            for (byte b : raw) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC", e);
        }
    }

    // constant-time compare to avoid timing attacks
    public static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        byte[] A = a.getBytes(StandardCharsets.UTF_8);
        byte[] B = b.getBytes(StandardCharsets.UTF_8);
        if (A.length != B.length) return false;
        int result = 0;
        for (int i = 0; i < A.length; i++) {
            result |= A[i] ^ B[i];
        }
        return result == 0;
    }

    // message format used in worker: key + "\n" + timestamp
    public static String buildMessage(String key, String timestamp) {
        return Objects.toString(key, "") + "\n" + Objects.toString(timestamp, "");
    }
}
