package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.ShoppingCart;
import com.sky.exception.BaseException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 查看购物车
     *
     * @return userId的购物车列表
     */
    @Override
    public Result<List<ShoppingCart>> list() {
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.listByUserId(userId);
        return Result.success(shoppingCartList);
    }

    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     */
    @Override
    public Result add(ShoppingCartDTO shoppingCartDTO) {
        if (shoppingCartDTO.getSetmealId() == null && shoppingCartDTO.getDishId() == null)
            throw new BaseException("请选择要添加的菜品或套餐");
        boolean isDish = shoppingCartDTO.getDishId() != null;
        //查找是否已经存在
        Long userId = BaseContext.getCurrentId();
        ShoppingCart findShoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, findShoppingCart);
        findShoppingCart.setUserId(userId);
        ShoppingCart existShoppingCart = shoppingCartMapper.getByUserIdAndDishOrSetmealId(findShoppingCart);
        if (existShoppingCart != null) {//如果已经存在，数量+1
            existShoppingCart.setNumber(existShoppingCart.getNumber() + 1);//加数量
            shoppingCartMapper.updateNumber(existShoppingCart);
        } else {//否则插入
            ShoppingCart shoppingCart = buildNewShoppingCart(shoppingCartDTO);
            shoppingCartMapper.insert(shoppingCart);
        }
        return Result.success();
    }

    /**
     * 删除购物车
     *
     * @param shoppingCartDTO
     */
    @Override
    public Result sub(ShoppingCartDTO shoppingCartDTO) {
        //构造查找对象，查找购物车内该id的信息
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(userId);
        ShoppingCart existShoppingCart = shoppingCartMapper.getByUserIdAndDishOrSetmealId(shoppingCart);

        if (existShoppingCart == null) {
            log.error("购物车中没有此商品");
            throw new BaseException("购物车中没有此商品");
        }

        if (existShoppingCart.getNumber() == 1) {//如果删光了，那就从表内删除此行
            shoppingCartMapper.deleteByShoppingCart(existShoppingCart);
        } else {//否则删除一个即可
            existShoppingCart.setNumber(existShoppingCart.getNumber() - 1);
            shoppingCartMapper.updateNumber(existShoppingCart);
        }
        return null;
    }

    /**
     * 清空购物车
     */
    @Override
    public Result clean() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
        return Result.success();
    }

    /**
     * 添加购物车的工具方法，填充新购物车对象
     *
     * @param dto
     * @return 新购物车对象
     */
    private ShoppingCart buildNewShoppingCart(ShoppingCartDTO dto) {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart cart = new ShoppingCart();
        cart.setUserId(userId);
        cart.setNumber(1);

        boolean isDish = dto.getDishId() != null;
        if (isDish) {
            Dish dish = dishMapper.getDishById(dto.getDishId());
            BeanUtils.copyProperties(dish, cart);
            cart.setDishId(dish.getId());
            cart.setAmount(dish.getPrice());
        } else {
            SetmealVO setmeal = setmealMapper.getById(dto.getSetmealId());
            BeanUtils.copyProperties(setmeal, cart);
            cart.setSetmealId(setmeal.getId());
            cart.setAmount(setmeal.getPrice());
        }
        return cart;
    }

}
