package com.lyq.shorturl.model;

import lombok.*;

import java.util.Date;

/**
 * 长短链接映射
 */
@NoArgsConstructor
@Data
@ToString
@AllArgsConstructor
@Builder
public class UrlMap {
    private Long id;

    /**
     * 短链
     */
    private String shortUrl;

    /**
     * 长链
     */
    private String longUrl;

    /**
     * 访问次数
     */
    private Integer viewTimes;

    /**
     * 创建时间
     */
    private Date createTime;

    public UrlMap(String shortUrl, String longUrl) {
        this.shortUrl = shortUrl;
        this.longUrl = longUrl;
        this.viewTimes = 0;
        this.createTime = new Date();
    }
}
