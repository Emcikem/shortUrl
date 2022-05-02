package com.lyq.shorturl.cache;


import java.util.HashMap;
import java.util.Map;

/**
 * 最不常用的
 * 删除策略：删除最近使用次数最少的
 * 在LRU的基础上改进，LRU是一个双向链表，而LFU是多个链表，链表个数取决于频次的个数
 * @param <K>
 * @param <V>
 */
public class LFUCache<K, V> implements ICache<K, V>{

    /**
     * 结点，有前驱和后继
     */
    class Node {
        K key;
        V value;
        int freq = 1;
        Node pre, next;
        public Node() {}
        public Node(K key, V value) {this.key = key; this.value = value;};
    }

    /**
     * 双向链表，可以删除点和加结点到首部
     */
    class DoublyLinkedList {
        Node head, tail;

        public DoublyLinkedList() {
            head = new Node(); tail = new Node();
            head.next = tail; tail.pre = head;
        }
        void removeNode(Node node) {
            node.pre.next = node.next;
            node.next.pre = node.pre;
        }
        void addToHead(Node node) {
            node.pre = head;
            head.next.pre = node;
            node.next = head.next;
            head.next = node;
        }
    }
    private int size; // 当前大小
    private final int capacity; // 当前大小和容量
    private int min; // 当前最小频次
    private final Map<K, Node> cache; // hashMap判断是否存在
    private final Map<Integer, DoublyLinkedList> freMap;// 存储每个频次对应的双向链表

    public LFUCache(int capacity) {
        assert capacity > 0;
        this.capacity = capacity; size = 0;
        cache = new HashMap<>();
        freMap = new HashMap<>();
    }

    /**
     * 没有这个点就直接返回-1，有这个点就把这个点出现的次数加1
     */
    @Override
    public V get(K key) {
        Node node = cache.get(key);
        if (node == null) {
            return null;
        }
        freInc(node);
        return node.value;
    }

    /**
     * 有这个点就更新这个点，然后把次数加1
     * 没有这个点，就注意是否会超出缓存大小，超出就删除最小频次的双向链表的尾部结点，没超出就加上这个点，注意链表的初始化，此时min = 1
     */
    @Override
    public void put(K key, V value) {
        Node node = cache.get(key);
        if (node == null) {
            if (size == capacity) {
                DoublyLinkedList minFreqLinkedList = freMap.get(min);
                cache.remove(minFreqLinkedList.tail.pre.key);
                minFreqLinkedList.removeNode(minFreqLinkedList.tail.pre);
                size--;
            }
            Node newNode = new Node(key, value);
            cache.put(key, newNode);
            DoublyLinkedList linkedList = freMap.get(1);
            if (linkedList == null) {
                linkedList = new DoublyLinkedList();
                freMap.put(1, linkedList);
            }
            linkedList.addToHead(newNode);
            size++;
            min = 1;
        } else {
            node.value = value;
            freInc(node);
        }
    }

    /**
     * 增加页面出现的次数
     * 先在对应频次双向链表删除这个点
     * 然后判断是这个点是不是就是最小频次的双向链表里唯一的点，如果是，那么最小频次就要加1了
     * 在更高的频次双向链表里把这个点加到首部，注意更高频次的初始化
     * 注意
     */
    void freInc(Node node) {
        int freq = node.freq;
        DoublyLinkedList linkedList = freMap.get(freq);
        linkedList.removeNode(node);
        if (freq == min && linkedList.head.next == linkedList.tail) {
            min = freq + 1;
        }
        node.freq++;
        linkedList = freMap.get(freq + 1);
        if (linkedList == null) {
            linkedList = new DoublyLinkedList();
            freMap.put(freq + 1, linkedList);
        }
        linkedList.addToHead(node);
    }
}
