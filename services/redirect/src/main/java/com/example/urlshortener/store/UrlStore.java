package com.example.urlshortener.store;

public interface UrlStore {
    //void put(String code, String longUrl);
    String get(String code);
}
