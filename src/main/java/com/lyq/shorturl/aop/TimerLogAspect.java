package com.lyq.shorturl.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class TimerLogAspect {

    /**
     * Service层切点  增加时间日志
     */
    @Pointcut("@annotation(com.lyq.shorturl.aop.TimerLog)")
    public void ServiceAspect() {

    }

    @Around("ServiceAspect()")
    public  Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        //获取方法名称
        String methodName = joinPoint.getSignature().getName();
        //获取类名
        String name = joinPoint.getTarget().getClass().getName();
        //开启计时
        long start = System.currentTimeMillis();
            //执行方法
        Object obj = joinPoint.proceed();
        log.info("【计时器@TimerLog】{}.{}，用时：{}ms 。", name, methodName, System.currentTimeMillis() - start);
        //建议将异常抛出去，否则其他AOP捕获不到异常，无法做出处理
        return obj;
    }
}
