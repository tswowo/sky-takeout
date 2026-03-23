package com.sky.controller.User;

import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Api(tags = "用户端店铺相关接口")
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

}
