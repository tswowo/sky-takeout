package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("套餐分页查询参数")
public class SetmealPageQueryDTO implements Serializable {

    @ApiModelProperty(value = "页码",required = true)
    private int page;

    @ApiModelProperty(value = "每页数据量",required = true)
    private int pageSize;

    private String name;

    //分类id
    private Integer categoryId;

    //状态 0表示禁用 1表示启用
    private Integer status;

}
