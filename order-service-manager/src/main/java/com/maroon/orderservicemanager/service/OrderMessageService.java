package com.maroon.orderservicemanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maroon.orderservicemanager.dao.OrderDetailDao;
import com.maroon.orderservicemanager.dto.OrderMessageDTO;
import com.maroon.orderservicemanager.enumeration.OrderStatus;
import com.maroon.orderservicemanager.po.OrderDetailPO;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 做和消息相关的业务逻辑
 */
@Service
@Slf4j
public class OrderMessageService {

    @Autowired
    OrderDetailDao orderDetailDao;

    ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 订单-餐厅交换机名称
     */
    private final static String Ex_ORDER_RENT = "exchange.order.restaurant";
    /**
     * exchange.order.restaurant
     * 骑手-餐厅交换机名称
     */
    private final static String Ex_ORDER_DEMAN = "exchange.order.deliveryman";
    /**
     * 结算-订单交换机名称
     */
    private final static String Ex_SEMENT_ORDER = "exchange.settlement.order";
    /**
     * 积分-订单交换机名称
     */
    private final static String Ex_ORDER_REWARD = "exchange.order.reward";
    /**
     * 订单-队列
     */
    private final static String QUEUE_NAME = "queue.order";
    /**
     * 路由模式
     */
    private final static String ROUTING_KEY = "key.order";

    /**
     * Async 我们需要一个异步线程去调用当前这个方法
     * 不能用主线程去调用 所以我们需要设置一个异步线程池
     * <p>
     * 消息 交换机 队列 的绑定
     */
    @Async
    public void handleMessage() {
        //定义一个连接工厂
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("127.0.0.1");
        //采用try-with-resources，这家伙可以在资源使用完后实现自动关闭回收。
        // 想想我们之前打开一个文件或流对象用完咋整的，是不是finally语句块中手动close的。
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()
        ) {
            //------餐厅需要的交换机和队列申明和绑定------/
            // 创建 餐厅 一个交换机 指定工作类型为
            channel.exchangeDeclare(Ex_ORDER_RENT,
                    BuiltinExchangeType.DIRECT, true, false, null);
            //创建一个队列
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            //绑定交换机和队列 路由模式key.order
            channel.queueBind(QUEUE_NAME, Ex_ORDER_RENT, ROUTING_KEY);

            //------骑手需要的交换机和队列申明和绑定------/
            channel.exchangeDeclare(
                    Ex_ORDER_DEMAN,
                    BuiltinExchangeType.DIRECT,
                    true,
                    false,
                    null);


            channel.queueBind(
                    QUEUE_NAME,
                    Ex_ORDER_DEMAN,
                    ROUTING_KEY);

            /*---------------------settlement 结算微服务需要的交换机和队列申明和绑定---------------------*/

            channel.exchangeDeclare(
                    Ex_SEMENT_ORDER,
                    BuiltinExchangeType.FANOUT,
                    true,
                    false,
                    null);

            channel.queueBind(
                    QUEUE_NAME,
                    Ex_SEMENT_ORDER,
                    ROUTING_KEY);

            /*---------------------reward 积分微服务需要的交换机和队列申明和绑定---------------------*/

            channel.exchangeDeclare(
                    Ex_ORDER_REWARD,
                    BuiltinExchangeType.TOPIC,
                    true,
                    false,
                    null);

            channel.queueBind(
                    QUEUE_NAME,
                    Ex_ORDER_REWARD,
                    ROUTING_KEY);
            /*---------------------------settlement 绑定结算微服务--------------------------------------------------*/
            channel.exchangeDeclare(
                    "exchange.settlement.order",
                    BuiltinExchangeType.FANOUT,
                    true,
                    false,
                    null);

            channel.queueBind(
                    "queue.order",
                    "exchange.settlement.order",
                    "key.order");

            /*---------------------reward-结算微服务--------------------*/

            channel.exchangeDeclare(
                    "exchange.order.reward",
                    BuiltinExchangeType.TOPIC,
                    true,
                    false,
                    null);

            channel.queueBind(
                    "queue.order",
                    "exchange.order.reward",
                    "key.order");


            /**
             * 监听队列收到消息后回调deliverCallback方法进行业务逻辑操作
             * 消费者相当于一个监听 不用关闭资源
             */
            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
            });
            while (true) {
                Thread.sleep(100000);
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
    /*  接收发送过来的消息 创建一个回调函数   **/

    DeliverCallback deliverCallback = ((consumerTag, message) -> {
        //获取发送过来的消息Json序列化后的字符串
        String messageBody = new String(message.getBody());
        log.info("deliverCallback:messageBody:{}", messageBody);
        OrderMessageDTO orderMessageDTO = null;
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");


        try {
            //将发送过来的消息json字符串解析转换成DTO对象
            orderMessageDTO = objectMapper.readValue(messageBody, OrderMessageDTO.class);
            //获取到发送过来的订单id 再去数据库查询
            OrderDetailPO orderDetailPO = orderDetailDao.selectOrder(orderMessageDTO.getOrderId());
            /**
             * 如果其他四个微服务都给订单微服务发送消息 那么我们怎么知道是那个微服务发送的 我们可以要求发送方设定一个字段用来表示
             * 这里我们可以根据订单状态来确定是那个微服务发送的
             */
            switch (orderDetailPO.getStatus()) {
                case ORDER_CREATING:
                    //判断餐厅是否确认订单
                    if (orderMessageDTO.getConfirmed() && null != orderMessageDTO.getPrice()) {
                        orderDetailPO.setStatus(OrderStatus.RESTAURANT_CONFIRMED);
                        orderDetailPO.setPrice(orderMessageDTO.getPrice());
                        orderDetailDao.update(orderDetailPO);
                        //发送消息给骑手微服务
                        try (Connection connection = connectionFactory.newConnection();
                             Channel channel = connection.createChannel()) {
                            String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
                            channel.basicPublish("exchange.order.deliveryman", "key.deliveryman", null,
                                    messageToSend.getBytes());
                        }
                    } else {
                        //如果餐厅没有确认 设置订单失败
                        orderDetailPO.setStatus(OrderStatus.FAILED);
                        orderDetailDao.update(orderDetailPO);
                    }
                    break;
                case RESTAURANT_CONFIRMED:
                    //骑手确定了订单 这么更新订单状态 先判断返回的消息体中是否有骑手id
                    if (null != orderMessageDTO.getDeliverymanId()) {
                        //更新订单状态
                        orderDetailPO.setStatus(OrderStatus.DELVERTMAN_CONFIRMED);
                        //更新骑手id到数据库
                        orderDetailPO.setDeliverymanId(orderMessageDTO.getDeliverymanId());
                        orderDetailDao.update(orderDetailPO);
                        //将订单发送给结算微服务
                        try (Connection connection = connectionFactory.newConnection();
                             Channel channel = connection.createChannel()
                        ) {
                            String messageToSend = new String(objectMapper.writeValueAsBytes(orderMessageDTO));
                            channel.basicPublish("exchange.order.settlement", "key.settlement", null, messageToSend.getBytes());

                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    } else {
                        orderDetailPO.setStatus(OrderStatus.FAILED);
                        orderDetailDao.update(orderDetailPO);
                    }
                    break;
                case DELVERTMAN_CONFIRMED:
                    if (null != orderMessageDTO.getSettlementId()) {
                        orderDetailPO.setStatus(OrderStatus.SETTLEMENT_CONFIRMED);
                        orderDetailPO.setSettlementId(orderMessageDTO.getSettlementId());
                        orderDetailDao.update(orderDetailPO);
                        try (Connection connection = connectionFactory.newConnection();
                             Channel channel = connection.createChannel()) {
                            String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
                            channel.basicPublish("exchange.order.reward", "key.reward", null, messageToSend.getBytes());
                        }
                    } else {
                        orderDetailPO.setStatus(OrderStatus.FAILED);
                        orderDetailDao.update(orderDetailPO);
                    }
                    break;
                case SETTLEMENT_CONFIRMED:
                    if (null != orderMessageDTO.getRewardId()) {
                        orderDetailPO.setStatus(OrderStatus.ORDER_CREATED);
                        orderDetailPO.setRewardId(orderMessageDTO.getRewardId());
                        orderDetailDao.update(orderDetailPO);
                    } else {
                        orderDetailPO.setStatus(OrderStatus.FAILED);
                        orderDetailDao.update(orderDetailPO);
                    }
                    break;
                case FAILED:
                    break;
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }


    });
}
