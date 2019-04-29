package ee.zed.gearvr360video;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import ee.zed.gearvr360video.service.BluetoothService;
import ee.zed.gearvr360video.service.SensorService;

public class SensorServiceReceiver extends BroadcastReceiver {
    private final MainScene mMainScene;

    public SensorServiceReceiver(MainScene activity) {
        this.mMainScene = activity;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(SensorService.STEP_UPDATE)) {
            boolean isWalking = intent.getBooleanExtra(SensorService.STEPS, false);
            if (isWalking) {
                mMainScene.pauseVideo(true);
            }
        } else if (intent.getAction().equals(SensorService.ANGLE_UPDATE)) {
            double currentAzimuth = intent.getDoubleExtra(SensorService.ANGLE, 0);
            mMainScene.setDeviceRotation((float)currentAzimuth);
        } else if (intent.getAction().equals(BluetoothService.BEACON_IN_RANGE)) {
            String  id = intent.getStringExtra(BluetoothService.BEACON_ID);
            double distance = intent.getDoubleExtra(BluetoothService.DISTANCE, 0);
            mMainScene.beaconUpdated(id, distance);
        }
    }

    /*
     Creates and registers two intent filters - for direction and steps update
     */
    public void regsiterBroadCastReceivers(Activity activity) {
        IntentFilter directionFilter = new IntentFilter(SensorService.ANGLE_UPDATE);
        IntentFilter stepsFilter = new IntentFilter(SensorService.STEP_UPDATE);
        IntentFilter beaconFilter = new IntentFilter(BluetoothService.BEACON_IN_RANGE);
        activity.registerReceiver(this, stepsFilter);
        activity.registerReceiver(this, directionFilter);
        activity.registerReceiver(this, beaconFilter);
    }

}
