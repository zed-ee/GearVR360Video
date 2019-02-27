package ee.zed.gearvr360video;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.view.Gravity;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.ISceneObjectEvents;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;

class DebugScene extends GVRSceneObject {
    private static final String TAG = "DebugScene";

    private GVRSceneObject object;
    private GVRSceneObject text;
    private GVRSceneObject mPlayedSide;

    public DebugScene(GVRContext gvrContext, final MainScene mainScene) {
        super(gvrContext);

        GVRTexture texture = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.mipmap.ic_launcher));
        GVRMaterial material = new GVRMaterial(gvrContext);
        material.setMainTexture(texture);

        object = new GVRCubeSceneObject(gvrContext, true, material);
        object.getTransform().setPosition(0, -2, -3);
        addChildObject(object);

        mPlayedSide = new GVRSceneObject(gvrContext, gvrContext.createQuad(
                4.0f, 1f), gvrContext.getAssetLoader()
                .loadTexture(new GVRAndroidResource(gvrContext, R.drawable.dark_gray)));
        mPlayedSide.getRenderData().setRenderingOrder(
                GVRRenderData.GVRRenderingOrder.TRANSPARENT + 2);
        mPlayedSide.getRenderData().setOffset(true);
        mPlayedSide.getRenderData().setOffsetFactor(-2.0f);
        mPlayedSide.getRenderData().setOffsetUnits(-2.0f);
        mPlayedSide.getTransform().setPosition(0f, -0f, -3.1f);

        text = new GVRSceneObject(gvrContext, gvrContext.createQuad(3f,
                0.3f), createText(gvrContext, "Palun liigu kaardil märgitud asukohta"));
        text.getTransform().setPosition(0f, -0f, -3f);
        text.getRenderData().setRenderingOrder(
                GVRRenderData.GVRRenderingOrder.TRANSPARENT + 2);


        addChildObject(mPlayedSide);

        addChildObject(text);


    }


    public void onStep(GVRContext mGVRContext) {
        if (object != null) {
            object.getTransform().rotateByAxis(1, 0.1f, 0.2f, 0.3f);
        }
    }

    public static GVRTexture createText(GVRContext gvrContext, String text) {
        Bitmap bitmap = Bitmap.createBitmap(456, 23, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(24);
        p.setTextAlign(Paint.Align.CENTER);
        p.setColor(Color.RED);

        int x = (int) (canvas.getWidth() / 2.0f);
        int y = canvas.getHeight();

        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawText(text, x, y, p);

        return new GVRBitmapTexture(gvrContext, bitmap);
    }

    public void onLocationUpdated(Location location) {
    }
}