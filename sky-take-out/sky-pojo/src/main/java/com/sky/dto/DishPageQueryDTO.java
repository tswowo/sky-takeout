package com.sky.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class DishPageQueryDTO implements Serializable {

    @ApiModelProperty(value ="页码", required = true)
    private int page;

    @ApiModelProperty(value ="每页记录数", required = true)
    private int pageSize;

    @ApiModelProperty(value ="菜品名称")
    private String name;

    //分类id
    @ApiModelProperty(value ="分类id")
    private Integer categoryId;

    //状态 0表示禁用 1表示启用
    @ApiModelProperty(value ="状态 0表示禁用 1表示启用")
    private Integer status;

}
