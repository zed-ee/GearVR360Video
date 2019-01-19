package ee.zed.gearvr360video;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Surface;


import com.google.android.exoplayer.ExoPlayer;

import org.gearvrf.GVRActivity;
import org.gearvrf.scene_objects.GVRVideoSceneObjectPlayer;

public class MainActivity extends GVRActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoSceneObjectPlayer = makeExoPlayer();
        //setMain(new Program());
        setMain(new MainScene(videoSceneObjectPlayer));
    }

    private GVRVideoSceneObjectPlayer<ExoPlayer> makeExoPlayer() {

        final ExoPlayer player = ExoPlayerFactory.newSimpleInstance(this);

        return new GVRVideoSceneObjectPlayer<ExoPlayer>() {
            @Override
            public ExoPlayer getPlayer() {
                return player;
            }

            @Override
            public void setSurface(Surface surface) {
                ((SimpleExoPlayer) player).setVideoSurface(surface);
            }

            @Override
            public void release() {

            }

            @Override
            public boolean canReleaseSurfaceImmediately() {
                return false;
            }

            @Override
            public void pause() {

            }

            @Override
            public void start() {

            }
        };
    }

    private GVRVideoSceneObjectPlayer<?> videoSceneObjectPlayer;

}
