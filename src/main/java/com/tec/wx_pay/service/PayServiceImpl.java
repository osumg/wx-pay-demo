package com.tec.wx_pay.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tec.wx_pay.util.CommonConstants;
import com.tec.wx_pay.util.PayUtils;
import com.tec.wx_pay.util.RedisUtils;
import com.tec.wx_pay.util.XmlMapConverter;
import com.tec.wx_pay.util.snowflake.SnowflakeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *
 * </p>
 *
 * @since 2020/11/2
 */
@Service
public class PayServiceImpl implements PayService {
    @Autowired
    private RedisUtils redisUtils;
    public static final Logger LOGGER = LoggerFactory.getLogger(PayServiceImpl.class);

    /**
     * 获取openid并且保存到redis，已sessionid作为key，有效期25分钟
     *
     * @param code
     * @param sessionId
     */
    @Override
    public String getOpenidAndSave(String code, String sessionId) {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        String retStr = restTemplate.getForObject(String.format(CommonConstants.JS_ACCESS_TOKEN_URL, code), String.class);
        LOGGER.info("请求网页access_token和openid返回结果:{}", retStr);
        Map retJson;
        try {
            retJson = objectMapper.readValue(retStr, Map.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            LOGGER.info("异常数据：{}", retStr);
            throw new RuntimeException("获取微信openid异常！");
        }
        String openid = (String) retJson.get("openid");

        LOGGER.info("获取Openid:{}", openid);
        // 可以不需要 redis 存储，直接返回使用
//        redisUtils.setEx(sessionId, openid, 25 * 60);
        return openid;
    }

    /**
     * 请求支付，真正的支付由用户在客户端发起，这里请求微信服务器获取支付的参数，将结果传给客户端，客户端调用wx.chooseWXPay发起支付
     *
     * @param openid
     * @return
     */
    @Override
    public Map<String, String> reqPayBody(String openid) {
        Map<String, String> payBody = new HashMap<>();
        payBody.put("appid", CommonConstants.APP_ID);
        payBody.put("mch_id", CommonConstants.MCH_ID); // 商户号
        payBody.put("nonce_str", PayUtils.getRandomStr(32)); // 随机字符串
        payBody.put("body", "支付测试"); // 随机字符串
        payBody.put("out_trade_no", SnowflakeUtils.createId().toString()); // 商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|* 且在同一个商户号下唯一。
        payBody.put("total_fee", "1");//订单总金额，单位为分
        payBody.put("spbill_create_ip", "123.12.12.123");//支持IPV4和IPV6两种格式的IP地址。用户的客户端IP
        payBody.put("notify_url", "http://dev-gyj.cdmcs.com/notice");//异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
        payBody.put("trade_type", "JSAPI"); //交易类型
        payBody.put("openid", openid); //用户标识
        payBody.put("sign", PayUtils.generateSignature(payBody)); // 签名
        String xmlResult = XmlMapConverter.mapToXml(payBody);

        RestTemplate restTemplate = new RestTemplate();
        //解决中文乱码
        StringHttpMessageConverter converter = new StringHttpMessageConverter(Charset.forName("utf-8"));
        restTemplate.setMessageConverters(Collections.singletonList(converter));

        String retStr = restTemplate.postForObject(CommonConstants.PAY_REQ_URL, xmlResult, String.class);
        LOGGER.info("请求统一下单接口返回结果:{}", retStr);

        Map<String, String> map = XmlMapConverter.xmlToMap(retStr);
        System.out.println(map);
        return map;
    }
}
