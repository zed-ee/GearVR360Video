package ee.zed.gearvr360video;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;


import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.AssetDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.FileDataSource;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.scene_objects.GVRVideoSceneObject;
import org.gearvrf.scene_objects.GVRVideoSceneObjectPlayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class VideoScene extends GVRSceneObject {

    private GVRVideoSceneObjectPlayer<ExoPlayer> videoSceneObjectPlayer;
    private GVRActivity mActivity;

    public VideoScene(GVRContext gvrContext, GVRActivity activity, final MainScene mainScene) {
        super(gvrContext);
        mActivity = activity;

        videoSceneObjectPlayer = makeExoPlayer();
        
        GVRSphereSceneObject sphere = new GVRSphereSceneObject(gvrContext, 72, 144, false);

        GVRMesh mesh = sphere.getRenderData().getMesh();
        GVRVideoSceneObject mMovieSceneObject = new GVRVideoSceneObject( gvrContext, mesh, videoSceneObjectPlayer, GVRVideoSceneObject.GVRVideoType.VERTICAL_STEREO );

        float newRadius = 10;
        mMovieSceneObject.getTransform().setScale(newRadius,newRadius,newRadius);

        addChildObject(mMovieSceneObject);

    }

    private GVRVideoSceneObjectPlayer<ExoPlayer> makeExoPlayer() {
        final SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(mActivity,
                new DefaultTrackSelector());
        player.setPlayWhenReady(true);


        return new GVRVideoSceneObjectPlayer<ExoPlayer>() {
            @Override
            public ExoPlayer getPlayer() {
                return player;
            }

            @Override
            public void setSurface(final Surface surface) {
                player.addListener(new Player.DefaultEventListener() {
                    @Override
                    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                        switch (playbackState) {
                            case Player.STATE_BUFFERING:
                                break;
                            case Player.STATE_ENDED:
                                player.seekTo(0);
                                break;
                            case Player.STATE_IDLE:
                                break;
                            case Player.STATE_READY:
                                break;
                            default:
                                break;
                        }
                    }
                });

                player.setVideoSurface(surface);
            }

            @Override
            public void release() {
                player.release();
            }

            @Override
            public boolean canReleaseSurfaceImmediately() {
                return false;
            }

            @Override
            public void pause() {
                player.setPlayWhenReady(false);
            }

            @Override
            public void start() {
                player.setPlayWhenReady(true);
            }

        };
    }

    public void init(GVRContext mGVRContext, String trackName) {

        ExoPlayer player = videoSceneObjectPlayer.getPlayer();

        File path = Environment.getExternalStoragePublicDirectory(
                "tartu1913");

        File video = new File(path, trackName);

        DataSource.Factory dataSourceFactory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                FileDataSource fileDataSource = new FileDataSource();
                Uri uri = fileDataSource.getUri();
                Uri uri2 = Uri.fromFile(video);
                try {
                    fileDataSource.open(new DataSpec(Uri.fromFile(video)));
                } catch (FileDataSource.FileDataSourceException e) {
                    e.printStackTrace();
                }
                return fileDataSource;
            }
        };

        final MediaSource mediaSource = new ExtractorMediaSource(Uri.fromFile(video),
                dataSourceFactory,
                new DefaultExtractorsFactory(), null, null);
        player.prepare(mediaSource, true, true);

    }

    private void prepareExoPlayerFromFileUri(Uri uri){
        ExoPlayer player = videoSceneObjectPlayer.getPlayer();

        DataSpec dataSpec = new DataSpec(uri);
        final FileDataSource fileDataSource = new FileDataSource();
        try {
            fileDataSource.open(dataSpec);
        } catch (FileDataSource.FileDataSourceException e) {
            e.printStackTrace();
        }

        DataSource.Factory factory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return fileDataSource;
            }
        };
        MediaSource audioSource = new ExtractorMediaSource(fileDataSource.getUri(),
                factory, new DefaultExtractorsFactory(), null, null);

        player.prepare(audioSource);
    }
}

