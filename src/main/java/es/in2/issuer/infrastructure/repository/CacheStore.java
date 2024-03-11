package es.in2.issuer.infrastructure.repository;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class CacheStore<T> {

    private final Cache<String, T> cache;

    public CacheStore(long expiryDuration, TimeUnit timeUnit) {
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(expiryDuration, timeUnit)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();
    }

    public T get(String key) {
        return cache.getIfPresent(key);
    }

    public void delete(String key) {
        cache.invalidate(key);
    }

    public void add(String key, T value) {
        if (key != null && !key.trim().isEmpty() && value != null) {
            cache.put(key, value);
        }
    }

}
