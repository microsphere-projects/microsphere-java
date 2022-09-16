package io.github.microsphere.commons.i18n.spring.beans.factory.config;

import io.github.microsphere.commons.i18n.spring.context.MessageSourceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * 国际化  {@link BeanPostProcessor}，处理：
 * <ul>
 *     <li>{@link LocalValidatorFactoryBean#setValidationMessageSource(MessageSource)} 关联 {@link MessageSourceAdapter}</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class I18nBeanPostProcessor implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(I18nBeanPostProcessor.class);

    private static final Class<?> LOCAL_VALIDATOR_FACTORY_BEAN_CLASS = LocalValidatorFactoryBean.class;

    private final ConfigurableApplicationContext context;

    public I18nBeanPostProcessor(ConfigurableApplicationContext context) {
        this.context = context;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        Class<?> beanType = AopUtils.getTargetClass(bean);
        if (LOCAL_VALIDATOR_FACTORY_BEAN_CLASS.equals(beanType)) {
            MessageSourceAdapter messageSourceAdapter = context.getBean(MessageSourceAdapter.class);
            LocalValidatorFactoryBean localValidatorFactoryBean = (LocalValidatorFactoryBean) bean;
            localValidatorFactoryBean.setValidationMessageSource(messageSourceAdapter);
            logger.debug("LocalValidatorFactoryBean[name : '{}'] 已关联 MessageSource : {}", beanName, messageSourceAdapter);
        }

        return bean;
    }
}
