package ee.zed.gearvr360video;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;

import org.gearvrf.GVRActivity;

public class MainActivity extends GVRActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ExoPlayer player = ExoPlayerFactory.newSimpleInstance(this);

        setMain(new Program());
    }
}
