package com.veneno.distributedLock;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Amei
 */
@Aspect
@Configuration
@ConditionalOnClass(DistributedLock.class)
@AutoConfigureAfter(DistributedLockAutoConfiguration.class)
public class DistributedLockAspectConfiguration {

    private final Logger logger = LoggerFactory.getLogger(DistributedLockAspectConfiguration.class);

    @Autowired
    private DistributedLock distributedLock;

    @Pointcut("@annotation(RedisLock)")
    private void lockPoint(){

    }

    @Around("lockPoint()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        RedisLock redisLock = method.getAnnotation(RedisLock.class);
//        String key = redisLock.value();
//        if(StringUtils.isEmpty(key)){
//            Object[] args = pjp.getArgs();
//            key = Arrays.toString(args);
//        }

        String key = redisLock.value();
        if(StringUtils.isBlank(key)){
            Object[] args = pjp.getArgs();
            if(redisLock.bindType().equals(RedisLock.BindType.DEFAULT)){
                key = StringUtils.join(args);
            }else if(redisLock.bindType().equals(RedisLock.BindType.ARGS_INDEX)){
                key = getArgsKey(redisLock, args);
            }else if(redisLock.bindType().equals(RedisLock.BindType.OBJECT_PROPERTIES)){
                key = getObjectPropertiesKey(redisLock, args);
            }
        }
        Assert.hasText(key, "key does not exist");

        int retryTimes = redisLock.action().equals(RedisLock.LockFailAction.CONTINUE) ? redisLock.retryTimes() : 0;
        boolean lock = distributedLock.lock(key, redisLock.keepMills(), retryTimes, redisLock.sleepMills());
        if(!lock) {
            logger.error("get lock failed : " + key);
            if(redisLock.errorStrategy().equals(RedisLock.ErrorStrategy.THROW_EXCEPTION)){
                throw new RuntimeException("服务器开小差了,请稍后再试");
            }
            return null;
        }


        //得到锁,执行方法，释放锁
        logger.debug("get lock success : " + key);
        try {
            return pjp.proceed();
        } finally {
            boolean releaseResult = distributedLock.releaseLock(key);
            logger.debug("release lock : " + key + (releaseResult ? " success" : " failed"));
        }
    }




    /**
     * 通过绑定的args生成key
     * @param redisLock redisLock注解
     * @param args 所有参数
     * @return key
     */
    private String getArgsKey(RedisLock redisLock, Object[] args){
        int[] index = redisLock.bindArgsIndex();
        Assert.notEmpty(Arrays.asList(index), "ArgsIndex is empty");

        int len = index.length;
        Object[] indexArgs = new Object[index.length];
        for(int i = 0; i < len; i++){
            indexArgs[i] = args[index[i]];
        }
        return StringUtils.join(indexArgs);
    }

    /**
     * 通过绑定的对象属性生成key
     * @param redisLock redisLock注解
     * @param args 所有参数
     * @return key
     */
    private String getObjectPropertiesKey(RedisLock redisLock, Object[] args) throws NoSuchFieldException, IllegalAccessException {

        String[] properties = redisLock.properties();
        List<Object> keylist = new ArrayList<>(properties.length);

        // 可以通过className获取args的位置
        Map<String, Integer> classNamesArgsIndex = getClassNameArgsIndex(args);
        // 可以通过className获取Class类型
        Map<String, Class<?>> classNameClass = getClassNameClass(args);

        for (String ppts : properties) {
            String[] classProperties = StringUtils.split(ppts, ".");
            String className = classProperties[0];
            String propertiesName = classProperties[1];
            Object argObject = args[classNamesArgsIndex.get(className)];

            Class<?> clazz = classNameClass.get(className);
            Field field = clazz.getDeclaredField(propertiesName);
            field.setAccessible(true);
            Object object = field.get(argObject);
            keylist.add(object);
        }
        return StringUtils.join(keylist.toArray());
    }

    /**
     * 获取类名和参数位置的对应关系
     * @param args 所有参数
     * @return Map<类名, 参数位置>
     */
    private Map<String, Integer> getClassNameArgsIndex(Object[] args){
        int len = args.length;
        Map<String, Integer> nameIndex = new HashMap<>();
        for(int i = 0; i < len; i++){
            String name = StringUtils.substringAfterLast(args[i].getClass().toString(), ".");
            nameIndex.put(name, i);
        }
        return nameIndex;
    }

    /**
     * 获取类名和类的对应关系
     * @param args 所有参数
     * @return Map<类名, 类>
     */
    private Map<String, Class<?>> getClassNameClass(Object[] args){
        int len = args.length;
        Map<String, Class<?>> nameClass = new HashMap<>();
        for(int i = 0; i < len; i++){
            Class<?> clazz = args[i].getClass();
            String name = StringUtils.substringAfterLast(clazz.toString(), ".");
            nameClass.put(name, clazz);
        }
        return nameClass;
    }

}