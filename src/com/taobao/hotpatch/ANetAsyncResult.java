package com.taobao.hotpatch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import org.android.spdy.SpdySession;

import android.util.Log;
import anetwork.channel.aidl.DefaultFinishEvent;
import anetwork.channel.anet.ACallback;
import anetwork.channel.anet.AsyncResult;
import anetwork.channel.anet.ResponseHelper;
import anetwork.channel.entity.RequestConfig;
import anetwork.channel.statist.Repeater;
import anetwork.channel.statist.Statistics;

public class ANetAsyncResult extends AsyncResult {

    private static final String   TAG       = "ANet.ANetAsyncResult";

    private ByteArrayOutputStream tmpStream = null;

    public ANetAsyncResult(RequestConfig config, Repeater forward, Statistics statistcs) {
        super(config, forward, statistcs);
        Log.i(TAG, " new ANetAsyncResult=================");
    }

    @Override
    public void spdyDataChunkRecvCB(SpdySession session, boolean fin, long streamId, byte[] data, int len,
                                    Object streamUserData) {
        int mIndex = getIntField("mIndex");
        boolean bGzip = getBooleanField("bGzip");
        StringBuilder sb = new StringBuilder();
        sb.append("[spdyDataChunkRecvCB] : streamId=")
          .append(streamId)
          .append(";session=")
          .append(session)
          .append(";len=")
          .append(len)
          .append(";fin=")
          .append(fin)
          .append(";bGzip=")
          .append(bGzip)
          .append(";index=")
          .append(mIndex)
          .append(";")
          .append(System.getProperty("line.separator"));
        if (data != null) {
            sb.append("data=").append(new String(data));
        } else {
            sb.append("data=null");
        }
        Log.i(TAG, sb.toString());

        Boolean bFinish = (Boolean) getObjectField("bFinish");
        if (bFinish) {
            return;
        }

        if (mIndex == 0) {
            mStatistcs.onDataFirstReceiveed();
        }
        byte[] ret = data;
        byte[] out = data;
        int length = len;
        if (bGzip) {
            if (tmpStream == null) {
                tmpStream = new ByteArrayOutputStream();
            }
            try {
                tmpStream.write(out);
            } catch (IOException e) {
                Log.e(TAG, "tmpStream.write(out) error", e);
            }
            if (fin) {
                try {
                    tmpStream.flush();
                    byte[] t = tmpStream.toByteArray();
                    ret = ResponseHelper.unGZip(t);
                    Log.d(TAG, "before:gzip:" + (t == null ? "" : new String(t)));
                    Log.d(TAG, "after:gzip:" + (ret == null ? "" : new String(ret)));
                } catch (IOException e) {
                    Log.e(TAG, "tmpStream.flush() error", e);
                } finally {
                    closeStream();
                }
                if (ret != null) {
                    length = ret.length;
                }
                mIndex++;
                setIntField("mIndex", mIndex);
                mForward.onDataReceiveSize(mIndex, length, mTotalLenght, ret);
                onDataReceiveSize(mIndex, length, mTotalLenght, ret);
            }
        } else {
            mIndex++;
            setIntField("mIndex", mIndex);
            mForward.onDataReceiveSize(mIndex, length, mTotalLenght, ret);
            onDataReceiveSize(mIndex, length, mTotalLenght, ret);
        }
    }

    private void sendOnFinishCallback(int errorCode) {
        Log.d(TAG, "[sendOnFinishCallback]");
        closeStream();
        Boolean bFinish = (Boolean) getObjectField("bFinish");
        synchronized (bFinish) {
            if (!bFinish) {
                DefaultFinishEvent event = new DefaultFinishEvent(errorCode, mStatistcs.getStatisticData());
                mStatistcs.onFinish(event);
                mForward.onFinish(event);
            }
            bFinish = true;
            setObjectField("bFinish", bFinish);
        }
    }

    @Override
    public void doFinish() {
        closeStream();
        super.doFinish();
    }

    private void closeStream() {
        if (tmpStream != null) {
            try {
                tmpStream.close();
            } catch (IOException e1) {
                Log.e(TAG, "tmpStream.close() error", e1);
            }
            tmpStream = null;
        }
    }

    private int getIntField(String fieldName) {
        Field field = getField(fieldName);
        if (field == null) {
            return 0;
        }
        try {
            return field.getInt(this);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "get field value error.", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "get field value error.", e);
        }
        return 0;
    }

    private void setIntField(String fieldName, int value) {
        Field field = getField(fieldName);
        if (field == null) {
            return;
        }
        try {
            field.setAccessible(true);
            field.setInt(this, value);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "set field value error.", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "set field value error.", e);
        }
    }

    private boolean getBooleanField(String fieldName) {
        Field field = getField(fieldName);
        if (field == null) {
            return false;
        }
        try {
            return field.getBoolean(this);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "get field value error.", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "get field value error.", e);
        }
        return false;
    }

    private Object getObjectField(String fieldName) {
        Field field = getField(fieldName);
        if (field == null) {
            return null;
        }
        try {
            return field.get(this);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "get field value error.", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "get field value error.", e);
        }
        return null;
    }

    private void setObjectField(String fieldName, Object value) {
        Field field = getField(fieldName);
        if (field == null) {
            return;
        }
        try {
            field.setAccessible(true);
            field.set(this, value);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "get field value error.", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "get field value error.", e);
        }
    }

    private Field getField(String fieldName) {
        try {
            return ACallback.class.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "get field error.", e);
        }
        return null;
    }
}
