package fi.helsinki.cs.tmc.langs.make;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import com.google.common.base.Optional;

import org.junit.Test;

import java.nio.file.Path;

public class MakePluginTest {

    MakePlugin makePlugin;

    public MakePluginTest() {
        makePlugin = new MakePlugin();
    }

    @Test
    public void testGetLanguageName() {
        assertEquals("make", makePlugin.getLanguageName());
    }

    @Test
    public void testRunTestsWhenBuildFailing() {
        RunResult runResult = makePlugin.runTests(TestUtils.getPath(getClass(), "build-failing"));
        assertEquals(RunResult.Status.COMPILE_FAILED, runResult.status);
    }

    @Test
    public void testmakeProjectWithFailingTestsCompilesAndFailsTests() {
        Path path = TestUtils.getPath(getClass(), "failing");
        RunResult result = makePlugin.runTests(path);

        assertEquals(RunResult.Status.TESTS_FAILED, result.status);
    }

    @Test
    public void testFailingMakeProjectHasOneFailedTest() {
        Path path = TestUtils.getPath(getClass(), "failing");
        RunResult result = makePlugin.runTests(path);

        assertEquals(1, result.testResults.size());
        assertEquals(false, result.testResults.get(0).passed);
    }

    @Test
    public void testFailingMakeProjectHasCorrectError() {
        Path path = TestUtils.getPath(getClass(), "failing");
        RunResult result = makePlugin.runTests(path);

        assertEquals("[Task 1.1] one returned 2. Should have returned: 1", result.testResults.get(0)
                .errorMessage);
    }

    @Test
    public void testmakeProjectWithPassingTestsCompilesAndPassesTests() {
        Path path = TestUtils.getPath(getClass(), "passing");
        RunResult result = makePlugin.runTests(path);

        assertEquals(RunResult.Status.PASSED, result.status);
    }

    @Test
    public void testPassingMakeProjectHasOnePassingTest() {
        Path path = TestUtils.getPath(getClass(), "passing");
        RunResult result = makePlugin.runTests(path);
        assertEquals(1, result.testResults.size());
        assertEquals(true, result.testResults.get(0).passed);
    }

    @Test
    public void testPassingMakeProjectHasNoError() {
        Path path = TestUtils.getPath(getClass(), "passing");
        RunResult result = makePlugin.runTests(path);

        assertEquals("", result.testResults.get(0).errorMessage);
    }

    @Test
    public void testPassingMakeProjectHasCorrectPoints() {
        Path path = TestUtils.getPath(getClass(), "passing");
        RunResult result = makePlugin.runTests(path);

        assertEquals("1.1", result.testResults.get(0).points.get(0));
        assertEquals(1, result.testResults.get(0).points.size());
    }

    @Test
    public void testFailingMakeProjectHasCorrectPoints() {
        Path path = TestUtils.getPath(getClass(), "failing");
        RunResult result = makePlugin.runTests(path);

        assertEquals(0, result.testResults.get(0).points.size());
    }

    @Test
    public void testPassingMakeProjectWillFailWithValgrindFailure() {
        Path path = TestUtils.getPath(getClass(), "valgrind-failing");
        RunResult result = makePlugin.runTests(path);

        assertTrue(result.testResults.get(0).passed);
        assertFalse(result.testResults.get(1).passed);
        assertTrue(result.testResults.get(0).backtrace.size() == 0);
        assertTrue(result.testResults.get(1).backtrace.size() > 0);
    }

    @Test
    public void testSuitePointsWithPassingSuite() {
        Path path = TestUtils.getPath(getClass(), "passing-suite");
        RunResult result = makePlugin.runTests(path);

        assertEquals(RunResult.Status.PASSED, result.status);
        assertEquals(3, result.testResults.size());
        assertEquals("1.3", result.testResults.get(2).points.get(0));
    }

    @Test
    public void testSuitePointsWithFailingSuite() {
        Path path = TestUtils.getPath(getClass(), "failing-suite");
        RunResult result = makePlugin.runTests(path);

        assertEquals(RunResult.Status.TESTS_FAILED, result.status);
        assertEquals(3, result.testResults.size());
        assertEquals("1.3", result.testResults.get(2).points.get(0));
    }

    @Test
    public void testIsExerciseTypeCorrect() {
        Path path = TestUtils.getPath(getClass(), "passing");
        assertTrue(makePlugin.isExerciseTypeCorrect(path));

        path = TestUtils.getPath(getClass(), "failing");
        assertTrue(makePlugin.isExerciseTypeCorrect(path));

        path = TestUtils.getPath(getClass(), "build-failing");
        assertTrue(makePlugin.isExerciseTypeCorrect(path));

        path = TestUtils.getPath(getClass(), "valgrind-failing");
        assertTrue(makePlugin.isExerciseTypeCorrect(path));

        path = TestUtils.getPath(getClass(), "passing-suite");
        assertTrue(makePlugin.isExerciseTypeCorrect(path));

        path = TestUtils.getPath(getClass(), "failing-suite");
        assertTrue(makePlugin.isExerciseTypeCorrect(path));

        path = TestUtils.getPath(getClass(), "nonexistent");
        assertFalse(makePlugin.isExerciseTypeCorrect(path));
    }

    @Test
    public void testScanExerciseWithPassing() {
        Path path = TestUtils.getPath(getClass(), "passing");
        Optional<ExerciseDesc> optional = makePlugin.scanExercise(path, "");
        assertTrue(optional.isPresent());

        ExerciseDesc desc = optional.get();

        assertEquals("passing", desc.name);

        assertEquals(1, desc.tests.size());
        assertEquals("test.test_one", desc.tests.get(0).name);

        assertEquals(1, desc.tests.get(0).points.size());
        assertEquals("1.1", desc.tests.get(0).points.get(0));
    }

    @Test
    public void testScanExerciseWithNonexistentProject() {
        Path path = TestUtils.getPath(getClass(), "nonexistent");
        Optional<ExerciseDesc> optional = makePlugin.scanExercise(path, "");

        assertFalse(optional.isPresent());
    }
}
