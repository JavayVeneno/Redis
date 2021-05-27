package com.veneno.distributedLock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @author Amei
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RedisLock {

    /** 锁的资源，redis的key*/
    String value() default "";

    /** 持锁时间,单位毫秒*/
    long keepMills() default 30000;

    /** 当获取失败时候动作*/
    LockFailAction action() default LockFailAction.CONTINUE;

    public enum LockFailAction{
        /** 放弃 */
        GIVEUP,
        /** 继续 */
        CONTINUE;
    }

    /** 重试的间隔时间,设置GIVEUP忽略此项*/
    long sleepMills() default 200;

    /** 重试次数*/
    int retryTimes() default 5;



    /**
     * 绑定类型(作用于key的生成)
     */
    BindType bindType() default BindType.DEFAULT;

    /**
     * 绑定参数索引，从0开始，与 bindType.ARGS_INDEX 组合使用
     */
    int[] bindArgsIndex() default 0;

    /**
     * 对象参数属性 示例：ClassName.field, 与bingType.OBJECT_PROPERTIES 组合使用
     */
    String[] properties() default "";

    /**
     * 失败策略
     */
    ErrorStrategy errorStrategy() default ErrorStrategy.THROW_EXCEPTION;

    /**
     * 参数key绑定类型
     */
    enum BindType {
        /**
         * 默认，将所有参数toString
         */
        DEFAULT,
        /**
         * 参数索引，从0开始
         */
        ARGS_INDEX,
        /**
         * 对象属性
         */
        OBJECT_PROPERTIES;
    }

    /**
     * 获取锁失败策略
     */
    enum ErrorStrategy {
        /**
         * 抛异常
         */
        THROW_EXCEPTION,

        /**
         * 返回NULL
         */
        RETURN_NULL;
    }

}