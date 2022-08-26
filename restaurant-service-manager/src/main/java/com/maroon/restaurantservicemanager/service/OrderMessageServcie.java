package com.maroon.restaurantservicemanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maroon.restaurantservicemanager.dao.ProductDao;
import com.maroon.restaurantservicemanager.dao.RestaurantDao;
import com.maroon.restaurantservicemanager.dto.OrderMessageDTO;
import com.maroon.restaurantservicemanager.enumeration.ProductStatus;
import com.maroon.restaurantservicemanager.enumeration.RestaurantStatus;
import com.maroon.restaurantservicemanager.po.ProductPO;
import com.maroon.restaurantservicemanager.po.RestaurantPO;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 商户微服务 就是为了收到消息
 * 所以直接实现回调方法
 */
@Service
@Slf4j
public class OrderMessageServcie {

    ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    ProductDao productDao;
    @Autowired
    RestaurantDao restaurantDao;

    /**
     * 异步执行 回调deliverCallback方法
     */
    @Async
    public void handleMessage() {
        ConnectionFactory connectionFactory =new ConnectionFactory();
        connectionFactory.setHost("localhost");
        try( Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()
        ) {
            //谁监听一个队列 谁就要做好申明一哥队列的工作
            channel.exchangeDeclare(
                    "exchange.order.restaurant",
                    BuiltinExchangeType.DIRECT,
                    true,
                    false,
                    null);

            channel.queueDeclare(
                    "queue.restaurant",
                    true,
                    false,
                    false,
                    null);

            channel.queueBind(
                    "queue.restaurant",
                    "exchange.order.restaurant",
                    "key.restaurant");

            //当queue.restaurant队列有消息发来的时候 就会回调deliverCallback方法里的函数
            channel.basicConsume("queue.restaurant", true, deliverCallback, consumerTag -> {

            });
            while (true) {
                Thread.sleep(100000);
            }

        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
    }

    /**
     * 函数式接口
     * consumerTag
     * message 传递过来的消息 调用 getBody方法获取传递过来的消息
     */
    DeliverCallback deliverCallback = ((consumerTag, message) -> {
        String messageBody = new String(message.getBody());

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        try {
            OrderMessageDTO orderMessageDTO = objectMapper.readValue(messageBody, OrderMessageDTO.class);
            log.info("orderMessageDTO:{}",orderMessageDTO);
            /**
             * 根据传过来的消息里面去查询数据库里的产品和餐馆
             */
            Integer productId =orderMessageDTO.getProductId();
            ProductPO productPO = productDao.selectProduct(productId);

            RestaurantPO restaurantPO = restaurantDao.selsctRestaurant(productPO.getRestaurantId());
            /**
             * 判断是否为产品和餐馆是否为可用状态
             */
            if (productPO.getStatus() == ProductStatus.AVALIABLE && restaurantPO.getStatus() == RestaurantStatus.OPEN) {
                //将消息体里价格和状态重新设置一下
                orderMessageDTO.setConfirmed(true);
                orderMessageDTO.setPrice(productPO.getPrice());
            } else {
                orderMessageDTO.setConfirmed(false);
            }
            //返回消息给生产者
            try (Connection connection = connectionFactory.newConnection();
                 Channel channel = connection.createChannel()) {
                String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
                channel.basicPublish("exchange.order.restaurant", "key.order", null, messageToSend.getBytes());

            }

        } catch (TimeoutException e) {
            log.error(e.getMessage(), e);
        }
    });
}
