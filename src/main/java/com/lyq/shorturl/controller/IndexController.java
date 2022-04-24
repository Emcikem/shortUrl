package com.lyq.shorturl.controller;

import com.lyq.shorturl.annotation.AccessLimit;
import com.lyq.shorturl.model.Result;
import com.lyq.shorturl.service.IUrlService;
import com.lyq.shorturl.utils.HashUtils;
import com.lyq.shorturl.utils.UrlUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Controller
public class IndexController {

    @Resource
    private IUrlService urlService;

    private static String host;

    @Value("${server.host}")
    public void setHost(String host) {
        this.host = host;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @ResponseBody
    @AccessLimit(seconds = 10, maxCount = 1, msg = "10秒内只能生成一次短链接")
    @PostMapping("/generate")
    public Result<Object> generateShortURL(@RequestParam String longURL) {
        if (UrlUtils.checkURL(longURL)) {
            if (!longURL.startsWith("http")) {
                longURL = "https://" + longURL;
            }
            String shortURL = urlService.saveUrlMap(HashUtils.hashToBase62(longURL), longURL, longURL);
            return Result.ok("请求成功", host + shortURL);
        }
        return Result.failed(400, "URL有误");
    }


    @AccessLimit(seconds = 10, maxCount = 5, msg = "Ip限流")
    @GetMapping("/{shortUrl}")
    public String redirect(@PathVariable String shortUrl) {
        String longURL = urlService.getLongUrlByShortUrl(shortUrl);
        if (longURL != null) {
            urlService.updateUrlViews(shortUrl);
            //查询到对应的原始链接，302重定向
            return "redirect:" + longURL;
        }
        //没有对应的原始链接，直接返回首页
        return "redirect:/";
    }

}
