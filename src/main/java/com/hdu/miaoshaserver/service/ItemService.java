package com.hdu.miaoshaserver.service;

import com.hdu.miaoshaserver.error.BusinessException;
import com.hdu.miaoshaserver.service.model.ItemModel;

import java.util.List;

public interface ItemService {

    //创建商品
    ItemModel createItem(ItemModel itemModel) throws BusinessException;

    //商品列表浏览
    List<ItemModel> listItem();

    //item与promo model缓存模型
    ItemModel getItemByIdInCache(Integer id);

    //商品详情浏览
    ItemModel getItemById(Integer id);

    //库存扣减
    boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException;

    //库存回补
    boolean increaseStock(Integer itemId, Integer amount) throws BusinessException;

    //异步更新库存
    boolean asyncDecreaseStock(Integer itemId, Integer amount);

    void increaseSales(Integer itemId, Integer amount) throws BusinessException;
}
