package es.in2.issuer.infrastructure.iam.util;

import es.in2.issuer.infrastructure.iam.exception.IamAdapterFactoryException;
import es.in2.issuer.infrastructure.iam.service.GenericIamAdapter;
import es.in2.issuer.infrastructure.iam.util.IamAdapterFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IamAdapterFactoryTest {
    @Mock
    private GenericIamAdapter mockAdapter;

    private IamAdapterFactory iamAdapterFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        iamAdapterFactory = new IamAdapterFactory(Collections.singletonList(mockAdapter));
    }

    @Test
    void getAdapter_Success() {
        GenericIamAdapter adapter = iamAdapterFactory.getAdapter();
        assertEquals(mockAdapter, adapter);
    }

    @Test
    void getAdapter_ExceptionThrownWhenNoAdapterPresent() {
        iamAdapterFactory = new IamAdapterFactory(Collections.emptyList());
        assertThrows(IamAdapterFactoryException.class, iamAdapterFactory::getAdapter);
    }

    @Test
    void getAdapter_ExceptionThrownWhenMultipleAdaptersPresent() {
        List<GenericIamAdapter> multipleAdapters = List.of(mockAdapter, mockAdapter);
        iamAdapterFactory = new IamAdapterFactory(multipleAdapters);
        assertThrows(IamAdapterFactoryException.class, iamAdapterFactory::getAdapter);
    }
}
