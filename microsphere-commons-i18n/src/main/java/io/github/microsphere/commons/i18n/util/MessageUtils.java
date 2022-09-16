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
 * 消息工具类
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class MessageUtils {

    private static final Logger logger = LoggerFactory.getLogger(MessageUtils.class);

    private MessageUtils() {
    }

    /**
     * 获取国际化 Message
     *
     * @param messagePattern Message 内容或者 Pattern
     * @param args           Message 模板参数列表
     * @return 国际化 Message 如果存在的话，否则返回原 Message
     */
    public static String getLocalizedMessage(String messagePattern, Object... args) {
        ServiceMessageSource serviceMessageSource = serviceMessageSource();
        Locale locale = serviceMessageSource.getLocale();
        return getLocalizedMessage(messagePattern, locale, args);
    }

    /**
     * 获取国际化 Message
     * <pre>
     * // 测试简体中文
     * // message 参数为 "a" 的情况，不包含模式 "{" "}"，返回原始内容
     * assertEquals("a", MessageUtils.getLocalizedMessage("a"));
     * // "{a}" 为 Message Code 模板，其中 "a" 为 Message Code
     * assertEquals("测试-a", MessageUtils.getLocalizedMessage("{a}"));
     *
     * // 同理如下，带 Message Pattern 参数的重载方法
     * assertEquals("hello", MessageUtils.getLocalizedMessage("hello", "World"));
     * assertEquals("您好,World", MessageUtils.getLocalizedMessage("{hello}", "World"));
     *
     * // 当 message code 不存在时，返回 message 原内容
     * assertEquals("{code-not-found}", MessageUtils.getLocalizedMessage("{code-not-found}"));
     *
     * // 测试英文
     * assertEquals("hello", MessageUtils.getLocalizedMessage("hello", Locale.ENGLISH, "World"));
     * assertEquals("Hello,World", MessageUtils.getLocalizedMessage("{hello}", Locale.ENGLISH, "World"));
     * </pre>
     *
     * @param messagePattern Message 内容或者 Pattern
     * @param locale         {@link Locale} 对象
     * @param args           Message 模板参数列表
     * @return 国际化 Message 如果存在的话，否则返回原 Message
     */
    public static String getLocalizedMessage(String messagePattern, Locale locale, Object... args) {
        if (messagePattern == null) {
            return null;
        }

        String messageCode = resolveMessageCode(messagePattern);

        if (messageCode == null) {
            logger.debug("messagePattern '{}' 未找到 message code", messagePattern);
            return messagePattern;
        }

        ServiceMessageSource serviceMessageSource = serviceMessageSource();
        String localizedMessage = serviceMessageSource.getMessage(messageCode, locale, args);
        if (hasText(localizedMessage)) {
            logger.debug("Message Pattern['{}'] 对应的 Locale['{}'] 的 Message 为 : '{}'", messagePattern, locale, localizedMessage);
        } else {
            int afterDotIndex = messageCode.indexOf(".") + 1;
            if (afterDotIndex > 0 && afterDotIndex < messageCode.length()) {
                localizedMessage = messageCode.substring(afterDotIndex);
            } else {
                localizedMessage = messagePattern;
            }
            logger.debug("未找到 Message Pattern['{}'] 的 Locale['{}'] Message, 返回 : {}", messagePattern, locale, localizedMessage);
        }

        return localizedMessage;
    }

    public static String resolveMessageCode(String messagePattern) {
        String messageCode = substringBetween(messagePattern, I18nConstants.MESSAGE_PATTERN_PREFIX, I18nConstants.MESSAGE_PATTERN_SUFFIX);
        return messageCode;
    }
}
