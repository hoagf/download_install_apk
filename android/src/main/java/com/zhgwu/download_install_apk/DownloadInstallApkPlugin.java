package com.zhgwu.download_install_apk;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.PluginRegistry;

public class DownloadInstallApkPlugin implements FlutterPlugin, EventChannel.StreamHandler {
    //CONSTANTS
    private static final String ARG_URL = "url";
    private static final String INSTALL_PATH = "path";
    public static final String TAG = "UpdatePlugin";
    //BASIC PLUGIN STATE
    private Context context;
    private Activity activity;
    private EventChannel.EventSink progressSink;
    private static String url = "";


    private void initialize(Context context, BinaryMessenger messanger) {
        final EventChannel progressChannel = new EventChannel(messanger, "com.zhgwu.download.install.apk");
        progressChannel.setStreamHandler(this);
        new EventChannel(messanger, "com.zhgwu.install.apk").setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object arguments, EventChannel.EventSink events) {
                Log.d(TAG, "INSTALL OPENED");
                InstallApk.install(context, new File(((Map) arguments).get(INSTALL_PATH).toString()), events);
            }

            @Override
            public void onCancel(Object arguments) {
                Log.d(TAG, "INSTALL CLOSED");
            }
        });
        this.context = context;
    }


    public static void registerWith(PluginRegistry.Registrar registrar) {
        Log.d(TAG, "registerWith");
        DownloadInstallApkPlugin plugin = new DownloadInstallApkPlugin();
        plugin.initialize(registrar.context(), registrar.messenger());
        plugin.activity = registrar.activity();
    }


    @Override
    public void onAttachedToEngine(FlutterPluginBinding binding) {
        Log.d(TAG, "onAttachedToEngine");
        initialize(binding.getApplicationContext(), binding.getBinaryMessenger());
    }

    @Override
    public void onDetachedFromEngine(FlutterPluginBinding binding) {
        Log.d(TAG, "onDetachedFromEngine");
    }

    //STREAM LISTENER
    @Override
    public void onListen(Object arguments, EventChannel.EventSink events) {
        if (progressSink != null) {
            progressSink.error("" + Status.ALREADY_RUNNING_ERROR.ordinal(), "Method call was cancelled. One method call is already running!", null);
        }
        Log.d(TAG, "STREAM OPENED");
        progressSink = events;
        //READ URL FROM CALL
        Map argumentsMap = ((Map) arguments);
        url = argumentsMap.get(ARG_URL).toString();

        executeDownload();
    }


    @Override
    public void onCancel(Object o) {
        Log.d(TAG, "STREAM CLOSED");
        progressSink = null;
    }

    private void executeDownload() {
        //PREPARE URLS
        new DownloadTask(context, progressSink).execute(url);
    }


    public enum Status {
        DOWNLOADING,
        DOWNLOAD_ERROR,
        INSTALLING,
        INSTALL_ERROR,
        ALREADY_RUNNING_ERROR,
    }
}
