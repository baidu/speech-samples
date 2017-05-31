/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.android.voicedemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.ScrollView;
import android.widget.TextView;
import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;

import com.baidu.speech.asr.SpeechConstant;
import com.baidu.speech.recognizerdemo.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class ActivityLongSpeech extends Activity {

    private static final String TAG = "ActivityLongSpeech";
    private EventManager asr;

    private TextView txtResult;
    private TextView txtLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sdk2_api);
        txtResult = (TextView) findViewById(R.id.txtResult);
        txtLog = (TextView) findViewById(R.id.txtLog);
        findViewById(R.id.setting).setVisibility(View.GONE);
        findViewById(R.id.btn).setVisibility(View.GONE);

        // ########### 识别功能 ###########
        // 1) 通过工厂创建语音识别的事件管理器
        asr = EventManagerFactory.create(this, "asr");

        // 2) 注册输出事件的监听器
        asr.registerListener(new EventListener() {

            String mAsrResult = "";
            String mAsrTemp = ""; // 临时识别结果

            @Override
            public void onEvent(String name, String params, byte[] data, int offset, int length) {
                if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_READY)) {
                    // 引擎就绪，可以说话，一般在收到此事件后通过UI通知用户可以说话了
                }
                if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
                    // 临时识别结果, 长语音模式需要从此消息中取出结果
                    try {
                        final JSONObject json = new JSONObject(params);
                        final String result_type = json.getString("result_type");
                        final String best_result = json.getJSONArray("results_recognition").getString(0);

                        print(name + ": " + json.toString(4));

                        if ("partial_result".equals(result_type)) {
                            mAsrTemp = best_result;
                        } else if ("final_result".equals(result_type)) {
                            mAsrResult += mAsrTemp;
                            mAsrTemp = "";
                        }
                        txtResult.setText(mAsrResult + mAsrTemp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_FINISH)) {
                    // 识别结束
                    try {
                        print(name + ": " + new JSONObject(params).toString());

                        print("已经停止长语音识别, 请检查日志确定停止原因。");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                // ... 支持的输出事件和事件支持的事件参数见“输出事件”一节
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 3.1) 发送开始事件（可以通过键值对数组控制识别方式）
        start();
        print("已经启动长语音识别, 现在可以说话了, 请确保网络正常。");
    }

    @Override
    protected void onPause() {
        super.onPause();

        cancel();
    }

    private void start() {
        HashMap intent = new HashMap();
        intent.put("pid", your pid); // 认证相关： pid
        intent.put("key", your key); // 认证相关： 与pid对应
        intent.put("url", your url); // 认证相关： 接入地址
        intent.put("dec-type", 1);
        intent.put("log_level", 6);
        intent.put("decoder", 0);
        intent.put("vad.endpoint-timeout", 0); // 0 = 长语音，不会自动停止
        intent.put("vad", "dnn");

        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
        asr.send(SpeechConstant.ASR_START, new JSONObject(intent).toString(), null, 0, 0);
        try {
            Log.d(TAG, "asr.start " + new JSONObject(intent).toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void cancel() {
        asr.send("asr.cancel", "{}", null, 0, 0);
    }

    private void print(String msg) {
        txtLog.append(msg + "\n");
        ScrollView sv = (ScrollView) txtLog.getParent();
        sv.smoothScrollTo(0, 1000000);
        Log.d(TAG, "----" + msg);
    }
}
