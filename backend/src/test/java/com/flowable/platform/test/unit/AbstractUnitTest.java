package com.flowable.platform.test.unit;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.*;

/**
 * Base class for unit tests with Mockito setup.
 * Provides common assertion methods and mock creation helpers.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public abstract class AbstractUnitTest {

    /**
     * Create a mock of the specified class
     */
    protected <T> T mock(Class<T> classToMock) {
        return Mockito.mock(classToMock);
    }

    /**
     * Create a spy of the specified class
     */
    protected <T> T spy(T instance) {
        return Mockito.spy(instance);
    }

    /**
     * Verify a mock was called exactly once
     */
    protected <T> void verifyCalledOnce(T mock) {
        verify(mock, times(1));
    }

    /**
     * Verify a mock was never called
     */
    protected <T> void verifyNeverCalled(T mock) {
        verify(mock, never());
    }

    /**
     * Reset a mock
     */
    protected <T> void resetMock(T mock) {
        reset(mock);
    }
}
