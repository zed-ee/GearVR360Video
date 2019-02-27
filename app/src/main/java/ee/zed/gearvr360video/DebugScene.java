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

import java.util.LinkedList;
import java.util.List;

import ee.zed.gearvr360video.focus.OnClickListener;
import ee.zed.gearvr360video.hud.Button;
import ee.zed.gearvr360video.model.LocationModel;
import lombok.val;

class DebugScene extends GVRSceneObject {
    private static final String TAG = "DebugScene";
    private final List<LocationModel> locations;
    private final List<Button> labels;
    private GVRSceneObject object;
    private GVRSceneObject text;
    private GVRSceneObject mPlayedSide;
    Button currentLocationText;
    String welcomeText = "Current location:";

    public DebugScene(GVRContext gvrContext, final MainScene mainScene) {
        super(gvrContext);

        GVRTexture texture = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.mipmap.ic_launcher));
        GVRMaterial material = new GVRMaterial(gvrContext);
        material.setMainTexture(texture);

        object = new GVRCubeSceneObject(gvrContext, true, material);
        object.getTransform().setPosition(2, 3, -6);
        addChildObject(object);
        labels = new LinkedList<>();
        locations = mainScene.getLocations();

        for (int i = 0; i<locations.size(); i++) {
            val location = locations.get(i);
            Button textViewSceneObject = new Button(gvrContext, 0.8f,0.4f, location.name);
            if (location.getVideo().equals("-")) {
                textViewSceneObject.setTextColor(Color.RED);
            } else {
                textViewSceneObject.setTextColor(Color.WHITE);
            }
            val x = i % 4;
            val y = i / 4;
            textViewSceneObject.getTransform().setPosition(x - 1.5f, (y - 0.5f)*0.7f, -3f);
            textViewSceneObject.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick() {
                    mainScene.showVideoScene(location.getVideo());
                }
            });
            textViewSceneObject.setTag(location);
            addChildObject(textViewSceneObject);
        }

        currentLocationText = new Button(gvrContext, 4f,1.4f,welcomeText);
        currentLocationText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick() {
                mainScene.showDefaultScene();
            }
        });
        currentLocationText.getTransform().setPosition(0f, -3f, -4f);

        addChildObject(currentLocationText);


    }

    public void onStep(GVRContext mGVRContext) {
        if (object != null) {
            object.getTransform().rotateByAxis(1, 0.1f, 0.2f, 0.3f);
        }
    }

    public void onLocationUpdated(Location currentLocation) {

        String debugMessage = String.format("lat: %1$,.5f, lng:  %2$,.5f, alt: %3$,.2f \nacc v: %4$,.2fm, h: %5$,.2fm, spd: %6$,.2fkmh",
                currentLocation.getLatitude(), currentLocation.getLongitude(), currentLocation.getAltitude(),
                currentLocation.getAccuracy(), currentLocation.getVerticalAccuracyMeters(), currentLocation.getSpeed());
        currentLocationText.setText(welcomeText+"\n\n"+debugMessage);
        currentLocationText.setTextColor(currentLocation.getAccuracy() < 5 ? Color.GREEN : Color.WHITE);

        for(GVRSceneObject sceneObject : getChildren()) {
            if (sceneObject instanceof Button && sceneObject.getTag() instanceof LocationModel) {
                LocationModel location = (LocationModel) sceneObject.getTag();
                double distance = currentLocation.distanceTo(location.getLocation());
                Button text = (Button) sceneObject;
                if (location.getVideo().equals("-")) {
                    if (distance < location.getRadius() * 1.5) {
                        text.setTextColor(Color.rgb(1f, 0f,1f));
                    } else {
                        text.setTextColor(Color.RED);
                    }
                } else {
                    if (distance < location.getRadius() * 1.5) {
                        text.setTextColor(Color.BLUE);
                    } else {
                        text.setTextColor(Color.WHITE);
                    }
                }
                text.setText(location.name + "\n" + String.format("%1$,.2f", distance) + " m");
            }
        }

    }
}