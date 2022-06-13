package com.lyq.shorturl.cache;

/**
 * @author Emcikem
 * @create 2022/6/12
 * @desc Count-Min Sketch
 * 用long去存数据，那么64位可以存16个counter，设置4个哈希，那么就还可以存4个key
 * counter就是4bit的分段,slot是数组下标
 */
public class FrequencySketch<E> {

    static final long[] SEED = { // A mixture of seeds from FNV-1a, CityHash, and Murmur3
            0xc3a5c85c97cb3127L, 0xb492b66fbe98f273L, 0x9ae16a3b2f90404fL, 0xcbf29ce484222325L};
    // 每个counter的值是0111，16个counter组成的值
    static final long RESET_MASK = 0x7777777777777777L;
    // 每个counter的值是0001，16个counter组成的值
    static final long ONE_MASK = 0x1111111111111111L;

    //size累加到sampleSize时，执行减半操作
    private int sampleSize;
    // hash&tableMask相当于取模，得到slot的index
    private int tableMask;
    // 记录frequency的一维数组
    private long[] table;
    // 所有的counter之和
    private int size;

    /**
     * 初始化
     *
     * 初始化table，
     * 若maximumSize = 0，table长度为1。否则table的长度为大于等于maximumSize的最小的2的整数倍。
     * 若maximumSize大于0，sampleSize=10倍数组长度
     * tableMask=数组长度减1，用于取模
     */
    public void ensureCapacity(long maximumSize) {
        int maximum = (int) Math.min(maximumSize, Integer.MAX_VALUE >>> 1);
        if ((table != null) && (table.length >= maximum)) {
            return;
        }

        table = new long[(maximum == 0) ? 1 : ceilingPowerOfTwo(maximum)];
        tableMask = Math.max(0, table.length - 1);
        sampleSize = (maximumSize == 0) ? 10 : (10 * maximum);
        if (sampleSize <= 0) {
            sampleSize = Integer.MAX_VALUE;
        }
        size = 0;
    }

    public boolean isNotInitialized() {
        return (table == null);
    }

    public FrequencySketch(){
    }

    /**
     * 获取元素的频次，由于每个元素都有4个hash算法，在4个位置记录了4个频次，取其中最小的频次作为该元素的频次
     * 获取元素的counter下标和slot小标，再取对应的4bit的数据
     * @param e key
     * @return 出现次数
     */
    public int frequency(E e) {
        if (isNotInitialized()) {
            return 0;
        }
        int hash = spread(e.hashCode());
        // 在4个key的第几个位置
        int start = (hash & 3) << 2;
        int frequency = Integer.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            int index = indexOf(hash, i);
            // ((start + i) << 2) 第几个counter，然后>>>再&1111求出counter的值
            int count = (int) ((table[index] >>> ((start + i) << 2)) & 0xfL);
            frequency = Math.min(frequency, count);
        }
        return frequency;
    }

    /**
     * 对于一个key，需要先找到slot下标，因为是哈希，所以4个slot是不一样的。然后再找到counter，为了简单，4个counter相对连续
     */
    public void increment(E e) {
        if (isNotInitialized()) {
            return;
        }
        int hash = spread(e.hashCode());
        // start: 0, 4, 8, 12      16个counter，同时需要4个哈希的counter进制位相邻，所以就取4个数字
        int start = (hash & 3) << 2;
        boolean added = false;
        for (int i = 0; i < 4; i++) {
            int index = indexOf(hash, i);
            //尝试在每个counter上加1，最多15
            added |= incrementAt(index, start + i);
        }

        // 根据&&的短路线来判断是否需要周期衰减
        if (added && (++size == sampleSize)) {
            reset();
        }
    }

    /**
     * j = 第几个counter
     * 0, 1, 2, 3
     * 4, 5, 6, 7
     * 8, 9, 10, 11
     * 12, 13, 14, 15
     * offset为counter的4位bit的最低位在64位bit中的下标。
     * 找到counter的值然后+1操作，最多15
     */
    private boolean incrementAt(int i, int j) {
        int offset = j << 2;
        //mask为掩码，counter的那4个bit，在mask中相同位置的4bit为1111，其他位置为0。
        long mask = (0xfL << offset);
        //(table[i] & mask) != mask，表示那4bit不全为1，也就是不等于15
        if ((table[i] & mask) != mask) {
            //counter的最右边加1
            table[i] += (1L << offset);
            return true;
        }
        return false;
    }

    /**
     * 每个counter除以2
     */
    void reset() {
        int count = 0;
        for (int i = 0; i < table.length; i++) {
            //16个counter中频次为奇数的个数
            count += Long.bitCount(table[i] & ONE_MASK);
            //每个counter都除2操作。>>>先右移，整体除2，但是每个counter的最高位是前面的counter的低位，那么&RESET_MASK，抹去最高位
            table[i] = (table[i] >>> 1) & RESET_MASK;
        }
        size = (size - (count >>> 2)) >>> 1;
    }

    /**
     * 根据哈希求出slot下标
     * @param item 哈希
     * @param i 第几个哈希
     * @return slot下标
     */
    int indexOf(int item, int i) {
        long hash = (item + SEED[i]) * SEED[i];
        hash += (hash >>> 32);
        return ((int) hash) & tableMask;
    }

    /**
     * 求哈希
     */
    private int spread(int x) {
        x = ((x >>> 16) ^ x) * 0x45d9f3b;
        x = ((x >>> 16) ^ x) * 0x45d9f3b;
        return (x >>> 16) ^ x;
    }

    private int ceilingPowerOfTwo(int x) {
        return 1 << -Integer.numberOfLeadingZeros(x - 1);
    }
}

