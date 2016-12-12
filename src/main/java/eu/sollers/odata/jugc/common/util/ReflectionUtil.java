package eu.sollers.odata.jugc.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.sollers.odata.jugc.common.entity.JpaOdataEntity;

/**
 * Helper class for extracting annotations from OData Entities.
 */
public class ReflectionUtil {
    public static Field[] getFieldsUpToJpaOdataEntity(Class<?> firstClass) {
        //return getUpToJpaOdataEntity(firstClass, Field.class);
        List<Field> result = new ArrayList<>();
        Class<?> superclass = firstClass.getSuperclass();
        result.addAll(Arrays.asList(firstClass.getDeclaredFields()));
        if (!isLastClass(superclass)) {
            result.addAll(Arrays.asList(getFieldsUpToJpaOdataEntity(superclass)));
        }
        return result.toArray(new Field[result.size()]);
    }

    public static Method[] getMethodsUpToJpaOdataEntity(Class<?> firstClass) {
        //return getUpToJpaOdataEntity(firstClass, Method.class);
        List<Method> result = new ArrayList<>();
        Class<?> superclass = firstClass.getSuperclass();
        result.addAll(Arrays.asList(firstClass.getDeclaredMethods()));
        if (!isLastClass(superclass)) {
            result.addAll(Arrays.asList(getMethodsUpToJpaOdataEntity(superclass)));
        }
        return result.toArray(new Method[result.size()]);
    }

    private static boolean isLastClass(Class<?> clazz) {
        return clazz == null || clazz.equals(JpaOdataEntity.class);
    }
}
