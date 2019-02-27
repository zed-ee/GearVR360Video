package ee.zed.gearvr360video.hud;

import android.graphics.Color;
import android.view.Gravity;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSphereCollider;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;

import ee.zed.gearvr360video.focus.FocusListener;
import ee.zed.gearvr360video.focus.FocusableSceneObject;

public class Button extends FocusableSceneObject {
    GVRTextViewSceneObject textViewSceneObject;
    public Button(GVRContext gvrContext, Float width, Float height, String text){
        super(gvrContext, width, height);
        textViewSceneObject = new GVRTextViewSceneObject(gvrContext, width, height, text);
        textViewSceneObject.setGravity(Gravity.CENTER);
        textViewSceneObject.setBackgroundColor(Color.LTGRAY);
        textViewSceneObject.setTextSize(5);
        //textViewSceneObject.setTag(location);
        attachComponent(new GVRSphereCollider(gvrContext));
        getCollider().setEnable(true);

        addChildObject(textViewSceneObject);

        super.setFocusListener(new FocusListener() {
            @Override
            public void gainedFocus(FocusableSceneObject object) {
                textViewSceneObject.setBackgroundColor(Color.BLACK);
                //textViewSceneObject.setTextColor(Color.YELLOW);
            }

            @Override
            public void lostFocus(FocusableSceneObject object) {
                textViewSceneObject.setBackgroundColor(Color.LTGRAY);
                //textViewSceneObject.setTextColor(Color.WHITE);
            }
        });

    }

    public void setText(String text) {
        textViewSceneObject.setText(text);
    }

    public void setBackgroundColor(int color) {
        textViewSceneObject.setBackgroundColor(color);
    }
    public void setTextColor(int color) {
        textViewSceneObject.setTextColor(color);
    }
}
