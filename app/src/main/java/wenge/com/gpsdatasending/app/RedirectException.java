package wenge.com.gpsdatasending.app;

import com.xuhao.android.libsocket.sdk.ConnectionInfo;

/**
 * Created by xuhao on 2017/6/30.
 */

public class RedirectException extends RuntimeException {
    public ConnectionInfo redirectInfo;

    public RedirectException(ConnectionInfo redirectInfo) {
        this.redirectInfo = redirectInfo;
    }
}
