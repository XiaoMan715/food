package com.maroon.settlementservicemanager.dao;

import com.maroon.settlementservicemanager.po.SettlementPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface SettlementDao {

    @Insert("INSERT INTO settlement (order_id, transaction_id, amount, status, date) " +
            "VALUES(#{orderId}, #{transactionId}, #{amount}, #{status}, #{date})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(SettlementPO settlementPO);
}
