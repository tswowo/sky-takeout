package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 根据用户id查询
     *
     * @param userId
     * @return List<ShoppingCart> 购物列表
     */
    List<ShoppingCart> listByUserId(Long userId);

    /**
     * 根据用户id和菜品或套餐id查询
     *
     * @param shoppingCart
     * @return ShoppingCart
     */
    ShoppingCart getByUserIdAndDishOrSetmealId(ShoppingCart shoppingCart);

    /**
     * 更新购物车内菜品和套餐状态
     *
     * @param shoppingCart
     * @return 成功的行数
     */
    int updateNumber(ShoppingCart shoppingCart);

    /**
     * 插入新商品到购物车
     *
     * @param shoppingCart
     */
    void insert(ShoppingCart shoppingCart);

    /**
     * 删除购物车商品
     * @param existShoppingCart
     */
    void deleteByShoppingCart(ShoppingCart existShoppingCart);

    /**
     * 清空用户的购物车
     * @param userId
     */
    void deleteByUserId(Long userId);

    /**
     * 批量插入到购物车
     * @param shoppingCartList
     */
    void insertBatch(List<ShoppingCart> shoppingCartList);
}
