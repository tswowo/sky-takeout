package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;

import java.util.List;

public interface CategoryService {
    Result<PageResult> pageCategory(CategoryPageQueryDTO categoryPageQueryDTO);

    Result<List<Category>> listCategory(Integer type);

    Result<String> updateCategory(CategoryDTO categoryDTO);

    Result<String> createCategory(CategoryDTO categoryDTO);

    Result<String> setCategoryStatus(Integer status, Long id);

    Result<String> deleteCategory(Long id);
}
