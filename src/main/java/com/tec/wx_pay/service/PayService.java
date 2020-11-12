package com.tec.wx_pay.service;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * <p>
 *
 * </p>
 *
 * @since 2020/11/2
 */
public interface PayService {
    /**
     * 获取openid并且保存到redis，已sessionid作为key，有效期25分钟
     *
     * @param code
     * @param sessionId
     */
    String getOpenidAndSave(String code, String sessionId);

    Map<String,String> reqPayBody(String openid);
}
