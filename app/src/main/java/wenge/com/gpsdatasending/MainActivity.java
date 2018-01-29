package wenge.com.gpsdatasending;

import android.content.Context;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    //创建lcoationManager对象
    private LocationManager manager;
    private static final String TAG = "LOCATION DEMO";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//        Criteria crit = new Criteria();
//        crit.setAccuracy(Criteria.ACCURACY_FINE);
//        String provider = lm.getBestProvider(crit, true);
//        Location location = lm.getLastKnownLocation(provider);
        //获取系统的服务，

        manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //第一次获得设备的位置

        updateLocation(location);

    }

    /*此处更新一下，当activity不处于活动状态时取消GPS的监听*/

    @Override
    public void onPause(){
        super.onPause();
        manager.removeUpdates(locationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //重要函数，监听数据测试
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
        manager.addGpsStatusListener(new GpsStatus.Listener() {
            @Override
            public void onGpsStatusChanged(int event) {

            }
        });
    }
    //创建一个事件监听器

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateLocation(location);

            double lat = location.getLatitude();
            double lng = location.getLongitude();

            Toast.makeText(MainActivity.this,"纬度："+lat+"   "+"经度："+lng,Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onProviderDisabled(String provider){
            updateLocation(null);

            Log.i(TAG, "Provider now is disabled..");
            Toast.makeText(MainActivity.this,"Provider now is disabled..",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider){
            Log.i(TAG, "Provider now is enabled..");
            Toast.makeText(MainActivity.this,"Provider now is enabled..",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras){
            Toast.makeText(MainActivity.this,provider,Toast.LENGTH_SHORT).show();
        }
    };



//获取用户位置的函数，利用Log显示

    private void updateLocation(Location location) {
        String latLng;
        if (location != null) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            latLng = "Latitude:" + lat + "  Longitude:" + lng;
        } else {
            latLng = "Can't access your location";
        }

        Log.i(TAG, "The location has changed..");
        Log.i(TAG, "Your Location:" +latLng);
        Toast.makeText(this,latLng,Toast.LENGTH_SHORT).show();
    }
}


