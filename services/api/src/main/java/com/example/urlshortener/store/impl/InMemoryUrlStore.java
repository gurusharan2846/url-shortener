//package com.example.urlshortener.store.impl;
//
//import com.example.urlshortener.store.UrlStore;
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Component
//public class InMemoryUrlStore implements UrlStore {
//    private final Map<String, String> map = new ConcurrentHashMap<>();
//
//    @Override
//    public void put(String code, String longUrl) {
//        map.put(code, longUrl);
//    }
//
//    @Override
//    public String get(String code) {
//        return map.get(code);
//    }
//}
