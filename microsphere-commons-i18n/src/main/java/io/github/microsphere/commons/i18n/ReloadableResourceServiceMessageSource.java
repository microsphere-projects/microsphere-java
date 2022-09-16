package io.github.microsphere.commons.i18n;

import java.util.Set;

/**
 * 可重加载 {@link ResourceServiceMessageSource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface ReloadableResourceServiceMessageSource extends ResourceServiceMessageSource {

    /**
     * 重载资源当 {@link #canReload(Iterable)} 为 <code>true</code>，默认调用 {@link #init()}
     */
    default void reload() {
        init();
    }

    /**
     * 指定的资源列表能否重载
     *
     * @param changedResources 变化资源
     * @return 默认支持，返回 <code>true</code>
     */
    default boolean canReload(Iterable<String> changedResources) {
        Set<String> resources = getResources();
        boolean reloadable = false;
        for (String changedResource : changedResources) {
            if (reloadable = resources.contains(changedResource)) {
                break;
            }
        }
        return reloadable;
    }
}
