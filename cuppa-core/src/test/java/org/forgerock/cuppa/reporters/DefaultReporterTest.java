package org.forgerock.cuppa.reporters;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.forgerock.cuppa.Cuppa.*;

import java.io.ByteArrayOutputStream;

import org.forgerock.cuppa.Cuppa;
import org.forgerock.cuppa.Reporter;
import org.forgerock.cuppa.TestFunction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DefaultReporterTest {

    @BeforeMethod
    public void setup() {
        Cuppa.reset();
    }

    @Test
    public void reporterShouldLookGreat() {

        //Given
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Reporter reporter = new DefaultReporter(baos);
        {
            describe("describe", () -> {
                when("when", () -> {
                    it("passing test", TestFunction.identity());
                    it("failing test", () -> {
                        assertThat(false).isTrue();
                    });
                });
            });
        }

        //When
        Cuppa.runTests(reporter);

        //Then
        String output = new String(baos.toByteArray(), UTF_8);
        String[] expectedLines = {
                "",
                "",
                "  describe",
                "    when",
                "      ✓ passing test",
                "      1) failing test",
                "",
                "",
                "  1 passing",
                "  1 failing",
                "",
                "  1) describe when failing test:",
                "     org.junit.ComparisonFailure: expected:<[tru]e> but was:<[fals]e>",
        };
        String expectedOutput = String.join(System.lineSeparator(), expectedLines);
        assertThat(output).startsWith(expectedOutput);
    }
}
