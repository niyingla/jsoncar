package com.github.jsoncat.core.aop.intercept;

import com.github.jsoncat.core.aop.factory.InterceptorFactory;

public abstract class AbstractAopProxyBeanPostProcessor implements BeanPostProcessor {

    /**
     * 初始化后的后处理，就是给包装bean加上aop拦截，并返回
     * @param bean
     * @return
     */
    @Override
    public Object postProcessAfterInitialization(Object bean) {
        Object wrapperProxyBean = bean;
        //链式包装目标类
        for (Interceptor interceptor : InterceptorFactory.getInterceptors()) {
            if (interceptor.supports(bean)) {
                wrapperProxyBean = wrapBean(wrapperProxyBean, interceptor);
            }
        }
        return wrapperProxyBean;
    }

    public abstract Object wrapBean(Object target, Interceptor interceptor);
}
