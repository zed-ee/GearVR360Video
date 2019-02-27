package ee.zed.gearvr360video;

import android.graphics.Color;
import android.location.Location;
import android.view.Gravity;
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;

import java.util.List;

import ee.zed.gearvr360video.model.LocationModel;
import io.nlopez.smartlocation.OnActivityUpdatedListener;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import lombok.val;

public class MainScene extends GVRMain implements OnLocationUpdatedListener, OnActivityUpdatedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private GVRContext mGVRContext = null;
    private GVRActivity mActivity;

    private DebugScene mDebugScene = null;
    private VideoScene mVideoScene = null;
    private GVRTextViewSceneObject textViewSceneObject = null;
    GVRScene mMainScene = null;
    int activeScene = 0;
    List<LocationModel> locations;
    private double geofenceRadius = 3.5;
    private String welcomeText ="Tere tulemast Tartu 1913 virtuaal reaalsusesse.\n\n Elamuse saamiseks liigu kaardil märgitud asukohtadesse";

    public MainScene(GVRActivity mainActivity, List<LocationModel> locations) {
        mActivity = mainActivity;
        this.locations = locations;
    }

    /** Called when the activity is first created. */
    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;

        mMainScene = mGVRContext.getMainScene();
        //mainScene.getMainCameraRig().getTransform().setPosition(0.0f, 0.0f, 0.0f);
        GVRCameraRig cameraRig = mMainScene.getMainCameraRig();
        cameraRig.getLeftCamera().setBackgroundColor(Color.DKGRAY);
        cameraRig.getRightCamera().setBackgroundColor(Color.DKGRAY);

        mVideoScene = new VideoScene(mGVRContext, mActivity, this);
        mDebugScene = new DebugScene(mGVRContext, this);

        //mMainScene.addSceneObject(mDebugScene);
        mGVRContext.setMainScene(mMainScene);

        textViewSceneObject = new GVRTextViewSceneObject(gvrContext, 4,1.2f,welcomeText);
        textViewSceneObject.setGravity(Gravity.CENTER);
        textViewSceneObject.setBackgroundColor(Color.LTGRAY);
        textViewSceneObject.setTextSize(6);
        textViewSceneObject.getTransform().setPosition(0f, -0f, -3f);
        //addChildObject(textViewSceneObject);

        mMainScene.getMainCameraRig().addChildObject(textViewSceneObject);

    }

    public void onPause() {
    }

    public void showVideoScene(String trackName) {
        textViewSceneObject.setEnable(false);

        mVideoScene.init(mGVRContext, trackName);
        mMainScene.removeSceneObject(mDebugScene);
        mMainScene.addSceneObject(mVideoScene);
    }

    public void showDefaultScene() {
        textViewSceneObject.setEnable(true);
        mMainScene.removeSceneObject(mVideoScene);
        mMainScene.removeSceneObject(mDebugScene);
    }

    public void showDebugScene() {
        textViewSceneObject.setEnable(false);
        mMainScene.removeSceneObject(mVideoScene);
        mMainScene.addSceneObject(mDebugScene);
    }

    public boolean onTouch(MotionEvent e) {
        activeScene = (activeScene +1) % 3;
        if (activeScene == 0) {
            showDefaultScene();
        } else if (activeScene == 1) {
            showDebugScene();
        } else {
            showVideoScene("");
        }
        return true;
    }

    @Override
    public void onStep() {
        for(GVRSceneObject sceneObject : mMainScene.getSceneObjects()) {
            if (sceneObject == mDebugScene) {
                ((DebugScene) sceneObject).onStep(mGVRContext);
            } else if (sceneObject instanceof VideoScene) {
               // ((VideoScene) sceneObject).onStep(mGVRContext);
            }
        }
    }

    @Override
    public void onActivityUpdated(com.google.android.gms.location.DetectedActivity detectedActivity) {

    }

    @Override
    public void onLocationUpdated(Location location) {
        String debugMessage = String.format("lat: %1$,.5f, lng:  %2$,.5f, alt: %3$,.2f \nacc v: %4$,.2fm, h: %5$,.2fm, spd: %6$,.2fkmh",
                location.getLatitude(), location.getLongitude(), location.getAltitude(),
                location.getAccuracy(), location.getVerticalAccuracyMeters(), location.getSpeed());
        textViewSceneObject.setText(welcomeText+"\n\n"+debugMessage);

        checkLocation(location);
        mDebugScene.onLocationUpdated(location);
    }

    private void checkLocation(Location currentLocation) {
        Boolean isInArea = false;
        for (val location: locations) {
            double distance = currentLocation.distanceTo(location.getLocation());
            if (distance < geofenceRadius) {
               isInArea = true;
               showVideoScene(location.getVideo());
            }
        }
        if (!isInArea && activeScene == 2){
            showDefaultScene();
        }
    }
}
