package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

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

    @Update("UPDATE setmeal SET status = 0 WHERE category_id = #{categoryId}")
    void disableByCategoryId(Long categoryId);

    @Update("<script>" +
            "UPDATE setmeal SET status = 0 WHERE id IN (" +
            "SELECT DISTINCT setmeal_id FROM setmeal_dish WHERE dish_id IN " +
            "<foreach item='id' collection='dishIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>)" +
            "</script>")
    void disableByDishIds(List<Long> dishIds);

    List<SetmealVO> listByCategoryId(Long categoryId);

    List<DishItemVO> getDishBySetmealId(Long id);

    /**
     * 根据条件统计套餐数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
