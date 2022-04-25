# 长短链
学习的一个项目，长链接转短链接，主要是hash，控制并发，Ip限流


## 技术栈
springboot + thymeleaf

参考https://hardcore.feishu.cn/docs/doccnAfY0f35ZgnrFg7jSTQmOOf

存储层：MySQL
缓存层：LRUCache，redis，布隆过滤器
业务：302重定向，MurmurHash计算hash
