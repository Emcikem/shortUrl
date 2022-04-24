package com.lyq.shorturl.service;

import org.springframework.scheduling.annotation.Async;

public interface IUrlService {

    /**
     * 通过短链接获取长链接
     */
    String getLongUrlByShortUrl(String shortURL);

    /**
     * 生成短链接，保存在数据库
     */
    String saveUrlMap(String shortURL, String longURL, String originalURL);

    /**
     * 统计次数
     */
    @Async
    void updateUrlViews(String shortURL);
}
