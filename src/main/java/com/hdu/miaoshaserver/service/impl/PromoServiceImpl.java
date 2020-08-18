package com.hdu.miaoshaserver.service.impl;

import com.hdu.miaoshaserver.dao.PromoDoMapper;
import com.hdu.miaoshaserver.dataobject.PromoDo;
import com.hdu.miaoshaserver.service.ItemService;
import com.hdu.miaoshaserver.service.PromoService;
import com.hdu.miaoshaserver.service.model.ItemModel;
import com.hdu.miaoshaserver.service.model.PromoModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoDoMapper promoDoMapper;

    @Autowired
    private ItemService itemService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        //获取对应商品的秒杀活动信息
        PromoDo promoDo = promoDoMapper.selectByItemId(itemId);

        //dataobject -> model
        PromoModel promoModel = convertFromDataObject(promoDo);
        if (promoModel == null) {
            return null;
        }

        //判断当前时间是否秒杀活动即将开始或正在进行
        if (promoModel.getStartTime().isAfterNow()) {
            promoModel.setStatus(1);
        } else if (promoModel.getEndTime().isBeforeNow()) {
            promoModel.setStatus(3);
        } else {
            promoModel.setStatus(2);
        }

        return promoModel;
    }

    //活动发布
    @Override
    public void publishPromo(Integer promoId) {
        PromoDo promoDo = promoDoMapper.selectByPrimaryKey(promoId);
        if (promoDo.getItemId() == null || promoDo.getItemId().intValue() == 0) {
            return;
        }
        ItemModel itemModel = itemService.getItemById(promoDo.getItemId());

        //将库存同步到redis内
        redisTemplate.opsForValue().set("promo_item_stock_" + itemModel.getId(), itemModel.getStock());
    }

    private PromoModel convertFromDataObject(PromoDo promoDo) {
        if (promoDo == null) {
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDo, promoModel);
        promoModel.setPromoItemPrice(BigDecimal.valueOf(promoDo.getPromoItemPrice()));
        promoModel.setStartTime(new DateTime(promoDo.getStartDate()));
        promoModel.setEndTime(new DateTime(promoDo.getEndDate()));

        return promoModel;
    }

}
