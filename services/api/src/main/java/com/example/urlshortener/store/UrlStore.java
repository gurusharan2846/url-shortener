package com.example.urlshortener.store;


public interface UrlStore {
    boolean putIfAbsent(String code, String longUrl); // returns true if inserted
    String get(String code);
}
