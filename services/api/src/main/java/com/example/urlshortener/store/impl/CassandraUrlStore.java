package com.example.urlshortener.store.impl;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.example.urlshortener.config.ConsistencyConfig;
import com.example.urlshortener.store.UrlStore;
import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
public class CassandraUrlStore implements UrlStore {

    private final CqlSession session;
    private final PreparedStatement selectByCode;
    private final PreparedStatement insertIfNotExists;

    private final ConsistencyLevel readCl;
    private final ConsistencyLevel writeCl;

    public CassandraUrlStore(CqlSession session, ConsistencyConfig.ConsistencyLevels cls) {
        this.session = session;
        this.readCl = cls.read;
        this.writeCl = cls.write;

        this.selectByCode = session.prepare("SELECT long_url FROM urls WHERE short_code = ?");
        this.insertIfNotExists = session.prepare(
                "INSERT INTO urls (short_code, long_url, created_at) VALUES (?, ?, ?) IF NOT EXISTS"
        );
    }

    @Override
    public String get(String code) {
        BoundStatement bs = selectByCode.bind(code).setConsistencyLevel(readCl);
        Row row = session.execute(bs).one();
        return row == null ? null : row.getString("long_url");
    }

    @Override
    public boolean putIfAbsent(String code, String longUrl) {
        BoundStatement bs = insertIfNotExists.bind(code, longUrl, Instant.now())
                .setConsistencyLevel(writeCl);
        Row row = session.execute(bs).one();
        return row != null && row.getBoolean("[applied]");
    }
}

/* Old 1 */
//@Component
//public class CassandraUrlStore implements UrlStore {
//
//    private final PreparedStatement selectByCode;
//    private final PreparedStatement insertIfNotExists;
//    private final CqlSession session;
//
//    public CassandraUrlStore(CqlSession session) {
//        this.session = session;
//        this.selectByCode = session.prepare("SELECT long_url FROM urls WHERE short_code = ?");
//        this.insertIfNotExists = session.prepare(
//                "INSERT INTO urls (short_code, long_url, created_at) VALUES (?, ?, ?) IF NOT EXISTS"
//        );
//    }
//
//    @Override
//    public String get(String shortCode) {
//        Row row = session.execute(selectByCode.bind(shortCode)).one();
//        return row == null ? null : row.getString("long_url");
//    }
//
//    @Override
//    public boolean putIfAbsent(String shortCode, String longUrl) {
//        Row row = session.execute(insertIfNotExists.bind(shortCode, longUrl, Instant.now())).one();
//        return row != null && row.getBoolean("[applied]");
//    }
//}


