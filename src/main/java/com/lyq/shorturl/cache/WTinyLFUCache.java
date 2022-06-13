package com.lyq.shorturl.cache;

/**
 * @author Emcikem
 * @create 2022/6/13
 * @desc
 */
public class WTinyLFUCache<K, V> implements ICache<K, V> {

    // 最大的个数限制
    private long maximum;
    // 当前的个数
    private long weightedSize;
    // window区的最大限制
    private long windowMaximum;
    // window区当前的个数
    private long windowWeightedSize;
    // protected区的最大限制
    private long mainProtectedMaximum;
    // protected当前的个数
    private long mainProtectedWeightedSize;
    // 下一次需要调整的大小(还需要进一步计算)
    private double stepSize;
    // window区需要调整的大小
    private long adjustment;
    // 命中计数
    private int hitsInSample;
    // 不命中的计数
    private int missesInSample;
    // 上一次的缓存命中率
    private double previousSampleHitRate;

    @Override
    public V get(K key) {
        return null;
    }

    @Override
    public void put(K key, V value) {

    }

//    private final FrequencySketch<K> sketch;
//    // window区的LRU queue (FIFO)
//    private final AccessOrderDeque<Node<K, V>> accessOrderDeque;
//    //probation区的LRU queue（FIFO）
//    private final AccessOrderDeque<Node<K, V>> accessOrderProbationDeque;
//    //protected区的LRU queue（FIFO）
//    private final AccessOrderDeque<Node<K, V>> accessOrderProtectedDeque;

}
