package com.sky.controller.User;

import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Api("C端-套餐浏览接口")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    public Result<List<SetmealVO>> list(Long categoryId) {
        log.info("根据分类id查询套餐");
        return setmealService.listByCategoryId(categoryId);
    }

    @GetMapping("/dish/{id}")
    @ApiOperation("根据套餐id查询包含的菜品")
    public Result<List<DishItemVO>> getDishesBySetmealId(Long id) {
        log.info("根据套餐id查询包含的菜品");
        return setmealService.getDishBySetmealId(id);
    }
}
