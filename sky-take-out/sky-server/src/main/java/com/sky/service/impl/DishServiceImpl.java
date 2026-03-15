package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return Result<PageResult>
     */
    @Override
    public Result<PageResult> pageDish(DishPageQueryDTO dishPageQueryDTO) {
        if (dishPageQueryDTO.getPage() < 1)
            dishPageQueryDTO.setPage(1);
        if (dishPageQueryDTO.getPageSize() < 1)
            dishPageQueryDTO.setPageSize(10);

        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<Dish> page = dishMapper.pageQuery(dishPageQueryDTO);

        List<DishVO> dishVOList = new ArrayList<>();
        for (Object dish : page.getResult()) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(dish, dishVO);
            dishVOList.add(dishVO);
        }

        PageResult pageResult = new PageResult(page.getTotal(), dishVOList);
        return Result.success(pageResult);
    }
}
