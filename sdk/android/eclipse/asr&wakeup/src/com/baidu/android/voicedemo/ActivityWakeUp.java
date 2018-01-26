package com.baidu.android.voicedemo;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.baidu.speech.recognizerdemo.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ActivityWakeUp extends Activity {
    private static final String TAG = "ActivityWakeUp";
    private TextView txtResult;
    private TextView txtLog;

    private final String DESC_TEXT = "" +
            "唤醒已经启动(首次使用需要联网授权)\n" +
            "如果无法正常使用请检查:\n" +
            " 1. 是否在AndroidManifest.xml配置了APP_ID\n" +
            " 2. 是否在开放平台对应应用绑定了包名\n" +
            "\n";

    private EventManager mWpEventManager;
    private EventManager mAsrEventManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sdk2_api);

        txtResult = (TextView) findViewById(R.id.txtResult);
        txtLog = (TextView) findViewById(R.id.txtLog);
        findViewById(R.id.btn).setVisibility(View.GONE);
        findViewById(R.id.setting).setVisibility(View.GONE);

        txtResult.setText("请说唤醒词:  小度你好 或 百度一下");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 唤醒功能打开步骤
        // 1) 创建唤醒事件管理器
        mWpEventManager = EventManagerFactory.create(ActivityWakeUp.this, "wp");
        // 2) 注册唤醒事件监听器
        mWpEventManager.registerListener(new EventListener() {
            @Override
            public void onEvent(String name, String params, byte[] data, int offset, int length) {
                Log.d(TAG, String.format("event: name=%s, params=%s", name, params));
                try {
                    JSONObject json = new JSONObject(TextUtils.isEmpty(params) ? "{}" : params);
                    if ("wp.data".equals(name)) { // 每次唤醒成功, 将会回调name=wp.data的时间, 被激活的唤醒词在params的word字段
                        String word = json.getString("word");
                        txtLog.append("唤醒成功, 唤醒词: " + word + "\r\n");

                        // 如果需要使用唤醒+识别, 则调用识别功能, 否则请删除该调用逻辑
                        startAsr();
                    } else if ("wp.exit".equals(name)) {
                        txtLog.append("唤醒已经停止: " + params + "\r\n");
                    }
                } catch (JSONException e) {
                    throw new AndroidRuntimeException(e);
                }
            }
        });

        // 3) 通知唤醒管理器, 启动唤醒功能
        HashMap params = new HashMap();
        params.put("kws-file", "assets:///WakeUp.bin"); // 设置唤醒资源, 唤醒资源请到 http://yuyin.baidu.com/wake#m4 来评估和导出
        mWpEventManager.send("wp.start", new JSONObject(params).toString(), null, 0, 0);



        // 如果使用唤醒+识别功能, 则再创建一个asr的EventManager
        mAsrEventManager = EventManagerFactory.create(ActivityWakeUp.this, "asr");

        mAsrEventManager.registerListener(new EventListener() {
            @Override
            public void onEvent(String name, String params, byte[] data, int offset, int length) {
                Log.d(TAG, String.format("event: name=%s, params=%s", name, params));

                if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
                    // 临时识别结果, 长语音模式需要从此消息中取出结果
                    try {
                        final JSONObject json = new JSONObject(params);

                        final String result_type = json.getString("result_type");
                        if ("partial_result".equals(result_type)) { // 处理临时识别结果
                            final String best_result = json.getJSONArray("results_recognition").getString(0);
                            txtResult.setText(best_result);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        txtLog.setText(DESC_TEXT);
    }

    private void startAsr() {
        HashMap intent = new HashMap();
        // 认证相关(BEGIN), key, TODO v3 SDK尚不支持从请在AndroidManifest.xml中直接读取key 和 secret, 为了兼容在此加入补丁代码
        try {
            final ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            final Object configAppId = appInfo.metaData.get("com.baidu.speech.APP_ID");
            final Object configKey = appInfo.metaData.getString("com.baidu.speech.API_KEY");
            intent.put(SpeechConstant.APP_ID, configAppId); // 认证相关, key, 从开放平台(http://yuyin.baidu.com)中获取的key
            intent.put("apikey", configKey); // 认证相关, apikey, 从开放平台(http://yuyin.baidu.com)中获取的key
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        // 认证相关(END)
        intent.put("dec-type", 1);
        intent.put("log_level", 6);
        intent.put("decoder", 0);

        // 唤醒+识别连续说的oneshot场景,需要设置该参数,
        // AUDIO_MILLS参数用于识别起始时间
        // 默认为当前时刻,即currentTimeMillis
        // 为了保证唤醒词也被识别, 需要根据自定义的唤醒词长度设置一个合理的回退时间
        // 对应"小度你好" "百度一下"这2个唤醒词, 可以参考下面这行设置
        intent.put(SpeechConstant.AUDIO_MILLS, System.currentTimeMillis() - 1500);

        intent.put("vad", "dnn");
        mAsrEventManager.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
        mAsrEventManager.send(SpeechConstant.ASR_START, new JSONObject(intent).toString(), null, 0, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 停止唤醒监听
        mWpEventManager.send("wp.stop", null, null, 0, 0);
    }
}
