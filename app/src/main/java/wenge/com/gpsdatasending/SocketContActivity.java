package wenge.com.gpsdatasending;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.xuhao.android.libsocket.sdk.ConnectionInfo;
import com.xuhao.android.libsocket.sdk.OkSocketOptions;
import com.xuhao.android.libsocket.sdk.SocketActionAdapter;
import com.xuhao.android.libsocket.sdk.bean.IPulseSendable;
import com.xuhao.android.libsocket.sdk.bean.ISendable;
import com.xuhao.android.libsocket.sdk.bean.OriginalData;
import com.xuhao.android.libsocket.sdk.connection.IConnectionManager;
import com.xuhao.android.libsocket.sdk.protocol.IHeaderProtocol;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

import wenge.com.gpsdatasending.app.NoneReconnect;
import wenge.com.gpsdatasending.data.HandShake;
import wenge.com.gpsdatasending.data.LogBean;
import wenge.com.gpsdatasending.data.MsgDataBean;
import wenge.com.gpsdatasending.location.GPSLocationListener;
import wenge.com.gpsdatasending.location.GPSLocationManager;
import wenge.com.gpsdatasending.location.GPSProviderStatus;
import wenge.com.gpsdatasending.utils.LogToFile;

import static android.widget.Toast.LENGTH_SHORT;
import static com.xuhao.android.libsocket.sdk.OkSocket.open;

/**
 * @author Administrator
 */
public class SocketContActivity extends AppCompatActivity {
    private IConnectionManager mManager;
    private ConnectionInfo mInfo;
    private OkSocketOptions mOkOptions;
    private Button mSend;
    public boolean mIsCon = false;


    private SocketActionAdapter adapter = new SocketActionAdapter() {

        @Override
        public void onSocketConnectionSuccess(Context context, ConnectionInfo info, String action) {
            mManager.send(new HandShake());
            mIsCon = true;
        }


        @Override
        public void onSocketDisconnection(Context context, ConnectionInfo info, String action, Exception e) {
            if (e != null) {
                logSend("异常断开:" + e.getMessage());
            } else {
                Toast.makeText(context, "正常断开", LENGTH_SHORT).show();
                logSend("正常断开");
            }
            mIsCon = false;
        }

        @Override
        public void onSocketConnectionFailed(Context context, ConnectionInfo info, String action, Exception e) {
            Toast.makeText(context, "连接失败" + e.getMessage(), LENGTH_SHORT).show();
            logSend("连接失败");
            mIsCon = false;
        }

        @Override
        public void onSocketReadResponse(Context context, ConnectionInfo info, String action, OriginalData data) {
            super.onSocketReadResponse(context, info, action, data);
            String str = new String(data.getBodyBytes(), Charset.forName("utf-8"));
            logRece(str);
        }

        @Override
        public void onSocketWriteResponse(Context context, ConnectionInfo info, String action, ISendable data) {
            super.onSocketWriteResponse(context, info, action, data);
            String str = new String(data.parse(), Charset.forName("utf-8"));
            logSend(str);
        }

        @Override
        public void onPulseSend(Context context, ConnectionInfo info, IPulseSendable data) {
            super.onPulseSend(context, info, data);
            String str = new String(data.parse(), Charset.forName("utf-8"));
            logSend(str);
        }
    };
    private GPSLocationManager gpsLocationManager;
    private TextView mText_gps;
    private Location mLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket_cont);
        mSend = (Button) findViewById(R.id.button);
        mText_gps = (TextView) findViewById(R.id.text_gps);
        LogToFile.init(this);
        initData();
        initOptions();
        content();
    }

    private void content() {
        if (mManager == null) {
            return;
        }
        mManager.registerReceiver(adapter);
        if (!mManager.isConnect()) {
            mManager.connect();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (gpsLocationManager == null) {
            gpsLocationManager = GPSLocationManager.getInstances(this);
        }
        gpsLocationManager.start(new MyListener());
    }

    private void initData() {

        mInfo = new ConnectionInfo("172.0.0.212", 3000);
        mOkOptions = new OkSocketOptions.Builder(OkSocketOptions.getDefault())
                .setReconnectionManager(new NoneReconnect())
                .build();
        mManager = open(mInfo, mOkOptions);
    }

    private void initOptions() {
        //设置自定义解析头
        OkSocketOptions.Builder okOptionsBuilder = new OkSocketOptions.Builder(mOkOptions);
        okOptionsBuilder.setHeaderProtocol(new IHeaderProtocol() {
            @Override
            public int getHeaderLength() {
                return 4;
            }

            @Override
            public int getBodyLength(byte[] header, ByteOrder byteOrder) {
                return (int) header[3];
            }
        });
        mManager.option(okOptionsBuilder.build());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mManager != null) {
            mManager.disConnect();
        }
    }

    private void logSend(String log) {
        LogBean logBean = new LogBean(System.currentTimeMillis(), log);
        System.out.println(logBean.toString());
    }

    private void logRece(String log) {
        LogBean logBean = new LogBean(System.currentTimeMillis(), log);

    }

    public void send(View view) {
        if (mIsCon) {
            String msg;
            if (mLocation == null) {
                msg = "测试坐标：AABBCC:23.1111111,115.1234567:DB";
            } else {
                msg = "真实坐标：AABBCC:" + mLocation.getLongitude() + "," + mLocation.getLatitude() + ":DB";
            }
            Toast.makeText(SocketContActivity.this, msg, Toast.LENGTH_SHORT).show();
            MsgDataBean msgDataBean = new MsgDataBean(msg);
            mManager.send(msgDataBean);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gpsLocationManager != null) {
            gpsLocationManager.stop();
            gpsLocationManager = null;
        }
    }

    class MyListener implements GPSLocationListener {

        @Override
        public void UpdateLocation(Location location) {
            if (location != null) {
                mLocation = location;
                mText_gps.setText("经度：" + location.getLongitude() + "\n纬度：" + location.getLatitude());
                LogToFile.w("location", location.toString());

            }
        }

        @Override
        public void UpdateStatus(String provider, int status, Bundle extras) {
            if ("gps" == provider) {
                Toast.makeText(SocketContActivity.this, "定位类型：" + provider, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void UpdateGPSProviderStatus(int gpsStatus) {
            switch (gpsStatus) {
                case GPSProviderStatus.GPS_ENABLED:
                    Toast.makeText(SocketContActivity.this, "GPS开启", Toast.LENGTH_SHORT).show();
                    break;
                case GPSProviderStatus.GPS_DISABLED:
                    Toast.makeText(SocketContActivity.this, "GPS关闭", Toast.LENGTH_SHORT).show();
                    break;
                case GPSProviderStatus.GPS_OUT_OF_SERVICE:
                    Toast.makeText(SocketContActivity.this, "GPS不可用", Toast.LENGTH_SHORT).show();
                    break;
                case GPSProviderStatus.GPS_TEMPORARILY_UNAVAILABLE:
                    Toast.makeText(SocketContActivity.this, "GPS暂时不可用", Toast.LENGTH_SHORT).show();
                    break;
                case GPSProviderStatus.GPS_AVAILABLE:
                    Toast.makeText(SocketContActivity.this, "GPS可用啦", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }
}
