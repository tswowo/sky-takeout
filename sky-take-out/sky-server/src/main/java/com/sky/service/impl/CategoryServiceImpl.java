package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.BaseException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 分类分页查询
     *
     * @param categoryPageQueryDTO
     * @return PageResult<PageResult>
     */
    @Override
    public Result<PageResult> pageCategory(CategoryPageQueryDTO categoryPageQueryDTO) {
        if (categoryPageQueryDTO.getPage() < 1)
            categoryPageQueryDTO.setPage(1);
        if (categoryPageQueryDTO.getPageSize() < 1)
            categoryPageQueryDTO.setPageSize(10);

        Page<Category> page = categoryMapper.categoryPageQuery(categoryPageQueryDTO);
        return Result.success(new PageResult(page.getTotal(), page.getResult()));
    }

    /**
     * 根据类型查询分类
     *
     * @param type
     * @return Result<List<Category>>
     */
    @Override
    public Result<List<Category>> listCategory(Integer type) {
        if (type != null && type != 1 && type != 2)
            return Result.error("类型查询参数错误");
        return Result.success(categoryMapper.listCategory(type), "");
    }

    /**
     * 修改分类
     *
     * @param categoryDTO
     * @return Result<String>
     */
    @Override
    public Result<String> updateCategory(CategoryDTO categoryDTO) {
        Category category = categoryMapper.getById(categoryDTO.getId());
        if (category == null)
            return Result.error("分类不存在");
        categoryDTO.setType(category.getType());
        BeanUtils.copyProperties(categoryDTO, category);
        categoryMapper.update(category);
        return Result.success("", "");
    }

    /**
     * 新增分类
     *
     * @param categoryDTO
     * @return Result<String>
     */
    @Override
    public Result<String> createCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        if (category.getType() != 1 && category.getType() != 2)
            return Result.error("类型参数错误");
        if (category.getName().isEmpty() || category.getName().length() > 32)
            return Result.error("分类名称长度必须在32位之内");
        if (categoryMapper.getByName(category.getName()) != null)
            return Result.error("该分类已存在");
        category.setStatus(StatusConstant.DISABLE);
        categoryMapper.insert(category);
        return Result.success("", "");
    }

    /**
     * 启用、禁用分类
     *
     * @param status
     * @param id
     * @return Result<String>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> setCategoryStatus(Integer status, Long id) {
        if (status != 0 && status != 1)
            return Result.error("状态参数错误");

        Category category = categoryMapper.getById(id);
        if (category == null)
            return Result.error("分类不存在");

        category.setStatus(status);
        categoryMapper.update(category);

        // 如果是禁用操作（status=0），需要级联禁用
        if (status == 0) {
            if (category.getType() == 1) { // 菜品分类
                //先禁用菜品
                dishMapper.disableByCategoryId(id);
                //再禁用菜品关联的套餐
                List<Long> dishIds = dishMapper.getIdsByCategoryId(id);
                if (!dishIds.isEmpty()) {
                    setmealMapper.disableByDishIds(dishIds);
                }
            } else if (category.getType() == 2) { // 套餐分类
                //禁用套餐
                setmealMapper.disableByCategoryId(id);
            }
        }

        return Result.success("", "");
    }


    /**
     * 删除分类
     *
     * @param id
     * @return Result<String>
     */
    @Override
    public Result<String> deleteCategory(Long id) {
        Category category = categoryMapper.getById(id);
        if (category == null)
            throw new BaseException("分类不存在");
        if (Objects.equals(category.getStatus(), StatusConstant.ENABLE))
            throw new BaseException(MessageConstant.CATEGORY_BE_USED);
        boolean hasDish = categoryMapper.countDishByCategoryId(id) > 0;
        boolean hasSetmeal = categoryMapper.countSetmealByCategoryId(id) > 0;
        if (hasDish)
            throw new BaseException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        if (hasSetmeal)
            throw new BaseException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        categoryMapper.deleteById(id);
        return Result.success("", "");
    }
}
