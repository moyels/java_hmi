package cn.crrczelc.common.hmi.utils;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author moyel
 */
public class Maps {
    public static <T, R> Map<T, R> expectedSize(int size) {
        return expectedSize(size, HashMap.class);
    }

    public static <T, R> Map<T, R> expectedSize(int size, Class<? extends Map> mapCls) {
        int round = Math.round(size / 0.75f + 1.0f);
        try {
            Constructor<? extends Map> mapConstructor = mapCls.getDeclaredConstructor(int.class);

            return (Map<T, R>) mapConstructor.newInstance(round);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new HashMap<>(round);
    }

    public static <T, R> LinkedHashMap<T, R> expectedLinked(int size) {
        return ((LinkedHashMap<T, R>) expectedSize(size, LinkedHashMap.class));
    }
}
