package com.hdu.miaoshaserver.service;

import com.hdu.miaoshaserver.error.BusinessException;
import com.hdu.miaoshaserver.service.model.OrderModel;

public interface OrderService {

    OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BusinessException;

}
