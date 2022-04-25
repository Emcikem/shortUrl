# 长短链
学习的一个项目，长链接转短链接，主要是hash，控制并发，Ip限流


## 技术栈
springboot + thymeleaf

参考
https://hardcore.feishu.cn/docs/doccnAfY0f35ZgnrFg7jSTQmOOf
https://github.com/Naccl/ShortURL

存储层：MySQL

缓存层：LRUCache，redis，布隆过滤器

布隆过滤器主要用于优化存储，LRUCache，redis主要用于优化查询

业务：302重定向，MurmurHash计算hash

采用302而不是301的原因在于301 为永久重定向、302 为临时重定向，因为302便于统计网站的访问次数。
