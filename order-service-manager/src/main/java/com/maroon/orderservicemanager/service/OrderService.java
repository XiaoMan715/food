package com.maroon.orderservicemanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maroon.orderservicemanager.dao.OrderDetailDao;
import com.maroon.orderservicemanager.dto.OrderMessageDTO;
import com.maroon.orderservicemanager.enumeration.OrderStatus;
import com.maroon.orderservicemanager.po.OrderDetailPO;
import com.maroon.orderservicemanager.vo.OrderCreateVO;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

/**
 * 做用户相关的业务逻辑
 */
@Service
@Slf4j
public class OrderService {
    @Autowired
    OrderDetailDao orderDetailDao;

    //用这个方法序列化传输对象
    ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建订单
     *
     * @param orderCreateVO 前端传入vo
     */
    public void createOrder(OrderCreateVO orderCreateVO) throws IOException, TimeoutException, InterruptedException {
        //将前端传入的vo赋值给数据库映射类po
        OrderDetailPO orderDetailPO = new OrderDetailPO();
        //设置用户id
        orderDetailPO.setAccountId(orderCreateVO.getAccountId());
        //设置地址
        orderDetailPO.setAddress(orderCreateVO.getAddress());
        //设置产品id
        orderDetailPO.setProductId(orderCreateVO.getProductId());
        //设置时间
        orderDetailPO.setDate(new Date());
        //设置订单状态
        orderDetailPO.setStatus(OrderStatus.ORDER_CREATING);
        log.info("orderDetailPO:{}", orderDetailPO);
        orderDetailDao.insert(orderDetailPO);

        //订单创建成功后要给餐厅发送一条信息
        OrderMessageDTO orderMessageDTO = new OrderMessageDTO();
        //订单id 在上面生成订单之后存库mybatis会自动生成id并设置到po里面 用的就是@Options(useGeneratedKeys = true, keyProperty = "id")
        orderMessageDTO.setOrderId(orderDetailPO.getId());
        orderMessageDTO.setProductId(orderDetailPO.getProductId());
        orderMessageDTO.setAccountId(orderCreateVO.getAccountId());

        //发送信息 创建连接
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
            //发送的对象转换成为字符串
            String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
            //开启发送端确认模式
            channel.confirmSelect();
            //异步消息确认机制 实现一个ConfirmListener接口
            ConfirmListener confirmListener = new ConfirmListener() {
                /**
                 *如果rabbitMQ 确认成功调用handleAck()方法 确认失败调用handleNack()方法
                 * @param deliveryTag 发送端发送多条消息的编号
                 * @param multiple  RabbitMQ再确定多条还是单条消息 如果返回true是多条 false则是单条 注意 这里的false和true并不是代表消息确认失败 而是代表确认单条还是多条
                 * @throws IOException
                 */
                @Override
                public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                    log.info("Ack  deliveryTag:{}, multiple:{} ", deliveryTag, multiple);
                }

                @Override
                public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                    log.info("Nack  deliveryTag:{}, multiple:{} ", deliveryTag, multiple);
                }
            };
            //把异步方法添加到channel里
            channel.addConfirmListener(confirmListener);

            for (int i = 0; i < 50; i++) {
                channel.basicPublish(
                        "exchange.order.restaurant",
                        "key.restaurant",
                        null,
                        messageToSend.getBytes());
            }

            log.info("send messgae");
            Thread.sleep(100000);

           /* channel.basicPublish(
                    "exchange.order.restaurant",
                    "key.restaurant",
                    null,
                    messageToSend.getBytes());*/
/*//多条同步消息确认 一次性先发送多条消息 然后再确认
            for (int i = 0; i < 10; i++) {
                channel.basicPublish(
                        "exchange.order.restaurant",
                        "key.restaurant",
                        null,
                        messageToSend.getBytes());
            }
            log.info("message");
            //发送消息后调用waitForConfirms()方法 会返回true或者false 然后就可以根据状态进行业务操作 重复或者之间入库的
            if (channel.waitForConfirms()) {
                log.info("发送成功");
            }else {
                log.info("发送失败");
            }*/


        }
    }

}
