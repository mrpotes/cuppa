package org.forgerock.cuppa;

import static org.forgerock.cuppa.Behaviour.*;
import static org.forgerock.cuppa.Cuppa.describe;
import static org.forgerock.cuppa.Cuppa.it;
import static org.forgerock.cuppa.Cuppa.when;
import static org.forgerock.cuppa.Reporter.Outcome.PASSED;
import static org.forgerock.cuppa.Reporter.Outcome.SKIPPED;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SkipTests {

    @BeforeMethod
    public void setup() {
        Cuppa.reset();
    }

    @Test
    public void shouldSkipTestIfTestIsMarkedSkip() throws Exception {

        //Given
        TestFunction testFunction = mock(TestFunction.class);
        {
            describe("basic API usage", () -> {
                when("the 'when' is run", () -> {
                    it(SKIP, "test", testFunction);
                });
            });
        }

        //When
        Cuppa.runTests(mock(Reporter.class));

        //Then
        verify(testFunction, never()).apply();
    }

    @Test
    public void shouldReportTestSkippedIfTestIsMarkedSkip() throws Exception {

        //Given
        Reporter reporter = mock(Reporter.class);
        TestFunction testFunction = mock(TestFunction.class);
        {
            describe("basic API usage", () -> {
                when("the 'when' is run", () -> {
                    it(SKIP, "test", testFunction);
                });
            });
        }

        //When
        Cuppa.runTests(reporter);

        //Then
        verify(reporter).testOutcome("test", SKIPPED);
    }

    @Test
    public void shouldSkipTestsNestedInSkippedBlock() throws Exception {

        //Given
        TestFunction testFunction1 = mock(TestFunction.class);
        TestFunction testFunction2 = mock(TestFunction.class);
        {
            describe("basic API usage", () -> {
                when(SKIP, "the 'when' is run", () -> {
                    it("runs the test", testFunction1);
                    it("runs the test", testFunction2);
                });
            });
        }

        //When
        Cuppa.runTests(mock(Reporter.class));

        //Then
        verify(testFunction1, never()).apply();
        verify(testFunction2, never()).apply();
    }

    @Test
    public void shouldReportAllSkippedTestsNestedInSkippedBlock() throws Exception {

        //Given
        Reporter reporter = mock(Reporter.class);
        TestFunction testFunction1 = mock(TestFunction.class);
        TestFunction testFunction2 = mock(TestFunction.class);
        {
            describe("basic API usage", () -> {
                when(SKIP, "the 'when' is run", () -> {
                    it("test1", testFunction1);
                    it("test2", testFunction2);
                });
            });
        }

        //When
        Cuppa.runTests(reporter);

        //Then
        verify(reporter).testOutcome("test1", SKIPPED);
        verify(reporter).testOutcome("test2", SKIPPED);
    }

    @Test
    public void shouldIgnoreTestsIfOtherTestIsMarkedOnly() throws Exception {

        //Given
        TestFunction testFunctionBefore = mock(TestFunction.class);
        TestFunction testFunction = mock(TestFunction.class);
        TestFunction testFunctionAfter = mock(TestFunction.class);
        {
            describe("basic API usage", () -> {
                when("the 'when' is run", () -> {
                    it("before test", testFunctionBefore);
                    it(ONLY, "test", testFunction);
                    it("after test", testFunctionAfter);
                });
            });
        }

        //When
        Cuppa.runTests(mock(Reporter.class));

        //Then
        verify(testFunctionBefore, never()).apply();
        verify(testFunction).apply();
        verify(testFunctionAfter, never()).apply();
    }

    @Test
    public void shouldNotReportTestsIfOtherTestIsMarkedOnly() throws Exception {

        //Given
        Reporter reporter = mock(Reporter.class);
        {
            describe("basic API usage", () -> {
                when("the 'when' is run", () -> {
                    it("before test", TestFunction.identity());
                    it(ONLY, "test", TestFunction.identity());
                    it("after test", TestFunction.identity());
                });
            });
        }

        //When
        Cuppa.runTests(reporter);

        //Then
        verify(reporter).testOutcome(any(), any());
        verify(reporter).testOutcome("test", PASSED);
    }

    @Test
    public void shouldRunAllTestsMarkedOnly() throws Exception {

        //Given
        TestFunction testFunctionOnly1 = mock(TestFunction.class);
        TestFunction testFunctionOnly2 = mock(TestFunction.class);
        {
            describe("basic API usage", () -> {
                when("the 'when' is run", () -> {
                    it("before test", TestFunction.identity());
                    it(ONLY, "only test 1", testFunctionOnly1);
                    it(ONLY, "only test 2", testFunctionOnly2);
                });
            });
        }

        //When
        Cuppa.runTests(mock(Reporter.class));

        //Then
        verify(testFunctionOnly1).apply();
        verify(testFunctionOnly2).apply();
    }

    @Test
    public void shouldRunTestsInBlockMarkedOnly() throws Exception {

        //Given
        TestFunction testFunction = mock(TestFunction.class);
        TestFunction testFunctionAfter = mock(TestFunction.class);
        {
            describe("basic API usage", () -> {
                when(ONLY, "only when", () -> {
                    it("test", testFunction);
                });
                when("after when", () -> {
                    it("test", testFunctionAfter);
                });
            });
        }

        //When
        Cuppa.runTests(mock(Reporter.class));

        //Then
        verify(testFunction).apply();
        verify(testFunctionAfter, never()).apply();
    }

    @Test
    public void shouldSkipTestsMarkedSkipInBlockMarkedOnly() throws Exception {

        //Given
        TestFunction testFunction = mock(TestFunction.class);
        {
            describe("basic API usage", () -> {
                when(ONLY, "only when", () -> {
                    it(SKIP, "test", testFunction);
                });
            });
        }

        //When
        Cuppa.runTests(mock(Reporter.class));

        //Then
        verify(testFunction, never()).apply();
    }

    @Test
    public void shouldSkipTestsMarkedOnlyInBlockMarkedSkip() throws Exception {

        //Given
        TestFunction testFunction = mock(TestFunction.class);
        {
            describe("basic API usage", () -> {
                when(SKIP, "only when", () -> {
                    it(ONLY, "test", testFunction);
                });
            });
        }

        //When
        Cuppa.runTests(mock(Reporter.class));

        //Then
        verify(testFunction, never()).apply();
    }

    @Test
    public void shouldIgnoreTestIfOtherTestIsMarkedOnlyInSkipBlock() throws Exception {

        //Given
        TestFunction testFunction = mock(TestFunction.class);
        {
            describe("basic API usage", () -> {
                when(SKIP, "only when", () -> {
                    it(ONLY, "test", TestFunction.identity());
                });
                it("test 2", testFunction);
            });
        }

        //When
        Cuppa.runTests(mock(Reporter.class));

        //Then
        verify(testFunction, never()).apply();
    }
}
