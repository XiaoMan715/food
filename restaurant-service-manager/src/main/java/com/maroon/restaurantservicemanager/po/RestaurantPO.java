package com.maroon.restaurantservicemanager.po;


import com.maroon.restaurantservicemanager.enumeration.RestaurantStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class RestaurantPO {
    private Integer id;
    private String name;
    private String address;
    private RestaurantStatus status;
    private Date date;
}
