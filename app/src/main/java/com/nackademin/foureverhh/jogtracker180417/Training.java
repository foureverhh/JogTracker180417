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
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
    String strDistanceTotal;
    float currentSpeed;
    long lastTimePause; //So that to record the timer when pause
    TextView speedText,distanceText;
    List<Polyline> polylines = new ArrayList<>();
    FragmentManager manager;
    SaveTrainingData saveTrainingData;

    DatabaseReference historyRefTest = FirebaseDatabase.getInstance().getReference("SportHistoryTest");
    DatabaseReference historyDetailRefTest = FirebaseDatabase.getInstance().getReference("SportHistoryDetailsTest");
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    HistoryItem historyItem;
    List<MyLatLng> locationsPassBy =new ArrayList<>();

    Date dateLabel;
    Toolbar toolbar;



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
        //Get toolbar
        toolbar = findViewById(R.id.app_bar_on_training);
        setSupportActionBar(toolbar);
        //Get location and sport information and updates information
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        findLastLocation();
        createLocationRequest();
        getLocationCallback();

        //Control training
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
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION );
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

                    Log.w("Result length",String.valueOf(locationResult.getLocations().size()));
                    locationsPassBy.add(new MyLatLng(lat,lng));

                    currentSpeed = location.getSpeed();
                    String strSpeed = String.format(Locale.US,"%.2f",currentSpeed*3.6);
                    //speedText.setText(" "+String.valueOf(currentSpeed*3.6)+" km/h");

                    speedText.setText(strSpeed);
                }
                /*myMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat,lng))
                        .title("Current location"));*/
                myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),15));

                Polyline polyline = myMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(latitude,longitude),new LatLng(lat,lng))
                        .width(50)
                        .color(Color.RED));
                polylines.add(polyline);
                double distance =
                        SphericalUtil.computeDistanceBetween(new LatLng(latitude,longitude),new LatLng(lat,lng));
                distanceTotal = distanceTotal+distance;
                strDistanceTotal = String.format(Locale.US,"%.3f",distanceTotal/1000);//"You've run: %.3f km"
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
                locationProviderClient.removeLocationUpdates(locationCallback);
                dialogShowUp();
                pauseTimer();
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

    }

    public void startTimer(){
        if(lastTimePause != 0){
            timer.setBase(timer.getBase()+SystemClock.elapsedRealtime()-lastTimePause);
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
//When no is pressed
    @Override
    public void restartUpdateLocations() {

       if(toggleButton.isChecked()) {
            Toast.makeText(this,"toggle is on",Toast.LENGTH_LONG).show();
            if (ContextCompat
                    .checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                startTimer();
            }

        } else {
            Toast.makeText(this,"toggle is off",Toast.LENGTH_LONG).show();
            pauseTimer();
            locationProviderClient.removeLocationUpdates(locationCallback);
        }
    }
//When yes is pressed
    @Override
    public void saveDataToFirebaseHistory() {
        String userID = user.getUid();
        String itemID = historyRefTest.push().getKey();
        String sportDate = String.valueOf(dateLabel).substring(0,19)+String.valueOf(dateLabel).substring(29);
        String sportDistance = strDistanceTotal;
        String sportDuration = timer.getText().toString();

        historyItem = new HistoryItem(sportDate,sportDistance,sportDuration,itemID,locationsPassBy);
       /* historyItems = new ArrayList<>();
        historyItems.add(historyItem);*/
        Intent toSportHistory =
                new Intent(Training.this,SportHistory.class);

        startActivity(toSportHistory);

        historyRefTest.child(userID).child(itemID).setValue(historyItem);

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
    }



}
