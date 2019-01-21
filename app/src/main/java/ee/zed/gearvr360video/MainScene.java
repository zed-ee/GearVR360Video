package ee.zed.gearvr360video;

import android.graphics.Color;
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;

public class MainScene extends GVRMain {

    private GVRContext mGVRContext = null;
    private GVRActivity mActivity = null;

    private DefaultScene mDefaultScene = null;
    private VideoScene mVideoScene = null;
    GVRScene mMainScene = null;
    boolean activeSceneIsDefault = true;
    public MainScene(GVRActivity mainActivity) {
        mActivity = mainActivity;
    }

    /** Called when the activity is first created. */
    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;

        mMainScene = mGVRContext.getMainScene();
        //mainScene.getMainCameraRig().getTransform().setPosition(0.0f, 0.0f, 0.0f);
        GVRCameraRig cameraRig = mMainScene.getMainCameraRig();
        cameraRig.getLeftCamera().setBackgroundColor(Color.WHITE);
        cameraRig.getRightCamera().setBackgroundColor(Color.WHITE);

        mVideoScene = new VideoScene(mGVRContext, mActivity, this);
        mDefaultScene = new DefaultScene(mGVRContext, this);

        mMainScene.addSceneObject(mDefaultScene);
        mGVRContext.setMainScene(mMainScene);

    }

    public void onPause() {
    }

    public void showVideoScene(String trackName) {

        mVideoScene.init(mGVRContext, trackName);
        mMainScene.removeSceneObject(mDefaultScene);
        mMainScene.addSceneObject(mVideoScene);
    }

    public void showDefaultScene() {
        mMainScene.removeSceneObject(mVideoScene);
        mMainScene.addSceneObject(mDefaultScene);
    }


    public boolean onTouch(MotionEvent e) {
        if (activeSceneIsDefault) {
            showVideoScene("");
        } else {
            showDefaultScene();
        }
        activeSceneIsDefault = !activeSceneIsDefault;
        return true;
    }

    @Override
    public void onStep() {
        for(GVRSceneObject sceneObject : mMainScene.getSceneObjects()) {
            if (sceneObject instanceof DefaultScene) {
                ((DefaultScene) sceneObject).onStep(mGVRContext);
            } else if (sceneObject instanceof VideoScene) {
               // ((VideoScene) sceneObject).onStep(mGVRContext);
            }
        }
    }

}
