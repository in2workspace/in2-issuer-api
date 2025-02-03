package es.in2.issuer.infrastructure.repository;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;
import java.util.Objects;
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

    public Mono<T> get(String key) {
        T value = cache.getIfPresent(key);
        if (value != null) {
            return Mono.just(value);
        } else {
            return Mono.error(new NoSuchElementException("Value is not present."));
        }
    }

    public Mono<Void> delete(String key) {
        return Mono.fromRunnable(() -> cache.invalidate(key));
    }

    public Mono<String> add(String key, T value) {
        return Mono.fromCallable(() -> {
            if (key != null && !key.trim().isEmpty() && value != null) {
                cache.put(key, value);
                return key;
            }
            return null;  // Return null to indicate that nothing was added
        }).filter(Objects::nonNull);  // Only emit if the result is non-null
    }

}
