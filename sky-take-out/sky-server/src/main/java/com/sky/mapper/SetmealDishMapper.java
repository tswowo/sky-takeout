package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    void insert(SetmealDish setmealDish);

    void deleteBySetmealIds(List<Long> ids);

    Integer getCountByDishId(Long id);

    void deleteBySetmealId(Long id);
}
