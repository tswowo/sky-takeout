package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.result.Result;
import com.sky.service.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ShopServiceImpl implements ShopService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取店铺营业状态
     * @return 营业状态 1营业 0打烊
     */
    @Override
    public Result<Integer> getShopStatus() {
        //Redis常量 KEY
        final String SHOP_STATUS_KEY = "SHOP_STATUS";

        Integer status = (Integer) redisTemplate.opsForValue().get(SHOP_STATUS_KEY);

        //不存在则设置默认值
        if (status == null) {
            status = StatusConstant.DISABLE;
            redisTemplate.opsForValue().set(SHOP_STATUS_KEY, status);
            log.info("店铺状态未初始化，自动设置为默认状态：打烊中");
        }

        String statusDesc = StatusConstant.ENABLE.equals(status) ? "营业中" : "打烊中";
        log.info("获取店铺营业状态：{}", statusDesc);

        return Result.success(status);
    }

    /**
     * 设置店铺营业状态
     * @param status 营业状态 1营业 0打烊
     */
    @Override
    public Result<String> setShopStatus(Integer status) {
        redisTemplate.opsForValue().set("SHOP_STATUS",status);
        log.info("设置营业状态:{}", status.equals(StatusConstant.ENABLE) ?"营业中":"打烊中");
        return Result.success();
    }
}
