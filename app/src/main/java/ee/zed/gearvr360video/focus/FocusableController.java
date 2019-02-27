package ee.zed.gearvr360video.focus;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IPickEvents;

public final class FocusableController implements IPickEvents {

    private FocusableSceneObject currentFocused = null;

    @Override
    public void onPick(GVRPicker picker) {

    }

    @Override
    public void onNoPick(GVRPicker picker) {

    }

    @Override
    public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
        if (FocusableSceneObject.class.isAssignableFrom(sceneObj.getClass())) {
            currentFocused = (FocusableSceneObject)sceneObj;
            currentFocused.setFocus(true);
        }
    }

    @Override
    public void onExit(GVRSceneObject sceneObj) {
        if (FocusableSceneObject.class.isAssignableFrom(sceneObj.getClass())) {
            currentFocused = (FocusableSceneObject)sceneObj;
            currentFocused.setFocus(false);
        }
        currentFocused = null;
    }

    @Override
    public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {

    }

    public boolean processClick(GVRContext context)
    {
        if (currentFocused != null) {
            currentFocused.dispatchInClick();
            return true;
        }
        return false;
    }
}
