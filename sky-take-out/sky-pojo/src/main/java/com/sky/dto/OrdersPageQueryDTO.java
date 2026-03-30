package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@ApiModel("订单分页查询参数")
public class OrdersPageQueryDTO implements Serializable {

    @ApiModelProperty(value = "页码", required = true)
    private int page;

    @ApiModelProperty(value = "每页记录数", required = true)
    private int pageSize;

    private String number;

    private String phone;

    @ApiModelProperty(value = "订单状态 1待付款 2待接单 3待派送 4派送中 5已完成 6已取消 7退款")
    private Integer status;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Long userId;

}
