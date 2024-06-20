/**
 *
 */
package io.microsphere.constants;

import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.constants.SymbolConstants.DOT_CHAR;

/**
 * File Constants
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @since 1.0.0
 */
public interface FileConstants {

    /**
     * Zip file
     */
    String ZIP = "zip";

    /**
     * Jar file
     */
    String JAR = "jar";

    /**
     * War file
     */
    String WAR = "war";

    /**
     * Ear file
     */
    String EAR = "ear";

    /**
     * Class file
     */
    String CLASS = "class";

    /**
     * File extension separator character
     */
    char EXTENSION_SEPARATOR_CHAR = DOT_CHAR;

    /**
     * File extension separator
     */
    String EXTENSION_SEPARATOR = DOT;

    /**
     * Zip File extension : ".zip"
     */
    String ZIP_EXTENSION = EXTENSION_SEPARATOR + ZIP;

    /**
     * Jar File extension : ".jar"
     */
    String JAR_EXTENSION = EXTENSION_SEPARATOR + JAR;

    /**
     * War File extension : ".jar"
     */
    String WAR_EXTENSION = EXTENSION_SEPARATOR + WAR;

    /**
     * Ear File extension : ".jar"
     */
    String EAR_EXTENSION = EXTENSION_SEPARATOR + EAR;

    /**
     * Class File extension : ".class"
     */
    String CLASS_EXTENSION = EXTENSION_SEPARATOR + CLASS;
}
