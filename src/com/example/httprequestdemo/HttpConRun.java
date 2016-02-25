package com.example.httprequestdemo;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * @author LiYueHui
 */
public class HttpConRun implements Runnable {

    private static final String TAG = HttpConRun.class.getSimpleName();

    public static final int NET_ERROR = 1;
    public static final int NET_SUCCESS = 2;

    public static final String POST = "POST";
    public static final String GET = "GET";
    public static final String UPLOAD = "UPLOAD";

    private String boundary = "---------------------------265001916915724";
    String lineEnd = "\r\n";

    private String mUrl; // 链接网址
    private HashMap<String, String> mMap; // 协议头擦恕
    private HashMap<String, String> requestParamsMap; // 协议体
    private Handler mHandler;
    private String method;
    public static String mCookieVal;
    private String pathOfPicture;

    private HttpURLConnection conn;

    /**
     * 清除cookie
     */
    public static void clearCookie() {
        mCookieVal = null;
    }

    public HttpURLConnection getHttpURLConnection() {
        return conn;
    }

    public HttpConRun(String url, HashMap<String, String> map, Handler handler, String method,
                      HashMap<String, String> requestParamsMap, String pathOfPicture) {
        setParams(url, map, handler, method, requestParamsMap, pathOfPicture);
    }

    @Override
    public void run() {
        URL url;
        Message msg = null;
        if (mHandler != null) {
            msg = mHandler.obtainMessage();
            msg.what = 0;
        }
        try {
            url = new URL(mUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 设置cookie
            if (!TextUtils.isEmpty(mCookieVal)) {
                conn.setRequestProperty("Cookie", mCookieVal);
            }

            if (mMap != null) {
                for (String key : mMap.keySet()) {
                    conn.setRequestProperty(key, mMap.get(key));
                }
            }

            if ("GET".equals(method)) {
                conn.setRequestMethod(method);
            } else if (POST.equals(method)) {
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod(method);
                PrintWriter out = new PrintWriter(conn.getOutputStream());
                out.print(getBody(requestParamsMap));
                out.flush();
                conn.setInstanceFollowRedirects(true);
            } else if (UPLOAD.equals(method)) {
                conn.setRequestMethod(POST);
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.setRequestProperty("connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                DataOutputStream out = new DataOutputStream(conn.getOutputStream());

                for (String key : requestParamsMap.keySet()) {
//                    if(Constant.ReqJson.equals(key)) {
//                        String req = requestParamsMap.get(Constant.ReqJson);
//                        try {
//
//                            String after = CommonUtil.encryptDES(req, Constant.DESKEY);
//                            requestParamsMap.put(Constant.ReqJson,after);
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
//                    }
                    String ContentMeta = "--" + boundary + lineEnd + "Content-Disposition: form-data; name=\"" + key
                            + "\"" + lineEnd + lineEnd + requestParamsMap.get(key) + lineEnd;
                    out.write(ContentMeta.getBytes());
                }

                // 判断文件是否存在
                File f = null;
                try {
                    f = new File(pathOfPicture);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (f != null && f.exists()) {
                    String fileMeta = "--" + boundary + lineEnd
                            + "Content-Disposition: form-data; name=\"portrait\"; filename=\"" + System.currentTimeMillis()+ f.getName()
                            + "\"" + lineEnd + "Content-Type: image/jpeg" + lineEnd + lineEnd;
                    out.write(fileMeta.getBytes());
                    FileInputStream fin = new FileInputStream(pathOfPicture);
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while ((len = fin.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                }
                out.writeBytes(lineEnd + lineEnd);
                out.writeBytes("--" + boundary + "--");

            }

            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            String line, result = "";
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            // 得到Cookies
            if (TextUtils.isEmpty(mCookieVal)) {
                getCookies(conn);
            }

            // Log.e(TAG, "liyue ret " + result);

            while ((line = in.readLine()) != null) {
                result += line;
            }
//            Log.e(TAG, "liyuehui ret getResponseCode:" + conn.getResponseCode());
            if (conn.getResponseCode() == 200) {
                try {
//                    Log.e(TAG, "liyuehui ret " + CommonUtil.decryptDES(result, Constant.DESKEY));
                    if(msg != null) {
//                        msg.obj = CommonUtil.decryptDES(result, Constant.DESKEY);
                        msg.what = NET_SUCCESS;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                if(msg != null) {
                    msg.obj = " responseCode:" + conn.getResponseCode();
                    msg.what = NET_ERROR;
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            if(msg != null) {
                msg.what = NET_ERROR;
                msg.obj = e;
            }
        } catch (IOException e) {
            e.printStackTrace();
            if(msg != null) {
                msg.obj = e;
                msg.what = NET_ERROR;
            }
        }
        if(mHandler != null) {
            mHandler.sendMessage(msg);
        }
    }

    private void getCookies(HttpURLConnection conn) {
        String key = "";
        for (int i = 1; (key = conn.getHeaderFieldKey(i)) != null; i++) {
            if (key.equalsIgnoreCase("set-cookie")) {
                mCookieVal = conn.getHeaderField(i);
                // cookieVal = cookieVal.substring(0, cookieVal.indexOf(";"));
                // sessionId = sessionId + cookieVal + ";";
                Log.e(TAG, "liyuehui getCookies " + mCookieVal);
            }
        }
    }

    private String getBody(HashMap<String, String> requestParamsMap) {
        StringBuilder params = new StringBuilder();

        String req = null;
        try {
            Log.e(TAG, "liyuehui :" + mUrl);
//            req = requestParamsMap.get(Constant.ReqJson);
            Log.e(TAG, "liyuehui req:" + req);
//            String reqAfter = CommonUtil.encryptDES(req, Constant.DESKEY);
//            requestParamsMap.put(Constant.ReqJson,reqAfter);
//            Log.e(TAG,TAG+" requestParamsMap:"+requestParamsMap.get(Constant.ReqJson));
//            Log.e(TAG,TAG+" reqAfter:"+CommonUtil.decryptDES(requestParamsMap.get(Constant.ReqJson), Constant.DESKEY));
        } catch (Exception e) {
            e.printStackTrace();
        }

//        if(!TextUtils.isEmpty(req)){
//            Log.e(TAG,TAG+" rq:"+req);
////            requestParamsMap.put(Constant.ReqJson,req);
//        }

        int size = requestParamsMap.keySet().size();
        int j = 0;
        for (String key : requestParamsMap.keySet()) {
            params.append(key);
            params.append("=");
            params.append(requestParamsMap.get(key));
            j++;
            if (j < size) {
                params.append("&");
            }
        }
        return params.toString();
    }

    /**
     * @param url
     * @param map
     * @param handler
     * @param method
     * @param requestParamsMap
     */
    public void setParams(String url, HashMap<String, String> map, Handler handler, String method,
                          HashMap<String, String> requestParamsMap, String pathOfPicture) {
        mUrl = url;
        mMap = map;
        mHandler = handler;
        this.method = method;
        this.requestParamsMap = requestParamsMap;
        this.pathOfPicture = pathOfPicture;
    }

}
