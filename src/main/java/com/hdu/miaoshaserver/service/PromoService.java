package com.hdu.miaoshaserver.service;

import com.hdu.miaoshaserver.service.model.PromoModel;

public interface PromoService {

    //根据item获取即将进行或正在进行的秒杀活动
    PromoModel getPromoByItemId(Integer itemId);

    //活动发布
    void publishPromo(Integer promoId);

}
