package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrdersRejectionDTO implements Serializable {

    private Long id;

    //订单拒绝原因
    private String rejectionReason;

    //订单取消原因
    private String cancelReason;

}
