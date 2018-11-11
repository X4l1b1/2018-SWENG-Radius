package ch.epfl.sweng.radius.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.radius.database.Location;
import ch.epfl.sweng.radius.database.User;
import ch.epfl.sweng.radius.R;
import ch.epfl.sweng.radius.utils.LocationUtility;
import ch.epfl.sweng.radius.utils.MapUtility;
import ch.epfl.sweng.radius.utils.TabAdapter;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    //constants
    private static final String TAG = "HomeFragment";
    private static final float DEFAULT_ZOOM = 13f;
    private static final double DEFAULT_RADIUS = 50000; //In meters

    //properties
    private static GoogleMap mobileMap;
    private static MapView mapView;
    private static CircleOptions radiusOptions;
    private static double radius;
    private LocationUtility locUtil;

    private Location myPos;
    private TabAdapter adapter;
    private TabLayout tabLayout;

    private ViewPager viewPager;

    //testing
    private static MapUtility mapListener;
    private static ArrayList<User> users;
    private static ArrayList<Location> usersLoc;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param radiusValue Parameter 1.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(int radiusValue) {
        HomeFragment fragment = new HomeFragment();
        radius = radiusValue * 1000; // converting to meters.
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locUtil = new LocationUtility(new Location());
        radius = DEFAULT_RADIUS;
        users = new ArrayList<User>();
    }

    @Override
    public View onCreateView(LayoutInflater infltr, ViewGroup container, Bundle savedInstanceState) {
        View view = infltr.inflate(R.layout.fragment_home, container, false);

        // Create the tab layout under the map
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        adapter = new TabAdapter(this.getChildFragmentManager());
        adapter.addFragment(new PeopleTab(), "People");
        adapter.addFragment(new TopicsTab(), "Topics");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mapListener = new MapUtility(radius, users);

        mapView = view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(getContext(), "Map is ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mobileMap = googleMap; //use map utility here
        mapListener.getLocationPermission(getContext(), getActivity());

        if (mapListener.getPermissionResult()) {
            mapListener.getDeviceLocation(getActivity()); // use map utility here

            if (ActivityCompat.checkSelfPermission(getContext(),
                   Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                   && ActivityCompat.checkSelfPermission(getContext(),
                   Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                return;
            }

            mobileMap.setMyLocationEnabled(true); initMap();
        }
    }

    public void initMap() {
        if (mapListener.getCurrCoordinates() != null) {
            initCircle(mapListener.getCurrCoordinates());
            moveCamera(mapListener.getCurrCoordinates(), DEFAULT_ZOOM*(float) 0.9);
        }

        // Push current location to DB
        double lat = mapListener.getCurrCoordinates().latitude;
        double lng = mapListener.getCurrCoordinates().longitude;
        myPos = new Location(FirebaseAuth.getInstance().getCurrentUser().getUid(), lat, lng);

        locUtil.setMyPos(myPos);
        // Do locations here
        markNearbyUsers();
    }

    public void initCircle(LatLng currentCoordinates) {
        radiusOptions = new CircleOptions().center(currentCoordinates)
                .strokeColor(Color.RED)
                .fillColor(Color.parseColor("#22FF0000"))
                .radius(radius);

        mobileMap.addCircle(radiusOptions);
    }

    public void moveCamera(LatLng latLng, float zoom) {
        Log.d( TAG, "moveCamera: moving the camera to: lat: "
                + latLng.latitude + " long: " + latLng.longitude);
        mobileMap.moveCamera(CameraUpdateFactory.newLatLngZoom( latLng, zoom));
    }

    public void getUsersInRadius(){

        locUtil.fetchUsersInRadius((int) radius);

        usersLoc = locUtil.getOtherLocations();
        Log.w("Map", "Size of others is " + Integer.toString(usersLoc.size()));

    }

    /**
     * Marks the other users that are within the distance specified by the users.
     * */
    public void markNearbyUsers() {
        mobileMap.clear();
        mobileMap.addCircle(radiusOptions);
        getUsersInRadius();
        for (int i = 0; usersLoc != null && i < usersLoc.size(); i++) {
            markNearbyUser(i, usersLoc.get(i).getMessage(), usersLoc.get(i).getTitle());
        }
    }

    public void markNearbyUser(int indexOfUser, String status, String userName) {

        LatLng newPos = new LatLng(usersLoc.get(indexOfUser).getLatitude(),
                                    usersLoc.get(indexOfUser).getLongitude()    );
        mobileMap.addMarker(new MarkerOptions().position(newPos)
                .title(userName + ": " + status)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

    }
        /*
        if ( mapListener.contains(users.get(indexOfUser).getLocation().getLatitude(),
                users.get(indexOfUser).getLocation().getLongitude()) && !mapListener.speaksSameLanguage(users.get(indexOfUser)))
        {
            LatLng newPos = new LatLng(users.get(indexOfUser).getLocation().getLatitude(),
                    users.get(indexOfUser).getLocation().getLongitude());
            mobileMap.addMarker(new MarkerOptions().position(newPos)
                    .title(userName + ": " + status)
                    .icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        } else if (mapListener.contains(users.get(indexOfUser).getLocation().getLatitude(),
                users.get(indexOfUser).getLocation().getLongitude()) && mapListener.speaksSameLanguage(users.get(indexOfUser))) {
            LatLng newPos = new LatLng(users.get(indexOfUser).getLocation().getLatitude(),
                    users.get(indexOfUser).getLocation().getLongitude());
                    mobileMap.addMarker(new MarkerOptions().position(newPos)
                    .title(userName + ": " + status).icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }
    }
    */
}