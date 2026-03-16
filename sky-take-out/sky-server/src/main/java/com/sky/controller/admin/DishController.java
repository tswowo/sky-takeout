package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品管理系统")
public class DishController {
    @Autowired
    private DishService dishService;

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return Result<PageResult>
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> pageDish(@ModelAttribute DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询");
        return dishService.pageDish(dishPageQueryDTO);
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return Result<List<DishVO>>
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> pageList(@RequestParam Integer categoryId){
        log.info("根据分类id查询菜品");
        return dishService.listDish(categoryId);
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return Result<DishVO>
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> queryDishById(@PathVariable Long id){
        log.info("根据id查询菜品");
        return dishService.getDishById(id);
    }

    /**
     * 新增菜品
     * @param dishDTO
     * @return Result<String>
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result<String> saveDish(@RequestBody DishDTO dishDTO){
        log.info("新增菜品:{}",dishDTO);
        return dishService.saveDish(dishDTO);
    }

    /**
     * 删除菜品
     * @param ids
     * @return Result<String>
     */
    @ApiOperation("批量删除菜品")
    @DeleteMapping
    public Result<String> deleteDishById(@RequestParam List<Long>ids){
        log.info("批量删除菜品");
        return dishService.deleteDishById(ids);
    }

    /**
     * 菜品起售、停售
     * @param status
     * @param id
     * @return Result<String>
     */
    @ApiOperation("菜品起售、停售")
    @PostMapping("/status/{status}")
    public Result<String> updateDishStatus(@RequestParam Long id,@PathVariable Integer status){
        log.info("菜品起售、停售");
        return dishService.updateDishStatus(status,id);
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return Result<String>
     */
    @ApiOperation("修改菜品")
    @PutMapping
    public Result<String> updateDish(@RequestBody DishDTO dishDTO){
        log.info("修改菜品:{}",dishDTO);
        return dishService.updateDish(dishDTO);
    }
}
