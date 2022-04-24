package com.lyq.shorturl.log;

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
     * TODO 该路径应写成TimerLogHutool注解的路径
     */
    @Pointcut("@annotation(com.lyq.shorturl.log.TimerLog)")
    public void ServiceAspect() {

    }

    @Around("ServiceAspect()")
    public  Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object obj = null;
        //获取方法名称
        String methodName = joinPoint.getSignature().getName();
        //获取类名
        String name = joinPoint.getTarget().getClass().getName();
        //开启计时
        long start = System.currentTimeMillis();
        try {
            //执行方法
            obj = joinPoint.proceed();
            log.info("【计时器@TimerLog】{}.{} - 结束计时，用时：{} 。", name, methodName, System.currentTimeMillis() - start);
        } catch (Throwable e) {
            log.error("【计时器@TimerLog】{}.{} - 方法出错，结束计时，用时：{} 毫秒，错误信息：{}。", name, methodName, System.currentTimeMillis() - start, e.getMessage());
            e.printStackTrace();
            //建议将异常抛出去，否则其他AOP捕获不到异常，无法做出处理
            throw e;
        }
        return obj;
    }
}
