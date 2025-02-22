package com.sky.anno;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  自定义注解，用于标识哪些方法需要公共字段填充
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFIll {
    //设置数据库操作类型:update,insert
    OperationType value();
}