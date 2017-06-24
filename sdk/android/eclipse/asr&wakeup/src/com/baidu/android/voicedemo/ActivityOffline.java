package com.baidu.android.voicedemo;

import android.app.Activity;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.HashMap;

public class ActivityOffline extends Activity {

    private static final String TAG = "ActivityLongSpeech";
    private TextView txtLog;
    private final String DESC_TEXT = "" +
            "离在线语法识别(首次使用需要联网授权)\n" +
            "如果无法正常使用请检查:\n" +
            " 1. 是否在AndroidManifest.xml配置了APP_ID\n" +
            " 2. 是否在开放平台对应应用绑定了包名\n" +
            "\n" +
            "点击开始后你可以说(可以根据语法自行定义离线说法):\n" +
            " 1. 打电话给张三(离线)\n" +
            " 2. 打电话给李四(离线)\n" +
            " 3. 打开计算器(离线)\n" +
            " 4. 明天天气怎么样(需要联网)\n" +
            " ..." +
            "\n";
    private EventManager asr;
    private TextView txtResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sdk2_api);
        findViewById(R.id.setting).setVisibility(View.GONE);
        findViewById(R.id.txtResult).setVisibility(View.GONE);

        txtResult = (TextView) findViewById(R.id.txtResult);
        txtLog = (TextView) findViewById(R.id.txtLog);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                start();
            }
        });

        txtLog.setText(DESC_TEXT);

        // ########### 识别功能 ###########
        // 1) 通过工厂创建语音识别的事件管理器
        asr = EventManagerFactory.create(this, "asr");

        // 2) 注册输出事件的监听器
        asr.registerListener(new EventListener() {

            @Override
            public void onEvent(String name, String params, byte[] data, int offset, int length) {
                print(name + ": " + params);
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
                        txtResult.setText(best_result);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_FINISH)) {
                    // 识别结束
                    try {
                        print(name + ": " + new JSONObject(params).toString());

                        print("已经停止识别, 请检查日志确定停止原因。");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                // ... 支持的输出事件和事件支持的事件参数见“输出事件”一节
            }
        });

        HashMap intent = new HashMap();
        bindParams(intent);
        asr.send(SpeechConstant.ASR_KWS_LOAD_ENGINE, new JSONObject(intent).toString(), null, 0, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Bundle results = data.getExtras();
            ArrayList<String> results_recognition = results.getStringArrayList("results_recognition");
            txtLog.append("识别结果(数组形式): " + results_recognition + "\n");
        }
    }

    public void bindParams(HashMap intent) {
        intent.put("grammar", "asset:///baidu_speech_grammar.bsg"); // 设置离线的授权文件(离线模块需要授权), 该语法可以用自定义语义工具生成, 链接http://yuyin.baidu.com/asr#m5
        //intent.putExtra("slot-data", your slots); // 设置grammar中需要覆盖的词条,如联系人名
        intent.put("decoder", 2); // decoder 可以设置解码器类型, 2=离在线混合识别详见文档
    }

    private void start() {
        HashMap intent = new HashMap();
        bindParams(intent);

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
