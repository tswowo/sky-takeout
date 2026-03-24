package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.BaseException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    public static final String WeChat_Login = "https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    /**
     * 微信登录
     *
     * @param userLoginDTO
     * @return User 登录用户信息
     */
    @Override
    public User weChatLogin(UserLoginDTO userLoginDTO) {
        //调用微信接口，获取OpenId
        String openid = getOpenid(userLoginDTO.getCode());
        //如果拿不到说明登陆失败
        if (openid == null) {
            throw new BaseException(MessageConstant.LOGIN_FAILED);
        }
        //校验用户是否已经注册
        User user = userMapper.getByOpenid(openid);
        if (user == null) {
            //用户未注册，自动完成注册
            user = User.builder()
                    .openid(openid)
                    .build();
            userMapper.insert(user);
        }
        //返回登录用户信息
        return user;
    }

    /**
     * 调用微信接口，查询微信用户的openid
     *
     * @param code
     * @return
     */
    private String getOpenid(String code) {
        Map<String, String> map = new HashMap<>();
        System.out.println(weChatProperties);
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        String json = HttpClientUtil.doGet(WeChat_Login, map);
        System.out.println(json);
        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject.getString("openid");
    }
}
