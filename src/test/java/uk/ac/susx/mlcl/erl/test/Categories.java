/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.test;

/**
 * Collection of test categories used to mark tests for exclusion or inclusion in particular testing
 * runs.
 * <p/>
 * @author Hamish Morgan
 */
public interface Categories {

    /**
     * Mark tests that likely to take quite a long time to complete. Definition of slow it pretty
     * subject and context sensitive, but generally any test that takes longer than a second should
     * be considered slow.
     * <p/>
     * Slows tests should not be run all the time (i.e on every build) because it will drive me up
     * the wall waiting for them to finish. They should, however, be run before every release.
     */
    public interface SlowTests {
    }

    /**
     * Mark tests that require an active Internet connection. For obvious reasons the should be
     * skipped when an Internet connection is not available.
     */
    public interface OnlineTests extends SlowTests {
    }

    /**
     * Marks tests as being for verification that a dependency is functioning as expected. Creation
     * of these tests a good way of learning new APIs, and insuring they don't change. However, they
     * really don't need to run all the time; perhaps only when something else goes wrong.
     */
    public interface LibraryVerificationTest {
    }

    /**
     * Test which assert correctness when various modules are integrated.
     */
    public interface IntegrationTests extends SlowTests {
    }

    /**
     * Marks tests which do not assert correctness, but rather measure the runtime characteristics
     * of something.
     */
    public interface PerformanceTests extends SlowTests {
    }

    /**
     * Marks tests that will instantiate a Graphical User Interface as part of their operation.
     * Consequently they are slow and can not be run on headless machines.
     */
    public interface GuiTest extends SlowTests {
    }

    /**
     * Marks tests require more than the default maximum RAM allocation (256MB) to run.
     */
    public interface HighMemory extends SlowTests {
    }
}
