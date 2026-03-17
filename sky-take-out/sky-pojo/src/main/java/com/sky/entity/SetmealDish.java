package com.sky.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 套餐菜品关系
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("套餐菜品关系")
public class SetmealDish implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    //套餐id
    @ApiModelProperty(value = "套餐id", required = true)
    private Long setmealId;

    //菜品id
    @ApiModelProperty(value = "菜品id", required = true)
    private Long dishId;

    //菜品名称 （冗余字段）
    @ApiModelProperty(value = "菜品名称", required = true)
    private String name;

    //菜品原价
    @ApiModelProperty(value = "菜品原价", required = true)
    private BigDecimal price;

    //份数
    @ApiModelProperty(value = "份数", required = true)
    private Integer copies;
}
