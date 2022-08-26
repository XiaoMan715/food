package com.maroon.restaurantservicemanager;

import com.maroon.restaurantservicemanager.dao.ProductDao;
import com.maroon.restaurantservicemanager.dao.RestaurantDao;
import com.maroon.restaurantservicemanager.po.ProductPO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class RestaurantServiceManagerApplicationTests {
@Autowired
    RestaurantDao restaurantDao;

@Autowired
    ProductDao productDao;
    @Test
    void contextLoads() {

         ProductPO productPO = productDao.selectProduct(2);
         log.info("productPO:{}",productPO);
        //  RestaurantPO restaurantPO = restaurantDao.selsctRestaurant(2);

    }

}
