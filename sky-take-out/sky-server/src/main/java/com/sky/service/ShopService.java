package com.sky.service;

import com.sky.result.Result;

public interface ShopService {
    Result<Integer> getShopStatus();

    Result<String> setShopStatus(Integer status);
}
