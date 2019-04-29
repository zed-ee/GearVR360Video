package ee.zed.gearvr360video.model;

import android.location.Location;

import com.google.android.gms.location.Geofence;

import org.altbeacon.beacon.Beacon;

import java.time.LocalDateTime;

import io.nlopez.smartlocation.geofencing.model.GeofenceModel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class LocationModel {
    public String name;
    public double lat;
    public double lng;
    public double radius;
    public String bt_address;
    public String video;
    @Builder.Default
    public boolean active = false;
    @Builder.Default
    public double geoDistance = 9999.0;
    @Builder.Default
    public double beaconDistance = 9999.0;

    public LocalDateTime fenceEnterTime;
    public LocalDateTime beaconEnterTime;

    @Builder.Default
    private Location location = null;

    public Location getLocation() {
        if (location == null){
            location = new Location(name);
            location.setLatitude(lat);
            location.setLongitude(lng);
        }
        return location;
    }

}

