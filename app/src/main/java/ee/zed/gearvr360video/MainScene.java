package ee.zed.gearvr360video;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.location.Location;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;
import org.gearvrf.GVRActivity;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import ee.zed.gearvr360video.focus.FocusableController;
import ee.zed.gearvr360video.focus.OnClickListener;
import ee.zed.gearvr360video.hud.Button;
import ee.zed.gearvr360video.model.LocationModel;
import ee.zed.gearvr360video.service.BluetoothService;
import ee.zed.gearvr360video.service.SensorService;
import io.nlopez.smartlocation.OnActivityUpdatedListener;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import lombok.val;

public class MainScene extends GVRMain implements OnLocationUpdatedListener, OnActivityUpdatedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    protected boolean serviceNotRunning = true;
    protected SensorServiceReceiver mSensorReceiver;

    private GVRContext mGVRContext = null;
    private GVRActivity mActivity;

    private DebugScene mDebugScene = null;
    private VideoScene mVideoScene = null;
    private Button textViewSceneObject = null;
    GVRScene mMainScene = null;
    boolean showMainScene = true;
    Boolean showDebug = false;
    private double minAccuracy = 6.0;
    private Location lastLocation = null;


    List<LocationModel> locations;
    private String welcomeText ="Tere tulemast Tartu 1913 virtuaal reaalsusesse.\n\n Elamuse saamiseks liigu kaardil märgitud asukohtadesse";
    private boolean initialized = false;

    private FocusableController mFocusableController = null;
    private GVRPicker mPicker = null;
    private boolean restart = false;
    private float deviceRotation;

    public MainScene(GVRActivity mainActivity, List<LocationModel> locations) {
        mActivity = mainActivity;
        this.locations = locations;

    }

    /** Called when the activity is first created. */
    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        startService();

        mMainScene = mGVRContext.getMainScene();
        //mainScene.getMainCameraRig().getTransform().setPosition(0.0f, 0.0f, 0.0f);
        GVRCameraRig cameraRig = mMainScene.getMainCameraRig();
        cameraRig.getLeftCamera().setBackgroundColor(Color.DKGRAY);
        cameraRig.getRightCamera().setBackgroundColor(Color.DKGRAY);


        mPicker = new GVRPicker(gvrContext, mMainScene);
        mFocusableController = new FocusableController();
        mMainScene.getEventReceiver().addListener(mFocusableController);


        mVideoScene = new VideoScene(mGVRContext, mActivity, this);
        mDebugScene = new DebugScene(mGVRContext, this);

        //mMainScene.addSceneObject(mDebugScene);
        mGVRContext.setMainScene(mMainScene);

        textViewSceneObject = new Button(gvrContext, 4f,1.2f,welcomeText);
        textViewSceneObject.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick() {
                showDebugScene();
            }
        });
        textViewSceneObject.getTransform().setPosition(0f, -0f, -3.5f);

        mMainScene.getMainCameraRig().addChildObject(textViewSceneObject);
        //mMainScene.addSceneObject(textViewSceneObject);
        initialized = true;
        showDefaultScene();

    }
    // Start the sensorService, this is unbind service
    private void startService() {
        if (serviceNotRunning) {
            mActivity.startService(new Intent(mActivity, SensorService.class));
            mActivity.startService(new Intent(mActivity, BluetoothService.class));
            serviceNotRunning = false;

            mSensorReceiver = new SensorServiceReceiver(this);
            mSensorReceiver.regsiterBroadCastReceivers(mActivity);
        }
    }


    public void onPause() {
        showDefaultScene();
    }
    public void onRestart() {
        restart = true;
    }

    public void showVideoScene(String trackName) {
        if (trackName.equals("-")) {
            showDefaultScene();
            return;
        }
        showMainScene = false;
        textViewSceneObject.setEnable(false);

        mVideoScene.init(mGVRContext, trackName, deviceRotation);
        mMainScene.addSceneObject(mVideoScene);
    }

    public void showDefaultScene() {
        showMainScene = true;
        textViewSceneObject.setEnable(true);
        mMainScene.removeSceneObject(mVideoScene);
    }

    public void showDebugScene() {
        showDebug = !showDebug;
        if (showDebug) {
            textViewSceneObject.setEnable(false);
            //mMainScene.removeSceneObject(mVideoScene);
            mMainScene.addSceneObject(mDebugScene);
        } else {
            if (showMainScene){
                textViewSceneObject.setEnable(false);
            }
            mMainScene.removeSceneObject(mDebugScene);
        }
    }

    public boolean onTouch(MotionEvent e) {
        if (!showDebug) {
            showDebugScene();
            return true;
        } else {
            //debug scene
            return mFocusableController.processClick(mGVRContext);
        }
    }

    @Override
    public void onStep() {
        for(GVRSceneObject sceneObject : mMainScene.getSceneObjects()) {
            if (sceneObject == mDebugScene) {
                ((DebugScene) sceneObject).onStep(mGVRContext);
            } else if (sceneObject instanceof VideoScene) {
               ((VideoScene) sceneObject).onStep(mGVRContext);
            }
        }
    }

    @Override
    public void onActivityUpdated(com.google.android.gms.location.DetectedActivity detectedActivity) {

    }

    @Override
    public void onLocationUpdated(Location location) {
        if(!initialized) return;

        String debugMessage = String.format("lat: %1$,.5f, lng:  %2$,.5f, alt: %3$,.2f \n" +
                        "acc v: %4$,.2fm, h: %5$,.2fm\n " +
                        "bearing %6$,.2f deg, speed: %6$,.2f kmh",
                location.getLatitude(), location.getLongitude(), location.getAltitude(),
                location.getAccuracy(), location.getVerticalAccuracyMeters(),
                location.getBearing(), location.getSpeed());
        textViewSceneObject.setText(welcomeText);
        textViewSceneObject.setTextColor(location.getAccuracy() < 5 ? Color.GREEN : Color.WHITE);
/*
        location.setAccuracy(4);
        location.setLatitude(58.3808087);
        location.setLongitude(26.7253286);
        location.setBearing(270);
        */
        if(location.getAccuracy() < minAccuracy) {
            lastLocation = location;
            checkLocation();
        }
        mDebugScene.onLocationUpdated(location);
    }

    private void checkLocation() {

        LocationModel closestGeoLocation = null;
        LocationModel closestBeaconLocation = null;
        LocationModel lastActive = null;

        for (val location : locations) {
            if (location.isActive()) {
                lastActive = location;
            }

            location.geoDistance = lastLocation != null ? lastLocation.distanceTo(location.getLocation()) : 999.0;
            //location.beaconDistance = location.getBeacon() != null ? location.getBeacon().getDistance() : 999.0;

            if (closestGeoLocation == null || closestGeoLocation.geoDistance > location.geoDistance) {
                closestGeoLocation = location;
            }
            if (closestBeaconLocation == null || closestBeaconLocation.beaconDistance > location.beaconDistance) {
                closestBeaconLocation = location;
            }
        }

        if (closestBeaconLocation != null && closestBeaconLocation.beaconDistance < 10) {
            if(lastActive != closestBeaconLocation) {
                if(lastActive != null) lastActive.setActive(false);
                closestBeaconLocation.setActive(true);
                showVideoScene(closestBeaconLocation.getVideo());
            }
            return;
        }
        if (closestGeoLocation != null && closestGeoLocation.getGeoDistance() < 4) {
            if( lastActive != closestGeoLocation) {
                if(lastActive != null) lastActive.setActive(false);
                closestGeoLocation.setActive(true);
                showVideoScene(closestGeoLocation.getVideo());
            }
            return;
        }
        if (lastActive != null && !showMainScene) {
            lastActive.setActive(false);
            exitVideo(false);
        }
        // no beacon, use gps location
/*
            if (geoDistance < 3 || beaconDistance < 3){
                isInArea = true;
                if (showMainScene) {
                    if(lastLocation != null) {
                        mVideoScene.getTransform().setRotationByAxis(lastLocation.getBearing(), 0, 1, 0);
                    }
                    showVideoScene(location.getVideo());
                    mDebugScene.setActiveLocation(location);
                }
            }
*/

    }

    public List<LocationModel> getLocations() {
        return locations;
    }

    public void beaconUpdated(String id, double distance) {
        if(!initialized) return;
        for(LocationModel location: locations) {
            if (location.bt_address.equals(id)) {
                location.setBeaconDistance(distance);
                location.beaconEnterTime = LocalDateTime.now();
            }
        }

        for(LocationModel location: locations) {
            if(location.getBeaconEnterTime() != null) {
                val duration = Duration.between(location.getBeaconEnterTime(), LocalDateTime.now());
                if (duration.getSeconds() > 15) {
                    location.setBeaconDistance(9999);
                    location.setBeaconEnterTime(null);
                }
            }

        }
        checkLocation();

    }

    public void exitVideo(Boolean timeout) {
        showDefaultScene();
    }

    public void setDeviceRotation(float currentAzimuth) {
        deviceRotation = currentAzimuth;
        //if (mVideoScene != null) {
        //    mVideoScene.getTransform().setRotationByAxis(deviceRotation, 0, 1, 0);
        //}
    }

    public void pauseVideo(boolean movement) {
        if(mVideoScene != null) mVideoScene.pause();
    }

}
