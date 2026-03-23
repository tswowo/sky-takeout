package com.sky.controller.admin;

import com.sky.constant.StatusConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "管理端店铺相关接口")
@Slf4j
public class ShopController {
    @Autowired
    private ShopService shopService;

    /**
     * 获取营业状态
     * @return Result<Integer> 1营业 2打烊
     */
    @GetMapping("/status")
    @ApiOperation("获取营业状态")
    public Result<Integer> getShopStatus() {
        log.info("获取营业状态");
        return shopService.getShopStatus();
    }

    /**
     * 设置营业状态
     * @param status 1营业 2打烊
     * @return Result<String>
     */
    @PutMapping("/{status}")
    @ApiOperation("设置营业状态")
    public Result<String> setShopStatus(@PathVariable Integer status) {
        if(status == null)
            throw new BaseException("状态不能为空");
        if(!status.equals(StatusConstant.ENABLE) && !status.equals(StatusConstant.DISABLE))
            throw new BaseException("状态值错误");
        log.info("设置营业状态:{}",status.equals(StatusConstant.ENABLE)?"营业中":"打烊中");
        return shopService.setShopStatus(status);
    }
}
