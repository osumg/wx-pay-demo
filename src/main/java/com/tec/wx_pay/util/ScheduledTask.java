package com.tec.wx_pay.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * <p>
 *
 * </p>
 *
 * @since 2020/10/30
 */
@Component
public class ScheduledTask {
    @Autowired
    private RedisUtils redisUtils;

    public static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTask.class);

    /**
     * 时间间隔100分钟刷新一次
     */
    @Scheduled(initialDelay = 1000, fixedDelay = 100 * 60 * 1000)
    public void wxPaySchedule() {
        getJsApiTicketAndSave(getTokenAndSave());
    }

    /**
     * 获取token并且保存到redis中
     *
     * @return token
     */
    private String getTokenAndSave() {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        String retStr = restTemplate.getForObject(CommonConstants.ACCESS_TOKEN_URL, String.class);
        Map retJson = null;
        try {
            retJson = objectMapper.readValue(retStr, Map.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            LOGGER.info("异常数据：{}", retStr);
            throw new RuntimeException("获取微信access_token异常！");
        }
        String token = (String) retJson.get("access_token");
        LOGGER.info("刷新了token：{}", token);

        redisUtils.set(RedisKeyConstants.ACCESS_TOKEN, token);
        return token;
    }

    /**
     * 获取ticket并且保存到redis中
     *
     */
    private void getJsApiTicketAndSave(String token) {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        String retStr = restTemplate.getForObject(CommonConstants.JSAPI_TICKET_URL + token, String.class);

        LOGGER.info("请求jsapi_ticket返回结果：{}", retStr);

        Map retJson;
        try {
            retJson = objectMapper.readValue(retStr, Map.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            LOGGER.info("异常数据：{}", retStr);
            throw new RuntimeException("获取jsapi_ticket结果转换异常！");
        }
        String ticket = (String) retJson.get("ticket");
        LOGGER.info("刷新了ticket：{}", ticket);

        redisUtils.set(RedisKeyConstants.TICKET, ticket);
    }
}
