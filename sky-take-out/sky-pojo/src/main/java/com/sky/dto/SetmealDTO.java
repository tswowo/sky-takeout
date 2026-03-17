package com.sky.dto;

import com.sky.entity.SetmealDish;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("套餐数据传输对象")
public class SetmealDTO implements Serializable {

    @ApiModelProperty("套餐id")
    private Long id;

    //分类id
    @ApiModelProperty(value = "分类id",required = true)
    private Long categoryId;

    //套餐名称
    @ApiModelProperty(value = "套餐名称",required = true)
    private String name;

    //套餐价格
    @ApiModelProperty(value = "套餐价格",required = true)
    private BigDecimal price;

    //状态 0:停用 1:启用
    @ApiModelProperty(value = "状态 0:停用 1:启用",required = true)
    private Integer status;

    //描述信息
    @ApiModelProperty(value = "描述信息")
    private String description;

    //图片
    @ApiModelProperty(value = "图片",required = true)
    private String image;

    //套餐菜品关系
    @ApiModelProperty(value = "套餐菜品关系",required = true)
    private List<SetmealDish> setmealDishes = new ArrayList<>();

}
