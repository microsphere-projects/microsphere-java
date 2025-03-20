package io.microsphere.io.scanner;


import io.microsphere.io.filter.IOFileFilter;
import io.microsphere.io.filter.TrueFileFilter;

import io.microsphere.annotation.Nonnull;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

/**
 * Simple File Scanner (Single-Thread)
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
