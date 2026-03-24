package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * C端用户登录
 */
@Data
@ApiModel("用户登录对象")
public class UserLoginDTO implements Serializable {

    @ApiModelProperty(value = "微信登录成功获取的code", required = true)
    private String code;

}
