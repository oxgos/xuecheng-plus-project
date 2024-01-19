package com.xuecheng.base.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @description 查询结果模型类
 */
@Data
@AllArgsConstructor
public class Result<T> implements Serializable {
    // 状态码
    private String code;

    // 描述
    private String message;

    // 数据
    private T data;

    public Result(T data) {
        this.code = "200";
        this.data = data;
        this.message = "";
    }

}
