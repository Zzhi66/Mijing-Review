package com.mj.mijing.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 统一返回结果
 */
@NoArgsConstructor
@AllArgsConstructor
public class Result {

    private Boolean success;
    private String errorMsg;
    private Object data;
    private Long total;

    public static Result ok() {
        return new Result(true, null, null, null);
    }

    public static Result ok(Object data) {
        return new Result(true, null, data, null);
    }

    public static Result ok(Object data, Long total) {
        return new Result(true, null, data, total);
    }

    public static Result fail(String errorMsg) {
        return new Result(false, errorMsg, null, null);
    }

    public Boolean getSuccess() { return success; }
    public String getErrorMsg() { return errorMsg; }
    public Object getData() { return data; }
    public Long getTotal() { return total; }
}
