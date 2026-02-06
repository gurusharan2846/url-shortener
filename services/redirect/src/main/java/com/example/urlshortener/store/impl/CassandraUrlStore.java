package com.example.urlshortener.store.impl;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.example.urlshortener.config.ConsistencyConfig;
import com.example.urlshortener.store.UrlStore;
import org.springframework.stereotype.Component;

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
}
