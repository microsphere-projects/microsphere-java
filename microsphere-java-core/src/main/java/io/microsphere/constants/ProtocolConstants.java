/**
 *
 */
package io.microsphere.constants;

import static io.microsphere.constants.FileConstants.EAR;
import static io.microsphere.constants.FileConstants.JAR;
import static io.microsphere.constants.FileConstants.WAR;
import static io.microsphere.constants.FileConstants.ZIP;

/**
 * Protocol Constants Definition
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ProtocolConstants
 * @since 1.0.0
 */
public interface ProtocolConstants {

    /**
     * File protocol
     */
    String FILE_PROTOCOL = "file";

    /**
     * HTTP protocol
     */
    String HTTP_PROTOCOL = "http";

    /**
     * HTTPS protocol
     */
    String HTTPS_PROTOCOL = "https";

    /**
     * FTP protocol
     */
    String FTP_PROTOCOL = "ftp";

    /**
     * Zip protocol
     */
    String ZIP_PROTOCOL = ZIP;

    /**
     * Jar protocol
     */
    String JAR_PROTOCOL = JAR;

    /**
     * War protocol
     */
    String WAR_PROTOCOL = WAR;

    /**
     * Ear protocol
     */
    String EAR_PROTOCOL = EAR;

    /**
     * Class-Path Protocol
     */
    String CLASSPATH_PROTOCOL = "classpath";

    /**
     * Console Protocol
     */
    String CONSOLE_PROTOCOL = "console";
}
