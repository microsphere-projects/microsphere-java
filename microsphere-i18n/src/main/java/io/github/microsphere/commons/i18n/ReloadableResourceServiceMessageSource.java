package io.github.microsphere.commons.i18n;

import java.util.Set;

/**
 * Reloadable {@link ResourceServiceMessageSource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface ReloadableResourceServiceMessageSource extends ResourceServiceMessageSource {

    /**
     * Reload if {@link #canReload(Iterable)} returns <code>true</code>,
     * The calling {@link #init()} as default
     */
    default void reload() {
        init();
    }

    /**
     * Whether the specified resource list can be overloaded
     *
     * @param changedResources Changes in the resource
     * @return Supported by default, returning <code>true<code>
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
