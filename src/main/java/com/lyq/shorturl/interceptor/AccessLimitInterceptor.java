package com.lyq.shorturl.interceptor;

import com.lyq.shorturl.annotation.AccessLimit;
import com.lyq.shorturl.model.Result;
import com.lyq.shorturl.utils.IpAddressUtils;
import com.lyq.shorturl.utils.JacksonUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

@Component
public class AccessLimitInterceptor implements HandlerInterceptor {

    @Resource
    private StringRedisTemplate redisTemplate;


    /**
     * 这个ip限流方法不对，无法保证连续区间都满足
     * 个人想法是在高并发场景下，用lua语言保证原子性，用list去存时间戳，然后通过时间戳比较
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            AccessLimit accessLimit = handlerMethod.getMethodAnnotation(AccessLimit.class);
            // 方法上没有访问控制的注解，直接通过
            if (accessLimit == null) {
                return true;
            }
            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            String ip = IpAddressUtils.getIpAddress(request);
            String method = request.getMethod();
            String requestURI = request.getRequestURI();

            String redisKey = String.format("%s:%s:%s", ip, method, requestURI);
            Object redisResult = redisTemplate.opsForValue().get(redisKey);
            Integer count = JacksonUtils.convertValue(redisResult, Integer.class);

            // 在规定周期内第一次访问，存入redis
            if (count == null) {
                redisTemplate.opsForValue().increment(redisKey, 1);
                redisTemplate.expire(redisKey, seconds, TimeUnit.SECONDS);
            } else {
                // 超出访问限制次数
                if (count >= maxCount) {
                    response.setContentType("application/json;charset=utf-8");
                    PrintWriter out = response.getWriter();
                    Result<Object> result = Result.failed(403, accessLimit.msg());
                    out.write(JacksonUtils.writeValueAsString(result));
                    out.flush();
                    out.close();
                    return false;
                } else {
                    // 没超出访问限制次数
                    redisTemplate.opsForValue().increment(redisKey, 1);
                }
            }
        }
        return true;
    }
}
