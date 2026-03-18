package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    public Result<String> upload(@RequestBody MultipartFile file) {
        log.info("文件上传:{}", file);
        try {
            // 1. 获取文件字节数组
            byte[] bytes = file.getBytes();

            // 2. 生成唯一的文件名（避免OSS中文件重名覆盖）
            // 规则：UUID + 原文件后缀（例如：123456.jpg）
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String objectName = UUID.randomUUID().toString() + extension;

            // 3. 调用AliOssUtil完成上传，获取文件访问URL
            String fileUrl = aliOssUtil.upload(bytes, objectName);

            // 4. 返回上传成功的URL
            return Result.success(fileUrl, "");
        } catch (Exception e) {
            throw new BaseException(MessageConstant.UPLOAD_FAILED + e.getMessage());
        }
    }
}
