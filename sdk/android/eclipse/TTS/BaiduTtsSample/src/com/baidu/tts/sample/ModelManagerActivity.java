/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.tts.sample;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.model.BasicHandler;
import com.baidu.tts.client.model.Conditions;
import com.baidu.tts.client.model.DownloadHandler;
import com.baidu.tts.client.model.LibEngineParams;
import com.baidu.tts.client.model.ModelBags;
import com.baidu.tts.client.model.ModelFileBags;
import com.baidu.tts.client.model.ModelInfo;
import com.baidu.tts.client.model.ModelManager;
import com.baidu.tts.client.model.OnDownloadListener;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

/**
 * @author liweigao 2015年10月26日
 */
public class ModelManagerActivity extends Activity implements OnClickListener {
    private Button mGetServerList, mGetLocalList, mGetServerListAvailable, mGetLocalListAvailable;
    private Button mGetServerFileInfo, mGetLocalFileInfo, mGetModelDefault, mGetModelFilePath;
    private Button mDownloadList, mStop, mUseModel, mSpeak;
    private EditText mInput;
    private ListView mListView;

    private ModelManager mModelManager;
    private SpeechSynthesizer mSpeechSynthesizer;
    private ConcurrentHashMap<String, DownloadHandler> mDownloadHandlers =
            new ConcurrentHashMap<String, DownloadHandler>();

    private static final int PRINT = 0;
    private static final int UPDATE_PROGRESS = 1;
    private static final int UPDATE_STATE = 2;
    private static final String TAG = "ModelManagerActivity";

    public static final String KEY_MODEL_ID = "modelId";
    public static final String KEY_DOWNLOAD_BYTES = "downloadBytes";
    public static final String KEY_TOTAL_BYTES = "totalBytes";
    public static final String KEY_STATE = "state";

    /*
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modelmanager);
        initialView();
        initialModelManager();
    }

    private void initialView() {
        this.mGetServerList = (Button) this.findViewById(R.id.getServerList);
        this.mGetServerList.setOnClickListener(this);
        this.mGetLocalList = (Button) this.findViewById(R.id.getLocalList);
        this.mGetLocalList.setOnClickListener(this);

        this.mGetServerListAvailable = (Button) this.findViewById(R.id.getServerListAvailable);
        this.mGetServerListAvailable.setOnClickListener(this);
        this.mGetLocalListAvailable = (Button) this.findViewById(R.id.getLocalListAvailable);
        this.mGetLocalListAvailable.setOnClickListener(this);

        this.mGetServerFileInfo = (Button) this.findViewById(R.id.getServerFileInfo);
        this.mGetServerFileInfo.setOnClickListener(this);
        this.mGetLocalFileInfo = (Button) this.findViewById(R.id.getLocalFileInfo);
        this.mGetLocalFileInfo.setOnClickListener(this);

        this.mGetModelDefault = (Button) this.findViewById(R.id.getModelDefault);
        this.mGetModelDefault.setOnClickListener(this);
        this.mGetModelFilePath = (Button) this.findViewById(R.id.getModelFilePath);
        this.mGetModelFilePath.setOnClickListener(this);

        this.mDownloadList = (Button) this.findViewById(R.id.downloadList);
        this.mDownloadList.setOnClickListener(this);
        this.mStop = (Button) this.findViewById(R.id.stop);
        this.mStop.setOnClickListener(this);
        this.mUseModel = (Button) this.findViewById(R.id.useModel);
        this.mUseModel.setOnClickListener(this);
        this.mSpeak = (Button) this.findViewById(R.id.speak);
        this.mSpeak.setOnClickListener(this);
        this.mInput = (EditText) this.findViewById(R.id.input);
        this.mListView = (ListView) this.findViewById(R.id.listview);
        this.mListView.setOnItemClickListener(mListClickListener);
    }

    private void initialModelManager() {
        this.mModelManager = new ModelManager(this);
        this.mSpeechSynthesizer = SpeechSynthesizer.getInstance();
    }

    /*
     * @param v
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.getServerList:
                getServerList();
                break;
            case R.id.getLocalList:
                getLocalList();
                break;
            case R.id.getServerListAvailable:
                getServerListAvailable();
                break;
            case R.id.getLocalListAvailable:
                getLocalListAvailable();
                break;
            case R.id.getServerFileInfo:
                getServerFileInfo();
                break;
            case R.id.getLocalFileInfo:
                getLocalFileInfos();
                break;
            case R.id.getModelDefault:
                getModelDefault();
                break;
            case R.id.getModelFilePath:
                getModelFilePath();
                break;
            case R.id.downloadList:
                downloadList();
                break;
            case R.id.stop:
                stop();
                break;
            case R.id.useModel:
                useModel();
                break;
            case R.id.speak:
                speak();
                break;

            default:
                break;
        }
    }

    private void speak() {
        String text = mInput.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            int result = mSpeechSynthesizer.speak(text);
            toPrint("speak result=" + result);
        }
    }

    //使用model文件
    private void useModel() {
        // AvailableConditions conditions = new AvailableConditions();
        // conditions.appendGender("male");
        //查询本地可用模型信息
        BasicHandler<ModelBags> handler = mModelManager.getLocalModelsAvailable(null);
        //获取查询的结果
        ModelBags bags = handler.get();
        if (bags != null) {
            if (!bags.isEmpty()) {
                List<ModelInfo> infos = bags.getModelInfos();
                ModelInfo mi = infos.get(0);
                String modelId = mi.getServerId();
                //根据modelId判断本地模型信息是否可用
                if (mModelManager.isModelValid(modelId)) {
                    //获取前端模型文件的绝对路径
                    String textFilePath = mModelManager.getTextModelFileAbsPath(modelId);
                    //获取后端模型文件的绝对路径
                    String speechFilePath = mModelManager.getSpeechModelFileAbsPath(modelId);
                    int result = mSpeechSynthesizer.loadModel(speechFilePath, textFilePath);
                    toPrint("loadModel result=" + result);
                }
            }
        }
    }

    //停止下载
    private void stop() {
        mModelManager.stop();
    }

    //显示下载列表
    private void downloadList() {
        //根据so库文件信息查询服务器对应的默认模型信息
        BasicHandler<ModelBags> handler = mModelManager.getServerModelsAvailable(null);
        //获取查询的结果
        ModelBags bags = handler.get();
        if (bags != null) {
            String str = bags.toJson().toString();
            toPrint(str);
            List<ModelInfo> list = bags.getModelInfos();
            DownloadListAdapter adapter = new DownloadListAdapter(list, this);
            mListView.setAdapter(adapter);
        }
    }

    //根据modelId获取本地模型文件的路径
    private void getModelFilePath() {
        String modelId = "4";
        //查询modelId=4的本地前端模型文件路径
        String textFilePath = mModelManager.getTextModelFileAbsPath(modelId);
        //查询modelId=4的本地后端模型文件路径
        String speechFilePath = mModelManager.getSpeechModelFileAbsPath(modelId);
        if (textFilePath != null) {
            toPrint("text=" + textFilePath);
        }
        if (speechFilePath != null) {
            toPrint("speech=" + speechFilePath);
        }
        //获取引擎参数信息，如版本号等信息
        LibEngineParams engineParams = mModelManager.getEngineParams();
        toPrint(engineParams.getResult());
    }

    //获取对应的默认模型信息
    private void getModelDefault() {
        //根据so库文件信息查询服务器对应的默认模型信息
        BasicHandler<ModelBags> handler = mModelManager.getServerDefaultModels();
        ModelBags bags = handler.get();
        if (bags != null) {
            String str = bags.toJson().toString();
            toPrint(str);
        }
    }

    //获取本地模型文件的信息
    private void getLocalFileInfos() {
        Set<String> set = new HashSet<String>();
        set.add("4");
        set.add("5");
        //获取本地模型文件FileId=4、5的信息
        BasicHandler<ModelFileBags> handler = mModelManager.getLocalModelFileInfos(set);
        ModelFileBags bags = handler.get();
        if (bags != null) {
            String str = bags.toJson().toString();
            toPrint(str);
        }
    }

    //获取服务器模型文件的信息
    private void getServerFileInfo() {
        Set<String> set = new HashSet<String>();
        set.add("4");
        set.add("5");
        //获取服务器模型文件fileId=4、5的信息
        BasicHandler<ModelFileBags> handler = mModelManager.getServerModelFileInfos(set);
        ModelFileBags bags = handler.get();
        if (bags != null) {
            String str = bags.toJson().toString();
            toPrint(str);
        }
    }

    //获取本地的可用模型信息
    private void getLocalListAvailable() {
        // AvailableConditions conditions = new AvailableConditions();
        // conditions.appendGender("female");
        //获取本地的可用的所有模型信息
        BasicHandler<ModelBags> handler = mModelManager.getLocalModelsAvailable(null);
        ModelBags bags = handler.get();
        if (bags != null) {
            String str = bags.toJson().toString();
            toPrint(str);
        }
    }

    //获取服务器的可用模型信息
    private void getServerListAvailable() {
        // AvailableConditions conditions = new AvailableConditions();
        // conditions.appendGender("female");
        //获取服务器的可用的所有模型信息
        BasicHandler<ModelBags> handler = mModelManager.getServerModelsAvailable(null);
        ModelBags bags = handler.get();
        if (bags != null) {
            String str = bags.toJson().toString();
            toPrint(str);
        }
    }

    //获取本地的模型信息
    private void getLocalList() {
        //按照条件查询
        Conditions conditions = new Conditions();
        //添加按照性别查询的条件
        conditions.appendGender("male");
        //根据性别的条件查询本地的model信息
        BasicHandler<ModelBags> handler = mModelManager.getLocalModels(conditions);
        ModelBags bags = handler.get();
        if (bags != null) {
            String str = bags.toJson().toString();
            toPrint(str);
        }
    }

    //获取服务器的模型信息
    private void getServerList() {
        //按照条件查询
        Conditions conditions = new Conditions();
        //添加按照性别查询的条件
        conditions.appendGender("female");
        //根据性别的条件查询服务器的model信息
        BasicHandler<ModelBags> handler = mModelManager.getServerModels(conditions);
        ModelBags bags = handler.get();
        if (bags != null) {
            toPrint(bags.toJson().toString());
        }
    }

    private OnItemClickListener mListClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            DownloadListAdapter adapter = (DownloadListAdapter) parent.getAdapter();
            String modelId = adapter.getModelId(position);
            //下载模型文件
            DownloadHandler downloadHandler = mModelManager.download(modelId, new OnDownloadListener() {

                //下载开始回调
                @Override
                public void onStart(String modelId) {
                    toPrint("onStart--modelId=" + modelId);
                    sendStateMsg(modelId, "开始");
                }

                //下载进度回调
                @Override
                public void onProgress(String modelId, long downloadBytes, long totalBytes) {
                    // toPrint("onProgress--modelId=" + modelId + "--db=" + downloadBytes + "--tb=" + totalBytes);
                    sendProgressMsg(modelId, downloadBytes, totalBytes);
                }

                //下载正常结束或出错停止下载时的回调
                @Override
                public void onFinish(String modelId, int code) {
                    toPrint("onFinish--modelId=" + modelId + "--code=" + code);
                    String state = null;
                    if (code == 0) {
                        state = "下载完成";
                    } else if (code == -1005) {
                        state = "已下载";
                    } else {
                        state = "下载失败";
                    }
                    sendStateMsg(modelId, state);
                }
            });
            mDownloadHandlers.put(modelId, downloadHandler);
        }
    };

    private Handler mHandler = new Handler() {

        /*
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what) {
                case PRINT:
                    print(msg);
                    break;
                case UPDATE_PROGRESS:
                    updateProgress(msg);
                    break;
                case UPDATE_STATE:
                    updateState(msg);
                    break;

                default:
                    break;
            }
        }

    };

    private void sendStateMsg(String modelId, String state) {
        Message message = Message.obtain();
        message.what = UPDATE_STATE;
        Bundle bundle = new Bundle();
        bundle.putString(KEY_MODEL_ID, modelId);
        bundle.putString(KEY_STATE, state);
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    private void sendProgressMsg(String modelId, long downloadBytes, long totalBytes) {
        Message message = Message.obtain();
        message.what = UPDATE_PROGRESS;
        Bundle bundle = new Bundle();
        bundle.putString(KEY_MODEL_ID, modelId);
        bundle.putLong(KEY_DOWNLOAD_BYTES, downloadBytes);
        bundle.putLong(KEY_TOTAL_BYTES, totalBytes);
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    private void updateState(Message msg) {
        DownloadListAdapter adapter = (DownloadListAdapter) mListView.getAdapter();
        Bundle bundle = msg.getData();
        String modelId = bundle.getString(KEY_MODEL_ID);
        String state = bundle.getString(KEY_STATE);
        adapter.updateState(modelId, state);
        adapter.notifyDataSetChanged();
    }

    private void updateProgress(Message msg) {
        DownloadListAdapter adapter = (DownloadListAdapter) mListView.getAdapter();
        Bundle bundle = msg.getData();
        String modelId = bundle.getString(KEY_MODEL_ID);
        long downloadBytes = bundle.getLong(KEY_DOWNLOAD_BYTES);
        long totalBytes = bundle.getLong(KEY_TOTAL_BYTES);
        adapter.updateProgress(modelId, downloadBytes, totalBytes);
        adapter.notifyDataSetChanged();
    }

    public void onModelStop(String modelId) {
        DownloadHandler handler = mDownloadHandlers.get(modelId);
        if (handler != null) {
            handler.stop();
        }
        DownloadListAdapter adapter = (DownloadListAdapter) mListView.getAdapter();
        String state = "停止";
        adapter.updateState(modelId, state);
        adapter.notifyDataSetChanged();
    }

    private void toPrint(String str) {
        Message msg = Message.obtain();
        msg.obj = str;
        this.mHandler.sendMessage(msg);
    }

    private void print(Message msg) {
        String message = (String) msg.obj;
        if (message != null) {
            Log.e(TAG, message);
        }
    }

}
