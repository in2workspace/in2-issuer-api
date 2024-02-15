package es.in2.issuer.api.repository;

import com.google.common.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CacheStoreTest {

    private Cache<String, String> mockCache;
    private CacheStore<String> cacheStore;

    @BeforeEach
    public void setUp() {
        mockCache = Mockito.mock(Cache.class);
        cacheStore = new CacheStore<>(mockCache);
    }

    @Test
    public void constructorWithExpiryDurationAndTimeUnit() {
        // Arrange
        long expiryDuration = 10;
        TimeUnit timeUnit = TimeUnit.MINUTES;
        CacheStore<String> cacheStore = new CacheStore<>(expiryDuration, timeUnit);

        // Assert
        assertNotNull(cacheStore);
    }

    @Test
    public void getExistingKey() {
        // Arrange
        String key = "existingKey";
        String value = "existingValue";
        when(mockCache.getIfPresent(key)).thenReturn(value);

        // Act
        String result = cacheStore.get(key);

        // Assert
        assertEquals(value, result);
        verify(mockCache).getIfPresent(key);
    }

    @Test
    public void getNonExistingKey() {
        // Arrange
        String key = "nonExistingKey";
        when(mockCache.getIfPresent(key)).thenReturn(null);

        // Act
        String result = cacheStore.get(key);

        // Assert
        assertNull(result);
        verify(mockCache).getIfPresent(key);
    }

    @Test
    public void deleteKey() {
        // Arrange
        String key = "keyToDelete";

        // Act
        cacheStore.delete(key);

        // Assert
        verify(mockCache).invalidate(key);
    }

    @Test
    public void addValidKeyAndValue() {
        // Arrange
        String key = "validKey";
        String value = "validValue";

        // Act
        cacheStore.add(key, value);

        // Assert
        verify(mockCache).put(key, value);
    }

    @Test
    public void addNullKey() {
        // Arrange
        String value = "validValue";

        // Act
        cacheStore.add(null, value);

        // Assert
        verify(mockCache, Mockito.never()).put(any(), any());
    }

    @Test
    public void addEmptyKey() {
        // Arrange
        String key = "   ";
        String value = "validValue";

        // Act
        cacheStore.add(key, value);

        // Assert
        verify(mockCache, Mockito.never()).put(any(), any());
    }

    @Test
    public void addNullValue() {
        // Arrange
        String key = "validKey";
        // Act
        cacheStore.add(key, null);

        // Assert
        verify(mockCache, Mockito.never()).put(any(), any());
    }

}
