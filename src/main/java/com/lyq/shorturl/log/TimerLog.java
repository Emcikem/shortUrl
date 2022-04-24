package com.lyq.shorturl.log;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TimerLog {


    //增加参数，用来支持不同的业务

}
