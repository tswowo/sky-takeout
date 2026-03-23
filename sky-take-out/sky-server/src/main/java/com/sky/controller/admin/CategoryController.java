package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController("adminCategoryController")
@RequestMapping("/admin/category")
@Api(tags = "管理端分类管理")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/page")
    @ApiOperation("分类分页查询")
    public Result<PageResult> pageCategory(@ModelAttribute CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("分类分页查询");
        return categoryService.pageCategory(categoryPageQueryDTO);
    }

    @GetMapping("/list")
    @ApiOperation("根据类型查询分类")
    public Result<List<Category>> listCategory(@RequestParam Integer type){
        log.info("根据类型查询分类");
        return categoryService.listCategory(type);
    }

    @PutMapping
    @ApiOperation("修改分类")
    public Result<String> updateCategory(@RequestBody CategoryDTO categoryDTO){
        log.info("修改分类");
        return categoryService.updateCategory(categoryDTO);
    }

    @PostMapping
    @ApiOperation("新增分类")
    public Result<String> createCategory(@RequestBody CategoryDTO categoryDTO){
        log.info("新增分类");
        return categoryService.createCategory(categoryDTO);
    }

    @PostMapping("/status/{status}")
    @ApiOperation("启用、禁用分类")
    public Result<String> setCategoryStatus(@PathVariable Integer status,@RequestParam Long id){
        log.info("启用、禁用分类");
        return categoryService.setCategoryStatus(status,id);
    }

    @DeleteMapping
    @ApiOperation("根据id删除分类")
    public Result<String> deleteCategory(@RequestParam Long id){
        log.info("根据id删除分类");
        return categoryService.deleteCategory(id);
    }
}
