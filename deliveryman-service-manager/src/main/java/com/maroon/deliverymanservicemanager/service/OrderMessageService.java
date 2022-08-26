package com.maroon.deliverymanservicemanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maroon.deliverymanservicemanager.dao.DeliverymanDao;
import com.maroon.deliverymanservicemanager.dto.OrderMessageDTO;
import com.maroon.deliverymanservicemanager.enummeration.DeliverymanStatus;
import com.maroon.deliverymanservicemanager.po.DeliverymanPO;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class OrderMessageService {

    @Autowired
    DeliverymanDao deliverymanDao;

    ObjectMapper objectMapper = new ObjectMapper();

    DeliverCallback deliverCallback = (consumerTag, message) -> {
        //  将发送来的消息转换成json字符串
        String messageBody = new String(message.getBody());
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        OrderMessageDTO orderMessageDTO = null;

        try  {
            orderMessageDTO = objectMapper.readValue(messageBody, OrderMessageDTO.class);
            log.info("orderMessageDTO:{}",orderMessageDTO);
            //订单发送来消息 需要给他分配一个骑手 查询出空闲骑手
            List<DeliverymanPO> deliverymanPOS = deliverymanDao.selectAvaliableDeliveryman(DeliverymanStatus.AVALIABLE);
            //获取第一个骑手设置到消息中并且发送给订单
            orderMessageDTO.setDeliverymanId(deliverymanPOS.get(0).getId());
            try(Connection connection = connectionFactory.newConnection();
                Channel channel = connection.createChannel()) {
                String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
                channel.basicPublish("exchange.order.deliveryman", "key.order", null, messageToSend.getBytes());

            }catch (Exception e){
                log.error(e.getMessage(),e);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    };

    @Async
    public void handleMessage() throws IOException, TimeoutException, InterruptedException {
        log.info("start linstening message");
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        connectionFactory.setHost("localhost");
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(
                    "exchange.order.deliveryman",
                    BuiltinExchangeType.DIRECT,
                    true,
                    false,
                    null);

            channel.queueDeclare(
                    "queue.deliveryman",
                    true,
                    false,
                    false,
                    null);

            channel.queueBind(
                    "queue.deliveryman",
                    "exchange.order.deliveryman",
                    "key.deliveryman");


            channel.basicConsume("queue.deliveryman", true, deliverCallback, consumerTag -> {
            });
            while (true) {
                Thread.sleep(100000);
            }
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        } catch (TimeoutException e) {
            log.error(e.getMessage(),e);
        }
    }


}
