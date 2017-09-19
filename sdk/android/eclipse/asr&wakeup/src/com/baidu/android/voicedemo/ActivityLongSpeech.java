/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.android.voicedemo;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;

import com.baidu.speech.asr.SpeechConstant;
import com.baidu.speech.recognizerdemo.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;


public class ActivityLongSpeech extends Activity {

    private static final String TAG = "ActivityLongSpeech";
    private static final String TEST_PCM_INPUT_FILE = "/sdcard/test.pcm";
    private static final String TEST_RESULT_TXT_FILE = "/sdcard/bds_result.txt";
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
                    Toast.makeText(ActivityLongSpeech.this, "引擎已经就绪, 若识别文件可能等待时间较长, 请耐心等待", Toast.LENGTH_LONG).show();
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
                            mAsrTemp = best_result;
                            mAsrResult += mAsrTemp;
                            mAsrTemp = "";
                        }

                        FileOutputStream out = new FileOutputStream(TEST_RESULT_TXT_FILE);
                        out.write(mAsrResult.getBytes());
                        out.close();
                        String tmp  = mAsrResult + mAsrTemp;
                        if (tmp.length() > 100) {
                            tmp = tmp.substring(tmp.length() - 100);
                        }
                        txtResult.setText(tmp);
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
        start();
        print("已经启动长语音识别, 现在可以说话了, 请确保网络正常。");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        cancel();
    }

    private void start() {
        HashMap intent = new HashMap();
        // 认证相关(BEGIN), key, TODO v3 SDK尚不支持从请在AndroidManifest.xml中直接读取key 和 secret, 为了兼容在此加入补丁代码
        try {
            final ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            final Object configAppId = appInfo.metaData.get("com.baidu.speech.APP_ID");
            final Object configKey = appInfo.metaData.getString("com.baidu.speech.API_KEY");
//            intent.put(SpeechConstant.APP_ID, configAppId); // 认证相关, key, 从开放平台(http://yuyin.baidu.com)中获取的key
//            intent.put("apikey", configKey); // 认证相关, apikey, 从开放平台(http://yuyin.baidu.com)中获取的key
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "请在AndroidManifest.xml中参考文档或demo配置认证信息", Toast.LENGTH_LONG).show();
            return;
        }
        // 认证相关(END)
        intent.put("dec-type", 1);
        intent.put("log_level", 6);
        intent.put("decoder", 0);
        intent.put("vad.endpoint-timeout", 0); // 0 = 长语音，不会自动停止
        intent.put("vad", "dnn");
        intent.put("url", "http://audiotest.baidu.com/v2");
        intent.put("key", "com.baidu.test");
        intent.put("pid", -20);
        if (!new File(TEST_PCM_INPUT_FILE).exists()) {
            Toast.makeText(this, "未找到音频文件" + TEST_PCM_INPUT_FILE + "直接通过麦克风录音", Toast.LENGTH_LONG).show();
        } else {
            intent.put("infile", TEST_PCM_INPUT_FILE);
        }

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
