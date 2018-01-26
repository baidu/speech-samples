package com.baidu.android.voicedemo;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.TreeSet;

public class BugCat {
    private static final String BUG_CAT_VERSION = "BugCat-20180829-1";

    private static String[] errors = new String[]{
            "1000", "DNS网络超时, 建议检查网络是否正常",
            "1001", "网络连接超时, 建议检查网络是否正常",
            "1002", "网络读取结果超时, 建议检查网络是否正常",
            "1003", "上行网络连接超时, 建议检查网络是否正常",
            "1004", "上行网络读取结果超时, 建议检查网络是否正常",
            "1005", "下行网络连接超时, 建议检查网络是否正常",
            "1006", "下行网络读取结果超时, 建议检查网络是否正常",
            // 2: 其他网络错误",
            "2000", "网络连接失败, 建议检查网络是否正常",
            "2001", "网络读取结果失败, 建议检查网络是否正常",
            "2002", "上行网络连接失败, 建议检查网络是否正常",
            "2003", "上行网络读取结果失败, 建议检查网络是否正常",
            "2004", "下行网络连接失败, 建议检查网络是否正常",
            "2005", "下行网络连接失败, 建议检查网络是否正常",
            "2006", "下行数据异常, 建议检查网络是否正常",
            "2100", "网络不可用, 请检查是否声明了网络权限并确保没有受到安全软件限制",
            "2101", "无网络权限, 请检查是否声明了网络权限并确保没有受到安全软件限制",
            // 3: 音频错误",
            "3000", "音频异常",
            "3001", "录音机打开失败",
            "3002", "录音机参数错误",
            "3003", "录音机不可用",
            "3006", "录音机读取失败",
            "3007", "录音机关闭失败",
            "3008", "文件打开失败",
            "3009", "文件读取失败",
            "3010", "文件关闭失败",
            "3011", "采样率错误",
            "3100", "VAD异常",
            "3101", "VAD检测到未说话",
            "3102", "VAD检测到语音过短",
            // 4: 服务端错误",
            "4001", "服务端参数错误（-3001）",
            "4002", "服务端协议错误（-3002）",
            "4003", "服务端识别错误（-3003）",
            "4004", "服务端鉴权错误（-3004）",
            // 5: 客户端错误",
            "5001", "客户端无法加载动态库",
            "5002", "客户端识别参数有误",
            "5003", "客户端获取token失败",
            "5004", "客户端解析URL失败",
            "5005", "客户端检测到非https URL",
            // 6: 语音过长",
            "6001", "语音过长",
            // 7: 没有匹配的识别结果",
            "7001", "无匹配识别结果, 一般无需解决, 建议进行友好的交互提示。出现原因一般为: 未说话或声音太小; 环境音过于嘈杂; 多人同时说话; ",
            // 8: 识别引擎繁忙",
            "8001", "识别引擎繁忙",
            // 9: 缺少权限",
            "9001", "无录音权限",
            // 10: 离线识别相关错误",
            "10001", "离线引擎异常",
            "10002", "没有授权文件",
            "10003", "授权文件不可用",
            "10004", "参数设置错误",
            "10005", "引擎没有被初始化",
            "10006", "模型文件不可用",
            "10007", "语法文件不可用",
            "10008", "引擎重置失败",
            "10009", "引擎初始化失败",
            "10010", "引擎释放失败",
            "10011", "引擎不支持",
            "10012", "识别失败",
            // 11: 唤醒相关错误",
            "11001", "唤醒引擎异常",
            "11002", "无授权文件",
            "11003", "授权文件异常",
            "11004", "唤醒异常",
            "11005", "模型文件异常",
            "11006", "引擎初始化失败",
            "11007", "内存分配失败",
            "11008", "引擎重置失败",
            "11009", "引擎释放失败",
            "11010", "引擎不支持该架构",
            "11011", "无识别数据",
    };

    private static HashMap<Integer, String> mapping = new HashMap<Integer, String>();

    static {
        for (int i = 0; i < errors.length; i += 2) {
            mapping.put(Integer.parseInt(errors[i]), errors[i + 1]);
        }
    }

    public static String cat(Context context, int error) {
        String msg = mapping.get(error);

        msg += checkLibForModelVad(context);
        msg += checkLibForDNNVad(context);

        if (msg != null && msg.length() > 0) {
            Toast.makeText(context, BUG_CAT_VERSION + "\n" + msg, Toast.LENGTH_LONG).show();
        }
        return msg;
    }

    static String checkLibForModelVad(Context context) {
        String msg = "";
        File[] files = new File(context.getApplicationInfo().nativeLibraryDir).listFiles();
        TreeSet<String> set = new TreeSet<>();
        for (File file : new File(context.getApplicationInfo().nativeLibraryDir).listFiles()) {
            set.add(file.getName());
        }

        if (!set.contains("libbd_easr_s1_merge_normal_20151216.dat.so")) {
            msg += "未找到libbd_easr_s1_merge_normal_20151216.dat.so, 请检查是否正确集成\n";
        }
        return msg;
    }

    static String checkLibForDNNVad(Context context) {
        String msg = "";
        File[] files = new File(context.getApplicationInfo().nativeLibraryDir).listFiles();
        TreeSet<String> set = new TreeSet<>();
        for (File file : new File(context.getApplicationInfo().nativeLibraryDir).listFiles()) {
            set.add(file.getName());
        }

        if (!set.contains("libglobal.cmvn.so")) {
            msg += "未找到libglobal.cmvn.so, 请检查是否正确集成\n";
        }

        if (!set.contains("libvad.dnn.so")) {
            msg += "未找到libvad.dnn.so, 请检查是否正确集成\n";
        }

        return msg;
    }
}
