package com.zhgwu.download_install_apk;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.Arrays;

import io.flutter.plugin.common.EventChannel;

public class InstallApk {
    static public void install(Context context, File downloadedFile, EventChannel.EventSink progressSink) {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".download_install_apk", downloadedFile);
            intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(apkUri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(downloadedFile), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        //SEND INSTALLING EVENT
        if (progressSink != null) {
            context.startActivity(intent);
            progressSink.success(Arrays.asList("" + DownloadInstallApkPlugin.Status.INSTALLING.ordinal(), ""));
            progressSink.endOfStream();
        }
    }
}
