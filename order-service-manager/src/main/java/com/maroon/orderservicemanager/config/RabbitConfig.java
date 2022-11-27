package com.maroon.orderservicemanager.config;

import com.maroon.orderservicemanager.service.OrderMessageService;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * 系统启动时会在配置类里自动注入Autowired方法 并自动调用异步线程池启动消息发送
 */

@Configuration
public class RabbitConfig {

    @Autowired
    OrderMessageService orderMessageService;

    @Autowired
    public void startListenMessage(){
        orderMessageService.handleMessage();
    }

    @Autowired
    public void  initRabbit(){
        CachingConnectionFactory  connectionFactory = new CachingConnectionFactory();
        Exchange exchange=new DirectExchange("");

    }
}
