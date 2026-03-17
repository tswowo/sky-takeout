package com.sky.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayInputStream;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /**
     * 文件上传
     * @param bytes
     * @param objectName
     * @return 文件访问路径
     */
    public String upload(byte[] bytes, String objectName) {

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 创建PutObject请求。
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        //文件访问路径规则 https://BucketName.Endpoint/ObjectName
        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder
                .append(bucketName)
                .append(".")
                .append(endpoint)
                .append("/")
                .append(objectName);

        log.info("文件上传到:{}", stringBuilder.toString());

        return stringBuilder.toString();
    }

    /**
     * 删除OSS上的图片
     * @param objectName 支持传入「完整图片URL」或「OSS文件名」（兼容两种格式）
     */
    public void deleteImage(String objectName) {
        // 1. 前置空值校验：避免传入null/空字符串导致后续报错
        if (objectName == null || objectName.isEmpty()) {
            log.warn("待删除的OSS图片参数为空，无需执行删除操作");
            return;
        }

        // 创建OSSClient实例（保留手动关闭逻辑）
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 2. 解析文件名：如果是完整URL则截取，否则直接使用原文件名
            String realObjectName = getObjectNameFromUrl(objectName);

            // 3. 解析后二次校验：避免解析出null/空字符串
            if (realObjectName == null || realObjectName.isEmpty()) {
                log.error("解析OSS文件名失败，原始参数：{}", objectName);
                throw new RuntimeException("解析图片文件名失败，无法删除OSS图片");
            }

            // 4. 执行删除操作
            ossClient.deleteObject(bucketName, realObjectName);
            log.info("OSS图片删除成功，文件名：{}", realObjectName);

        } catch (OSSException oe) {
            log.error("OSS删除图片异常 - 错误码：{}，请求ID：{}，错误信息：{}",
                    oe.getErrorCode(), oe.getRequestId(), oe.getErrorMessage(), oe);
            throw new RuntimeException("OSS图片删除失败：" + oe.getErrorMessage());
        } catch (ClientException ce) {
            log.error("OSS客户端异常 - 错误信息：{}", ce.getMessage(), ce);
            throw new RuntimeException("客户端连接失败：" + ce.getMessage());
        } finally {
            // 保留手动关闭逻辑，仅增加非空判断（避免空指针）
            if (ossClient != null) {
                ossClient.shutdown();
                log.debug("OSS客户端已手动关闭");
            }
        }
    }

    /**
     * 工具方法：从图片URL中解析出OSS的objectName
     * @param imageUrl 数据库中存储的完整URL 或 已有的OSS文件名
     * @return 文件名（objectName），解析失败返回null
     */
    public String getObjectNameFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        // 兼容两种场景：如果是URL则截取，不是URL（已是文件名）则直接返回
        int lastSlashIndex = imageUrl.lastIndexOf("/");
        // 场景1：是完整URL（包含http/https），截取最后一个/后的部分
        if (imageUrl.contains("http") && lastSlashIndex != -1) {
            // 防止URL以/结尾（如https://xxx.com/）导致截取空字符串
            if (lastSlashIndex == imageUrl.length() - 1) {
                log.error("图片URL格式错误，以/结尾：{}", imageUrl);
                return null;
            }
            return imageUrl.substring(lastSlashIndex + 1);
        }
        // 场景2：已经是OSS文件名，直接返回
        return imageUrl;
    }
}
