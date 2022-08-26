package com.maroon.restaurantservicemanager.dao;

import com.maroon.restaurantservicemanager.enumeration.ProductStatus;
import com.maroon.restaurantservicemanager.po.ProductPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface ProductDao {
   // @Results({ @Result(property = "status", column = "status",javaType = ProductStatus.class, jdbcType = JdbcType.VARCHAR,typeHandler = EnumOrdinalTypeHandler.class) })
@Select("SELECT id,name,price,restaurant_id restaurantId,status,date FROM product WHERE id=#{id}")
    ProductPO selectProduct(Integer id);
}
