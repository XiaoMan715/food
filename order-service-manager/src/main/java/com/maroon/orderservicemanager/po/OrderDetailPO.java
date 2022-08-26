package com.maroon.orderservicemanager.po;

import com.maroon.orderservicemanager.enumeration.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
//存数据库类型
@Getter
@Setter
@ToString
public class OrderDetailPO {
    /**
     * 订单id
     */
    private Integer id;
    /**
     * 状态
     */
    private OrderStatus status;
    /**
     * 地址
     */
    private String address;
    /**
     * 用户id
     */
    private Integer accountId;
    /**
     * 产品id
     */
    private Integer productId;
    /**
     * 骑手id
     */
    private Integer deliverymanId;
    /**
     * 结算id
     */
    private Integer settlementId;
    /**
     * 积分奖励id
     */
    private Integer rewardId;
    /**
     * 价格
     */
    private BigDecimal price;
    /**
     * 时间
     *
     */
    private Date date;
}