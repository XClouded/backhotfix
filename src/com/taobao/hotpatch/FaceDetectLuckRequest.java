
package com.taobao.hotpatch;

import android.taobao.common.i.IMTOPDataObject;

public class FaceDetectLuckRequest implements IMTOPDataObject {
    public String API_NAME = "mtop.wlp.award.doAward4Face";
    public String version = "1.0";

    public boolean NEED_ECODE   = true;

    @Override
    public String toString() {
        return "FaceDetectLuckRequest [API_NAME=" + API_NAME + ", version="
                + version + ", NEED_ECODE=" + NEED_ECODE + ", ename=" + ename
                + ", eventId=" + eventId + ", channelId=" + channelId
                + ", sid=" + sid + "]";
    }

    public String getSid() {
        return sid;
    }
    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getEname() {
        return ename;
    }
    public void setEname(String ename) {
        this.ename = ename;
    }
    public String getEventId() {
        return eventId;
    }
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    public String getChannelId() {
        return channelId;
    }
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
    public String ename;
    public String eventId;
    public String channelId;
    public String sid;
    public String wua;
    public String userId;
}
