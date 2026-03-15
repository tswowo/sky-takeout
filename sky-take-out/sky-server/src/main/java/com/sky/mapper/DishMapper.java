package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.DishPageQueryDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper {
    Page pageQuery(DishPageQueryDTO dishPageQueryDTO);
}
