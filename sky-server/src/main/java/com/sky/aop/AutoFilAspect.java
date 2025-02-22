package com.sky.aop;

import com.sky.anno.AutoFIll;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFilAspect {
    /**
     * 定义切入点：拦截 controller 包下的所有方法
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.anno.AutoFIll)")
    public void autoFillPointcut() {
    }

    /**
     * 前置通知(在方法执行前设置字段)
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Before("autoFillPointcut()")
    public void autoFill(JoinPoint joinPoint) throws Throwable {
        log.info("开始执行自动填充");
        //从方法签名中提取@AutoFIll注解中获取当前数据库的操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//获取方法签名
        AutoFIll autoFIll = signature.getMethod().getAnnotation(AutoFIll.class);//获取方法上的注解
        OperationType value = autoFIll.value();

        //获取当前方法的参数-实体对象
        Object[] args = joinPoint.getArgs();
        if (args.length == 0 || args[0] == null) {
            return;
        }

        Object entity = args[0];

        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //根据数据库操作类型，插入or更新
        if (value == OperationType.INSERT) {
            /*entity.getClass() 获取实体对象的类,getDeclaredMethod() 根据方法名和参数类型列表获取该类中声明的方法。*/
            Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

            //通过反射设置数据
            setCreateTime.invoke(entity, now);
            setCreateUser.invoke(entity, currentId);
            setUpdateTime.invoke(entity, now);
            setUpdateUser.invoke(entity, currentId);
        } else if (value == OperationType.UPDATE) {
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            setUpdateTime.invoke(entity, now);
            setUpdateUser.invoke(entity, currentId);
        }
    }
}
