package com.mylove.okhttp;

import java.util.Map;

/**
 * 字符串工具类
 */
class FormatUtil {

    /**
     * 判断字符串是否为空
     *
     * @param str
     * @return true 不为空, false 为空
     */
    static boolean isNotEmpty(String str) {
        return str != null && !"null".equals(str) && str.trim().length() != 0;
    }

    /**
     * 判断字符串是否为空
     *
     * @param str
     * @return true 为空，false 不为空
     */
    static boolean isEmpty(String str) {
        return str == null || "null".equals(str) || str.trim().length() == 0;
    }

    /**
     * 判断MAP是否为空
     */
    static <K, V> boolean isMapNotEmpty(Map<K, V> map) {
        return map != null && map.size() > 0;
    }

}