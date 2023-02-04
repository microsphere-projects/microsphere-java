package io.github.microsphere.commons.i18n.spring.context;

import io.github.microsphere.commons.i18n.ResourceServiceMessageSource;
import org.springframework.context.ApplicationEvent;

/**
 * {@link ResourceServiceMessageSource} Changed {@link ApplicationEvent Event}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ApplicationEvent
 * @since 1.0.0
 */
public class ResourceServiceMessageSourceChangedEvent extends ApplicationEvent {

    public ResourceServiceMessageSourceChangedEvent(Iterable<String> changedResources) {
        super(changedResources);
    }

    public Iterable<String> getChangedResources() {
        return (Iterable<String>) getSource();
    }
}
