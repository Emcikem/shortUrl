package com.lyq.shorturl.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class Result <T> {

    private Integer code;

    private String msg;

    private T data;

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> ok(String msg, T data) {
        return new Result<>(200, msg, data);
    }

    public static <T> Result<T> failed(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

}
