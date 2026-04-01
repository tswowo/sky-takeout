package com.sky.utils;

import com.sky.exception.BaseException;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

//java版计算signature签名
@Slf4j
public class SnCalUtil {

    /**
     * 计算sn的方法
     *
     * @param address 需要查询经纬度的地址
     * @param output  返回数据类型 json/xml
     * @param ak      密钥
     * @return sn 计算出来的签名
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    static public String getSn(String address, String output, String ak, String sk) throws UnsupportedEncodingException,
            NoSuchAlgorithmException {
        /* 计算sn跟参数对出现顺序有关，
           get请求请使用LinkedHashMap保存<key,value>，该方法根据key的插入顺序排序；
          post请使用TreeMap保存<key,value>，该方法会自动将key按照字母a-z顺序排序。
          所以get请求可自定义参数顺序（sn参数必须在最后）发送请求，但是post请求必须按照字母a-z顺序填充body（sn参数必须在最后）。
          以get请求为例：http://api.map.baidu.com/geocoding/v3/address=百度大厦&output=json&ak=yourak，
          paramsMap中先放入address，再放output，然后放ak，放入顺序必须跟get请求中对应参数的出现顺序保持一致。
         */

        //校验参数
        if (address == null || address.isEmpty())
            log.info("地址为空");
        if (output == null || output.isEmpty())
            log.info("返回数据类型错误");
        if (ak == null || ak.isEmpty())
            log.info("百度地图 AK 未加载，请检查配置");
        if (sk == null || sk.isEmpty())
            log.info("百度地图 SK 未加载，请检查配置");

        //开始计算
        log.debug("开始计算sn:{}", address);
        Map paramsMap = new LinkedHashMap<String, String>();
        paramsMap.put("address", address);
        paramsMap.put("output", output);
        paramsMap.put("ak", ak);

        // 调用下面的toQueryString方法，对LinkedHashMap内所有value作utf8编码，拼接返回结果
        String paramsStr = toQueryString(paramsMap);

        // 对paramsStr前面拼接上/geocoding/v3/，后面直接拼接yoursk得到/geocoding/v3/address=%E7%99%BE%E5%BA%A6%E5%A4%A7%E5%8E%A6&output=json&ak=yourakyoursk
        String wholeStr = new String("/geocoding/v3/?" + paramsStr + sk);

        // 对上面wholeStr再作utf8编码
        String tempStr = URLEncoder.encode(wholeStr, "UTF-8");

        // 调用下面的MD5方法得到最后的sn签名
        String sn = MD5(tempStr);
        log.debug("sn:{}", sn);
        return sn;
    }

    /**
     * 工具方法 对Map内所有value作utf8编码,拼接返回结果
     *
     * @param data 待拼接的参数对
     * @return 拼接好的字符串
     * @throws UnsupportedEncodingException
     */
    static public String toQueryString(Map<?, ?> data)
            throws UnsupportedEncodingException {
        StringBuffer queryString = new StringBuffer();
        for (Entry<?, ?> pair : data.entrySet()) {
            queryString.append(pair.getKey() + "=");
            queryString.append(URLEncoder.encode((String) pair.getValue(),
                    "UTF-8") + "&");
        }
        if (queryString.length() > 0) {
            queryString.deleteCharAt(queryString.length() - 1);
        }
        return queryString.toString();
    }


    /**
     * 工具方法 对字符串进行MD5加密
     * 来自stackoverflow的MD5计算方法，调用了MessageDigest库函数，并把byte数组结果转换成16进制
     *
     * @param md5
     * @return 加密后的字符串
     */
    static public String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest
                    .getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
                        .substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }
}