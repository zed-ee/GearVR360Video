package ee.zed.gearvr360video;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.view.Gravity;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;
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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import ee.zed.gearvr360video.focus.OnClickListener;
import ee.zed.gearvr360video.hud.Button;
import ee.zed.gearvr360video.hud.Panel;
import ee.zed.gearvr360video.model.LocationModel;
import lombok.val;

import static java.lang.Math.min;

class DebugScene extends GVRSceneObject {
    private static final String TAG = "DebugScene";
    private final List<LocationModel> locations;
    private final List<Button> labels;
    private GVRSceneObject object;
    private GVRSceneObject text;
    private GVRSceneObject mPlayedSide;
    Panel currentLocationText;
    String welcomeText = "Current location:";
    private LocationModel activeLocation;

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
        Button back = new Button(gvrContext, 0.8f,0.2f, "Back");
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick() {
                mainScene.showDefaultScene();
            }
        });
        back.getTransform().setPosition(0f, -2f, -4f);
        addChildObject(back);

        currentLocationText = new Panel(gvrContext, 4f,1.4f,welcomeText);

        currentLocationText.getTransform().setPosition(0f, -1.5f, -5f);
        currentLocationText.setBackgroundColor(Color.BLACK);
        addChildObject(currentLocationText);


    }

    public void onStep(GVRContext mGVRContext) {
        if (object != null) {
            object.getTransform().rotateByAxis(1, 0.1f, 0.2f, 0.3f);
        }
    }

    public void onLocationUpdated(Location currentLocation) {
        String debugMessage = String.format("lat: %1$,.5f, lng:  %2$,.5f, alt: %3$,.2f \n" +
                        "acc v: %4$,.2fm, h: %5$,.2fm\n " +
                        "bearing %6$,.2f deg, speed: %6$,.2f kmh",
                currentLocation.getLatitude(), currentLocation.getLongitude(), currentLocation.getAltitude(),
                currentLocation.getAccuracy(), currentLocation.getVerticalAccuracyMeters(),
                currentLocation.getBearing(), currentLocation.getSpeed());
        currentLocationText.setText(welcomeText+"\n\n"+debugMessage);
        currentLocationText.setTextColor(currentLocation.getAccuracy() < 5 ? Color.GREEN : Color.WHITE);

        for(GVRSceneObject sceneObject : getChildren()) {
            if (sceneObject instanceof Button && sceneObject.getTag() instanceof LocationModel) {
                LocationModel location = (LocationModel) sceneObject.getTag();
                double geoDistance = currentLocation.distanceTo(location.getLocation());
                double beaconDistance = location.getBeaconDistance();
                double distance = min(geoDistance, beaconDistance);
                Button text = (Button) sceneObject;
                if (location.getVideo().equals("-")) {
                    if (distance < location.getRadius() * 1.5) {
                        text.setTextColor(Color.rgb(1f, 0f,1f));
                    } else {
                        text.setTextColor(Color.RED);
                    }
                } else {
                    if (distance < location.getRadius() * 1.5) {
                        text.setTextColor(activeLocation == location ? Color.GREEN : Color.BLUE );
                    } else {
                        text.setTextColor(Color.WHITE);
                    }
                }
                text.setText(location.name + "\ngps:" + String.format("%1$,.2f", geoDistance) + " m"+ "\nbeacon:" + String.format("%1$,.2f", beaconDistance) + " m");
            }
        }

    }

    public void setActiveLocation(LocationModel location) {
        activeLocation = location;
    }
}