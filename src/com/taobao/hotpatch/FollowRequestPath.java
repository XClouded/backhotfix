package com.taobao.hotpatch;

import com.taobao.socialsdk.core.BasicParam;
import com.taobao.socialsdk.follow.FollowRequest;

public class FollowRequestPath extends FollowRequest {
	public String VERSION = "1.0";

	public String getVERSION() {
		return VERSION;
	}

	public void setVERSION(String vERSION) {
		VERSION = vERSION;
	}

	public FollowRequestPath(BasicParam param) {
		super(param);
	}

}
