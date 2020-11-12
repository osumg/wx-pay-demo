package com.tec.wx_pay.util;

/**
 * <p>
 *
 * </p>
 *
 * @since 2020/10/29
 */
public class CommonConstants {
    // 公众号appid
    public static final String APP_ID = "";
    // 公众号app_secret
    public static final String APP_SECRET = "";
    // key为商户平台设置的密钥key
    public static final String PAY_API_KEY = "";
    // 商户号
    public static final String MCH_ID = "";
    // api密钥
    public static final String API_KEY = "";

    // 基础 access_token 请求地址
    public static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + APP_ID + "&secret=" + CommonConstants.APP_SECRET;
    //jsapi_ticket（有效期7200秒，开发者必须在自己的服务全局缓存jsapi_ticket），用于 wx.config
    public static final String JSAPI_TICKET_URL = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?type=jsapi&access_token=";
    //统一下单地址
    public static final String PAY_REQ_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";

    // 网页 access_token 请求地址（有效期7200秒，开发者必须在自己的服务全局缓存access_token），包含openid的获取
    public static final String JS_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+ APP_ID +"&secret="+ APP_SECRET +"&code=%s&grant_type=authorization_code";
}
