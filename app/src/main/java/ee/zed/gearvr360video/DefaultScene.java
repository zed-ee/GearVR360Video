package ee.zed.gearvr360video;

import android.graphics.Color;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.ISceneObjectEvents;
import org.gearvrf.scene_objects.GVRCubeSceneObject;

class DefaultScene extends GVRSceneObject {
    private static final String TAG = "DefaultScene";

    private GVRSceneObject object;

    public DefaultScene(GVRContext gvrContext, final MainScene mainScene) {
        super(gvrContext);

        GVRTexture texture = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.mipmap.ic_launcher));
        GVRMaterial material = new GVRMaterial(gvrContext);
        material.setMainTexture(texture);

        object = new GVRCubeSceneObject(gvrContext, true, material);
        object.getTransform().setPosition(0, -1, -3);
        addChildObject(object);

    }


    public void onStep(GVRContext mGVRContext) {
        if (object != null) {
            object.getTransform().rotateByAxis(1, 0.1f, 0.2f, 0.3f);
        }
    }
}