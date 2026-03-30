package com.sky.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel("用户下单提交订单数据")
public class OrdersSubmitDTO implements Serializable {
    //地址簿id
    @ApiModelProperty(value = "地址簿id", required = true)
    private Long addressBookId;
    //付款方式
    @ApiModelProperty(value = "付款方式", required = true)
    private int payMethod;
    //备注
    @ApiModelProperty(value = "备注", required = true)
    private String remark;
    //预计送达时间
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "预计送达时间", required = true)
    private LocalDateTime estimatedDeliveryTime;
    //配送状态  1立即送出  0选择具体时间
    @ApiModelProperty(value = "配送状态  1立即送出  0选择具体时间", required = true)
    private Integer deliveryStatus;
    //餐具数量
    @ApiModelProperty(value = "餐具数量", required = true)
    private Integer tablewareNumber;
    //餐具数量状态  1按餐量提供  0选择具体数量
    @ApiModelProperty(value = "餐具数量状态  1按餐量提供  0选择具体数量", required = true)
    private Integer tablewareStatus;
    //打包费
    @ApiModelProperty(value = "打包费", required = true)
    private Integer packAmount;
    //总金额
    @ApiModelProperty(value = "总金额", required = true)
    @DecimalMin(value = "0.00", message = "总金额不能为负数")
    private BigDecimal amount;
}
