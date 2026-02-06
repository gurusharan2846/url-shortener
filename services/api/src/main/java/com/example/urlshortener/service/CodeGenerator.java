package com.example.urlshortener.service;

import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class CodeGenerator {

    private static final char[] BASE62 =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    private final int minLen = 6;
    private final int maxLen = 12;

    private final ThreadLocal<MessageDigest> sha256 = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    });

    /** Create deterministic Base62 string from (url or url#attempt). */
    private String base62For(String longUrl, int attempt) {
        String input = (attempt == 0) ? longUrl : (longUrl + "#" + attempt);

        MessageDigest md = sha256.get();
        md.reset();
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        return toBase62(digest);
    }

    /** Generate candidate code for a given attempt + length. */
    public String candidateCode(String longUrl, int attempt, int len) {
        String base62 = base62For(longUrl, attempt);
        if (len > base62.length()) throw new IllegalArgumentException("len too large");
        return base62.substring(0, len);
    }

    public int minLen() { return minLen; }
    public int maxLen() { return maxLen; }

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

/* Old 1*/
//@Component
//public class CodeGenerator {
//
//    private static final char[] BASE62 =
//            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
//
//    private final UrlStore store;
//    private final MessageDigest sha256;
//
//    // Tune these
//    private final int minLen = 6;
//    private final int maxLen = 12;
//
//    public CodeGenerator(UrlStore store) {
//        this.store = store;
//        try {
//            this.sha256 = MessageDigest.getInstance("SHA-256");
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    /**
//     * Deterministic + collision-safe:
//     * - Same URL tries to get same code
//     * - If collision with different URL, it deterministically tries url#1, url#2...
//     */
//    public String generateCodeForUrl(String longUrl) {
//        for (int attempt = 0; attempt < 10_000; attempt++) {
//            byte[] digest = sha256.digest((attempt == 0 ? longUrl : longUrl + "#" + attempt)
//                    .getBytes(StandardCharsets.UTF_8));
//            System.out.println(Arrays.toString(digest));
//            String base62 = toBase62(digest);
//            System.out.println(base62);
//
//            for (int len = minLen; len <= maxLen && len <= base62.length(); len++) {
//                String code = base62.substring(0, len);
//
//                String mapped = store.get(code);
//                if (mapped == null || mapped.equals(longUrl)) {
//                    return code;
//                }
//                // else: collision with different URL -> try longer, then next attempt
//            }
//        }
//        throw new IllegalStateException("Unable to allocate code");
//    }
//
//    private static String toBase62(byte[] bytes) {
//        int[] digits = new int[]{0};
//
//        for (byte b : bytes) {
//            int carry = b & 0xFF;
//            for (int i = 0; i < digits.length; i++) {
//                int val = (digits[i] << 8) + carry;
//                digits[i] = val % 62;
//                carry = val / 62;
//            }
//            while (carry > 0) {
//                int[] nd = new int[digits.length + 1];
//                System.arraycopy(digits, 0, nd, 0, digits.length);
//                nd[digits.length] = carry % 62;
//                digits = nd;
//                carry /= 62;
//            }
//        }
//
//        StringBuilder sb = new StringBuilder(digits.length);
//        for (int i = digits.length - 1; i >= 0; i--) sb.append(BASE62[digits[i]]);
//        return sb.toString();
//    }
//}
