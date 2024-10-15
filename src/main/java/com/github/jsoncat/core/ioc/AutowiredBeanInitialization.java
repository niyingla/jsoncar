package com.github.jsoncat.core.ioc;

import com.github.jsoncat.annotation.config.Value;
import com.github.jsoncat.annotation.ioc.Autowired;
import com.github.jsoncat.annotation.ioc.Qualifier;
import com.github.jsoncat.common.util.ObjectUtil;
import com.github.jsoncat.common.util.ReflectionUtil;
import com.github.jsoncat.core.aop.factory.AopProxyBeanPostProcessorFactory;
import com.github.jsoncat.core.aop.intercept.BeanPostProcessor;
import com.github.jsoncat.core.config.ConfigurationManager;
import com.github.jsoncat.exception.CanNotDetermineTargetBeanException;
import com.github.jsoncat.exception.InterfaceNotHaveImplementedClassException;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author shuang.kou
 * @createTime 2020年10月19日 10:08:00
 **/
public class AutowiredBeanInitialization {
    private final String[] packageNames;

    public AutowiredBeanInitialization(String[] packageNames) {
        this.packageNames = packageNames;
    }

    //二级缓存（解决循环依赖问题）
    /**
     * 存放注入了属性的的bean
     */
    private static final Map<String, Object> SINGLETON_OBJECTS = new ConcurrentHashMap<>(64);

    /**
     * 初始化对象属性
     * @param beanInstance
     */
    public void initialize(Object beanInstance) {
        Class<?> beanClass = beanInstance.getClass();
        Field[] beanFields = beanClass.getDeclaredFields();
        // 遍历bean的属性
        for (Field beanField : beanFields) {
            //注入对象
            if (beanField.isAnnotationPresent(Autowired.class)) {
                //获取容器中的字段对象
                Object beanFieldInstance = processAutowiredAnnotationField(beanField);
                //获取字段对象bean名称
                String beanFieldName = BeanHelper.getBeanName(beanField.getType());
                // 解决循环依赖问题
                beanFieldInstance = resolveCircularDependency(beanInstance, beanFieldInstance, beanFieldName);
                // AOP 获取一个代理类（jdk/cglib）
                BeanPostProcessor beanPostProcessor = AopProxyBeanPostProcessorFactory.get(beanField.getType());
                //包装代理方法
                beanFieldInstance = beanPostProcessor.postProcessAfterInitialization(beanFieldInstance);
                //设置到字段
                ReflectionUtil.setField(beanInstance, beanField, beanFieldInstance);
            }
            //注入属性值
            if (beanField.isAnnotationPresent(Value.class)) {
                //处理@Value注解
                Object convertedValue = processValueAnnotationField(beanField);
                //设置到字段
                ReflectionUtil.setField(beanInstance, beanField, convertedValue);
            }
        }
    }

    /**
     * 处理被 @Autowired 注解标记的字段
     *
     * @param beanField 目标类的字段
     * @return 目标类的字段对应的对象
     */
    private Object processAutowiredAnnotationField(Field beanField) {
        //当前字段的类型
        Class<?> beanFieldClass = beanField.getType();
        //根据类型获取注入的bean名字
        String beanFieldName = BeanHelper.getBeanName(beanFieldClass);
        Object beanFieldInstance;
        //当注入类型为接口时
        if (beanFieldClass.isInterface()) {
            // 获取接口的实现类
            @SuppressWarnings("unchecked")
            Set<Class<?>> subClasses = ReflectionUtil.getSubClass(packageNames, (Class<Object>) beanFieldClass);
            if (subClasses.isEmpty()) {
                throw new InterfaceNotHaveImplementedClassException(beanFieldClass.getName() + "is interface and do not have implemented class exception");
            }
            if (subClasses.size() == 1) {
                Class<?> subClass = subClasses.iterator().next();
                // 获取接口的实现类名字
                beanFieldName = BeanHelper.getBeanName(subClass);
            }
            if (subClasses.size() > 1) {
                Qualifier qualifier = beanField.getDeclaredAnnotation(Qualifier.class);
                beanFieldName = qualifier == null ? beanFieldName : qualifier.value();
            }
        }
        //缓存中的对象
        beanFieldInstance = BeanFactory.BEANS.get(beanFieldName);
        if (beanFieldInstance == null) {
            throw new CanNotDetermineTargetBeanException("can not determine target bean of" + beanFieldClass.getName());
        }
        return beanFieldInstance;
    }

    /**
     * 处理被 @Value 注解标记的字段
     *
     * @param beanField 目标类的字段
     * @return 目标类的字段对应的对象
     */
    private Object processValueAnnotationField(Field beanField) {
        String key = beanField.getDeclaredAnnotation(Value.class).value();
        //从配置信息里面获取
        ConfigurationManager configurationManager = (ConfigurationManager) BeanFactory.BEANS.get(ConfigurationManager.class.getName());
        String value = configurationManager.getString(key);
        if (value == null) {
            throw new IllegalArgumentException("can not find target value for property:{" + key + "}");
        }
        //转化类型
        return ObjectUtil.convert(beanField.getType(), value);
    }

    /**
     * 二级缓存解决循环依赖问题
     */
    private Object resolveCircularDependency(Object beanInstance, Object beanFieldInstance, String beanFieldName) {
        if (SINGLETON_OBJECTS.containsKey(beanFieldName)) {
            beanFieldInstance = SINGLETON_OBJECTS.get(beanFieldName);
        } else {
            SINGLETON_OBJECTS.put(beanFieldName, beanFieldInstance);
            initialize(beanInstance);
        }
        return beanFieldInstance;
    }

}
