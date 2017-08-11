package com.baidu.android.voicedemo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.baidu.speech.*;
import com.baidu.speech.asr.SpeechConstant;
import com.baidu.speech.core.BDSParamBase;
import com.baidu.speech.recognizerdemo.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class ApiActivity extends Activity {
    private static final String TAG = "Sdk2Api";
    private static final int REQUEST_UI = 1;
    private TextView txtLog;
    private Button btn;
    private Button setting;

    public static final int STATUS_None = 0;
    public static final int STATUS_WaitingReady = 2;
    public static final int STATUS_Ready = 3;
    public static final int STATUS_Speaking = 4;
    public static final int STATUS_Recognition = 5;
    private EventManager asr;
    private int status = STATUS_None;
    private TextView txtResult;
    private long speechEndTime = -1;
    private static final int EVENT_ERROR = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sdk2_api);
        txtResult = (TextView) findViewById(R.id.txtResult);
        txtLog = (TextView) findViewById(R.id.txtLog);
        btn = (Button) findViewById(R.id.btn);
        setting = (Button) findViewById(R.id.setting);

        // ########### 识别功能 ###########
        // 1) 通过工厂创建语音识别的事件管理器
        asr = EventManagerFactory.create(this, "asr");

        // 2) 注册输出事件的监听器
        asr.registerListener(new com.baidu.speech.EventListener() {

            String mAsrResult = "";
            String mAsrTemp = ""; // 临时识别结果

            @Override
            public void onEvent(String name, String params, byte[] data, int offset, int length) {
                if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_READY)) {
                    // 引擎就绪，可以说话，一般在收到此事件后通过UI通知用户可以说话了
                    status = STATUS_Ready;
                    print("准备就绪，可以开始说话");
                }
                if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_BEGIN)) {
                    // SDK已经检测到用户在说话
                    time = System.currentTimeMillis();
                    status = STATUS_Speaking;
                    btn.setText("说完了");
                    print("检测到用户的已经开始说话");
                }
                if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_VOLUME)) {
                    // 音量回调
                }
                if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_AUDIO)) {
                    // 音频回调
                }
                if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_END)) {
                    // SDK已经检测到用户在说话
                    speechEndTime = System.currentTimeMillis();
                    status = STATUS_Recognition;
                    print("检测到用户的已经停止说话");
                    btn.setText("识别中");
                }
                if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
                    // 临时识别结果, 长语音模式需要从此消息中取出结果
                    try {
                        final JSONObject json = new JSONObject(params);
                        final String result_type = json.getString("result_type");
                        final String best_result = json.getJSONArray("results_recognition").getString(0);

                        print(name + ": " + json.toString(4));

                        if ("partial_result".equals(result_type)) {
                            print("~临时识别结果：" + best_result);
                            txtResult.setText(best_result);
                        } else if ("final_result".equals(result_type)) {
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_FINISH)) {
                    // 识别结束
                    try {
                        JSONObject json = new JSONObject(params);
                        int error = json.optInt("error");
                        if (error == 0) {
                            long end2finish = System.currentTimeMillis() - speechEndTime;
                            status = STATUS_None;
                            String best_result = json.getJSONArray(SpeechRecognizer.RESULTS_RECOGNITION).getString(0);

                            print("识别成功：" + best_result);
                            String json_res = json.optString("origin_result");
                            try {
                                print("origin_result=\n" + new JSONObject(json_res).toString(4));
                            } catch (Exception e) {
                                print("origin_result=[warning: bad json]\n" + json_res);
                            }
                            btn.setText("开始");
                            String strEnd2Finish = "";
                            if (end2finish < 60 * 1000) {
                                strEnd2Finish = "(waited " + end2finish + "ms)";
                            }
                            txtResult.setText(best_result + strEnd2Finish);
                            time = 0;
                        } else {
                            time = 0;
                            status = STATUS_None;
                            StringBuilder sb = new StringBuilder();
                            switch (error) {
                                case SpeechRecognizer.ERROR_AUDIO:
                                    sb.append("音频问题");
                                    break;
                                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                                    sb.append("没有语音输入");
                                    break;
                                case SpeechRecognizer.ERROR_CLIENT:
                                    sb.append("其它客户端错误");
                                    break;
                                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                                    sb.append("权限不足");
                                    break;
                                case SpeechRecognizer.ERROR_NETWORK:
                                    sb.append("网络问题");
                                    break;
                                case SpeechRecognizer.ERROR_NO_MATCH:
                                    sb.append("没有匹配的识别结果");
                                    break;
                                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                                    sb.append("引擎忙");
                                    break;
                                case SpeechRecognizer.ERROR_SERVER:
                                    sb.append("服务端错误");
                                    break;
                                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                                    sb.append("连接超时");
                                    break;
                                default:
                                    sb.append("其它错误, 请对照错误和错误描述");
                                    break;
                            }
                            sb.append(":" + error);
                            print("识别失败：" + sb.toString());
                            btn.setText("开始");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                // ... 支持的输出事件和事件支持的事件参数见“输出事件”一节
            }
        });
        setting.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.baidu.speech.asr.demo.setting");
                startActivity(intent);
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ApiActivity.this);
                boolean api = sp.getBoolean("api", false);
                if (api) {
                    switch (status) {
                        case STATUS_None:
                            start();
                            btn.setText("取消");
                            status = STATUS_WaitingReady;
                            break;
                        case STATUS_WaitingReady:
                            cancel();
                            status = STATUS_None;
                            btn.setText("开始");
                            break;
                        case STATUS_Ready:
                            cancel();
                            status = STATUS_None;
                            btn.setText("开始");
                            break;
                        case STATUS_Speaking:
                            stop();
                            status = STATUS_Recognition;
                            btn.setText("识别中");
                            break;
                        case STATUS_Recognition:
                            cancel();
                            status = STATUS_None;
                            btn.setText("开始");
                            break;
                    }
                } else {
                    start();
                }
            }
        });
    }

    public void bindParams(HashMap intent) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getBoolean("tips_sound", true)) {
            intent.put(Constant.EXTRA_SOUND_START, R.raw.bdspeech_recognition_start);
            intent.put(Constant.EXTRA_SOUND_END, R.raw.bdspeech_speech_end);
            intent.put(Constant.EXTRA_SOUND_SUCCESS, R.raw.bdspeech_recognition_success);
            intent.put(Constant.EXTRA_SOUND_ERROR, R.raw.bdspeech_recognition_error);
            intent.put(Constant.EXTRA_SOUND_CANCEL, R.raw.bdspeech_recognition_cancel);
        }
        if (sp.contains(Constant.EXTRA_INFILE)) {
            String tmp = sp.getString(Constant.EXTRA_INFILE, "").replaceAll(",.*", "").trim();
            intent.put(Constant.EXTRA_INFILE, tmp);
            intent.put("accept-audio-data", true); // TODO v3 sdk需要此参数来保证数据回传
        }
        if (sp.getBoolean(Constant.EXTRA_OUTFILE, false)) {
            intent.put(Constant.EXTRA_OUTFILE, "sdcard/outfile.pcm");
        }
        if (sp.getBoolean(Constant.EXTRA_GRAMMAR, false)) {
            intent.put(Constant.EXTRA_GRAMMAR, "assets:///baidu_speech_grammar.bsg");
        }
        if (sp.contains(Constant.EXTRA_SAMPLE)) {
            String tmp = sp.getString(Constant.EXTRA_SAMPLE, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.put(Constant.EXTRA_SAMPLE, Integer.parseInt(tmp));
            }
        }
        if (sp.contains(Constant.EXTRA_LANGUAGE)) {
            String tmp = sp.getString(Constant.EXTRA_LANGUAGE, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.put(Constant.EXTRA_LANGUAGE, tmp);
            }
        }
        if (sp.contains(Constant.EXTRA_NLU)) {
            String tmp = sp.getString(Constant.EXTRA_NLU, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.put(Constant.EXTRA_NLU, tmp);
            }
        }

        if (sp.contains(Constant.EXTRA_VAD)) {
            String tmp = sp.getString(Constant.EXTRA_VAD, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.put(Constant.EXTRA_VAD, tmp);
            }
        }
        String prop = null;
        if (sp.contains(Constant.EXTRA_PROP)) {
            String tmp = sp.getString(Constant.EXTRA_PROP, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.put(Constant.EXTRA_PROP, Integer.parseInt(tmp));
                prop = tmp;
            }
        }

        // offline asr
        {
            intent.put(Constant.EXTRA_OFFLINE_ASR_BASE_FILE_PATH, "/sdcard/easr/s_1");
            if (null != prop) {
                int propInt = Integer.parseInt(prop);
                if (propInt == 10060) {
                    intent.put(Constant.EXTRA_OFFLINE_LM_RES_FILE_PATH, "/sdcard/easr/s_2_Navi");
                } else if (propInt == 20000) {
                    intent.put(Constant.EXTRA_OFFLINE_LM_RES_FILE_PATH, "/sdcard/easr/s_2_InputMethod");
                }
            }
            intent.put(Constant.EXTRA_OFFLINE_SLOT_DATA, buildTestSlotData());
        }
    }

    private String buildTestSlotData() {
        JSONObject slotData = new JSONObject();
        JSONArray name = new JSONArray().put("李涌泉").put("郭下纶");
        JSONArray song = new JSONArray().put("七里香").put("发如雪");
        JSONArray artist = new JSONArray().put("周杰伦").put("李世龙");
        JSONArray app = new JSONArray().put("手机百度").put("百度地图");
        JSONArray usercommand = new JSONArray().put("关灯").put("开门");
        try {
            slotData.put(Constant.EXTRA_OFFLINE_SLOT_NAME, name);
            slotData.put(Constant.EXTRA_OFFLINE_SLOT_SONG, song);
            slotData.put(Constant.EXTRA_OFFLINE_SLOT_ARTIST, artist);
            slotData.put(Constant.EXTRA_OFFLINE_SLOT_APP, app);
            slotData.put(Constant.EXTRA_OFFLINE_SLOT_USERCOMMAND, usercommand);
        } catch (JSONException e) {

        }
        return slotData.toString();
    }

    private void start() {
        txtLog.setText("");
        print("点击了“开始”");
        HashMap intent = new HashMap();
        bindParams(intent);

        // 认证相关(BEGIN), key, TODO v3 SDK尚不支持从请在AndroidManifest.xml中直接读取key 和 secret, 为了兼容在此加入补丁代码
        try {
            final ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            final Object configAppId = appInfo.metaData.get("com.baidu.speech.APP_ID");
            final Object configKey = appInfo.metaData.getString("com.baidu.speech.API_KEY");
            intent.put(SpeechConstant.APP_ID, configAppId); // 认证相关, key, 从开放平台(http://yuyin.baidu.com)中获取的key
            intent.put("apikey", configKey); // 认证相关, apikey, 从开放平台(http://yuyin.baidu.com)中获取的key
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "请在AndroidManifest.xml中参考文档或demo配置认证信息", Toast.LENGTH_LONG).show();
            return;
        }
        // 认证相关(END)

        intent.put("dec-type", 1); // SDK的协议号, 0=分包协议, 1=流式协议。TODO 目前需要强制设置为0启动分包协议
        intent.put("log_level", 6); // 打开日志, 不设置则为关闭
        intent.put("decoder", 0); // 使用纯在线识别
        intent.put("vad", "dnn"); // 开启基于dnn的语音活动检测模块
        intent.put("auth", false); // TODO 开启流失协议时(即当dec-type设置为1时), 需要关闭非流式协议的认证
//        intent.put(SpeechConstant.PID, 1932); // 设置产品ID, 见 https://github.com/baidu/speech-samples/wiki/docs-android-v3 2.3输入事件一节的PID参数

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        {

            String args = sp.getString("args", "");
            if (null != args) {
                print("参数集：" + args);
                intent.put("args", args);
            }
        }
        boolean api = sp.getBoolean("api", false);
        if (api) {
            speechEndTime = -1;

            asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
            asr.send(SpeechConstant.ASR_START, new JSONObject(intent).toString(), null, 0, 0);
        } else {
            Toast.makeText(this, "v3版本暂不支持UI模式, 请在设置中取消api参数的勾选", Toast.LENGTH_SHORT).show();
        }

        txtResult.setText("");
        try {
            txtLog.append(new JSONObject(intent).toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void stop() {
        asr.send(SpeechConstant.ASR_STOP, "{}", null, 0, 0);
        print("点击了“说完了”");
    }

    private void cancel() {
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
        status = STATUS_None;
        print("点击了“取消”");
    }

    long time;
    private void print(String msg) {
        long t = System.currentTimeMillis() - time;
        if (t > 0 && t < 100000) {
            txtLog.append(t + "ms, " + msg + "\n");
        } else {
            txtLog.append("" + msg + "\n");
        }
        ScrollView sv = (ScrollView) txtLog.getParent();
        sv.smoothScrollTo(0, 1000000);
        Log.d(TAG, "----" + msg);
    }
}
