package com.lyq.shorturl.cache;


import java.util.HashMap;
import java.util.Map;


public class LRUCache<K, V> {

    private final Map<K, DLinkedNode> cache;
    private int size;
    private final int capacity; // size是当前的大小，capacity表示lru的容量
    private final DLinkedNode head, tail;

    /**
     * 节点，有前指针和后指针
     */
    class DLinkedNode {
        K key;
        V value;
        DLinkedNode prev, next;
        public DLinkedNode() {}
        public DLinkedNode(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    public LRUCache(int capacity) {
        this.size = 0;
        assert capacity > 0;
        this.capacity = capacity;
        head = new DLinkedNode();
        tail = new DLinkedNode();
        cache = new HashMap<>(capacity);
        head.next = tail; // 循环链表
        tail.prev = head;
    }

    /**
     * 不存在就返回null
     * 存在就把值移到头部
     * O(1)
     */
    public V get(K key) {
        DLinkedNode node = cache.get(key);
        if (node == null) {
            return null;
        }
        moveToHead(node);
        return node.value;
    }

    /**
     * 不存在就创建一个新的结点，并判断是否超过容量，如果超过容量，那么移除链表最后一个结点，然后加到头部
     * 存在就更新值，移到头部
     * O(1)
     */
    public void put(K key, V value) {
        DLinkedNode node = cache.get(key);
        if(node == null){
            DLinkedNode newNode = new DLinkedNode(key, value);
            cache.put(key, newNode);
            addToHead(newNode);
            ++size;
            if(size > capacity) {
                DLinkedNode tail = removeTail();
                cache.remove(tail.key);
                --size;
            }
        }else {
            node.value = value;
            moveToHead(node);
        }
    }


    /**
     * 删除该节点
     */
    private void removeNode(DLinkedNode node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    /**
     * 把节点node加入首部
     */
    private void addToHead(DLinkedNode node) {
        node.prev = head;
        node.next = head.next;
        node.next.prev = node;
        head.next = node;
    }

    /**
     * 把节点node移到首部
     * 方法: 在原位置删除该节点，然后再首部加入该节点
     */
    private void moveToHead(DLinkedNode node) {
        removeNode(node);
        addToHead(node);
    }

    /**
     * 删除尾部的结点，并返回这个结点
     * 有个问题就是当链表为空，即只存在head和tail时，不能removeTail，保证了capacity＞0即可
     */
    private DLinkedNode removeTail() {
        DLinkedNode node = tail.prev;
        removeNode(node);
        return node;
    }
}
