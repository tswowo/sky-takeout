package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {

    Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO);

    Result<String> save(SetmealDTO setmealDTO);

    Result<String> deleteSetmeal(List<Long> ids);

    Result<SetmealVO> getById(Long id);

    Result<String> update(SetmealDTO setmealDTO);

    Result<String> updateStatus(Integer status, Long id);

    Result<List<SetmealVO>> listByCategoryId(Long categoryId);

    Result<List<DishItemVO>> getDishBySetmealId(Long id);
}
