package ru.akirakozov.sd.refactoring.common;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class TestUtils {

    // Java 9 Map.of
    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        Map<K, V> res = new HashMap<>();
        res.put(k1, v1);
        res.put(k2, v2);
        return res;
    }
}
