package ee.zed.gearvr360video;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import ee.zed.gearvr360video.model.LocationModel;
import ee.zed.gearvr360video.service.SensorService;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.activity.config.ActivityParams;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;
import lombok.val;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;


import com.opencsv.CSVReader;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.gearvrf.GVRActivity;
import org.gearvrf.scene_objects.GVRVideoSceneObjectPlayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends GVRActivity {

    // Storage Permissions
    private static final int PERMISSIONSE_REQUEST = 15;
    private static String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private LocationGooglePlayServicesProvider provider;

    // device sensor manager
    private SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mMain = new MainScene(this, loadData());
        setMain(mMain, "gvr.xml");
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return mMain.onTouch(e);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
        checkPermissions();
    }

    private List<LocationModel> loadData() {
        File path = Environment.getExternalStoragePublicDirectory("tartu1913");
        File file = new File(path, "locations.txt");
        LinkedList<LocationModel> data = new LinkedList<>();
        try {
            CSVReader reader = new CSVReader(new FileReader(file), '\t');
            String[] r;
            while ((r = reader.readNext()) != null) {
                LocationModel loc = LocationModel.builder()
                        .name(r[0])
                        .lat(Double.parseDouble(r[1]))
                        .lng(Double.parseDouble(r[2]))
                        .radius(Double.parseDouble(r[4]))
                        .bt_address(r[5])
                        //.bt_name(r[6])
                        .video(r[7]).build();
                data.add(loc);
            }
        } catch (IOException e) {

        }
        return data;
    }


    private void checkPermissions() {
        List<String> required_permissions = new LinkedList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            required_permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            required_permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(!required_permissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    required_permissions.toArray(new String[required_permissions.size()]),
                    PERMISSIONSE_REQUEST
            );

        } else {
            startTracking();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        startTracking();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mMain.onPause();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        mMain.onRestart();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!gestureDetector.onTouchEvent(event)){
            return super.onTouchEvent(event);
        } else {
            return true;
        }
    }

    @Override
    public void onUserLeaveHint(){
        mMain.onRestart();
    }
    private MainScene mMain = null;
    private GestureDetector gestureDetector;




    // TRACKING

    private void startTracking() {

        provider = new LocationGooglePlayServicesProvider();
        provider.setCheckLocationSettings(true);

        val activityParams = (new ActivityParams.Builder()).setInterval(0).build();
        val locationParams = LocationParams.NAVIGATION;

        SmartLocation smartLocation = new SmartLocation.Builder(this).logging(true).build();

        smartLocation.location(provider).config(locationParams).start(mMain);
        smartLocation.activity().config(activityParams).start(mMain);


    }

    private void stopTracking() {
        SmartLocation.with(this).location().stop();
        SmartLocation.with(this).activity().stop();
        SmartLocation.with(this).geofencing().stop();
    }

}
