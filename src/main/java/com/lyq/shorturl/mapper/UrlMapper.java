package com.lyq.shorturl.mapper;

import com.lyq.shorturl.model.UrlMap;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface UrlMapper {

    String getLongUrlByShortUrl(String shortUrl);

    void saveUrlMap(UrlMap urlMap);

    void updateUrlViewTimes(String shortUrl);
}
