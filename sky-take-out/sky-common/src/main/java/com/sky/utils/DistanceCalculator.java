package com.sky.utils;

import com.sky.dto.baidu.Location;
import com.sky.dto.baidu.MapLocationResponse;
import com.sky.exception.BaseException;
import com.sky.properties.BaiduMapProperties;
import com.sky.properties.ShopProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


@Component
@Slf4j
public class DistanceCalculator {

    @Autowired
    private ShopProperties shopProperties;
    @Autowired
    private BaiduMapProperties baiduMapProperties;

    private Location cacheShopLocation = null;


    public double calculateDistanceFromShop(String userAddress) throws Exception {
        // 调试代码：检查配置是否加载
        if (baiduMapProperties == null) {
            throw new IllegalStateException("BaiduMapProperties 未注入");
        }
        if (baiduMapProperties.getMapAk() == null) {
            throw new IllegalStateException("百度地图 AK 未加载，请检查配置");
        }
        if (baiduMapProperties.getMapSk() == null) {
            throw new IllegalStateException("百度地图 SK 未加载，请检查配置");
        }


        if (cacheShopLocation == null)
            cacheShopLocation = getShopLocation();

        Location shopLocation = cacheShopLocation;
        Location userLocation = getLocation(userAddress);


        return calculateHaversineDistance(//单位: 米
                shopLocation.getLat(), shopLocation.getLng(),
                userLocation.getLat(), userLocation.getLng());
    }

    private Location getShopLocation() throws Exception {
        return getLocation(shopProperties.getAddress());
    }

    private Location getLocation(String address) throws Exception {
        //校验参数
        if (address == null || address.isEmpty())
            throw new BaseException("地址为空");

        //计算加密sn码
        String sn = SnCalUtil.getSn(address, "json", baiduMapProperties.getMapAk(), baiduMapProperties.getMapSk());

        //构建请求地址
        String URL = baiduMapProperties.getMapUrl();
        String AK = baiduMapProperties.getMapAk();

        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("address", address);
        params.put("output", "json");
        params.put("ak", AK);

        params.put("sn", sn);

        //获取请求结果-请求地址经纬度
        return requestGetAK(URL, params);
    }

    private Location requestGetAK(String strUrl, Map<String, String> param) throws Exception {
        if (strUrl == null || strUrl.length() <= 0 || param == null || param.size() <= 0) {
            throw new BaseException("地址参数错误，获取经纬度失败");
        }

        StringBuffer queryString = new StringBuffer();
        queryString.append(strUrl);
        for (Map.Entry<?, ?> pair : param.entrySet()) {
            queryString.append(pair.getKey() + "=");
            //使用spring的转码方法
            queryString.append(UriUtils.encode((String) pair.getValue(), "UTF-8") + "&");
        }

        if (queryString.length() > 0) {
            queryString.deleteCharAt(queryString.length() - 1);
        }

        java.net.URL url = new URL(queryString.toString());
        log.info("获取经纬度-请求地址:{}", queryString);

        URLConnection httpConnection = url.openConnection();
        httpConnection.connect();

        InputStreamReader isr = new InputStreamReader(httpConnection.getInputStream());
        BufferedReader reader = new BufferedReader(isr);
        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        reader.close();
        isr.close();
        log.info("获取经纬度-响应结果:{}", buffer);

        return parseLocation(buffer.toString());
    }

    private Location parseLocation(String jsonString) {

        //JSON字符串 → MapLocationResponse 对象
        MapLocationResponse response = com.alibaba.fastjson.JSON.parseObject(
                jsonString,
                MapLocationResponse.class
        );
        if (response.getStatus() != 0) {
            throw new BaseException("获取经纬度失败");
        }

        double lng = response.getResult().getLocation().getLng();
        double lat = response.getResult().getLocation().getLat();
        log.debug("经度:{} 纬度:{}", lng, lat);
        return response.getResult().getLocation();
    }

    /**
     * Haversine 公式计算两点间球面距离（单位：米）
     *
     * @param lat1 点 1 纬度
     * @param lng1 点 1 经度
     * @param lat2 点 2 纬度
     * @param lng2 点 2 经度
     * @return 距离（米）
     */
    private double calculateHaversineDistance(double lat1, double lng1, double lat2, double lng2) {
        double R = 6378137; // 地球半径（米）

        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double a = radLat1 - radLat2;
        double b = Math.toRadians(lng1) - Math.toRadians(lng2);

        double s = 2 * Math.asin(Math.sqrt(
                Math.pow(Math.sin(a / 2), 2) +
                        Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)
        ));

        return s * R;
    }

}