package ee.zed.gearvr360video;

import android.graphics.Color;
import android.location.Location;
import android.view.Gravity;
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;

import java.util.List;

import ee.zed.gearvr360video.focus.FocusableController;
import ee.zed.gearvr360video.focus.OnClickListener;
import ee.zed.gearvr360video.hud.Button;
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
    private Button textViewSceneObject = null;
    GVRScene mMainScene = null;
    int activeScene = 0;
    List<LocationModel> locations;
    private String welcomeText ="Tere tulemast Tartu 1913 virtuaal reaalsusesse.\n\n Elamuse saamiseks liigu kaardil märgitud asukohtadesse";
    private boolean initialized = false;

    private FocusableController mFocusableController = null;
    private GVRPicker mPicker = null;

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
        showDebugScene();


    }

    public void onPause() {
    }

    public void showVideoScene(String trackName) {
        if (trackName.equals("-")) {
            activeScene = 0;
            return;
        }
        activeScene = 2;
        textViewSceneObject.setEnable(false);

        mVideoScene.init(mGVRContext, trackName);
        mMainScene.removeSceneObject(mDebugScene);
        mMainScene.addSceneObject(mVideoScene);
    }

    public void showDefaultScene() {
        activeScene = 0;
        textViewSceneObject.setEnable(true);
        mMainScene.removeSceneObject(mVideoScene);
        mMainScene.removeSceneObject(mDebugScene);
    }

    public void showDebugScene() {
        activeScene = 1;
        textViewSceneObject.setEnable(false);
        mMainScene.removeSceneObject(mVideoScene);
        mMainScene.addSceneObject(mDebugScene);
    }

    public boolean onTouch(MotionEvent e) {
        if (activeScene == 0) {
            showDebugScene();
            return true;
        } else if (activeScene == 2) {
            showDefaultScene();
            return true;
        } else {
            return mFocusableController.processClick(mGVRContext);
        }
        // if (activeScene == 0) {
        //     showDefaultScene();
        // } else if (activeScene == 1) {
        //     showDebugScene();
        // } else {
        //     showVideoScene("");
        // }
        // activeScene = (activeScene +1) % 3;
        // return true;
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
        if(!initialized) return;

        String debugMessage = String.format("lat: %1$,.5f, lng:  %2$,.5f, alt: %3$,.2f \nacc v: %4$,.2fm, h: %5$,.2fm, spd: %6$,.2fkmh",
                location.getLatitude(), location.getLongitude(), location.getAltitude(),
                location.getAccuracy(), location.getVerticalAccuracyMeters(), location.getSpeed());
        textViewSceneObject.setText(welcomeText+"\n\n"+debugMessage);
        textViewSceneObject.setTextColor(location.getAccuracy() < 5 ? Color.GREEN : Color.WHITE);

        if (location.getAccuracy() < 5) {
            checkLocation(location);
        }
        mDebugScene.onLocationUpdated(location);
    }

    private void checkLocation(Location currentLocation) {
        Boolean isInArea = false;
        for (val location: locations) {
            double distance = currentLocation.distanceTo(location.getLocation());
            if (distance < location.getRadius()) {
               isInArea = true;
               showVideoScene(location.getVideo());
            }
        }
        if (!isInArea && activeScene == 2){
            showDefaultScene();
       }
    }

    public List<LocationModel> getLocations() {
        return locations;
    }
}
