/**
 * LoginActivity SdkResponseData.java
 * 
 * File Created at 2014年5月11日 下午3:31:32
 * $Id$
 * 
 * Copyright 2013 Taobao.com Croporation Limited.
 * All rights reserved.
 */
package com.taobao.hotpatch;

/**
 * @create 2014年5月11日 下午3:31:32
 * @author jojo
 * @version
 */
public class AliUserResponseData {

    /** sessionId */
    public String   sid;

    /**
     * 加签参数 用于后续mtop接口的加签
     */
    public String   ecode;

    /**
     * 淘宝昵称
     */
    public String   nick;

    /**
     * 用户数字id
     */
    public String   userId;

    /**
     * havana数字ID
     */
    public long     havanaId;

    /**
     * 登录时间 会话创建时间，单位：秒
     */
    public long     loginTime;

    /**
     * 免登token 免登token，用于后续autoLogin
     */
    public String   autoLoginToken;

    /**
     * webview免登cookie 用于免登到webview的cookie，native登录成功之后，种到webview
     */
    public String[] cookies;
    
    /**
     * ssoToken，存入本地sso
     */
    public String   ssoToken;
    
    /**
     * 用户头像地址
     */
    public String headPicLink;
}
