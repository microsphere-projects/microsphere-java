/**
 *
 */
package io.microsphere.io;

import io.microsphere.util.ArrayUtils;
import io.microsphere.util.BaseUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static io.microsphere.constants.FileConstants.FILE_EXTENSION_CHAR;
import static io.microsphere.constants.PathConstants.SLASH_CHAR;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.util.ArrayUtils.isEmpty;
import static io.microsphere.util.CharSequenceUtils.isEmpty;
import static io.microsphere.util.StringUtils.isBlank;
import static io.microsphere.util.SystemUtils.IS_OS_WINDOWS;
import static java.io.File.separatorChar;

/**
 * {@link File} Utility
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see FileUtils
 * @since 1.0.0
 */
public abstract class FileUtils extends BaseUtils {

    /**
     * An empty immutable {@code File} array.
     */
    public static final File[] EMPTY_FILE_ARRAY = ArrayUtils.EMPTY_FILE_ARRAY;

    /**
     * Resolve Relative Path
     *
     * @param parentDirectory Parent Directory
     * @param targetFile      Target File
     * @return If <code>targetFile</code> is a sub-file of <code>parentDirectory</code> , resolve relative path, or
     * <code>null</code>
     */
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
     * Get File Extension
     *
     * @param fileName the name of {@link File}
     * @return the file extension if found
     */
    public static String getFileExtension(String fileName) {
        if (isBlank(fileName)) {
            return null;
        }
        int index = fileName.lastIndexOf(FILE_EXTENSION_CHAR);
        return index > -1 ? fileName.substring(index + 1) : null;
    }

    /**
     * Deletes a directory recursively.
     *
     * @param directory directory to delete
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
            String message = "Unable to delete directory " + directory + ".";
            throw new IOException(message);
        }

        return deletedFilesCount;
    }


    /**
     * Cleans a directory without deleting it.
     *
     * @param directory directory to clean
     * @throws NullPointerException if the directory is {@code null}
     * @throws IOException          in case cleaning is unsuccessful
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
     * Deletes a file. If file is a directory, delete it and all sub-directories.
     * <p>
     * The difference between File.delete() and this method are:
     * <ul>
     * <li>A directory to be deleted does not have to be empty.</li>
     * <li>You get exceptions when a file or directory cannot be deleted.
     *      (java.io.File methods returns a boolean)</li>
     * </ul>
     *
     * @param file file or directory to delete, must not be {@code null}
     * @return the number of deleted files and directories
     * @throws NullPointerException  if the file is {@code null}
     * @throws FileNotFoundException if the file was not found
     * @throws IOException           in case deletion is unsuccessful
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
     * Schedules a file to be deleted when JVM exits.
     * If file is directory delete it and all sub-directories.
     *
     * @param file file or directory to delete, must not be {@code null}
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
     * Schedules a directory recursively for deletion on JVM exit.
     *
     * @param directory directory to delete, must not be {@code null}
     * @throws NullPointerException if the directory is {@code null}
     * @throws IOException          in case deletion is unsuccessful
     */
    private static void deleteDirectoryOnExit(File directory) {
        if (!directory.exists()) {
            return;
        }

        directory.deleteOnExit();
        if (!isSymlink(directory)) {
            cleanDirectoryOnExit(directory);
        }
    }

    /**
     * Cleans a directory without deleting it.
     *
     * @param directory directory to clean, must not be {@code null}
     * @throws NullPointerException if the directory is {@code null}
     */
    private static void cleanDirectoryOnExit(File directory) {
        for (File file : listFiles(directory)) {
            forceDeleteOnExit(file);
        }
    }

    /**
     * List Files from the specified directory
     *
     * @param directory the specified directory
     * @return {@link #EMPTY_FILE_ARRAY the empty file array} if the specified directory is not exist or not a directory
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

    public static boolean isSymlink(File file) {
        if (file == null) {
            throw new NullPointerException("File must not be null");
        }
        if (IS_OS_WINDOWS) {
            return false;
        }
        File fileInCanonicalDir = null;
        if (file.getParent() == null) {
            fileInCanonicalDir = file;
        } else {
            File canonicalDir = getCanonicalFile(file.getParentFile());
            fileInCanonicalDir = new File(canonicalDir, file.getName());
        }

        return !getCanonicalFile(fileInCanonicalDir).equals(fileInCanonicalDir.getAbsoluteFile());
    }

    /**
     * Invoke {@link File#getCanonicalFile()} without throwing {@link IOException}.
     *
     * @param file the {@link File} instance
     * @return {@link File#getCanonicalFile()}
     * @throws NullPointerException if <code>file</code> is <code>null</code>
     * @throws RuntimeException     If an I/O error occurs, which is possible because the construction of the canonical
     *                              pathname may require filesystem queries
     */
    public static final File getCanonicalFile(File file) {
        return execute(file::getCanonicalFile);
    }
}
