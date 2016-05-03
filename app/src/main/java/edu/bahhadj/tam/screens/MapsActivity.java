package edu.bahhadj.tam.screens;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.Stack;

import edu.bahhadj.tam.R;
import edu.bahhadj.tam.adapters.ActiveUserAdapter;
import edu.bahhadj.tam.models.ActiveUserDataObj;
import edu.bahhadj.tam.utils.Coords;
import edu.bahhadj.tam.utils.DateTime;
import edu.bahhadj.tam.utils.Itinerary;
import edu.bahhadj.tam.utils.Locations;
import edu.cjj.sva.JJSearchView;
import edu.cjj.sva.anim.controller.JJDotGoPathController;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, DirectionCallback {

    private GoogleMap mMap;
    private static final String FIREBASE_URL = "https://blazing-heat-8520.firebaseio.com/";
    private Firebase firebaseRef;
    private static final String FIREBASE_STRING = "https://itinerarydb.firebaseio.com/";
    private Firebase fbRef ;
    private final String SERVER_API_KEY = "AIzaSyDaOSGWqwh7PT4elH60TLpqKMsQIwpJ5Vk";

    private JJSearchView mJJSearchView;
    private Handler uiHandler;
    private final float CAMERA_ZOOM_MOVEMENT = 17.0f; //2.0 max zoom-out and 21.0 is max zoom-in
    private final float CAMERA_ZOOM_ANIM = 18.0f;

    private final int PLACE_PICKER_REQUEST = 101;
    private View ivSearch;
    private boolean isFirstTime;
    private Stack<Marker> markerStack;
    private final int paddingBound = 60;
    private Polyline itineraryLines;
    private Marker objectiveMarker;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView lvUsers;
    private ActiveUserAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_new);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initViewsVars();
    }


    private void initViewsVars() {

        uiHandler = new Handler();
        markerStack = new Stack<>();
        isFirstTime = true;

        initDrawerLayout();

        mJJSearchView = (JJSearchView) findViewById(R.id.jjsv);
        mJJSearchView.setController(new JJDotGoPathController());
        ivSearch = findViewById(R.id.ivSearch);

        Firebase.setAndroidContext(this);
        firebaseRef = new Firebase(FIREBASE_URL);
        fbRef = new Firebase(FIREBASE_STRING);
    }

    private void initDrawerLayout() {

        lvUsers = (ListView) findViewById(R.id.lvActiveUser);
        mAdapter = new ActiveUserAdapter(getSampleDataForSlider());
        lvUsers.setAdapter(mAdapter);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
                getSupportActionBar().setTitle(R.string.drawer_open);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
                getSupportActionBar().setTitle(R.string.drawer_close);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                //toolbar.setAlpha(1 - slideOffset / 2);
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        lvUsers.setOnItemClickListener(new MySlideMenuClickListener());
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
    }

    public ArrayList<ActiveUserDataObj> getSampleDataForSlider() {

        //// TODO: 5/3/2016   this sample data; change this when u read from firebase
        ArrayList<ActiveUserDataObj> list = new ArrayList<>();
        for(int i = 0; i < 5; i++){
            list.add(new ActiveUserDataObj("Name "+i, String.valueOf(10*i)));
        }
        return list;
    }


    private class MySlideMenuClickListener implements ListView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //// TODO: 5/3/2016  do necessary changes to map routes and marker here when a user is selected.
            mDrawerLayout.closeDrawer(lvUsers);
        }
    }


    protected Coords startCoords = new Coords(33.927197, -6.897140);
    protected DateTime startDT = new DateTime("HM", "Date");
    protected String idUser = "normalUser";
    protected Locations normalUser = new Locations(idUser, startCoords, startDT);

    private LatLng chuLatLng = new LatLng(33.986633, -6.854197);


    protected void setUpEventListener() {
        firebaseRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                Log.d("dj", "call back - onDataChange()");
                //Toast.makeText(getApplicationContext(), "There are " + snapshot.getChildrenCount() + " locations", Toast.LENGTH_SHORT).show();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Locations post = postSnapshot.getValue(Locations.class);

                    DateTime finalDate = post.getTime();
                    String FinalDate = finalDate.getDate();
                    Date today = Calendar.getInstance().getTime();
                    DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                    String reportDate = df.format(today);
                    //Comparing dates
                    Log.d("dj", "date - onDataChange(): " + FinalDate);
                    boolean dateStat = FinalDate.equals(reportDate);
                    Log.d("dj", "date comparison status - onDataChange(): " + dateStat);
                    if (dateStat) {

                        Coords latlong = post.getCoords();
                        Locations normalUser = new Locations(post.getIdUser(), latlong, post.getTime());
                        LatLng point = new LatLng(normalUser.getCoords().getLatitude(), normalUser.getCoords().getLongitude());
                        Log.d("dj", "LatLng - onDataChange(normalUser marker): " + point);

                        if (isFirstTime) {
                            isFirstTime = false;
                            boundOriginDest(normalUser);
                        } else {
                            showUpOnMap(point);
                        }
                    }

                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Toast.makeText(getApplicationContext(), "The read failed: " + firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void showUpOnMap(final LatLng location) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (location != null) {
                        Marker previousMarker = markerStack.pop();
                        //LatLng newLatlng = currentLocation;
                        animateMarker(previousMarker, location, false);
                        previousMarker.setPosition(location);
                        markerStack.add(previousMarker);
                    }
                } catch (EmptyStackException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private final long MARKER_MOVEMENT_SPEED = 6000;

    public void animateMarker(final Marker marker, final LatLng toPosition,
                              final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = MARKER_MOVEMENT_SPEED;

        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                LatLng newPosition = new LatLng(lat, lng);
                marker.setPosition(newPosition);

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                    //moveCamera(toPosition, false);
                    optimizeZoomLevel();
                }
            }
        });
    }


    private void optimizeZoomLevel() {

        float currentZoom = mMap.getCameraPosition().zoom;
        Log.d("dj", "current zoom factor: " + currentZoom);
        if (Math.abs(currentZoom) > CAMERA_ZOOM_MOVEMENT) {
            Log.d("dj", "****optimizing zoom****");
            mMap.moveCamera(CameraUpdateFactory.zoomTo(CAMERA_ZOOM_MOVEMENT));
        }
    }


    private void boundOriginDest(Locations normalUser) {

        LatLng point = new LatLng(normalUser.getCoords().getLatitude(), normalUser.getCoords().getLongitude());
        Marker normalUserMarker = mMap.addMarker(new MarkerOptions().position(point).title(normalUser.getIdUser())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));//Normal user: green
        markerStack.add(normalUserMarker);
        positionCamera(chuLatLng, point);
    }

    private void positionCamera(final LatLng origin, LatLng dest) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(origin);
        if (dest != null)
            builder.include(dest);
        LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, paddingBound));
    }

    public void drawRoute() {

        Coords coords = normalUser.getCoords();
        double lat = coords.getLatitude();
        double lng = coords.getLongitude();
        LatLng samu = new LatLng(lat, lng);

        Log.d("dj", "LatLng - drawRoute(normalUser marker): " + samu);
        requestDirection(chuLatLng, samu);
        //Toast.makeText(MapsActivity.this, "normalUser drawRoute: "+lat+" "+lng, Toast.LENGTH_SHORT).show();
        //Toast.makeText(MapsActivity.this, "normalUser drawRoute: "+lat+" "+lng, Toast.LENGTH_SHORT).show();
        //Toast.makeText(MapsActivity.this, "normalUser drawRoute: "+lat+" "+lng, Toast.LENGTH_SHORT).show();
    }


    public void onSearchClicked(View view) {

        if (markerStack.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Normal users location not yet available", Toast.LENGTH_SHORT).show();
            return;
        }

        mJJSearchView.setVisibility(View.VISIBLE);
        ivSearch.setEnabled(false);
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mJJSearchView.startAnim();
            }
        }, 300);

        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                mJJSearchView.setVisibility(View.GONE);
                mJJSearchView.resetAnim();
                startSearchFrame();
            }
        }, 3880);

    }

    private void startSearchFrame() {

        //int PLACE_PICKER_REQUEST = 1;
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();

        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }

        /*Intent intent = new Intent(this, PlacePicker.class);
        intent.putExtra(PlacePicker.PARAM_API_KEY, SERVER_API_KEY);
        intent.putExtra(PlacePicker.PARAM_EXTRA_QUERY, "&components=country:gh&types=(cities)");
        startActivityForResult(intent, PlacePicker.REQUEST_CODE_PLACE);*/
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ivSearch.setEnabled(true);
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                final Place place = PlacePicker.getPlace(this, data);
                /*String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();*/
                String placeName = String.format("Place: %s", place.getName());
                placeMarker(place.getLatLng(), placeName, false);

                uiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setUpItineraryRoute(place.getLatLng());
                    }
                }, 500);
            }
        }
        /*if(requestCode == PlacePicker.REQUEST_CODE_PLACE && resultCode == RESULT_OK) {
            PlaceDetail placeDetail = PlacePicker.fromIntent(data);

            LatLng tempLatLng = new LatLng(placeDetail.latitude, placeDetail.longitude);
            placeMarkerAndZoomOn(tempLatLng, placeDetail.placeId);

            Log.v("dj", data.getStringExtra(PlacePicker.PARAM_PLACE_ID));
            Log.v("dj", data.getStringExtra(PlacePicker.PARAM_PLACE_DESCRIPTION));
            Log.v("dj", data.toString());
            Log.v("dj", placeDetail.toString());
        }*/
    }

    private void setUpItineraryRoute(LatLng itineraryPoint) {

        //requestDirection(markerStack.peek().getPosition(), itineraryPoint);
        sendItinerary();
    }



    private void sendItinerary(){
        LatLng marker = markerStack.peek().getPosition();
        double lat = marker.latitude;
        double lng = marker.longitude;
        Coords coords = new Coords(lat, lng);
        Itinerary itinerary = new Itinerary("SAMU1", coords, "onMission");
        fbRef.setValue(null);
        fbRef.push().setValue(itinerary);
    }


    private void placeMarker(final LatLng point, String placeId, boolean focusMarker) {

        if (itineraryLines != null) {
            itineraryLines.remove();
        }
        if (objectiveMarker != null) {
            objectiveMarker.remove();
        }
        objectiveMarker = mMap.addMarker(new MarkerOptions().position(point).title(placeId).icon(BitmapDescriptorFactory.
                defaultMarker(BitmapDescriptorFactory.HUE_RED)));//itinerary location Marker color: Red
        if (focusMarker) {

            uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    moveCamera(point, true);
                }
            }, 600);
        }
    }


    private void moveCamera(LatLng point, boolean shouldAnimate) {

        if (shouldAnimate) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, CAMERA_ZOOM_ANIM));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, CAMERA_ZOOM_MOVEMENT));
        }
    }


    public void setStart(String id, Coords cd, DateTime dt) {
        normalUser.setTime(dt);
        normalUser.setCoords(cd);
        normalUser.setIdUser(id);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setUpEventListener();
        // Add a marker in Sydney and move the camera
        mMap.addMarker(new MarkerOptions().position(chuLatLng).title("CHU").icon(BitmapDescriptorFactory.
                defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));// Master user- blue

        // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(chuLatLng, 17));
        // retrieveData();
    }

    public void requestDirection(final LatLng nUserCurrentLoc, final LatLng objectiveLatLng) {
        Toast.makeText(getBaseContext(), "Requesting direction, just a moment...", Toast.LENGTH_SHORT).show();
        GoogleDirection.withServerKey(SERVER_API_KEY)
                .from(nUserCurrentLoc)
                .to(objectiveLatLng)
                .transportMode(TransportMode.DRIVING)
                .execute(this);

        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                positionCamera(nUserCurrentLoc, objectiveLatLng);
            }
        }, 2800);
    }


    /*public void drawRoute(){
        Coords coords = Start.getCoords();
        double lat = coords.getLatitude();
        double lng = coords.getLongitude();
        LatLng samu = new LatLng(lat,lng);

        requestDirection(chuLatLng, samu);
        Toast.makeText(MapsActivity.this, "Start drawRoute: "+lat+" "+lng, Toast.LENGTH_SHORT).show();
        Toast.makeText(MapsActivity.this, "Start drawRoute: "+lat+" "+lng, Toast.LENGTH_SHORT).show();
        Toast.makeText(MapsActivity.this, "Start drawRoute: "+lat+" "+lng, Toast.LENGTH_SHORT).show();

    }*/


    /*public void onHoldClick(){

        LatLng objective = onSingleTapConfirmed();
        Location origin = new Location(*//*Start*//*);
        Location destination = new Location(*//*objective*//*);
        showAlert();
        //Draw the route
        requestDirection(origin,destination);
        //Track the user
        checkDistance(origin,destination);

    }*/

    public void showAlert() {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Send itinerary to this location to the closest user ?");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        //LatLng destination = onSingleTapConfirmed();
                        //Marker destMarker = mMap.addMarker(new MarkerOptions().position(destination).title("objective"));
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    public void showProximityAlert1() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("User arrived at objective's location");
        builder.setCancelable(true);
    }

    public void showProximityAlert2() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("User quit objective's location");
        builder.setCancelable(true);
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {

        if (direction.isOK()) {
            Toast.makeText(getBaseContext(), "Routes are marked successfully...", Toast.LENGTH_SHORT).show();
            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
            itineraryLines = mMap.addPolyline(DirectionConverter.createPolyline(getApplicationContext(),
                    directionPositionList, 5, Color.RED));
            /*uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    positionCamera(nUserCurrentLoc, itinerary);
                }
            }, 300);*/
        } else {
            Toast.makeText(getBaseContext(), "Routes are suspicious!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        Toast.makeText(getBaseContext(), "No routes found!", Toast.LENGTH_SHORT).show();
    }



    //Get location on map click
    /*public LatLng onSingleTapConfirmed(MotionEvent e, MapView mapView) {

        Projection proj = mapView.getProjection();
        p = (GeoPoint) proj.fromPixels((int) e.getX(), (int) e.getY());
        proj = mapView.getProjection();
        loc = (GeoPoint) proj.fromPixels((int) e.getX(), (int) e.getY());
        longitude = Double.toString(((double) loc.getLongitudeE6()) / 1000000);
        latitude = Double.toString(((double) loc.getLatitudeE6()) / 1000000);
        LatLng objective = new LatLng(latitude,longitude);
         *//*
         Toast toast = Toast.makeText(getApplicationContext(),
         "Longitude: "
         + longitude + " Latitude: " + latitude, Toast.LENGTH_SHORT);
         toast.show();
         *//*
        return objective;
    }*/

    //Check distance between the user and the objective
    /*public void checkDistance(final Location user,final Location objective){
        fbRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                //Toast.makeText(getApplicationContext(), "There are " + snapshot.getChildrenCount() + " locations", Toast.LENGTH_SHORT).show();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Itinerary post = postSnapshot.getValue(Itinerary.class);

                    //Have to check the normalUser's status, if he's free or already has an objective (multiple users)

                    boolean shown = false;
                    if(user.distanceTo(objective)<=10){
                        if (shown == false) {
                            showProximityAlert1();
                            shown = true;
                        }else if (user.distanceTo(objective)>10) {
                            showProximityAlert2();
                            shown = false;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Toast.makeText(getApplicationContext(), "An error occured! " + firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }*/

}
