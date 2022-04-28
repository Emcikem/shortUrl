package com.lyq.shorturl.utils;

import cn.hutool.core.lang.hash.MurmurHash;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * 1. hash算法选择
 * 不要用hashcode函数，不够散列，而且有负值要处理
 * 2. 数据结构选择
 * （1）要根据hash值排序存储
 * （2）排序存储
 * （3）排序查找还有能方便变更
 *
 * 采用红黑树，TreeMap
 */
@Service
public class ConsistencyHashUtil {

    private List<String> shardNodes;

    private final int VIRTUAL_NODE_NUM = 1000;

    private final TreeMap<Long, String> virtualHash2RealNode = new TreeMap<>();

    /**
     * 初始化hash环
     */
    public void initVirtual2RealRing(List<String> shardNodes) {
        this.shardNodes = shardNodes;
        for (String node : shardNodes) {
            for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
                long hashCode = hash("SHARD-" + node + "-NODE-" + i);
                virtualHash2RealNode.put(hashCode, node);
            }
        }
    }

    public String getShardInfo(String key) {
        long hashCode = hash(key);
        SortedMap<Long, String> tailMap = virtualHash2RealNode.tailMap(hashCode);
        if (tailMap.isEmpty()) {
            return virtualHash2RealNode.firstEntry().getValue();
        }
        return virtualHash2RealNode.get(tailMap.firstKey());
    }


    private int hash(String key) {
        return MurmurHash.hash32(key);
    }


    public void removeNode(String shardNode) {
        shardNodes.remove(shardNode);
        for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
            long hashCode = hash("SHARD-" + shardNode + "-NODE-" + i);
            virtualHash2RealNode.remove(hashCode);
        }
    }

    public void addNode(String shardNode) {
        shardNodes.add(shardNode);
        for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
            long hashCode = hash("SHARD-" + shardNode + "-NODE-" + i);
            virtualHash2RealNode.put(hashCode, shardNode);
        }
    }
}
