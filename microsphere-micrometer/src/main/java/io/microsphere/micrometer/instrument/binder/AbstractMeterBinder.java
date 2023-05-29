package io.github.microsphere.micrometer.instrument.binder;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.emptyList;

/**
 * Abstract {@link MeterBinder}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 */
public abstract class AbstractMeterBinder implements MeterBinder {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final Iterable<Tag> tags;

    public AbstractMeterBinder() {
        this(emptyList());
    }

    public AbstractMeterBinder(Iterable<Tag> tags) {
        this.tags = tags;
    }

    @Override
    public final void bindTo(MeterRegistry registry) {
        if (!supports(registry)) {
            logger.info("Current Metrics is not Supported");
            return;
        }
        try {
            doBindTo(registry);
        } catch (Throwable e) {
            logger.error("Bind To MeterRegistry[{}] failed", registry, e);
        }
    }

    protected Iterable<Tag> combine(String... tags) {
        return Tags.concat(this.tags, tags);
    }

    protected abstract boolean supports(MeterRegistry registry);

    protected abstract void doBindTo(MeterRegistry registry) throws Throwable;

}
