package com.lyq.shorturl.mapper;

import com.lyq.shorturl.model.UrlMap;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface UrlMapper {

    String getLongUrlByShortUrl(String shortUrl);

    int saveUrlMap(UrlMap urlMap);

    int updateUrlViewTimes(String shortUrl);
}
