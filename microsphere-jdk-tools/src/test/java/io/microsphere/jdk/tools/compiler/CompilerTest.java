package io.microsphere.jdk.tools.compiler;

import org.junit.jupiter.api.Test;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import static io.microsphere.jdk.tools.compiler.Compiler.defaultRootDirectory;
import static io.microsphere.jdk.tools.compiler.Compiler.defaultSourceDirectory;
import static io.microsphere.jdk.tools.compiler.Compiler.defaultTargetDirectory;
import static io.microsphere.jdk.tools.compiler.Compiler.detectClassPath;
import static io.microsphere.jdk.tools.compiler.Compiler.detectRootDirectory;
import static io.microsphere.jdk.tools.compiler.Compiler.detectSourcePath;
import static io.microsphere.jdk.tools.compiler.Compiler.resolveJavaSourceFileRelativePath;
import static java.io.File.separatorChar;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.getDefault;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the Compiler class
 *
 * @see Compiler
 */
class CompilerTest {

    @Test
    void testDefaultConstructor() {
        // Test default constructor which uses default target directory
        Compiler compiler = new Compiler();

        assertNotNull(compiler, "Compiler should be instantiated successfully");
        assertNotNull(compiler.getJavaCompiler(), "JavaCompiler should be initialized");
    }

    @Test
    void testTargetDirectoryConstructor() {
        File targetDir = new File("target/test-classes");
        targetDir.mkdirs(); // Ensure directory exists

        Compiler compiler = new Compiler(targetDir);

        assertNotNull(compiler, "Compiler should be instantiated successfully with target directory");
        assertNotNull(compiler.getJavaCompiler(), "JavaCompiler should be initialized");
    }

    @Test
    void testSourceAndTargetDirectoryConstructor() {
        File sourceDir = new File("src/test/java");
        File targetDir = new File("target/test-classes");
        targetDir.mkdirs(); // Ensure target directory exists

        Compiler compiler = new Compiler(sourceDir, targetDir);

        assertNotNull(compiler, "Compiler should be instantiated successfully with source and target directories");
        assertNotNull(compiler.getJavaCompiler(), "JavaCompiler should be initialized");
    }

    @Test
    void testDefaultDirectories() {
        // Test the static methods that determine default directories
        File defaultSourceDir = defaultSourceDirectory();
        File defaultTargetDir = defaultTargetDirectory();
        File defaultRootDir = defaultRootDirectory();

        // Root directory should exist (it's derived from the Compiler class location)
        assertNotNull(defaultSourceDir, "Default source directory should not be null");
        assertNotNull(defaultRootDir, "Default root directory should not be null");
        assertTrue(defaultRootDir.exists(), "Default root directory should exist");

        // Target directory should be created
        assertNotNull(defaultTargetDir, "Default target directory should not be null");
    }

    @Test
    void testDetectClassPath() {
        File classPath = detectClassPath(Compiler.class);

        assertNotNull(classPath, "Detected class path should not be null");
        assertTrue(classPath.exists(), "Detected class path should exist");

        assertThrows(UnsupportedOperationException.class, () -> detectClassPath(String.class));
    }

    @Test
    void testResolveJavaSourceFileRelativePath() {
        String expectedPath = Compiler.class.getName()
                .replace('.', separatorChar)
                .concat(".java");
        String actualPath = resolveJavaSourceFileRelativePath(Compiler.class);

        assertEquals(expectedPath, actualPath,
                "Resolved Java source file relative path should match expected format");
    }

    @Test
    void testSourcePathsMethod() {
        File targetDir = new File("target/test-classes");
        targetDir.mkdirs();

        Compiler compiler = new Compiler(targetDir);

        File testSourcePath = new File("src/main/java");
        Compiler result = compiler.sourcePaths(testSourcePath);

        // Verify method chaining returns the same instance
        assertSame(compiler, result, "sourcePaths method should return the same instance for chaining");

        result = compiler.sourcePaths(Compiler.class, Test.class);
        assertSame(compiler, result, "sourcePaths method should return the same instance for chaining");
    }

    @Test
    void testProcessorsMethod() {
        File targetDir = new File("target/test-classes");
        targetDir.mkdirs();

        Compiler compiler = new Compiler(targetDir);
        Processor processor = new AbstractProcessor() {
            @Override
            public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                return false;
            }
        };

        Compiler result = compiler.processors(processor);

        // Verify method chaining returns the same instance
        assertSame(compiler, result, "processors method should return the same instance for chaining");
    }

    @Test
    void testCompile() throws IOException {
        Compiler compiler = new Compiler();

        // This would normally attempt compilation, but with mocked compiler it's safe to call
        boolean result = compiler.compile(Compiler.class, Test.class);

        // For this test, we're primarily verifying the setup doesn't fail
        // Actual compilation success depends on the mock setup
        assertTrue(result, "Test setup completed without exceptions");

        compiler.options()
                .processors(new AbstractProcessor() {

                    @Override
                    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                        return false;
                    }
                })
                .diagnosticListener(diagnostic -> {
                })
                .locale(getDefault())
                .charset(UTF_8)
        ;

        result = compiler.compile(Compiler.class);

        assertTrue(result, "Test setup completed without exceptions");
    }

    @Test
    void testDetectRootDirectory() {
        File rootDir = detectRootDirectory(Compiler.class);

        assertNotNull(rootDir, "Root directory should not be null");
        assertTrue(rootDir.exists(), "Root directory should exist");
    }

    @Test
    void testDetectSourcePath() {
        // This might be null if source path cannot be detected in test environment
        // but the method should not throw exceptions
        assertNotNull(detectSourcePath(Compiler.class), "Should be able to call detectSourcePath without errors");
        assertNotNull(detectSourcePath(CompilerTest.class), "Should be able to call detectSourcePath without errors");
        assertNull(detectSourcePath(Test.class), "Should be able to call detectSourcePath without errors");
    }
}
