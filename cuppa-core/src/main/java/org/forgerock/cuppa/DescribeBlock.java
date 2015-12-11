package org.forgerock.cuppa;

import static org.forgerock.cuppa.Reporter.Outcome.ERRORED;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Encapsulates the 'describe' and 'when' function blocks and all nested 'describe', 'when' and
 * test ('it') function blocks.
 */
class DescribeBlock {

    private final Behaviour behaviour;
    private final String description;
    private final List<DescribeBlock> describeBlocks = new ArrayList<>();
    private final List<HookFunction> beforeFunctions = new ArrayList<>();
    private final List<HookFunction> afterFunctions = new ArrayList<>();
    private final List<HookFunction> beforeEachFunctions = new ArrayList<>();
    private final List<HookFunction> afterEachFunctions = new ArrayList<>();
    private final List<TestBlock> testBlocks = new ArrayList<>();

    DescribeBlock(Behaviour behaviour, String description) {
        Objects.requireNonNull(behaviour, "Block must have a behaviour");
        Objects.requireNonNull(description, "Block must have a description");
        this.behaviour = behaviour;
        this.description = description;
    }

    void runTests(Reporter reporter) {
        runTests(behaviour, reporter, TestFunction::apply);
    }

    private void runTests(Behaviour behaviour, Reporter reporter, TestWrapper outerTestWrapper) {
        Behaviour combinedBehaviour = behaviour.combine(this.behaviour);
        TestWrapper testWrapper = createWrapper(outerTestWrapper, reporter);
        try {
            reporter.describeStart(description);
            try {
                for (HookFunction f : beforeFunctions) {
                    f.apply();
                }
            } catch (Exception e) {
                reporter.testOutcome("before", ERRORED);
                return;
            }
            for (TestBlock t : testBlocks) {
                testWrapper.apply(() -> t.runTest(combinedBehaviour, reporter));
            }
            describeBlocks.stream().forEach((d) -> d.runTests(combinedBehaviour, reporter, testWrapper));
        } catch (HookException e) {
            if (e.getDescribeBlock() != this) {
                throw e;
            }
        } catch (Exception e) {
            // This should never happen if the test framework is correct because all exceptions from user code should've
            // been caught by now.
            throw new RuntimeException(e);
        } finally {
            try {
                for (HookFunction f : afterFunctions) {
                    f.apply();
                }
            } catch (Exception e) {
                reporter.testOutcome("after", ERRORED);
            }
            reporter.describeEnd(description);
        }
    }

    private TestWrapper createWrapper(TestWrapper outerTestRunner, Reporter reporter) {
        return outerTestRunner.compose((f) -> {
            try {
                try {
                    for (HookFunction g : beforeEachFunctions) {
                        g.apply();
                    }
                } catch (Exception e) {
                    reporter.testOutcome("beforeEach", ERRORED);
                    throw new HookException(this, e);
                }
                f.apply();
            } finally {
                try {
                    for (HookFunction g : afterEachFunctions) {
                        g.apply();
                    }
                } catch (Exception e) {
                    reporter.testOutcome("afterEach", ERRORED);
                    throw new HookException(this, e);
                }
            }
        });
    }

    void addDescribeBlock(DescribeBlock describeBlock) {
        describeBlocks.add(describeBlock);
    }

    void addBefore(Optional<String> description, HookFunction function) {
        beforeFunctions.add(function);
    }

    void addAfter(Optional<String> description, HookFunction function) {
        afterFunctions.add(function);
    }

    void addBeforeEach(Optional<String> description, HookFunction function) {
        beforeEachFunctions.add(function);
    }

    void addAfterEach(Optional<String> description, HookFunction function) {
        afterEachFunctions.add(function);
    }

    void addTest(TestBlock testBlock) {
        testBlocks.add(testBlock);
    }

    @FunctionalInterface
    private interface TestWrapper {
        void apply(TestFunction testRunner) throws Exception;

        default TestWrapper compose(TestWrapper after) {
            return (f) -> apply(() -> after.apply(f));
        }
    }
}
