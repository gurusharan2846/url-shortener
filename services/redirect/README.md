Consistency
    app.cassandra.writeConsistency default LOCAL_QUORUM
    app.cassandra.readConsistency default LOCAL_ONE
    Trade-off: LOCAL_ONE reads may be stale immediately after writes; switch read to LOCAL_QUORUM for stronger “read-your-writes” behavior (higher latency).