package com.maroon.deliverymanservicemanager;

import com.maroon.deliverymanservicemanager.dao.DeliverymanDao;
import com.maroon.deliverymanservicemanager.enummeration.DeliverymanStatus;
import com.maroon.deliverymanservicemanager.po.DeliverymanPO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@Slf4j
class DeliverymanServiceManagerApplicationTests {
    @Autowired
    DeliverymanDao deliverymanDao;

    @Test
    void contextLoads() {
        List<DeliverymanPO> deliverymanPOS = deliverymanDao.selectAvaliableDeliveryman(DeliverymanStatus.AVALIABLE);
        log.info("deliverymanPOS:{}",deliverymanPOS);

        log.info("id:{}",deliverymanPOS.get(0).getId());

    }

}
