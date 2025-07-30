package io.microsphere.io.scanner;


import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;
import io.microsphere.io.filter.IOFileFilter;
import io.microsphere.io.filter.TrueFileFilter;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

/**
 * Simple File Scanner (Single-Thread)
 *
 * <p>
 * This class provides a simple mechanism to scan files and directories using customizable filters.
 * It supports both recursive and non-recursive scanning operations.
 * </p>
 *
 * <h3>Features</h3>
 * <ul>
 *     <li>Scans files and directories based on provided filters.</li>
 *     <li>Supports recursive scanning of subdirectories.</li>
 *     <li>Returns an unmodifiable set of matched files, preserving order based on the underlying file system's implementation.</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Scan all files in the given directory (non-recursive)
 * Set<File> files = SimpleFileScanner.INSTANCE.scan(new File("/your/path"), false);
 *
 * // Scan all files recursively under the given directory
 * Set<File> recursiveFiles = SimpleFileScanner.INSTANCE.scan(new File("/your/path"), true);
 *
 * // Scan with custom filter (e.g., only .txt files)
 * IOFileFilter filter = new IOFileFilter() {
 *     public boolean accept(File file) {
 *         return file.getName().endsWith(".txt");
 *     }
 * };
 * Set<File> txtFiles = SimpleFileScanner.INSTANCE.scan(new File("/your/path"), true, filter);
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SimpleFileScanner#INSTANCE
 * @see IOFileFilter
 * @since 1.0.0
 */
public class SimpleFileScanner {

    /**
     * Singleton
     */
    public final static SimpleFileScanner INSTANCE = new SimpleFileScanner();

    public SimpleFileScanner() {
    }

    /**
     * Scan all {@link File} {@link Set} under root directory
     *
     * @param rootDirectory Root directory
     * @param recursive     is recursive on sub directories
     * @return Read-only {@link Set} , and the order be dependent on {@link File#listFiles()} implementation
     * @see IOFileFilter
     */
    @Nonnull
    @Immutable
    public Set<File> scan(File rootDirectory, boolean recursive) {
        return scan(rootDirectory, recursive, TrueFileFilter.INSTANCE);
    }

    /**
     * Scan all {@link File} {@link Set} that are accepted by {@link IOFileFilter} under root directory
     *
     * @param rootDirectory Root directory
     * @param recursive     is recursive on sub directories
     * @param ioFileFilter  {@link IOFileFilter}
     * @return Read-only {@link Set} , and the order be dependent on {@link File#listFiles()} implementation
     * @see IOFileFilter
     */
    @Nonnull
    @Immutable
    public Set<File> scan(File rootDirectory, boolean recursive, IOFileFilter ioFileFilter) {

        final Set<File> filesSet = new LinkedHashSet<>();

        if (ioFileFilter.accept(rootDirectory)) {
            filesSet.add(rootDirectory);
        }

        File[] subFiles = rootDirectory.listFiles();

        if (subFiles != null) {
            for (File subFile : subFiles) {
                if (ioFileFilter.accept(subFile)) {
                    filesSet.add(subFile);
                }
                if (recursive && subFile.isDirectory()) {
                    filesSet.addAll(this.scan(subFile, recursive, ioFileFilter));
                }
            }
        }
        return unmodifiableSet(filesSet);
    }

}
