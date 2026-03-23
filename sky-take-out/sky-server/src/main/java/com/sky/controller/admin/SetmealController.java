package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminSetmealController")
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags = "管理端套餐相关接口")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    /**
     * 分页查询
     * @param setmealPageQueryDTO 套餐数据模型
     * @return  Result<PageResult> total,list
     */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(@ModelAttribute SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("分页查询");
        return setmealService.page(setmealPageQueryDTO);
    }
    /**
     * 新增套餐
     * @param setmealDTO
     * @return Result<String>
     */
    @ApiOperation("新增套餐")
    @PostMapping
    public Result<String> save(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐");
        return setmealService.save(setmealDTO);
    }

    /**
     * 批量删除套餐
     * @param ids
     * @return Result<String>
     */
    @ApiOperation("批量删除套餐")
    @DeleteMapping
    public Result<String> deleteSetmeal(@RequestParam List<Long> ids){
        log.info("批量删除套餐");
        return setmealService.deleteSetmeal(ids);
    }

    /**
     * 根据id查询套餐
     * @param id
     * @return Result<DishVO>
     */
    @ApiOperation("根据id查询套餐")
    @GetMapping("/{id}")
    public Result<SetmealVO> getById(@PathVariable Long id){
        log.info("根据id查询套餐");
        return setmealService.getById(id);
    }

    /**
     * 修改套餐
     * @param setmealDTO
     * @return Result<String>
     */
    @ApiOperation("修改套餐")
    @PutMapping
    public Result<String> update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐");
        return setmealService.update(setmealDTO);
    }

    /**
     * 套餐起售、停售
     * @param status
     * @param id
     * @return Result<String>
     */
    @ApiOperation("套餐起售、停售")
    @PostMapping("/status/{status}")
    public Result<String> startOrStop(@PathVariable Integer status,@RequestParam Long id){
        log.info("套餐起售、停售");
        return setmealService.updateStatus(status,id);
    }
}
