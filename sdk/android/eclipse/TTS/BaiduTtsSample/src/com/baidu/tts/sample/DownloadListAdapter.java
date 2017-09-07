/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.tts.sample;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baidu.tts.client.model.ModelInfo;

/**
 * @author liweigao 2015年12月11日
 */
public class DownloadListAdapter extends BaseAdapter {
    private List<ModelInfo> mModelInfos;
    private Map<String, ViewHolder> mHolders = new HashMap<String, ViewHolder>();
    private ModelManagerActivity mActivity;

    /**
     * @param modelInfos
     */
    public DownloadListAdapter(List<ModelInfo> modelInfos, ModelManagerActivity activity) {
        super();
        mModelInfos = modelInfos;
        mActivity = activity;
    }

    /*
     * @return
     */
    @Override
    public int getCount() {
        return mModelInfos != null ? mModelInfos.size() : 0;
    }

    /*
     * @param position
     * 
     * @return
     */
    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * @param position
     * 
     * @return
     */
    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * @param position
     * 
     * @param convertView
     * 
     * @param parent
     * 
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            ViewHolder vh = new ViewHolder();
            vh.modelIdTV = (TextView) convertView.findViewById(R.id.modelId);
            vh.modelStateTV = (TextView) convertView.findViewById(R.id.modelState);
            vh.progressBar = (ProgressBar) convertView.findViewById(R.id.progressbar);
            vh.downloadBytesTV = (TextView) convertView.findViewById(R.id.downloadBytes);
            vh.totalBytesTV = (TextView) convertView.findViewById(R.id.totalBytes);
            vh.stopBT = (Button) convertView.findViewById(R.id.stop);
            convertView.setTag(vh);
        }
        String modelId = getModelId(position);
        if (modelId != null) {
            ViewHolder vh = (ViewHolder) convertView.getTag();
            vh.modelIdTV.setText(modelId);
            mHolders.put(modelId, vh);
            vh.stopBT.setTag(modelId);
            vh.stopBT.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    String modelId = (String) v.getTag();
                    mActivity.onModelStop(modelId);
                }
            });
        }
        return convertView;
    }

    private class ViewHolder {
        TextView modelIdTV;
        TextView modelStateTV;
        ProgressBar progressBar;
        TextView downloadBytesTV;
        TextView totalBytesTV;
        Button stopBT;
    }

    public String getModelId(int position) {
        ModelInfo modelInfo = mModelInfos.get(position);
        return modelInfo != null ? modelInfo.getServerId() : null;
    }

    public void updateProgress(String modelId, long downloadBytes, long totalBytes) {
        ViewHolder vh = mHolders.get(modelId);
        vh.downloadBytesTV.setText(String.valueOf(downloadBytes));
        vh.totalBytesTV.setText(String.valueOf(totalBytes));
        vh.progressBar.setMax(100);
        long percent = downloadBytes * 100 / totalBytes;
        vh.progressBar.setProgress((int) percent);
    }

    public void updateState(String modelId, String state) {
        ViewHolder vh = mHolders.get(modelId);
        vh.modelStateTV.setText(state);
    }

}
