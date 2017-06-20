package com.coolyota.logreport.tools.log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by feizan on 2016/9/21.
 */

public class ReflectionCall {
    private static final CYLog logger = new CYLog(ReflectionCall.class.getSimpleName());

    public static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        Method method = null;
        try {
            if (clazz == Object.class) return null;
            method = clazz.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            //e.printStackTrace();
        } finally {
            if (null == method) {
                method = getMethod(clazz.getSuperclass(), name, parameterTypes);
            } else {
                logger.debug("getMethod method:" + method);
            }
        }
        if (null == method) {
            logger.w("getMethod null method for " + clazz);
        }
        return method;
    }

    public static Field getField(Class<?> clazz, String name) {
        Field field = null;
        try {
            if (clazz == Object.class) return null;
            field = clazz.getDeclaredField(name);
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            //e.printStackTrace();
        } finally {
            if (null == field) {
                field = getField(clazz.getSuperclass(), name);
            } else {
                logger.debug("getField field: " + field);
            }
        }
        if (null == field) {
            logger.w("getField null field for " + clazz);
        }
        return field;
    }

    public static <T> T invoke(Method method, Object object, Object... parameterTypes) {
        try {
            if (null == method) return null;
            return (T) method.invoke(object, parameterTypes);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T value(Field field, Object object) {
        try {
            if (null == field) return null;
            return (T) field.get(object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
