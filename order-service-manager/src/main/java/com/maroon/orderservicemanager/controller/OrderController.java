package com.maroon.orderservicemanager.controller;

import com.maroon.orderservicemanager.service.OrderService;
import com.maroon.orderservicemanager.vo.OrderCreateVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RestController
@Slf4j
public class OrderController {
    @Autowired
    OrderService orderService;
@PostMapping("/orders")
    public void createOrder(@RequestBody OrderCreateVO orderCreateVO) throws IOException, TimeoutException {
        log.info("createOrder:orderCreateVO:{}", orderCreateVO);
        orderService.createOrder(orderCreateVO);
    }
}
