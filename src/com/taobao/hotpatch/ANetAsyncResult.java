package com.taobao.hotpatch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import mtopsdk.common.util.TBSdkLog;

import org.android.spdy.SpdySession;

import anetwork.channel.anet.AsyncResult;
import anetwork.channel.anet.ResponseHelper;
import anetwork.channel.entity.RequestConfig;
import anetwork.channel.statist.Repeater;
import anetwork.channel.statist.Statistics;

public class ANetAsyncResult extends AsyncResult {

    private static final String   TAG       = "ANet.ANetAsyncResult";

    private int                   mIndex;
    private boolean               bGzip     = false;
    private volatile Boolean      bFinish   = false;

    private ByteArrayOutputStream tmpStream = null;

    public ANetAsyncResult(RequestConfig config, Repeater forward, Statistics statistcs) {
        super(config, forward, statistcs);
    }

    @Override
    public void spdyDataChunkRecvCB(SpdySession session, boolean fin, long streamId, byte[] data, int len,
                                    Object streamUserData) {

        byte[] ret = data;
        if (bFinish) {
            return;
        }
        if (mIndex == 0) {
            mStatistcs.onDataFirstReceiveed();
        }
        byte[] out = data;
        int length = len;
        if (bGzip) {
            if (tmpStream == null) {
                tmpStream = new ByteArrayOutputStream();
            }
            try {
                tmpStream.write(out);
            } catch (IOException e) {
                TBSdkLog.e(TAG, "tmpStream.write(out) error", e);
            }
            if (fin) {
                try {
                    tmpStream.flush();
                    byte[] t = tmpStream.toByteArray();
                    ret = ResponseHelper.unGZip(t);
                    if (TBSdkLog.isPrintLog()) {
                        TBSdkLog.d(TAG, "before:gzip:" + (t == null ? "" : new String(t)));
                        TBSdkLog.d(TAG, "after:gzip:" + (ret == null ? "" : new String(ret)));
                    }
                } catch (IOException e) {
                    TBSdkLog.e(TAG, "tmpStream.flush() error", e);
                } finally {
                    if (tmpStream != null) {
                        try {
                            tmpStream.close();
                        } catch (IOException e1) {
                            TBSdkLog.e(TAG, "tmpStream.close() error", e1);
                        }
                        tmpStream = null;
                    }
                }
                if (ret != null) {
                    length = ret.length;
                }
                mIndex++;
                mForward.onDataReceiveSize(mIndex, length, mTotalLenght, ret);
                onDataReceiveSize(mIndex, length, mTotalLenght, ret);
            }
        } else {
            mIndex++;
            mForward.onDataReceiveSize(mIndex, length, mTotalLenght, ret);
            onDataReceiveSize(mIndex, length, mTotalLenght, ret);
        }
    }
}
