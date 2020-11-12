package com.tec.wx_pay.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 *
 * </p>
 *
 * @since 2020/10/20
 */
@Component
public class RedisUtils {
    /**
     * 1 redisTemplate.opsForValue().* //操作String字符串类型
     * 2 redisTemplate.delete(key/collection) //根据key/keys删除
     * 3 redisTemplate.opsForList().*  //操作List类型
     * 4 redisTemplate.opsForHash().*  //操作Hash类型
     * 5 redisTemplate.opsForSet().*  //操作set类型
     * 6 redisTemplate.opsForZSet().*  //操作有序set
     */
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public String getToken() {
        return redisTemplate.opsForValue().get(RedisKeyConstants.ACCESS_TOKEN);
    }

    public String getTicket() {
        return redisTemplate.opsForValue().get(RedisKeyConstants.TICKET);
    }

    public void setEx(String key, String value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

}
