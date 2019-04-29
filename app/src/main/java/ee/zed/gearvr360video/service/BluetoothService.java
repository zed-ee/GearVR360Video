package ee.zed.gearvr360video.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

import androidx.annotation.Nullable;
import lombok.val;

public class BluetoothService extends Service implements RangeNotifier{
    final private String IBEACON_LAYOUT = "m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24";
    private static final String TAG = "ee.zed.service.BluetoothService";
    public static final String BEACON_IN_RANGE = TAG + ".action.BEACON_IN_RANGE";
    public static final String BEACON_ID = "BEACON_ID";
    public static final String DISTANCE = "DISTANCE";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setupBeacons();
        return START_NOT_STICKY;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setupBeacons() {
        val beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.setForegroundBetweenScanPeriod(0);

        // Add all the beacon types we want to discover
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_LAYOUT));
        beaconManager.addRangeNotifier(this);
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("com.bridou_n.beaconscanner", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
        for(Beacon beacon: collection){
            Intent intent = new Intent(BEACON_IN_RANGE);
            intent.putExtra(BEACON_ID, beacon.getBluetoothAddress());
            intent.putExtra(DISTANCE, beacon.getDistance());
            sendBroadcast(intent);
        }
    }
}
