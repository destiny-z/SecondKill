package com.hdu.miaoshaserver.service.impl;

import com.hdu.miaoshaserver.dao.OrderDoMapper;
import com.hdu.miaoshaserver.dao.SequenceDoMapper;
import com.hdu.miaoshaserver.dataobject.OrderDo;
import com.hdu.miaoshaserver.dataobject.SequenceDo;
import com.hdu.miaoshaserver.error.BusinessException;
import com.hdu.miaoshaserver.error.EmBusinessError;
import com.hdu.miaoshaserver.service.ItemService;
import com.hdu.miaoshaserver.service.OrderService;
import com.hdu.miaoshaserver.service.UserService;
import com.hdu.miaoshaserver.service.model.ItemModel;
import com.hdu.miaoshaserver.service.model.OrderModel;
import com.hdu.miaoshaserver.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OderServiceImpl implements OrderService {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDoMapper orderDoMapper;

    @Autowired
    private SequenceDoMapper sequenceDoMapper;

    public static void main(String[] args) {
        LocalDateTime now = LocalDateTime.now();
        System.out.println(now.format(DateTimeFormatter.ISO_DATE).replace("-", ""));

    }

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BusinessException {

        //1.校验下单状态,如下单的商品是否存在,用户是否合法,购买数量是否正确
        //ItemModel itemModel = itemService.getItemById(itemId);
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if (itemModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "商品信息不存在");
        }
//        UserModel userModel = userService.getUserById(userId);
        UserModel userModel = userService.getUserByIdInCache(userId);
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "用户信息不存在");
        }
        if (amount <= 0 || amount > 99) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "数量信息不正确");
        }
        if (promoId != null) {
            //1.校验对应活动是否存在这个使用商品
            if (promoId.intValue() != itemModel.getPromoModel().getId()) {
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动信息不正确");
                //2.校验活动是否正在进行中
            } else if (itemModel.getPromoModel().getStatus().intValue() != 2) {
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动信息未开始");
            }
        }


        //2,落单减库存
        boolean result = itemService.decreaseStock(itemId, amount);
        if (!result) {
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }

        //3.订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        if (promoId != null) {
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        } else {
            orderModel.setItemPrice(itemModel.getPrice());
        }
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(BigDecimal.valueOf(amount)));
        orderModel.setPromoId(promoId);

        //生成交易流水号,订单号
        orderModel.setId(generateOrderNo());
        OrderDo orderDo = convertFromOrderModel(orderModel);
        orderDoMapper.insertSelective(orderDo);

        //加上商品销量
        itemService.increaseSales(itemId, amount);


//        //异步更新库存
        boolean mqResult = itemService.asyncDecreaseStock(itemId, amount);
        if (!mqResult) {
            itemService.increaseSales(itemId, amount);
            throw new BusinessException(EmBusinessError.MQ_SEND_FAIL);
        }
        //4.返回前端
        return orderModel;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    String generateOrderNo() {
        //订单号有16位
        StringBuilder stringBuilder = new StringBuilder(16);
        //前8位为时间信息,年月日
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        stringBuilder.append(nowDate);

        //中间6位为自增序列
        int sequence = 0;
        SequenceDo sequenceDO = sequenceDoMapper.getSequenceByName("order_info");
        sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue() + sequenceDO.getStep());
        sequenceDoMapper.updateByPrimaryKeySelective(sequenceDO);
        String sequenceStr = String.valueOf(sequence);
        for (int i = 0; i < 6 - sequenceStr.length(); i++) {
            stringBuilder.append(0);
        }
        stringBuilder.append(sequenceStr);

        //最后2位为分库分表位,暂时写死
        stringBuilder.append("00");

        return stringBuilder.toString();
    }

    private OrderDo convertFromOrderModel(OrderModel orderModel) {
        if (orderModel == null) {
            return null;
        }
        OrderDo orderDo = new OrderDo();
        BeanUtils.copyProperties(orderModel, orderDo);
        orderDo.setItemPrice(new Double(String.valueOf(orderModel.getItemPrice())));
        orderDo.setOrderPrice(new Double(String.valueOf(orderModel.getOrderPrice())));
        return orderDo;
    }
}
