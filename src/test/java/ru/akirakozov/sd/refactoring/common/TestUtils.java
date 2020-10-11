package ru.akirakozov.sd.refactoring.common;

import java.util.HashMap;
import java.util.Map;

public class TestUtils {

    // Java 9 Map.of
    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        Map<K, V> res = new HashMap<>();
        res.put(k1, v1);
        res.put(k2, v2);
        return res;
    }
}
