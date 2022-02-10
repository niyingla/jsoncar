package com.github.jsoncat.core.aop.intercept;

import com.github.jsoncat.annotation.aop.After;
import com.github.jsoncat.annotation.aop.Before;
import com.github.jsoncat.annotation.aop.Pointcut;
import com.github.jsoncat.common.util.ReflectionUtil;
import com.github.jsoncat.core.aop.lang.JoinPoint;
import com.github.jsoncat.core.aop.lang.JoinPointImpl;
import com.github.jsoncat.core.aop.util.PatternMatchUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class InternallyAspectInterceptor extends Interceptor {

    private final Object adviceBean;
    //切面路径
    private final HashSet<String> expressionUrls = new HashSet<>();
    //前置方法
    private final List<Method> beforeMethods = new ArrayList<>();
    //后置方法
    private final List<Method> afterMethods = new ArrayList<>();

    public InternallyAspectInterceptor(Object adviceBean) {
        //切面增强对象
        this.adviceBean = adviceBean;
        init();
    }

    /**
     * 初始化切面信息
     */
    private void init() {
        for (Method method : adviceBean.getClass().getMethods()) {
            Pointcut pointcut = method.getAnnotation(Pointcut.class);
            if (!Objects.isNull(pointcut)) {
                expressionUrls.add(pointcut.value());
            }
            Before before = method.getAnnotation(Before.class);
            if (!Objects.isNull(before)) {
                beforeMethods.add(method);
            }
            After after = method.getAnnotation(After.class);
            if (!Objects.isNull(after)) {
                afterMethods.add(method);
            }
        }
    }

    /**
     * 判断是否支持切面
     * @param bean
     * @return
     */
    @Override
    public boolean supports(Object bean) {
        return expressionUrls.stream().anyMatch(url -> PatternMatchUtils.simpleMatch(url, bean.getClass().getName())) && (!beforeMethods.isEmpty() || !afterMethods.isEmpty());
    }

    /**
     * 执行拦截方法
     * @param methodInvocation
     * @return
     */
    @Override
    public Object intercept(MethodInvocation methodInvocation) {
        //组装切点信息
        JoinPoint joinPoint = new JoinPointImpl(adviceBean, methodInvocation.getTargetObject(), methodInvocation.getArgs());
        //前置方法
        beforeMethods.forEach(method -> ReflectionUtil.executeTargetMethodNoResult(adviceBean, method, joinPoint));
        //被拦截对象方法
        Object result = methodInvocation.proceed();
        //后置方法
        afterMethods.forEach(method -> ReflectionUtil.executeTargetMethodNoResult(adviceBean, method, result, joinPoint));
        return result;
    }
}
