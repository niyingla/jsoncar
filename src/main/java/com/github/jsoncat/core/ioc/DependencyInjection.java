package com.github.jsoncat.core.ioc;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author shuang.kou & tom
 * @createTime 2020年09月30日 07:51:00
 **/
@Slf4j
public class DependencyInjection {


    /**
     * 遍历ioc容器所有bean的属性, 为所有带@Autowired/@Value注解的属性注入实例
     */
    public static void inject(String[] packageNames) {
        AutowiredBeanInitialization autowiredBeanInitialization = new AutowiredBeanInitialization(packageNames);
        Map<String, Object> beans = BeanFactory.BEANS;
        if (!beans.isEmpty()) {
            BeanFactory.BEANS.values().forEach(autowiredBeanInitialization::initialize);
        }
    }

}
