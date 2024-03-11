package es.in2.issuer.infrastructure.configuration.util;

import es.in2.issuer.infrastructure.configuration.exception.ConfigAdapterFactoryException;
import es.in2.issuer.infrastructure.configuration.service.GenericConfigAdapter;
import es.in2.issuer.infrastructure.configuration.util.ConfigAdapterFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigAdapterFactoryTest {
    @Mock
    private GenericConfigAdapter mockAdapter;

    private ConfigAdapterFactory configAdapterFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        configAdapterFactory = new ConfigAdapterFactory(Collections.singletonList(mockAdapter));
    }

    @Test
    void getAdapter_Success() {
        GenericConfigAdapter adapter = configAdapterFactory.getAdapter();
        assertEquals(mockAdapter, adapter);
    }

    @Test
    void getAdapter_ExceptionThrownWhenNoAdapterPresent() {
        configAdapterFactory = new ConfigAdapterFactory(Collections.emptyList());
        assertThrows(ConfigAdapterFactoryException.class, configAdapterFactory::getAdapter);
    }

    @Test
    void getAdapter_ExceptionThrownWhenMultipleAdaptersPresent() {
        List<GenericConfigAdapter> multipleAdapters = List.of(mockAdapter, mockAdapter);
        configAdapterFactory = new ConfigAdapterFactory(multipleAdapters);
        assertThrows(ConfigAdapterFactoryException.class, configAdapterFactory::getAdapter);
    }
}
