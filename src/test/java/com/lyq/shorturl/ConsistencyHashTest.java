package com.lyq.shorturl;

import com.lyq.shorturl.utils.ConsistencyHashUtil;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;

public class ConsistencyHashTest extends BaseTest{


    @Autowired
    private ConsistencyHashUtil consistencyHashUtil;

    @Test
    public void test() {
        List<String> nodes = Lists.newArrayList();
        HashMap<String, String> cache = new HashMap<>();
        List<String> key = Lists.newArrayList();
        for (int i = 0; i < 10000; i++) {
            key.add("key" + i);
        }
        for (int i = 0; i < 10; i++) {
            nodes.add("node" + i);
        }
        consistencyHashUtil.initVirtual2RealRing(nodes);
        for (String s : key) {
            String value = consistencyHashUtil.getShardInfo(s);
            cache.put(s, value);
        }




        consistencyHashUtil.removeNode("node5");
        cal(key, cache);

        consistencyHashUtil.addNode("node5");
        cal(key, cache);
        for (int i = 10; i < 20; i++) {
            consistencyHashUtil.addNode("node" + i);
            cal(key, cache);
        }
    }

    private void cal(List<String> key, HashMap<String, String> cache) {
        int tot = 0;
        for (String s : key) {
            String value = consistencyHashUtil.getShardInfo(s);
            String s1 = cache.get(s);
            if (!s1.equals(value)) {
                tot++;
                cache.put(s, value);
            }
        }
        System.out.printf("%.2f\n", 100.0 * tot / key.size());
    }

}
