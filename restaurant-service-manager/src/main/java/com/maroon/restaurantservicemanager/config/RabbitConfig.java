package com.maroon.restaurantservicemanager.config;


import com.maroon.restaurantservicemanager.service.OrderMessageServcie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * 系统启动时会在配置类里自动注入Autowired方法 并自动调用异步线程池启动消息发送
 */

@Configuration
public class RabbitConfig {

    @Autowired
    OrderMessageServcie orderMessageServcie;

    @Autowired
    public void startListenMessage(){
        orderMessageServcie.handleMessage();
    }
}
