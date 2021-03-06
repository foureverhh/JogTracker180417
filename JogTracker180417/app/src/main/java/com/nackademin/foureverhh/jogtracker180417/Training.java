package com.nackademin.foureverhh.jogtracker180417;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.PolyUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.nackademin.foureverhh.jogtracker180417.datamodel.HistoryItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class Training extends AppCompatActivity implements OnMapReadyCallback,SaveTrainingData.Communication {
    private GoogleMap myMap;
    private ToggleButton toggleButton;
    private Button btnFinish;
    private FusedLocationProviderClient locationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private double latitude,longitude;
    private double lat,lng;
    private Chronometer timer;
    private double distanceTotal;
    private String strDistanceTotal;
    private float currentSpeed;
    private long lastTimePause; //So that to record the timer when pause
    private TextView speedText,distanceText;
    private List<Polyline> polylines = new ArrayList<>();
    private FragmentManager manager;
    private SaveTrainingData saveTrainingData;
    private DatabaseReference historyRefTest = FirebaseDatabase.getInstance().getReference("SportHistoryTest");
    private DatabaseReference historyDetailRefTest = FirebaseDatabase.getInstance().getReference("SportHistoryDetailsTest");
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private HistoryItem historyItem;
    private List<MyLatLng> locationsPassBy =new ArrayList<>();
    private Date dateLabel;
    private Toolbar toolbar;
    //To store location callbacks each time
    private String path;
    private List<String> pathCollection = new ArrayList<>();
    private List<LatLng> locationCallbackToDraw = new ArrayList<>();
    private List<List<MyLatLng>> locationsPassByCollector = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        toggleButton = findViewById(R.id.startAndPause);
        btnFinish = findViewById(R.id.finish);
        speedText = findViewById(R.id.speed);
        distanceText = findViewById(R.id.distance);
        timer = findViewById(R.id.time);
        distanceTotal = 0;
        //Get google maps fragment
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);
        //Get toolbar
        toolbar = findViewById(R.id.app_bar_on_training);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.bar_title_Training));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Get location and sport information and updates information
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        findLastLocation();
        createLocationRequest();
        getLocationCallback();
        //Control training time
        startAndPauseSport();
        endSport();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_menu_on_training,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.historyPage:
                Intent toCheckHistory = new Intent(this,SportHistory.class);
                startActivity(toCheckHistory);
                break;
            case R.id.logOut:
                FirebaseAuth.getInstance().signOut();
                Intent toMainActivity = new Intent(this,MainActivity.class);
                startActivity(toMainActivity);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void findLastLocation(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            int REQUEST_LOCATION = 101;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
        dateLabel = Calendar.getInstance().getTime();
        locationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
               if(location != null){
                   latitude = location.getLatitude();
                   longitude = location.getLongitude();
               }
            }
        });
    }

    public void createLocationRequest(){
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
    }

    public void getLocationCallback(){
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if(locationResult != null) {
                    for (Location location : locationResult.getLocations()) {
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                        Log.w("Result length", String.valueOf(locationResult.getLocations().size()));
                        //show speed
                        currentSpeed = location.getSpeed();
                        String strSpeed = String.format(Locale.US, "%.2f", currentSpeed * 3.6);
                        speedText.setText(strSpeed);

                        //To store the updated locations callback in LatLng
                        locationCallbackToDraw.add(new LatLng(lat, lng));

                        Log.w("loc size", String.valueOf(locationCallbackToDraw.size()));

                        locationsPassBy.add(new MyLatLng(lat, lng));
                        Log.w("passBy size", String.valueOf(locationsPassBy.size()));
                        //To draw polyline based on locationCallbackToDraw
                        if (locationCallbackToDraw.size() > 1) {
                            double distance = SphericalUtil
                                    .computeDistanceBetween(locationCallbackToDraw.get(locationCallbackToDraw.size() - 1),
                                            locationCallbackToDraw.get(locationCallbackToDraw.size() - 2));
                            //new LatLng(latitude, longitude), new LatLng(lat, lng)
                            Log.e("distance", String.valueOf(distance));
                            distanceTotal = distanceTotal + distance;
                            strDistanceTotal = String.format(Locale.US, "%.3f", distanceTotal / 1000);//"You've run: %.3f km"
                            distanceText.setText(strDistanceTotal);
                        }
                    }
                }
                myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),20));
                //To draw polyline based on locationCallbackToDraw
                Polyline polyline = myMap.addPolyline(new PolylineOptions().width(40)
                        .color(Color.RED));
                polyline.setPoints(locationCallbackToDraw);

                //To encode the polyline to string
                path = PolyUtil.encode(locationCallbackToDraw);

                //Later to remove those polylines from map
                polylines.add(polyline);
            }
        };
    }

    public void startAndPauseSport(){
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    if (ContextCompat
                            .checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    }
                    //To store each trace of start
                    if(locationCallbackToDraw.size() != 0) {
                        //locationsPassByCollector.add(locationsPassBy);
                        //Log.w("collector", String.valueOf(locationsPassByCollector.size()));
                        locationsPassByCollector.add(locationsPassBy);
                        //locationsPassBy.clear();
                        pathCollection.add(path);
                        locationCallbackToDraw.clear();
                        Log.w("loc pass size", String.valueOf(locationCallbackToDraw.size())+" "
                        +String.valueOf(locationCallbackToDraw.size()));
                    }
                    startTimer();
                } else {
                    // The toggle is disabled
                    locationProviderClient.removeLocationUpdates(locationCallback);
                    pauseTimer();
                }
            }
        });
    }

    public void endSport(){
        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogShowUp();
                if(toggleButton.isChecked()){
                    pauseTimer();
                    locationProviderClient.removeLocationUpdates(locationCallback);
                }
            }
        });
    }
    public void dialogShowUp(){
        manager = getFragmentManager();
        saveTrainingData = new SaveTrainingData();
        saveTrainingData.show(manager,"HandleTrainingData");

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;
        myMap.getUiSettings().isMapToolbarEnabled();
        myMap.getUiSettings().setZoomControlsEnabled(true);
    }

    public void startTimer(){
        if(lastTimePause != 0){
            timer.setBase(timer.getBase() + SystemClock.elapsedRealtime() - lastTimePause);
        }else {
            timer.setBase(SystemClock.elapsedRealtime());
        }
        timer.start();
    }

    public void pauseTimer(){
        lastTimePause = SystemClock.elapsedRealtime();
        timer.stop();
    }

    public void resetTimer(){
        timer.setBase(SystemClock.elapsedRealtime());
        lastTimePause = 0;
    }

    //When no is pressed on dialog fragment
    @Override
    public void restartUpdateLocations() {
       if(toggleButton.isChecked()) {
            //Toast.makeText(this,"toggle is on",Toast.LENGTH_LONG).show();
            if (ContextCompat
                    .checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                startTimer();
            }
        }
    }

    //When yes is pressed on dialog fragment
    @Override
    public void saveDataToFirebaseHistory() {
        String userID = user.getUid();
        String itemID = historyRefTest.push().getKey();
        String sportDate = String.valueOf(dateLabel).substring(0,19)+String.valueOf(dateLabel).substring(29);
        String sportDistance = strDistanceTotal;
        String sportDuration = timer.getText().toString();

        if(toggleButton.isChecked()){
            //To store each trace of start
            if(locationCallbackToDraw.size() != 0) {
                //locationsPassByCollector.add(locationsPassBy);
                //Log.w("collector", String.valueOf(locationsPassByCollector.size()));
                locationsPassByCollector.add(locationsPassBy);
                //locationsPassBy.clear();
                pathCollection.add(path);
                locationCallbackToDraw.clear();
                Log.w("loc pass size", String.valueOf(locationCallbackToDraw.size())+" "
                        +String.valueOf(locationCallbackToDraw.size()));
            }
        }
        historyItem = new HistoryItem(sportDate, sportDistance, sportDuration, itemID, pathCollection);
        historyRefTest.child(userID).child(itemID).setValue(historyItem);

        Intent toSportHistory =
                new Intent(Training.this, SportHistory.class);
        startActivity(toSportHistory);



        Log.e("In save data", "Before set txt");
        distanceText.setText("");
        speedText.setText("");
        resetTimer();
        toggleButton.setChecked(false);
        distanceTotal = 0.0;
        for(Polyline line: polylines){
            line.remove();
        }
        polylines.clear();
        locationCallbackToDraw.clear();
        locationsPassBy.clear();
    }
}
