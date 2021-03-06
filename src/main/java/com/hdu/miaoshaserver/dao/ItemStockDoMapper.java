package com.hdu.miaoshaserver.dao;

import com.hdu.miaoshaserver.dataobject.ItemStockDo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

@Component(value = "ItemStockDoMapper")
public interface ItemStockDoMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Sun Mar 01 11:10:22 CST 2020
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Sun Mar 01 11:10:22 CST 2020
     */
    int insert(ItemStockDo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Sun Mar 01 11:10:22 CST 2020
     */
    int insertSelective(ItemStockDo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Sun Mar 01 11:10:22 CST 2020
     */
    ItemStockDo selectByPrimaryKey(Integer id);

    ItemStockDo selectByItemId(Integer itemId);

    int decreaseStock(@Param("itemId") Integer itemId, @Param("amount") Integer amount);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Sun Mar 01 11:10:22 CST 2020
     */
    int updateByPrimaryKeySelective(ItemStockDo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Sun Mar 01 11:10:22 CST 2020
     */
    int updateByPrimaryKey(ItemStockDo record);
}