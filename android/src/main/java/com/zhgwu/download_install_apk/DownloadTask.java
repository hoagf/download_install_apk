package com.zhgwu.download_install_apk;

import static com.zhgwu.download_install_apk.DownloadInstallApkPlugin.Status.DOWNLOADING;
import static com.zhgwu.download_install_apk.DownloadInstallApkPlugin.Status.DOWNLOAD_ERROR;

import android.content.Context;
import android.content.Intent;
import android.net.SSLCertificateSocketFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.content.FileProvider;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import javax.net.ssl.HttpsURLConnection;

import io.flutter.plugin.common.EventChannel;


public class DownloadTask extends AsyncTask<String, Integer, String> {
    /**const*/
    private static final String TAG = "DownloadTask";
    private static int currentProgress = 0;

    private EventChannel.EventSink progressSink;
    private Context context;
    //private PowerManager.WakeLock mWakeLock;


    private File file;
    private String fileName = "";

    public DownloadTask(Context context, EventChannel.EventSink progressSink) {
        this.context = context;
        this.progressSink = progressSink;
        currentProgress = 0;
    }

    @Override
    protected String doInBackground(String... sUrl) {
        InputStream input = null;
        OutputStream output = null;
        HttpsURLConnection connection = null;
        String[] urlSplit = sUrl[0].split("/");
        fileName = urlSplit[urlSplit.length-1];


        try {
            file = new File(context.getCacheDir(), fileName);
            if (file.exists()) {
                if (!file.delete()) {
                    String error = "ERROR: unable to delete old apk file before starting OTA";
                    Log.e(TAG, error);
                    return error;
                }
            }
            if (!file.createNewFile()) {
                    String error = "ERROR: unable to create apk file before starting OTA";
                    Log.e(TAG, error);
                    return error;
            }
            Log.d(TAG, "DOWNLOAD STARTING");

            URL url = new URL(sUrl[0]);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(SSLCertificateSocketFactory.getInsecure(0, null));
            connection.setHostnameVerifier(new AllowAllHostnameVerifier());
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setConnectTimeout(60000);
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                String result = "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
                return result;
            }
            int fileLength = connection.getContentLength();

            input = connection.getInputStream();
            output = new FileOutputStream(file.toString());

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;
                // publishing the progress....
                if (fileLength > 0){
                    int progress = (int) (total * 100 / fileLength);
                    if (progress!=currentProgress){
                        currentProgress = progress;
                        publishProgress(progress);
                    }
                }
                output.write(data, 0, count);
            }
            Log.e(TAG, "DOWNLOAD SUCCESS: " + file.toString());
        } catch (Exception e) {
            Log.d(TAG, "DOWNLOAD FAIL: " + e.toString());
            return e.getMessage();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
       // mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
       //         getClass().getName());
       // mWakeLock.acquire();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        progressSink.success(Arrays.asList("" + DOWNLOADING.ordinal(), "" + progress[0]));

    }

    @Override
    protected void onPostExecute(String result) {
       // mWakeLock.release();

        if (result==null){
            if (!file.exists()) {
                if (progressSink != null) {
                    progressSink.error("" + DOWNLOAD_ERROR.ordinal(), "File was not downloaded", null);
                    progressSink.endOfStream();
                    progressSink = null;
                }
                return;
            }
            InstallApk.install(context, file, progressSink);
        }else {
            progressSink.error("" + DOWNLOAD_ERROR.ordinal(), result, null);
        }
    }




}