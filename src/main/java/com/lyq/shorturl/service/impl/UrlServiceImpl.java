package com.lyq.shorturl.service.impl;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import cn.hutool.bloomfilter.BloomFilterUtil;
import com.lyq.shorturl.mapper.UrlMapper;
import com.lyq.shorturl.model.UrlMap;
import com.lyq.shorturl.service.IUrlService;
import com.lyq.shorturl.utils.HashUtils;
import com.lyq.shorturl.cache.LRUCache;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class UrlServiceImpl implements IUrlService {

    @Resource
    private UrlMapper urlMapper;

    @Resource
    private StringRedisTemplate redisTemplate;

    //自定义长链接防重复字符串
    private static final String DUPLICATE = "*";
    //最近使用的短链接缓存过期时间(分钟)
    private static final long TIMEOUT = 10;
    //创建布隆过滤器
    private static final BitMapBloomFilter FILTER = BloomFilterUtil.createBitMap(10);
    // LRUCache
    private static final LRUCache<String, String> cache = new LRUCache<>(100);

    @Override
    public String getLongUrlByShortUrl(String shortURL) {
        // 本地cache里查找
        String longURL = cache.get(shortURL);
        if (longURL != null) {
            return longURL;
        }
        // 查找Redis中是否有缓存
        longURL = redisTemplate.opsForValue().get(shortURL);
        if (longURL != null) {
            // 有缓存，延迟缓存时间
            redisTemplate.expire(shortURL, TIMEOUT, TimeUnit.MINUTES);
            cache.put(shortURL, longURL);
            return longURL;
        }
        // Redis没有缓存，从数据库查找
        longURL = urlMapper.getLongUrlByShortUrl(shortURL);
        if (longURL != null) {
            // 如果是并发大量数据走到这里，那就是缓存雪崩
            // 如果是并发同一个请求，那就是缓存击穿
            // 数据库有此短链接，添加缓存
            redisTemplate.opsForValue().set(shortURL, longURL, TIMEOUT, TimeUnit.MINUTES);
            cache.put(shortURL, longURL);
            return longURL;
        }

        // 此处是缓存穿透问题：即数据库和redis都没有数据，解决方法：ip限流
        return null;
    }

    @Override
    public String saveUrlMap(String shortURL, String longURL, String originalURL) {
        // 保存长度为1的短链接
        if (shortURL.length() == 1) {
            longURL += DUPLICATE;
            shortURL = saveUrlMap(HashUtils.hashToBase62(longURL), longURL, originalURL);
        } else if (FILTER.contains(shortURL)) {
            // 布隆过滤器中可能存在
            String redisLongURL = redisTemplate.opsForValue().get(shortURL);
            if (redisLongURL != null && redisLongURL.equals(originalURL)) {
                // Redis有缓存，重置过期时间
                redisTemplate.expire(shortURL, TIMEOUT, TimeUnit.MINUTES);
                cache.put(shortURL, longURL);
                return shortURL;
            }
            // 没有缓存，在长链接后加上指定字符串，重新hash
            longURL += DUPLICATE;
            shortURL = saveUrlMap(HashUtils.hashToBase62(longURL), longURL, originalURL);
        } else {
            // 短链本地不存在，直接存入数据库
            String longUrlByShortUrl = urlMapper.getLongUrlByShortUrl(shortURL);
            if (originalURL.equals(longUrlByShortUrl)) {
                FILTER.add(shortURL);
                cache.put(shortURL, originalURL);
                // 添加缓存
                redisTemplate.opsForValue().set(shortURL, originalURL, TIMEOUT, TimeUnit.MINUTES);
            } else {
                shortURL = saveToDB(shortURL, longURL, originalURL);
            }
        }
        return shortURL;
    }

    private String saveToDB(String shortURL, String longURL, String originalURL) {
        try {
            urlMapper.saveUrlMap(new UrlMap(shortURL, originalURL));
            FILTER.add(shortURL);
            cache.put(shortURL, originalURL);
            // 添加缓存
            redisTemplate.opsForValue().set(shortURL, originalURL, TIMEOUT, TimeUnit.MINUTES);
        } catch (Exception e) {
            if (e instanceof DuplicateKeyException) {
                // 数据库已经存在此短链接，则可能是布隆过滤器误判，在长链接后加上指定字符串，重新hash
                // 为什么会有这种情况，因为单机部署是不会出现的，只存在多机部署的情况。
                longURL += DUPLICATE;
                shortURL = saveToDB(HashUtils.hashToBase62(longURL), longURL, originalURL);
                return shortURL;
            } else {
                throw e;
            }
        }
        return null;
    }


    @Override
    public void updateUrlViews(String shortURL) {
        urlMapper.updateUrlViewTimes(shortURL);
    }
}
