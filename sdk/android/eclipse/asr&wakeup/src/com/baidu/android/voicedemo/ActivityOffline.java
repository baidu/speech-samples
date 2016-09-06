package com.baidu.android.voicedemo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import com.baidu.speech.VoiceRecognitionService;
import com.baidu.speech.recognizerdemo.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ActivityOffline extends Activity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sdk2_api);
        findViewById(R.id.setting).setVisibility(View.GONE);
        findViewById(R.id.txtResult).setVisibility(View.GONE);

        txtLog = (TextView) findViewById(R.id.txtLog);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.baidu.action.RECOGNIZE_SPEECH");
                intent.putExtra("grammar", "asset:///baidu_speech_grammar.bsg"); // 设置离线的授权文件(离线模块需要授权), 该语法可以用自定义语义工具生成, 链接http://yuyin.baidu.com/asr#m5
                //intent.putExtra("slot-data", your slots); // 设置grammar中需要覆盖的词条,如联系人名
                startActivityForResult(intent, 1);

                txtLog.setText(DESC_TEXT);
            }
        });

        txtLog.setText(DESC_TEXT);
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
}
