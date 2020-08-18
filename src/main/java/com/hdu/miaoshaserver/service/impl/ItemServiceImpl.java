package com.hdu.miaoshaserver.service.impl;

import com.hdu.miaoshaserver.dao.ItemDoMapper;
import com.hdu.miaoshaserver.dao.ItemStockDoMapper;
import com.hdu.miaoshaserver.dataobject.ItemDo;
import com.hdu.miaoshaserver.dataobject.ItemStockDo;
import com.hdu.miaoshaserver.error.BusinessException;
import com.hdu.miaoshaserver.error.EmBusinessError;
import com.hdu.miaoshaserver.mq.MqProcucer;
import com.hdu.miaoshaserver.service.ItemService;
import com.hdu.miaoshaserver.service.PromoService;
import com.hdu.miaoshaserver.service.model.ItemModel;
import com.hdu.miaoshaserver.service.model.PromoModel;
import com.hdu.miaoshaserver.validator.ValidationResult;
import com.hdu.miaoshaserver.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ValidatorImpl validator;
    @Autowired
    private ItemDoMapper itemDoMapper;
    @Autowired
    private ItemStockDoMapper itemStockDoMapper;
    @Autowired
    private PromoService promoService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MqProcucer mqProducer;


    private ItemDo convertItemDOFromItemModel(ItemModel itemModel) {
        if (itemModel == null) return null;
        ItemDo itemDo = new ItemDo();
        BeanUtils.copyProperties(itemModel, itemDo);
        itemDo.setPrice(itemModel.getPrice().doubleValue());
        return itemDo;
    }

    private ItemStockDo convertItemStockDOFromItemModel(ItemModel itemModel) {
        if (itemModel == null) return null;

        ItemStockDo itemStockDo = new ItemStockDo();
        itemStockDo.setItemId(itemModel.getId());
        itemStockDo.setStock(itemModel.getStock());
        return itemStockDo;
    }

    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        //校验入参
        ValidationResult result = validator.validate(itemModel);
        if (result.isHasErrors()) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        }
        //转化itemmodel -》 dataobject
        ItemDo itemDo = this.convertItemDOFromItemModel(itemModel);

        //写入数据库
        itemDoMapper.insertSelective(itemDo);
        itemModel.setId(itemDo.getId());

        ItemStockDo itemStockDo = this.convertItemStockDOFromItemModel(itemModel);
        itemStockDoMapper.insertSelective(itemStockDo);


        //返回创建完成的对象

        return getItemById(itemModel.getId());
    }

    @Override
    public List<ItemModel> listItem() {

        List<ItemDo> itemDosList = itemDoMapper.listItem();
        List<ItemModel> itemModelList = itemDosList.stream().map(itemDo -> {
            ItemStockDo itemStockDo = itemStockDoMapper.selectByItemId(itemDo.getId());
            ItemModel itemModel = convertModelFromDataObject(itemDo, itemStockDo);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }

    @Override
    public ItemModel getItemByIdInCache(Integer id) {
        ItemModel itemModel = (ItemModel) redisTemplate.opsForValue().get("item_validate_" + id);
        if (itemModel == null) {
            itemModel = this.getItemById(id);
            redisTemplate.opsForValue().set("item_validate_" + id, itemModel);
            redisTemplate.expire("item_validate_" + id, 10, TimeUnit.MINUTES);
        }
        return itemModel;
    }

    @Override
    public ItemModel getItemById(Integer id) {
        ItemDo itemDo = itemDoMapper.selectByPrimaryKey(id);
        if (itemDo == null) return null;

        //操作获得库存数量
        ItemStockDo itemStockDo = itemStockDoMapper.selectByItemId(itemDo.getId());

        //将dataobject -> model
        ItemModel itemModel = convertModelFromDataObject(itemDo, itemStockDo);

        //获取活动商品信息
        PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());
        if (promoModel != null && promoModel.getStatus() != 3) {
            itemModel.setPromoModel(promoModel);
        }
        return itemModel;
    }

    @Transactional
    @Override
    public boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException {
//        int affectedRow = itemStockDoMapper.decreaseStock(itemId, amount);
//        更新库存成功
//        更新库存失败
//        return affectedRow > 0;
        Long result = redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount.intValue() * -1);
        if (result >= 0) {
//            boolean mqResult = mqProducer.asyncReduceStock(itemId, amount);
//            if (!mqResult){
//                redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount.intValue());
//                return false;
//            }
            return true;
        } else {
            increaseStock(itemId, amount);
            return false;
        }
    }

    @Override
    public boolean increaseStock(Integer itemId, Integer amount) throws BusinessException {
        redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount.intValue());
        return true;
    }

    @Override
    public boolean asyncDecreaseStock(Integer itemId, Integer amount) {
        boolean mqResult = mqProducer.asyncReduceStock(itemId, amount);
        return mqResult;
    }

    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) throws BusinessException {
        itemDoMapper.increaseSales(itemId, amount);
    }

    private ItemModel convertModelFromDataObject(ItemDo itemDo, ItemStockDo itemStockDo) {
        ItemModel itemModel = new ItemModel();
        if (itemDo == null) return null;
        BeanUtils.copyProperties(itemDo, itemModel);
        itemModel.setPrice(BigDecimal.valueOf(itemDo.getPrice()));
        itemModel.setStock(itemStockDo.getStock());
        return itemModel;
    }
}
