package com.mylove.okhttp;

/**
 * @author myLove
 */
public class OkHttpInfo {
    public static boolean isLOG = true;
    /**
     * soap协议类型
     */
    public static String soapMediaType = "text/xml; charset=UTF-8";
    /**
     * soap请求数据前半部分xml
     */
    public static String soapDataTopString = "";
    /**
     * soap请求数据后半部分xml
     */
    public static String soapDataBottomString = "";

    public static String TAG = "OkHttpUtil--->>>>";

    public static boolean xmlToJson = false;
}
