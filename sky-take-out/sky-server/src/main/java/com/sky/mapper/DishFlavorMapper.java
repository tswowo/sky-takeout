package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 根据菜品id查询
     * @param dishId
     * @return List<DishFlavor>
     */
    List<DishFlavor> getByDishId(Long dishId);

    /**
     * 删除菜品关联的口味数据
     * @param dishId
     */
    void deleteByDishId(Long dishId);

    /**
     * 插入菜品关联的口味数据
     * @param dishFlavor
     */
    Integer insertDishFlavor(DishFlavor dishFlavor);

}
