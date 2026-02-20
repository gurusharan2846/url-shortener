package com.example.analytics.repository;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public class HourlyClicksRepository {
    private final PreparedStatement incStmt;
    private final CqlSession session;

    public HourlyClicksRepository(CqlSession session) {
        this.session = session;
        this.incStmt = session.prepare(
                "UPDATE redirect_clicks_hourly " +
                        "SET clicks = clicks + 1 " +
                        "WHERE short_code = ? AND hour_bucket = ?"
        );
    }

    public void increment(String shortCode, Instant hourBucket) {
        BoundStatement bound = incStmt.bind(shortCode, hourBucket);
        session.execute(bound);
    }
}
