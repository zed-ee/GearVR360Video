package ee.zed.gearvr360video.model;

import android.location.Location;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class LocationModel {
    public String name;
    public double lat;
    public double lng;
    public double radius;
    public String video;
    @Builder.Default
    public double distance = -1;
    @Builder.Default
    public boolean inFence = false;

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

