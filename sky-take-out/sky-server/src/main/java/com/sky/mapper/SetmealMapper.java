package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealMapper {

    Page<SetmealVO> page(SetmealPageQueryDTO setmealPageQueryDTO);

    @AutoFill(value = OperationType.INSERT)
    void insert(Setmeal setmeal);

    void deleteSetmeal(List<Long> ids);

    List<String> getImageFileByIds(List<Long> ids);

    SetmealVO getById(Long id);

    @AutoFill(value = OperationType.UPDATE)
    void update(Setmeal setmeal);

    void updateStatus(Setmeal setmeal);
}
