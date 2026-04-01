package com.sky.dto.baidu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Location {

    //经度
    private double lat;
    //纬度
    private double lng;

}
