package com.lyq.shorturl.cache;

/**
 * @author Emcikem
 * @create 2022/5/2
 * @desc cache统一接口
 */
public interface ICache <K, V> {

    V get(K key);

    void put(K key, V value);
}
