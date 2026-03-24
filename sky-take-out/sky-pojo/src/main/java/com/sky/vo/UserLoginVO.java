package com.sky.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "客户端用户登录返回的数据格式")
public class UserLoginVO implements Serializable {

    @ApiModelProperty(value = "用户id", required = true)
    private Long id;
    @ApiModelProperty(value = "微信openid", required = true)
    private String openid;
    @ApiModelProperty(value = "用户昵称", required = true)
    private String token;

}
