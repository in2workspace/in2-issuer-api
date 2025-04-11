package es.in2.issuer.shared.infrastructure.config.properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiPropertiesTest {

    @Test
    void testApiProperties() {
        ApiProperties.MemoryCache memoryCache = new ApiProperties.MemoryCache(10L, 10L);
        ApiProperties apiProperties = new ApiProperties("externalDomain", "internalDomain", "configSource", memoryCache);

        assertEquals("externalDomain", apiProperties.externalDomain());
        assertEquals("internalDomain", apiProperties.internalDomain());
        assertEquals("configSource", apiProperties.configSource());
        assertEquals(memoryCache, apiProperties.cacheLifetime());
    }

}