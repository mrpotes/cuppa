package org.forgerock.cuppa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.forgerock.cuppa.Cuppa.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StackTraceTests {

    @BeforeMethod
    public void setup() {
        Cuppa.reset();
    }

    @Test
    public void cuppaStackTraceShouldBeSmall() {

        //Given
        StackTraceElement[] baseStackTrace;
        try {
            throw new RuntimeException();
        } catch (RuntimeException e) {
            baseStackTrace = e.getStackTrace();
        }
        Reporter reporter = mock(Reporter.class);

        {
            describe("basic API usage", () -> {
                when("the 'when' is run", () -> {
                    it("runs the test", () -> {
                        throw new RuntimeException();
                    });
                });
            });
        }

        //When
        runTests(reporter);

        //Then
        ArgumentCaptor<Throwable> argument = ArgumentCaptor.forClass(Throwable.class);
        verify(reporter).testError(anyString(), argument.capture());
        StackTraceElement[] testStackTrace = argument.getValue().getStackTrace();

        assertThat(testStackTrace.length - baseStackTrace.length).isLessThan(5);
    }
}
