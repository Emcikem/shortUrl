package com.lyq.shorturl;

import com.lyq.shorturl.cache.LRUCache;
import org.junit.jupiter.api.Test;

public class LRUTest extends BaseTest{

    @Test
    public void speed() {
        long start = System.currentTimeMillis();
        LRUCache<String, String> cache = new LRUCache<>(1000);
        for (int i = 1; i <= 10000000; i++) {
            if ((i & 1) == 1) cache.put(String.valueOf(i), "1021021");
            else cache.get(String.valueOf(i - 1000));
        }
        System.out.println(System.currentTimeMillis() - start);
        start = System.currentTimeMillis();
        cn.hutool.cache.impl.LRUCache cache1 = new cn.hutool.cache.impl.LRUCache(1000);
        for (int i = 1; i <= 10000000; i++) {
            if ((i & 1) == 1) cache1.put(String.valueOf(i), "1021021");
            else cache1.get(String.valueOf(i - 1000));
        }
        System.out.println(System.currentTimeMillis() - start);
    }
}
