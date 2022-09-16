package io.github.microsphere.commons.i18n;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

/**
 * 资源 {@link ResourceServiceMessageSource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface ResourceServiceMessageSource extends ServiceMessageSource {

    /**
     * 获取所有的 {@link Locale} 的资源列表
     *
     * @return
     */
    Set<String> getResources();

    /**
     * 获取资源内容字符编码
     *
     * @return 默认为 UTF-8
     */
    default Charset getEncoding() {
        return StandardCharsets.UTF_8;
    }
}
