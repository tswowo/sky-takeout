package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    List<DishVO> listDish(Long categoryId);

    DishVO getDishVOById(Long id);

    @AutoFill(value = OperationType.INSERT)
    Long insertDish(Dish dish);

    void deleteDishById(Long id);

    @AutoFill(value = OperationType.UPDATE)
    void updateDishStatus(Dish dish);

    Dish getDishById(Long id);

    @AutoFill(value = OperationType.UPDATE)
    void updateDish(Dish dish);

    int getCountDishByDishIdInSetmeal(Long id);

    @Update("UPDATE dish SET status = 0 WHERE category_id = #{categoryId}")
    void disableByCategoryId(Long categoryId);

    @Select("SELECT id FROM dish WHERE category_id = #{categoryId}")
    List<Long> getIdsByCategoryId(Long categoryId);

    List<DishVO> listDishWithFlavors(Long categoryId);

    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);

}
