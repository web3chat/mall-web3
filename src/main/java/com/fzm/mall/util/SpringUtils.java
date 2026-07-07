package com.fzm.mall.util;

import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Spring(Spring boot)工具类
 */
@Component
public class SpringUtils implements BeanFactoryPostProcessor, ApplicationContextAware {
    /**
     * "@PostConstruct"注解标记的类中，由于ApplicationContext还未加载，导致空指针<br>
     * 因此实现BeanFactoryPostProcessor注入ConfigurableListableBeanFactory实现bean的操作
     */
    private static ConfigurableListableBeanFactory beanFactory;
    /**
     * Spring应用上下文环境
     */
    @Getter
    private static ApplicationContext applicationContext;

    /**
     * 获取ListableBeanFactory，可能为ConfigurableListableBeanFactory 或 ApplicationContextAware
     *
     * @return ListableBeanFactory bean factory
     */
    public static ListableBeanFactory getBeanFactory() {
        return null == beanFactory ? applicationContext : beanFactory;
    }

    /**
     * 通过name获取 Bean
     *
     * @param <T>  Bean类型
     * @param name Bean名称
     * @return Bean bean
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        return (T) getBeanFactory().getBean(name);
    }

    /**
     * 通过class获取Bean
     *
     * @param <T>   Bean类型
     * @param clazz Bean类
     * @return Bean对象 bean
     */
    public static <T> T getBean(Class<T> clazz) {
        return getBeanFactory().getBean(clazz);
    }

    /**
     * 通过name,以及Clazz返回指定的Bean
     *
     * @param <T>   bean类型
     * @param name  Bean名称
     * @param clazz bean类型
     * @return Bean对象 bean
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getBeanFactory().getBean(name, clazz);
    }

    /**
     * 获取指定类型对应的所有Bean，包括子类
     *
     * @param <T>  Bean类型
     * @param type 类、接口，null表示获取所有bean
     * @return 类型对应的bean ，key是bean注册的name，value是Bean
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> type) {
        return getBeanFactory().getBeansOfType(type);
    }

    /**
     * 获取指定类型对应的Bean名称，包括子类
     *
     * @param type 类、接口，null表示获取所有bean名称
     * @return bean名称 string [ ]
     */
    public static String[] getBeanNamesForType(Class<?> type) {
        return getBeanFactory().getBeanNamesForType(type);
    }

    /**
     * 获取配置文件配置项的值
     *
     * @param key 配置项key
     * @return 属性值 property
     */
    public static String getProperty(String key) {
        if (null == applicationContext) {
            return null;
        }
        return applicationContext.getEnvironment().getProperty(key);
    }

    /**
     * 动态向Spring注册Bean
     * 由BeanFactory实现，通过工具开放API
     *
     * @param <T>      Bean类型
     * @param beanName 名称
     * @param bean     bean
     */
    public static <T> void registerBean(String beanName, T bean) {
        if (null != beanFactory) {
            beanFactory.autowireBean(bean);
            beanFactory.registerSingleton(beanName, bean);
        } else if (applicationContext instanceof ConfigurableApplicationContext context) {
            AutowireCapableBeanFactory factory = context.getAutowireCapableBeanFactory();
            factory.autowireBean(bean);
            context.getBeanFactory().registerSingleton(beanName, bean);
        }
    }

    /**
     * 注销bean
     * 将Spring中的bean注销，请谨慎使用
     *
     * @param beanName bean名称
     */
    public static void unRegisterBean(String beanName) {
        if (applicationContext instanceof ConfigurableApplicationContext context) {
            DefaultSingletonBeanRegistry registry = (DefaultSingletonBeanRegistry) context.getBeanFactory();
            registry.destroySingleton(beanName);
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        SpringUtils.beanFactory = configurableListableBeanFactory;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtils.applicationContext = applicationContext;
    }
}
