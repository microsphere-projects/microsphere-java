/**
 *
 */
package io.microsphere.io;

import io.microsphere.annotation.Nullable;
import io.microsphere.util.ArrayUtils;
import io.microsphere.util.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static io.microsphere.constants.FileConstants.FILE_EXTENSION_CHAR;
import static io.microsphere.constants.PathConstants.SLASH_CHAR;
import static io.microsphere.constants.SymbolConstants.DOT_CHAR;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.util.ArrayUtils.isEmpty;
import static io.microsphere.util.CharSequenceUtils.isEmpty;
import static io.microsphere.util.StringUtils.isBlank;
import static java.io.File.separatorChar;
import static java.nio.file.Files.isSymbolicLink;

/**
 * {@link File} Utility
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see FileUtils
 * @since 1.0.0
 */
public abstract class FileUtils implements Utils {

    /**
     * An empty immutable {@code File} array.
     */
    public static final File[] EMPTY_FILE_ARRAY = ArrayUtils.EMPTY_FILE_ARRAY;

    /**
     * Resolves the relative path from a parent directory to a target file.
     *
     * <p>If the {@code targetFile} is not under the specified {@code parentDirectory}, this method returns
     * {@code null}. If the paths are equal, an empty string is returned.</p>
     *
     * <h3>Example Usage</h3>
     * <ul>
     *   <li>{@code resolveRelativePath(new File("/home/user"), new File("/home/user/docs/file.txt"))} returns
     *       {@code "docs/file.txt"}</li>
     *   <li>{@code resolveRelativePath(new File("/home/user"), new File("/home/user/file.txt"))} returns
     *       {@code "file.txt"}</li>
     *   <li>{@code resolveRelativePath(new File("/home/user"), new File("/tmp/file.txt"))} returns
     *       {@code null}</li>
     *   <li>{@code resolveRelativePath(new File("/home/user"), new File("/home/user"))} returns
     *       an empty string</li>
     * </ul>
     *
     * @param parentDirectory the base directory to calculate the relative path from
     * @param targetFile      the target file or directory whose relative path is to be determined
     * @return the relative path from the parent directory to the target file, using forward slashes ({@code /}),
     * or {@code null} if the target file is not under the parent directory
     */
    @Nullable
    public static String resolveRelativePath(File parentDirectory, File targetFile) {
        if (!parentDirectory.isDirectory()) {
            return null;
        }
        String parentDirectoryPath = parentDirectory.getAbsolutePath();
        String targetFilePath = targetFile.getAbsolutePath();
        int index = targetFilePath.indexOf(parentDirectoryPath);
        if (index == 0) {
            String relativePath = targetFilePath.substring(parentDirectoryPath.length());
            if (isEmpty(relativePath)) {
                return relativePath;
            }
            return relativePath.substring(1).replace(separatorChar, SLASH_CHAR);
        }
        return null;
    }

    /**
     * Gets the extension of a file name, if any.
     *
     * <h3>Example Usage</h3>
     * <ul>
     *   <li>{@code getFileExtension("file.txt")} returns {@code "txt"}</li>
     *   <li>{@code getFileExtension("file.tar.gz")} returns {@code "gz"}</li>
     *   <li>{@code getFileExtension(".hiddenfile")} returns {@code null} (no extension)</li>
     *   <li>{@code getFileExtension("file")} returns {@code null} (no extension)</li>
     *   <li>{@code getFileExtension("")} returns {@code null} (blank string)</li>
     *   <li>{@code getFileExtension(null)} returns {@code null}</li>
     * </ul>
     *
     * @param fileName the name of the file, may be {@code null} or blank
     * @return the file's extension without the dot (.), or {@code null} if there's no extension or input is blank
     */
    @Nullable
    public static String getFileExtension(String fileName) {
        if (isBlank(fileName)) {
            return null;
        }
        int index = fileName.lastIndexOf(FILE_EXTENSION_CHAR);
        return index > -1 ? fileName.substring(index + 1) : null;
    }

    /**
     * Deletes a directory and returns the number of deleted files and directories.
     *
     * <p>If the directory does not exist, it is considered already deleted, and this method returns 0.</p>
     *
     * <h3>Example Usage</h3>
     * <ul>
     *   <li>{@code deleteDirectory(new File("/tmp/testDir"))} deletes the directory and all its contents,
     *       returning the total count of deleted files and directories.</li>
     *   <li>{@code deleteDirectory(new File("/nonexistent/dir"))} returns {@code 0} since the directory does not exist.</li>
     * </ul>
     *
     * @param directory the directory to delete, must not be {@code null}
     * @return the number of deleted files and directories
     * @throws NullPointerException if the directory is {@code null}
     * @throws IOException          in case deletion is unsuccessful
     */
    public static int deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return 0;
        }

        int deletedFilesCount = 0;

        if (!isSymlink(directory)) {
            deletedFilesCount += cleanDirectory(directory);
        }

        if (directory.delete()) {
            deletedFilesCount++;
        } else {
            String message = "Unable to delete directory " + directory + DOT_CHAR;
            throw new IOException(message);
        }

        return deletedFilesCount;
    }

    /**
     * Cleans a directory by deleting all files and sub-directories without deleting the directory itself.
     *
     * <p>
     * This method recursively deletes all files and directories within the provided directory.
     * If any file or sub-directory cannot be deleted, an IOException is thrown after attempting to delete as many as possible.
     * </p>1
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * File dir = new File("/path/to/directory");
     * int deletedCount = cleanDirectory(dir);
     * System.out.println("Deleted " + deletedCount + " files/directories.");
     * }</pre>
     *
     * @param directory the directory to clean, must not be {@code null}
     * @return the number of deleted files and directories
     * @throws NullPointerException if the directory is {@code null}
     * @throws IOException          if deletion fails for any file or sub-directory
     */
    public static int cleanDirectory(File directory) throws IOException {
        int deletedFilesCount = 0;
        IOException exception = null;
        for (File file : listFiles(directory)) {
            try {
                deletedFilesCount += forceDelete(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
        return deletedFilesCount;
    }

    /**
     * Deletes a file or directory and all its contents recursively.
     *
     * <p>If the provided {@code file} is a directory, this method deletes all sub-directories and files,
     * then deletes the directory itself. If it's a regular file, it deletes that single file.</p>
     *
     * <h3>Example Usage</h3>
     * <ul>
     *   <li>{@code forceDelete(new File("/tmp/file.txt"))} deletes the file and returns {@code 1}</li>
     *   <li>{@code forceDelete(new File("/tmp/testDir"))} deletes the directory and all its contents,
     *       returning the total count of deleted files and directories.</li>
     *   <li>{@code forceDelete(new File("/nonexistent/file"))} throws a {@link FileNotFoundException}</li>
     * </ul>
     *
     * @param file the file or directory to delete, must not be {@code null}
     * @return the number of deleted files and directories
     * @throws NullPointerException  if the file is {@code null}
     * @throws FileNotFoundException if the file does not exist
     * @throws IOException           if deletion fails for any reason
     */
    public static int forceDelete(File file) throws IOException {
        final int deletedFilesCount;
        if (file.isDirectory()) {
            deletedFilesCount = deleteDirectory(file);
        } else {
            boolean filePresent = file.exists();
            if (file.delete()) {
                deletedFilesCount = 1;
            } else {
                if (!filePresent) {
                    throw new FileNotFoundException("File does not exist: " + file);
                }
                String message = "Unable to delete file: " + file;
                throw new IOException(message);
            }
        }
        return deletedFilesCount;
    }

    /**
     * Schedules a file or directory for deletion on JVM exit.
     *
     * <p>If the provided {@code file} is a directory, this method schedules all sub-directories and files,
     * then schedules the directory itself. If it's a regular file, it schedules that single file.</p>
     *
     * <h3>Example Usage</h3>
     * <ul>
     *   <li>{@code forceDeleteOnExit(new File("/tmp/file.txt"))} schedules the file for deletion on exit.</li>
     *   <li>{@code forceDeleteOnExit(new File("/tmp/testDir"))} schedules the directory and all its contents for deletion on exit.</li>
     * </ul>
     *
     * @param file the file or directory to schedule for deletion, must not be {@code null}
     * @throws NullPointerException if the file is {@code null}
     */
    public static void forceDeleteOnExit(File file) {
        if (file.isDirectory()) {
            deleteDirectoryOnExit(file);
        } else {
            file.deleteOnExit();
        }
    }

    /**
     * Schedules a directory for deletion on JVM exit, including all its contents.
     *
     * <p>If the directory does not exist, this method does nothing. If it does exist,
     * it schedules the directory for deletion and recursively schedules all files
     * and subdirectories for deletion.</p>
     *
     * <h3>Example Usage</h3>
     * <ul>
     *   <li>{@code deleteDirectoryOnExit(new File("/tmp/testDir"))} ensures that the directory and all its contents are deleted when the JVM exits.</li>
     *   <li>{@code deleteDirectoryOnExit(new File("/nonexistent/dir"))} does nothing since the directory does not exist.</li>
     * </ul>
     *
     * @param directory the directory to schedule for deletion on exit, must not be {@code null}
     * @throws NullPointerException if the directory is {@code null}
     */
    public static void deleteDirectoryOnExit(File directory) {
        if (!directory.exists()) {
            return;
        }

        directory.deleteOnExit();
        if (!isSymlink(directory)) {
            cleanDirectoryOnExit(directory);
        }
    }


    /**
     * Schedules all files and subdirectories within the given directory for deletion on JVM exit.
     *
     * <p>This method does not delete the directory itself, only its contents. If the directory is
     * a symbolic link, its contents will not be processed.</p>
     *
     * <h3>Example Usage</h3>
     * <ul>
     *   <li>{@code cleanDirectoryOnExit(new File("/tmp/testDir"))} ensures that all contents of the directory
     *       are deleted when the JVM exits, but the directory itself remains.</li>
     *   <li>{@code cleanDirectoryOnExit(new File("/nonexistent/dir"))} does nothing since the directory
     *       does not exist.</li>
     * </ul>
     *
     * @param directory the directory whose contents should be deleted on exit, must not be {@code null}
     * @throws NullPointerException if the directory is {@code null}
     */
    private static void cleanDirectoryOnExit(File directory) {
        for (File file : listFiles(directory)) {
            forceDeleteOnExit(file);
        }
    }

    /**
     * Lists the files in the specified directory.
     *
     * <p>If the provided {@code directory} is not valid (i.e., it does not exist, or it is not a directory),
     * this method returns an empty file array.</p>
     *
     * <h3>Example Usage</h3>
     * <ul>
     *   <li>{@code listFiles(new File("/tmp"))} returns an array of files in the "/tmp" directory.</li>
     *   <li>{@code listFiles(new File("/nonexistent/dir"))} returns an empty file array since the directory does not exist.</li>
     *   <li>{@code listFiles(null)} returns an empty file array as the input is null.</li>
     * </ul>
     *
     * @param directory the directory to list files from
     * @return an array of {@link File} objects representing the files in the specified directory,
     * or {@link #EMPTY_FILE_ARRAY} if the directory is not valid
     */
    public static File[] listFiles(File directory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return EMPTY_FILE_ARRAY;
        }
        File[] files = directory.listFiles();
        if (isEmpty(files)) {  // empty
            files = EMPTY_FILE_ARRAY;
        }
        return files;
    }

    /**
     * Determines if the provided {@link File} is a symbolic link.
     *
     * <h3>Example Usage</h3>
     * <ul>
     *   <li>{@code isSymlink(new File("/path/to/symlink"))} returns {@code true} if it's a symbolic link.</li>
     *   <li>{@code isSymlink(new File("/path/to/regularfile"))} returns {@code false} as it's not a symbolic link.</li>
     *   <li>{@code isSymlink(null)} throws a {@link NullPointerException}.</li>
     * </ul>
     *
     * @param file the file to check, must not be {@code null}
     * @return {@code true} if the file is a symbolic link, otherwise {@code false}
     * @throws NullPointerException if the file is {@code null}
     */
    public static boolean isSymlink(File file) {
        return isSymbolicLink(file.toPath());
    }

    /**
     * Returns the canonical form of the specified {@link File}.
     *
     * <p>This method wraps the call to {@link File#getCanonicalFile()} in a try-catch block to handle any
     * checked exceptions via the {@link io.microsphere.lang.function.ThrowableSupplier} utility.</p>
     *
     * <h3>Example Usage</h3>
     * <ul>
     *   <li>{@code getCanonicalFile(new File("relative/path"))} returns the canonical file object.</li>
     *   <li>If the file does not exist or I/O error occurs, it will propagate as an unchecked exception.</li>
     * </ul>
     *
     * @param file the file for which the canonical representation is required, must not be {@code null}
     * @return the canonical representation of the given file
     * @throws NullPointerException if the provided file is {@code null}
     * @throws RuntimeException     if an I/O error occurs while retrieving the canonical file
     */
    public static final File getCanonicalFile(File file) {
        return execute(file::getCanonicalFile);
    }

    private FileUtils() {
    }
}
