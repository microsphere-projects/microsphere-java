package io.github.microsphere.commons.i18n.util;

import io.github.microsphere.commons.i18n.ServiceMessageSource;
import io.github.microsphere.commons.i18n.constants.I18nConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

import static io.github.microsphere.commons.i18n.util.I18nUtils.serviceMessageSource;
import static org.apache.commons.lang3.StringUtils.substringBetween;
import static org.springframework.util.StringUtils.hasText;

/**
 * Message Utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class MessageUtils {

    private static final Logger logger = LoggerFactory.getLogger(MessageUtils.class);

    private MessageUtils() {
    }

    /**
     * Get I18n Message
     *
     * @param messagePattern Message or Message Pattern
     * @param args           the arguments of Message Pattern
     * @return Internationalized Message returns the original message if it exists
     */
    public static String getLocalizedMessage(String messagePattern, Object... args) {
        ServiceMessageSource serviceMessageSource = serviceMessageSource();
        Locale locale = serviceMessageSource.getLocale();
        return getLocalizedMessage(messagePattern, locale, args);
    }

    /**
     * Get I18n Message
     * <pre>
     * //Test Simplified Chinese
     * // message parameter is 'a', does not contain pattern'{''}', returns original content
     * assertEquals ('a', MessageUtils.getLocalizedMessage ('a'));
     *
     * //'{a}'is the Message Code template, where 'a' is the Message Code
     * assertEquals ("test-a", MessageUtils.getLocalizedMessage ("{a}"));
     *
     * // Similarly, overloaded methods with Message Patterns parameter
     * assertEquals ('hello', MessageUtils.getLocalizedMessage ('hello','World'));
     * assertEquals ("Hello, World", MessageUtils.getLocalizedMessage ("{hello}", "World"));
     *
     * //Return the original message content when message code does not exist
     * assertEquals ('{code-not-found}', MessageUtils.getLocalizedMessage ('{code-not-found}'));
     *
     * //Test English
     * assertEquals ("hello", MessageUtils.getLocalizedMessage ("hello", Locale.ENGLISH, "World"));
     * assertEquals ('Hello, World', MessageUtils.getLocalizedMessage ('{hello}', Locale.ENGLISH,'World'));
     * </pre>
     *
     * @param messagePattern Message or Message Pattern
     * @param locale         {@link Locale}
     * @param args           the arguments of Message Pattern
     * @return Internationalized Message returns the original message if it exists
     */
    public static String getLocalizedMessage(String messagePattern, Locale locale, Object... args) {
        if (messagePattern == null) {
            return null;
        }

        String messageCode = resolveMessageCode(messagePattern);

        if (messageCode == null) {
            logger.debug("Message code not found in messagePattern'{}", messagePattern);
            return messagePattern;
        }

        ServiceMessageSource serviceMessageSource = serviceMessageSource();
        String localizedMessage = serviceMessageSource.getMessage(messageCode, locale, args);
        if (hasText(localizedMessage)) {
            logger.debug("Message Pattern ['{}'] corresponds to Locale ['{}'] with MessageSage:'{}'", messagePattern, locale, localizedMessage);
        } else {
            int afterDotIndex = messageCode.indexOf(".") + 1;
            if (afterDotIndex > 0 && afterDotIndex < messageCode.length()) {
                localizedMessage = messageCode.substring(afterDotIndex);
            } else {
                localizedMessage = messagePattern;
            }
            logger.debug("No Message['{}'] found for Message Pattern ['{}'], returned: {}", messagePattern, locale, localizedMessage);
        }

        return localizedMessage;
    }

    public static String resolveMessageCode(String messagePattern) {
        String messageCode = substringBetween(messagePattern, I18nConstants.MESSAGE_PATTERN_PREFIX, I18nConstants.MESSAGE_PATTERN_SUFFIX);
        return messageCode;
    }
}
