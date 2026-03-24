package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    Result<PageResult> pageDish(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return Result<DishVO>
     */
    Result<List<DishVO>> listDish(Long categoryId);

    /**
     * 根据id查询菜品
     *
     * @param id
     * @return Result<DishVO>
     */
    Result<DishVO> getDishById(Long id);

    /**
     * 新增菜品
     *
     * @param dishDTO
     * @return Result<String>
     */
    Result<String> saveDish(DishDTO dishDTO);

    /**
     * 删除菜品
     *
     * @param idList
     * @return Result<String>
     */
    Result<String> deleteDishById(List<Long> idList);

    /**
     * 修改菜品起售、停售状态
     *
     * @param status
     * @param id
     * @return Result<String>
     */
    Result<String> updateDishStatus(Integer status, Long id);

    /**
     * 修改菜品
     *
     * @param dishDTO
     * @return Result<String>
     */
    Result<String> updateDish(DishDTO dishDTO);

    /**
     * 根据分类id查询菜品详细信息列表
     *
     * @param categoryId
     * @return Result<List<DishVO>>
     */
    Result<List<DishVO>> listDishByCategoryId(Long categoryId);
}
