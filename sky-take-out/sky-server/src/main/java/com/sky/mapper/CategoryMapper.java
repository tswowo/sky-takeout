package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return Page<Category>
     */
    Page<Category> categoryPageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 根据类型查询分类
     * @param type
     * @return List<Category>
     */
    List<Category> listCategory(Integer type);

    /**
     *根据id查询分类
     */
    @Select("select * from category where id = #{id}")
    Category getById(Long id);

    /**
     * 修改分类
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Category category);

    /**
     * 根据名称查询分类
     */
    @Select("select * from category where name = #{name}")
    Category getByName(String name);

    /**
     * 新增分类
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Category category);

    /** 查询该分类下是否有菜品*/
    int countDishByCategoryId(Long categoryId);

    /** 查询该分类下是否有套餐*/
    int countSetmealByCategoryId(Long categoryId);

    /**根据id删除分类*/
    void deleteById(Long id);
}
