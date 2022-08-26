package com.maroon.orderservicemanager.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 前端需要订单传递数据结构
 */
@Getter
@Setter
@ToString
public class OrderCreateVO {
    //用户id
    private Integer accountId;
    //地址
    private String address;
    // 产品id
    private Integer productId;
}
