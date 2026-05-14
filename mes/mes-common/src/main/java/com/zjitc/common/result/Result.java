package com.zjitc.common.result;

import lombok.Data;

@Data
public class Result<T> {

    private int code;
    private String msg;
    private T data;

    /**
     * 构造函数
     * @param code
     * @param msg
     * @param data
     */
    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> fail(String msg) {
        return new Result<>(500, msg, null);
    }

    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }



}
