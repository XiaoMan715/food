package com.maroon.restaurantservicemanager.enumeration;

public enum OrderStatus {
    /**
     * 订单创建中
     */
    ORDER_CREATING,
    /**
     * 餐厅已确认
     */
    RESTAURANT_CONFIRMED,
    /**
     * 骑手已确认
     */
    DELVERTMAN_CONFIRMED,
    /**
     * 已结算
     */
    SETTLEMENT_CONFIRMED,
    /**
     * 订单创建失败
     */
    FAILED,
}