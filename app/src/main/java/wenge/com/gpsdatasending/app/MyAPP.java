package wenge.com.gpsdatasending.app;

import android.app.Application;

import com.xuhao.android.libsocket.sdk.OkSocket;

/**
 * Created by WENGE on 2018/1/24.
 * 备注：
 */


public class MyAPP extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        OkSocket.initialize(this, true);
    }
}
