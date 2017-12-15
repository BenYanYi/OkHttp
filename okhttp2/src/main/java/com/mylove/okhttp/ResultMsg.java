package com.mylove.okhttp;

/**
 * @author myLove
 * @time 2017/12/14 16:50
 * @e-mail mylove.520.y@gmail.com
 * @overview
 */

public class ResultMsg {
    private String code;
    private String result;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "ResultMsg{" +
                "code='" + code + '\'' +
                ", result='" + result + '\'' +
                '}';
    }
}
