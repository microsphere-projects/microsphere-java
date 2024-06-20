/**
 *
 */
package io.microsphere.io;

import java.io.File;

import static io.microsphere.constants.SeparatorConstants.FILE_SEPARATOR;
import static io.microsphere.net.URLUtils.normalizePath;
import static io.microsphere.util.StringUtils.replace;

/**
 * {@link File} Utility
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see FileUtils
 * @since 1.0.0
 */
public abstract class FileUtils {

    /**
     * Resolve Relative Path
     *
     * @param parentDirectory Parent Directory
     * @param targetFile      Target File
     * @return If <code>targetFile</code> is a sub-file of <code>parentDirectory</code> , resolve relative path, or
     * <code>null</code>
     * @since 1.0.0
     */
    public static String resolveRelativePath(File parentDirectory, File targetFile) {
        String parentDirectoryPath = parentDirectory.getAbsolutePath();
        String targetFilePath = targetFile.getAbsolutePath();
        if (!targetFilePath.contains(parentDirectoryPath)) {
            return null;
        }
        return normalizePath(replace(targetFilePath, parentDirectoryPath, FILE_SEPARATOR));
    }
}
