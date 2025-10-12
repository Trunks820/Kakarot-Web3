package com.ruoyi.crypto.domain.dto;

import lombok.Data;

/**
 * Twitter API响应
 *
 * @author ruoyi
 */
@Data
public class TwitterApiResponse<T> {

    /**
     * 状态码：1=成功
     */
    private Integer code;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return code != null && code == 1;
    }
}

