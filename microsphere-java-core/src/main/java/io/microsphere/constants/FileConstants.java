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
     * File extension character
     */
    char FILE_EXTENSION_CHAR = DOT_CHAR;

    /**
     * File extension separator
     */
    String FILE_EXTENSION = DOT;

    /**
     * Zip File extension : ".zip"
     */
    String ZIP_EXTENSION = FILE_EXTENSION + ZIP;

    /**
     * Jar File extension : ".jar"
     */
    String JAR_EXTENSION = FILE_EXTENSION + JAR;

    /**
     * War File extension : ".war"
     */
    String WAR_EXTENSION = FILE_EXTENSION + WAR;

    /**
     * Ear File extension : ".ear"
     */
    String EAR_EXTENSION = FILE_EXTENSION + EAR;

    /**
     * Class File extension : ".class"
     */
    String CLASS_EXTENSION = FILE_EXTENSION + CLASS;

    /**
     * Java File extension : ".java"
     */
    String JAVA_EXTENSION = FILE_EXTENSION + "java";
}
