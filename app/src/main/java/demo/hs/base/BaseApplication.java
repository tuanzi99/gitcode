package demo.hs.base;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import androidx.work.Configuration;
import androidx.work.WorkManager;
import android.util.Log;
import android.webkit.WebView;

import com.bumptech.glide.request.target.ViewTarget;

import demo.hs.R;
import demo.hs.api.ExceptionHandle;
import demo.hs.application.AppLifeCycle;
import demo.hs.thread.GeekThreadManager;
import demo.hs.utils.AppUtils;
import demo.hs.utils.RxUtils;
import demo.hs.utils.SharedPreferencesUtil;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;


/**
 * Created by fanjianli on 2018/1/3.
 */
public class BaseApplication extends MultiDexApplication implements Configuration.Provider {

    private static BaseApplication sInstance;

    public static BaseApplication getsInstance() {
        return sInstance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        WorkManager.initialize(this, getWorkManagerConfiguration());
        initWebViewDataDirectory(this);
        sInstance = this;
        RxUtils.init(this);
        ViewTarget.setTagId(R.id.image_glide);
        // TTFUtils.init(this);
        GeekThreadManager.getInstance().init();
        AppUtils.init(this);
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                ExceptionHandle.ResponeThrowable responeThrowable = ExceptionHandle.handleException(throwable);
                Log.e("error is", responeThrowable.message);
                throwable.printStackTrace();
            }
        });
        SharedPreferencesUtil.init(this, "com.hongshu", MODE_PRIVATE);
        registerActivityLifecycleCallbacks(new AppLifeCycle());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }

    }

    /**
     * 得到进程名称
     * @param context
     * @return
     */
    public static String getProcessName(Context context) {
        try {
            if (context == null)
                return null;
            ActivityManager manager = (ActivityManager)
                    context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningAppProcessInfo processInfo :
                    manager.getRunningAppProcesses()) {
                if (processInfo.pid == android.os.Process.myPid()) {
                    return processInfo.processName;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 为webView设置目录后缀
     * @param context
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    public static void initWebViewDataDirectory(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = getProcessName(context);
            if (!context.getPackageName().equals(processName)) {//判断是否是默认进程名称
                WebView.setDataDirectorySuffix(processName);
            }
        }
    }
    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder().build();
    }
}
