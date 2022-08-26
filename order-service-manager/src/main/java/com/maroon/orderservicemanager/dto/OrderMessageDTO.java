package com.maroon.orderservicemanager.dto;

import com.maroon.orderservicemanager.enumeration.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

//消息传输的时候需要的数据
@Getter
@Setter
@ToString
public class OrderMessageDTO {
    /**
     * 订单id
     */
    private  Integer orderId;
    /**
     * 订单状态
     */
    private OrderStatus orderStatus;
    /**
     * 价格
     */
    private BigDecimal price;
    /**
     * 骑手id
     */
      private Integer deliverymanId;
    /**
     * 产品id
     */
    private Integer productId;
    /**
     * 用户id
     */
    private Integer accountId;
    /**
     * 结算id
     */
    private Integer settlementId;
    /**
     * 积分计算id
     */
    private Integer rewardId;
    /**
     * 积分数量
     */
    private BigDecimal rewardAmount;
    /**
     * 订单消息确认
     */
    private Boolean confirmed;
}
