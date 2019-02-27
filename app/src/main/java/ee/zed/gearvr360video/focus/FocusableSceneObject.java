package ee.zed.gearvr360video.focus;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

public class FocusableSceneObject extends GVRSceneObject {

    private FocusListener focusListener = null;
    private OnClickListener onClickListener = null;
    private boolean focus = false;
    public boolean isVisible = true;

    public FocusableSceneObject(GVRContext context) {
        super(context);
    }

    public FocusableSceneObject(GVRContext context, GVRMesh mesh, GVRTexture texture) {
        super(context, mesh, texture);
    }

    public FocusableSceneObject(GVRContext context, float width, float height, GVRTexture texture) {
        super(context, width, height, texture);
    }
    public FocusableSceneObject(GVRContext context, float width, float height) {
        super(context, width, height);
    }

    public void setFocusListener(FocusListener focusListener) {
        this.focusListener = focusListener;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void dispatchGainedFocus() {
        if (this.focusListener != null) {
            this.focusListener.gainedFocus(this);
        }
    }

    public void dispatchLostFocus() {
        if (this.focusListener != null) {
            focusListener.lostFocus(this);
        }
    }

    public void dispatchInClick() {
        if (this.onClickListener != null && isVisible) {
            this.onClickListener.onClick();
        }
    }

    public void setFocus(boolean state) {
        if (state == true && focus == false) {
            focus = true;
            this.dispatchGainedFocus();
            return;
        }

        if (state == false && focus == true) {
            focus = false;
            this.dispatchLostFocus();
        }
    }
}
