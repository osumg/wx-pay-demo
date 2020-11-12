package com.tec.wx_pay.controller;

import com.tec.wx_pay.service.PayService;
import com.tec.wx_pay.util.CommonConstants;
import com.tec.wx_pay.util.PayUtils;
import com.tec.wx_pay.util.RedisUtils;
import com.tec.wx_pay.util.XmlMapConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;

/**
 * <p>
 *
 * </p>
 *
 * @since 2020/10/29
 */
@Controller
public class PayController {
    public static final Logger LOGGER = LoggerFactory.getLogger(PayController.class);

    @Autowired
    private PayService payService;
    @Autowired
    private RedisUtils redisUtils;

    /**
     * 进入首页
     *
     * @param session
     * @param modelMap
     * @return
     */
    @GetMapping("/home")
    public String index(HttpSession session, ModelMap modelMap) {
        modelMap.addAttribute("msg", session.getId());
        return "index";
    }

    /**
     * 接受支付通知，由微信调用
     *
     * @param request
     * @throws IOException
     */
    @RequestMapping("/notice")
    @ResponseBody
    public void notice(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //读取参数
        InputStream inputStream = request.getInputStream();;
        StringBuilder sb = new StringBuilder();
        String s;
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        while ((s = in.readLine()) != null) {
            sb.append(s);
        }
        in.close();
        inputStream.close();

        LOGGER.info("微信支付结果回调信息：{}", sb.toString());

        //解析xml成map
        Map<String, String> m = XmlMapConverter.xmlToMap(sb.toString());

        //过滤空 设置 TreeMap
        SortedMap<Object, Object> packageParams = new TreeMap<>();
        for (String parameter : m.keySet()) {
            String parameterValue = m.get(parameter);

            String v = "";
            if (null != parameterValue) {
                v = parameterValue.trim();
            }
            packageParams.put(parameter, v);
        }

        String key = CommonConstants.API_KEY;

        //判断签名是否正确
        if (PayUtils.judgeSign(packageParams, key)) {
            //------------------------------
            //处理业务开始
            //------------------------------
            String resXml = "";
            if ("SUCCESS".equals(packageParams.get("result_code"))) {
                // 这里是支付成功
                //执行自己的业务逻辑
                String mch_id = (String) packageParams.get("mch_id");
                String openid = (String) packageParams.get("openid");
                String is_subscribe = (String) packageParams.get("is_subscribe");
                String out_trade_no = (String) packageParams.get("out_trade_no");

                String total_fee = (String) packageParams.get("total_fee");

                LOGGER.info("mch_id:" + mch_id);
                LOGGER.info("openid:" + openid);
                LOGGER.info("is_subscribe:" + is_subscribe);
                LOGGER.info("out_trade_no:" + out_trade_no);
                LOGGER.info("total_fee:" + total_fee);

                //执行自己的业务逻辑

                LOGGER.info("支付成功");
                //通知微信.异步确认成功.必写.不然会一直通知后台.八次之后就认为交易失败了.
                resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
                        + "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";

            } else {
                LOGGER.info("支付失败,错误信息：" + packageParams.get("err_code"));
                resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>"
                        + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
            }
            //------------------------------
            //处理业务完毕
            //------------------------------
            BufferedOutputStream out = new BufferedOutputStream(
                    response.getOutputStream());
            out.write(resXml.getBytes());
            out.close();
        } else {
            LOGGER.info("通知签名验证失败");
        }

    }

    /**
     * 公众号首页进入时，接受code参数，根据code获取openid并保存到redis，以sessionId作为key值，有效期25分钟。
     * 然后处理并返回 wx.config() 参数
     * payBody 为支付请求体相关内容，用于 wx.chooseWXPay() 函数的参数
     *
     * @param reqMap
     * @param session
     */
    @PostMapping("/init")
    @ResponseBody
    public Map<String, Object> init(@RequestBody Map<String, String> reqMap, HttpSession session) {
        LOGGER.info("sessionId:{}", session.getId());
        String openid = payService.getOpenidAndSave(reqMap.get("code"), session.getId());

        //ticket 相关，用于授权 wx.chooseWXPay() 函数的调用
        String randomStr = PayUtils.getRandomStr(10);
        String timestamp = PayUtils.getTimeStamp();
        String sign = "jsapi_ticket=" +
                redisUtils.getTicket() +
                "&noncestr=" +
                randomStr +
                "&timestamp=" +
                timestamp +
                "&url=" +
                "http://dev-gyj.cdmcs.com";

        HashMap<String, Object> map = new HashMap<>();
        map.put("sign", PayUtils.sha1Encrypt(sign));
        map.put("timestamp", timestamp);
        map.put("nonceStr", randomStr);
        map.put("appId", CommonConstants.APP_ID);

        Map<String, String> payRepMap = payService.reqPayBody(openid);

        // 页面支付请求参数
        //        即最后参与签名的参数有appId, timeStamp, nonceStr, package, signType。
        HashMap<String, String> md5SignMap = new HashMap<>();
        md5SignMap.put("appId", CommonConstants.APP_ID);
        md5SignMap.put("timeStamp", timestamp);
        md5SignMap.put("nonceStr", randomStr);
        md5SignMap.put("package", "prepay_id=" + payRepMap.get("prepay_id"));
        md5SignMap.put("signType", "MD5");

        HashMap<String, Object> payJsApiMap = new HashMap<>(payRepMap);
        payJsApiMap.put("paySign", PayUtils.generateSignature(md5SignMap));

        map.put("payBody", payJsApiMap);

        return map;
    }
}
