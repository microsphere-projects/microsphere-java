/**
 *
 */
package io.microsphere.constants;


import java.io.File;

import static io.microsphere.constants.PathConstants.SLASH;
import static io.microsphere.constants.SymbolConstants.EXCLAMATION;

/**
 * Separator Constants
 * <p/>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface SeparatorConstants {

    /**
     * Archive Entry Separator : "!/"
     */
    String ARCHIVE_ENTRY_SEPARATOR = EXCLAMATION + SLASH;

    /**
     * File Separator : {@link File#separator}
     */
    String FILE_SEPARATOR = File.separator;

    /**
     * Path Separator : {@link File#pathSeparator}
     */
    String PATH_SEPARATOR = File.pathSeparator;

    /**
     * Line Separator : {@link System#lineSeparator()}
     */
    String LINE_SEPARATOR = System.lineSeparator();

}
