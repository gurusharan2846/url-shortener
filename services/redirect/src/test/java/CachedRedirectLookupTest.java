import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import com.example.urlshortener.cache.RedisUrlCache;
import com.example.urlshortener.service.CachedRedirectLookup;
import com.example.urlshortener.store.UrlStore;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class CachedRedirectLookupTest {

    @Test
    void singleflight_allows_only_one_db_read_per_key() throws Exception {
        // mocks
        RedisUrlCache cache = mock(RedisUrlCache.class);
        UrlStore store = mock(UrlStore.class);

        // Redis always miss initially
        when(cache.get("vpn7ke")).thenReturn(null);

        // Cassandra returns a URL, but we slow it to increase contention
        when(store.get("vpn7ke")).thenAnswer(inv -> {
            Thread.sleep(150);
            return "https://example.com";
        });

        CachedRedirectLookup lookup =
                new CachedRedirectLookup(cache, store, new SimpleMeterRegistry());

        int threads = 25;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<String>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                ready.countDown();
                start.await();
                return lookup.resolveLongUrl("vpn7ke");
            }));
        }

        ready.await();      // all threads lined up
        start.countDown();  // release them together

        for (Future<String> f : futures) {
            String v = f.get(2, TimeUnit.SECONDS);
            assert "https://example.com".equals(v);
        }

        pool.shutdownNow();

        // KEY ASSERTION: only one Cassandra read
        verify(store, times(1)).get("vpn7ke");

        // cache.put likely called once (only loader does it)
        verify(cache, times(1)).put("vpn7ke", "https://example.com");
    }
}
