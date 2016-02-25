package com.example.httprequestdemo;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;

/**
 * @author LiYueHui
 */
public class HttpUtil {

    private HttpConRun mHttpConn;
    private static ExecutorService mPool =Executors.newFixedThreadPool(6);
//    private static ExecutorService mPool =Executors.newSingleThreadExecutor();

    /**
     * @param url              网址
     * @param map              请求头
     * @param handler
     * @param method
     * @param requestParamsMap 请求体 内容
     */
    public void start(String url, HashMap<String, String> map, Handler handler, String method,
                      HashMap<String, String> requestParamsMap, String picPath) {
        if (mHttpConn == null) {
            mHttpConn = new HttpConRun(url, map, handler, method, requestParamsMap, picPath);
        }
        mHttpConn.setParams(url, map, handler, method, requestParamsMap, picPath);
        mPool.execute(mHttpConn);
    }

    private HashMap<String, String> mMap; //请求头
    private String mUrl;
    private String method;
    private Handler mHandler;
    private HashMap<String, String> mRequestParamsMap;//请求体
    private String mPicPath;

    public HttpUtil setUrl(String url) {
        mUrl = url;
        return this;
    }

    public HttpUtil setMethod(String method) {
        this.method = method;
        return this;
    }

    public HttpUtil setHandler(Handler handler) {
        mHandler = handler;
        return this;
    }

    public HttpUtil setBody(HashMap<String, String> requestParamsMap) {
        this.mRequestParamsMap = requestParamsMap;
        return this;
    }

    public HttpUtil setPath(String path) {
        this.mPicPath = path;
        return this;
    }

    public void start() {

        if (mHttpConn == null) {
            mHttpConn = new HttpConRun(mUrl, mMap, mHandler, method, mRequestParamsMap, mPicPath);
        }

        mHttpConn.setParams(mUrl, mMap, mHandler, method, mRequestParamsMap, mPicPath);
        mPool.execute(mHttpConn);
    }

    public static void clearCookie() {
        HttpConRun.clearCookie();
    }

    //断开和服务器的连接
    public void disConnection() {
        mHttpConn.getHttpURLConnection().disconnect();
    }
}
