package com.nackademin.foureverhh.jogtracker180417;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.google.maps.android.SphericalUtil;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Locale;


public class Training extends AppCompatActivity implements OnMapReadyCallback,SaveTrainingData.Communication {

    GoogleMap myMap;
    SupportMapFragment fragment;
    final int REQUEST_LOCATION =101;
    ToggleButton toggleButton;
    Button btnFinish;

    FusedLocationProviderClient locationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    double latitude,longitude;
    double lat,lng;
    Chronometer timer;
    double distanceTotal;
    float currentSpeed;
    long lastTimePause; //So that to record the timer when pause
    TextView speedText,distanceText,timeText;

    FragmentManager manager;
    SaveTrainingData saveTrainingData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        toggleButton = findViewById(R.id.startAndPause);
        btnFinish = findViewById(R.id.finish);
        speedText = findViewById(R.id.speed);
        distanceText = findViewById(R.id.distance);
        timer = findViewById(R.id.time);

        //Get google maps fragment
        fragment =(SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);

        //Get location and sport information and updates information
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        findLastLocation();
        createLocationRequest();
        getLocationCallback();

        //Control training
        startAndPauseSport();
        endSport();
    }

    public void findLastLocation(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION );
        }
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
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
    }

    public void getLocationCallback(){
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for(Location location:locationResult.getLocations()){
                    lat = location.getLatitude();
                    lng = location.getLongitude();
                    currentSpeed = location.getSpeed();
                    String strSpeed = String.format(Locale.US,"Speed: %.2f km/h",currentSpeed*3.6);
                    //speedText.setText(" "+String.valueOf(currentSpeed*3.6)+" km/h");
                    speedText.setText(strSpeed);
                }
                myMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat,lng))
                        .title("New location"));
                myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),15));
                myMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(latitude,longitude),new LatLng(lat,lng))
                        .width(20)
                        .color(Color.RED));
                double distance =
                        SphericalUtil.computeDistanceBetween(new LatLng(latitude,longitude),new LatLng(lat,lng));
                distanceTotal = distanceTotal+distance;
                String strDistanceTotal = String.format(Locale.US,"You've run: %.3f km",distanceTotal/1000);
                distanceText.setText(strDistanceTotal);
                latitude = lat;
                longitude = lng;
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
                    startTimer();
                   /* //To start the timer
                    if(lastTimePause != 0){
                        timer.setBase(timer.getBase()+SystemClock.elapsedRealtime()-lastTimePause);
                        //Log.w("Base","timer start if base"+String.valueOf(timer.getBase()));
                    }else{
                        timer.setBase(SystemClock.elapsedRealtime());
                        *//*Log.e("timer","setBase works");
                        Log.w("Base","timer start else base"+String.valueOf(timer.getBase()));*//*
                    }
                    timer.start();
                    *//*Log.w("Base","timer start"+String.valueOf(lastTimePause));
                    Log.e("timer","start works");*//*
                    timer.setVisibility(View.VISIBLE);*/

                } else {
                    // The toggle is disabled
                    locationProviderClient.removeLocationUpdates(locationCallback);
                    pauseTimer();
                    /*//To pause the timer
                    lastTimePause = SystemClock.elapsedRealtime();
                    timer.stop();*/
                }
            }
        });
    }

    public void endSport(){
        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationProviderClient.removeLocationUpdates(locationCallback);
                saveThisTraining();
                resetTimer();
               /* //Reset the timer
                timer.stop();
                timer.setBase(SystemClock.elapsedRealtime());
                lastTimePause =0;*/

            }
        });
    }

    public void saveThisTraining(){
        manager = getFragmentManager();
        saveTrainingData = new SaveTrainingData();
        saveTrainingData.show(manager,"HandleTrainingData");
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;
    }

    public void startTimer(){
        if(lastTimePause != 0){
            timer.setBase(timer.getBase()+SystemClock.elapsedRealtime()-lastTimePause);
        }else {
            timer.setBase(SystemClock.elapsedRealtime());
        }
        timer.start();
        timer.setVisibility(View.VISIBLE);
    }

    public void pauseTimer(){
        lastTimePause = SystemClock.elapsedRealtime();
        timer.stop();
    }

    public void resetTimer(){
        lastTimePause = SystemClock.elapsedRealtime();
        timer.stop();
        /*timer.setBase(SystemClock.elapsedRealtime());
        lastTimePause = 0;*/
    }



    @Override
    public void restartUpdateLocations() {
        if (ContextCompat
                .checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
        startTimer();
    }
}
