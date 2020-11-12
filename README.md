# wx-pay-demo
微信支付demo

* 只用于简单测试，使用了redis简单存储部分数据，需要启动redis
* CommonConstants中需要配置相关参数
* resources/static 路径中需要存放配置域名时的校验文件，该文件通过公众号后台相关配置选项中获取

##### 开发思路
1. 在微信公众号的自定义菜单中配置url地址，使用网页授权，参考文档【微信网页开发 - 网页授权】
2. 公众号网页中获取code并传给服务器
3. 服务器根据code获取网页授权access_token和openid
===至此得到openid
4. 公众号网页调用服务器请求，获取wx.config()的参数，wx.config()的参数具体获取，参考文档【微信网页开发 - JS-SDK说明文档】
5. 公众号网页发起wx.chooseWXPay前的处理：
    5.1 调用统一下单接口，地址（https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_1），也可以在【JS-SDK说明文档 - wx.chooseWXPay】中根据提示进入
    5.2 统一下单接口返回的数据需要签名再传给 wx.chooseWXPay
6. 服务器创建微信支付结果回调接口，验证微信回调结果的正确性，并给微信服务器返回信息，不返回信息微信会调用八次，八次之后将认为失败

注意：
1. 配置支付目录的时候，如：http://www.123.com/home/，此时的自定义菜单中url为http://www.123.com/home即可访问，但是还是要在最后加上斜杠，即应该在自定义菜单中配置为：http://www.123.com/home/
