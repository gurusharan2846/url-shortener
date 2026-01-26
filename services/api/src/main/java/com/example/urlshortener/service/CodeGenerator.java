package com.example.urlshortener.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class CodeGenerator {
    public String generateCode() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(
                    ThreadLocalRandom.current().nextInt(chars.length())
            ));
        }
        return sb.toString();
    }
}
