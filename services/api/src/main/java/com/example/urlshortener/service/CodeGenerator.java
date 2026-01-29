package com.example.urlshortener.service;

import com.example.urlshortener.store.UrlStore;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;


@Component
public class CodeGenerator {

    private static final char[] BASE62 =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    private final UrlStore store;
    private final MessageDigest sha256;

    // Tune these
    private final int minLen = 6;
    private final int maxLen = 12;

    public CodeGenerator(UrlStore store) {
        this.store = store;
        try {
            this.sha256 = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deterministic + collision-safe:
     * - Same URL tries to get same code
     * - If collision with different URL, it deterministically tries url#1, url#2...
     */
    public String generateCodeForUrl(String longUrl) {
        for (int attempt = 0; attempt < 10_000; attempt++) {
            byte[] digest = sha256.digest((attempt == 0 ? longUrl : longUrl + "#" + attempt)
                    .getBytes(StandardCharsets.UTF_8));
            System.out.println(Arrays.toString(digest));
            String base62 = toBase62(digest);
            System.out.println(base62);

            for (int len = minLen; len <= maxLen && len <= base62.length(); len++) {
                String code = base62.substring(0, len);

                String mapped = store.get(code);
                if (mapped == null || mapped.equals(longUrl)) {
                    return code;
                }
                // else: collision with different URL -> try longer, then next attempt
            }
        }
        throw new IllegalStateException("Unable to allocate code");
    }

    private static String toBase62(byte[] bytes) {
        int[] digits = new int[]{0};

        for (byte b : bytes) {
            int carry = b & 0xFF;
            for (int i = 0; i < digits.length; i++) {
                int val = (digits[i] << 8) + carry;
                digits[i] = val % 62;
                carry = val / 62;
            }
            while (carry > 0) {
                int[] nd = new int[digits.length + 1];
                System.arraycopy(digits, 0, nd, 0, digits.length);
                nd[digits.length] = carry % 62;
                digits = nd;
                carry /= 62;
            }
        }

        StringBuilder sb = new StringBuilder(digits.length);
        for (int i = digits.length - 1; i >= 0; i--) sb.append(BASE62[digits[i]]);
        return sb.toString();
    }
}
