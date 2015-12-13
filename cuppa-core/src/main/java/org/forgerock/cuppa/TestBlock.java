package org.forgerock.cuppa;

import static org.forgerock.cuppa.Behaviour.SKIP;
import static org.forgerock.cuppa.Reporter.Outcome.*;

/**
 * Encapsulates the test ('it') function block.
 */
class TestBlock {

    private final Behaviour behaviour;
    private final String description;
    private final TestFunction function;

    TestBlock(Behaviour behaviour, String description, TestFunction function) {
        this.behaviour = behaviour;
        this.description = description;
        this.function = function;
    }

    public Behaviour getBehaviour() {
        return behaviour;
    }

    void runTest(Behaviour behaviour, Reporter reporter) {
        if (behaviour.combine(this.behaviour) != SKIP) {
            try {
                function.apply();
                reporter.testOutcome(description, PASSED);
            } catch (AssertionError e) {
                reporter.testOutcome(description, FAILED);
            } catch (Exception e) {
                reporter.testOutcome(description, ERRORED);
            }
        } else {
            reporter.testOutcome(description, SKIPPED);
        }
    }
}
