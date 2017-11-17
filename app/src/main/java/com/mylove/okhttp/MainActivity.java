package com.mylove.okhttp;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.mylove.loglib.JLog;

import java.util.HashMap;
import java.util.Map;


/**
 * @author myLove
 * @time 2017/11/16 19:18
 * @e-mail mylove.520.y@gmail.com
 * @overview
 */

public class MainActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_main);
        String url = "https://www.yjw1020.club/api/myIndex.php";
        Map<Object, Object> oMap = new HashMap<>();
        oMap.put("type", "1");
        OkHttpUtil.getInstance(this).post(url).async(oMap, new onOkHttpListener<String, String>() {
            public void onCompleted() {
            }

            public void onSuccess(String s) {
                JLog.v(s);
            }

            public void onFailure(String s) {
                JLog.v(s);
            }
        });
    }
}
