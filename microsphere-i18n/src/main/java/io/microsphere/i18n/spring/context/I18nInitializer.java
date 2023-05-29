package io.github.microsphere.i18n.spring.context;

import io.github.microsphere.i18n.spring.beans.factory.config.I18nBeanPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigRegistry;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.springframework.util.ObjectUtils.toObjectArray;

/**
 * Internationalization initializer
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class I18nInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger logger = LoggerFactory.getLogger(I18nInitializer.class);

    private static final Set<String> processedContextIds = new ConcurrentSkipListSet<>();

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        if (shouldInitialization(context)) {
            registerConfigurations(context);
            addBeanPostProcessors(context);
        }
    }

    private boolean shouldInitialization(ConfigurableApplicationContext context) {
        if (!I18nConfiguration.isEnabled(context)) {
            return false;
        }
        if (isProcessed(context)) {
            logger.debug("Application context [id: '{}' , class: {}] has been initialized", context.getId(), context.getClass());
            return false;
        }
        return true;
    }

    private boolean isProcessed(ConfigurableApplicationContext context) {
        return !processedContextIds.add(context.getId());
    }

    private void registerConfigurations(ConfigurableApplicationContext context) {
        register(context, I18nConfiguration.class);
    }

    private void register(ConfigurableApplicationContext context, Class<?>... componentClasses) {
        if (context instanceof AnnotationConfigRegistry) {
            register((AnnotationConfigRegistry) context, componentClasses);
        } else if (context instanceof BeanDefinitionRegistry) {
            register((BeanDefinitionRegistry) context, componentClasses);
        } else {
            logger.warn("Application context [id: '{}' , class: {}] does not support registering component class: {}", context.getId(), context.getClass().getName(), toObjectArray(componentClasses));
        }
    }

    private void register(AnnotationConfigRegistry registry, Class<?>... componentClasses) {
        registry.register(componentClasses);
    }

    private void register(BeanDefinitionRegistry context, Class<?>... componentClasses) {
        new AnnotatedBeanDefinitionReader(context).register(componentClasses);
    }

    private void addBeanPostProcessors(ConfigurableApplicationContext context) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        addI18nBeanPostProcessor(beanFactory, context);
    }

    private void addI18nBeanPostProcessor(ConfigurableListableBeanFactory beanFactory,
                                          ConfigurableApplicationContext context) {
        beanFactory.addBeanPostProcessor(new I18nBeanPostProcessor(context));
    }
}
