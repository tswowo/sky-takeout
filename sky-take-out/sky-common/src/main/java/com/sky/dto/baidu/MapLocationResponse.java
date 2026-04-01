package com.sky.dto.baidu;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MapLocationResponse {

    private Integer status;

    private Result result;

}
