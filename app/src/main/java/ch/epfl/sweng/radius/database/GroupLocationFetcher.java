package ch.epfl.sweng.radius.database;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.HashMap;

import ch.epfl.sweng.radius.utils.MapUtility;

public class GroupLocationFetcher implements CallBackDatabase {

    private final Database database = Database.getInstance();
    private ArrayList<MLocation> groupLocations;
    private MapUtility mapUtility;
    private MLocation currentUserLoc;

    public GroupLocationFetcher() {
        groupLocations = new ArrayList<>();
    }

    public GroupLocationFetcher(double radius) {
        groupLocations = new ArrayList<>();
    }

    public void setCurrentUserLoc() {
        currentUserLoc = new MLocation(database.getCurrent_user_id());

        database.readObjOnce(currentUserLoc, Database.Tables.LOCATIONS, new CallBackDatabase() {
            @Override
            public void onFinish(Object value) {
                currentUserLoc = (MLocation) value;
                Log.e("GroupLocationFetcher: ", "currentUser latitude" + currentUserLoc.getLatitude() +
                        "currentUser longitude" + currentUserLoc.getLongitude());
            }

            @Override
            public void onError(DatabaseError error) {
                Log.e("Firebase Error", error.getMessage());
            }
        });
    }

    @Override
    public void onFinish(Object value) {
        System.out.println(currentUserLoc.getLatitude() + " " + currentUserLoc.getLongitude());
        for(MLocation location : (ArrayList<MLocation>) value) {
            System.out.println("location.getID()" + location.getID());
            mapUtility = new MapUtility(location.getRadius());
            mapUtility.setMyPos(location);
            if(mapUtility.contains(currentUserLoc.getLatitude(), currentUserLoc.getLongitude())) {
                recordLocationIfGroup(location);
            }
        }
    }

    @Override
    public void onError(DatabaseError error) {
        Log.e("Firebase", error.getMessage());
    }

    public ArrayList<MLocation> getGroupLocations() {
        return groupLocations;
    }

    private void recordLocationIfGroup(final MLocation location) {
        final Database database = Database.getInstance();
        database.readObjOnce(new MLocation(location.getID()),
                Database.Tables.LOCATIONS,
                new CallBackDatabase() {
                    @Override
                    public void onFinish(Object value) {
                        if (((MLocation) value).getIsGroupLocation() == 1) {
                            groupLocations.add((MLocation) value);
                            Log.e("value.getID()", ((MLocation) value).getID());
                        }
                    }

                    @Override
                    public void onError(DatabaseError error) {
                        Log.e("Firebase Error", error.getMessage());
                    }
                });
    }

}
