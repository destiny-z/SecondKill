package com.hdu.miaoshaserver.service.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderModel {

    //2020022100012828
    private String id;

    //购买的用户id
    private Integer userId;

    //若非空,则表示是以秒杀商品方式下单
    private Integer promoId;

    //购买商品的单价,随时都会改变,因此需要加入这个冗余的字段
    //若非空,则表示秒杀商品价格
    private BigDecimal itemPrice;

    //购买的商品id
    private Integer itemId;

    //购买数量
    private Integer amount;

    //购买金额,若非空,则表示秒杀商品价格
    private BigDecimal orderPrice;

}
