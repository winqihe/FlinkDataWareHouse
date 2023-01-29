package com.keven.mall.realtime.bean;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author KevenHe
 * @create 2022/1/21 17:20
 */
@Data
public class OrderInfo {
    Long id;
    Long province_id;
    String order_status;
    Long user_id;
    BigDecimal total_amount;
    BigDecimal activity_reduce_amount;
    BigDecimal coupon_reduce_amount;
    BigDecimal original_total_amount;
    BigDecimal feight_fee;
    String expire_time;
    String create_time;  //yyyy-MM-dd HH:mm:ss
    String operate_time;
    String create_date; // �������ֶδ���õ�
    String create_hour;
    Long create_ts;
}