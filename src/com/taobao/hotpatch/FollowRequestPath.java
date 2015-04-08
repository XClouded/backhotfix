package com.taobao.hotpatch;

import com.taobao.socialsdk.core.BasicParam;
import com.taobao.socialsdk.core.BasicRequest;

public class FollowRequestPath extends BasicRequest{
    public FollowRequestPath(BasicParam param){
        super(param);
    }
    /**
     * API的名称 (Required)
     */
    private  String  API_NAME     = "mtop.cybertron.follow.add.isv";
    /**
     * origin (Required)
     */
    private String  isvAppkey       = null;
    /**
     * pubAccountId (Required)
     */
    private long    pubAccountId = 0L;
    /**
     * 淘宝无线用户会话ID (Required)
     */
    public boolean NEED_SESSION    = true;
    public boolean NEED_ECODE=true;
    public String   VERSION="1.0";
    
    public String getVERSION() {
		return VERSION;
	}

	public void setVERSION(String vERSION) {
		VERSION = vERSION;
	}

	/**
     * API的名称 (Required)
     */
    public String getAPI_NAME() {
        return API_NAME;
    }

    /**
     * API的名称 (Required)
     */
    public void setAPI_NAME(String API_NAME) {
        this.API_NAME = API_NAME;
    }

    
    public String getIsvAppkey() {
        return isvAppkey;
    }

    
    public void setIsvAppkey(String isvAppkey) {
        this.isvAppkey = isvAppkey;
    }

    /**
     * pubAccountId (Required)
     */
    public long getPubAccountId() {
        return pubAccountId;
    }

    /**
     * pubAccountId (Required)
     */
    public void setPubAccountId(long pubAccountId) {
        this.pubAccountId = pubAccountId;
    }

}
