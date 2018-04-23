package com.nackademin.foureverhh.jogtracker180417;

import android.content.Intent;
import android.graphics.Color;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.ButtCap;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.util.ArrayList;
import java.util.List;

public class SportHistoryDetails extends AppCompatActivity implements OnMapReadyCallback{

    GoogleMap mapTwo;
    SupportMapFragment mapWithDetails;

    String sportSpeed ;
    String sportDuration;
    String sportDistance;
    ArrayList<MyLatLng> sportLocations;
    String sportDate;
    List<LatLng> positionsToDraw = new ArrayList<>();

    TextView distanceDetailText,speedDetailText,timeDetailText,dateDetailText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport_history_details);

        mapWithDetails = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapDetails);
        mapWithDetails.getMapAsync(this);


        distanceDetailText = findViewById(R.id.distanceDetail);
        speedDetailText = findViewById(R.id.speedDetail);
        timeDetailText = findViewById(R.id.timeDetail);
        dateDetailText = findViewById(R.id.dateDetail);

        Intent getSportDetails = getIntent();
        sportSpeed = getSportDetails.getStringExtra(SportHistory.SPORT_SPEED_KEY);
        sportDuration = getSportDetails.getStringExtra(SportHistory.SPORT_DURATION_KEY);
        sportDistance = getSportDetails.getStringExtra(SportHistory.SPORT_DISTANCE_KEY);
        sportLocations = getSportDetails.getParcelableArrayListExtra(SportHistory.SPORT_LOCATIONS_KEY);
        sportDate = getSportDetails.getStringExtra(SportHistory.SPORT_DATE_KEY);

        distanceDetailText.setText(sportDistance);
        speedDetailText.setText(sportSpeed);
        timeDetailText.setText(sportDuration);
        dateDetailText.setText(sportDate);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapTwo = googleMap;

        for(MyLatLng myLatLng :sportLocations){
            double lat = myLatLng.getLatitude();
            double lng = myLatLng.getLongitude();
            LatLng position = new LatLng(lat,lng);
            positionsToDraw.add(position);
        }


        int length = positionsToDraw.size();
        Log.e("Details","It works before for-loop");
        /*for(int i = 0; i < length-1;i++){
            Log.e("Details","It works in for-loop");
            mapTwo.animateCamera(CameraUpdateFactory.newLatLngZoom(positionsToDraw.get(0),20));
            mapTwo.addPolyline(new PolylineOptions()
                    .add(positionsToDraw.get(i),positionsToDraw.get(i+1))
                    .width(50)
                    .color(Color.GREEN));


            Log.e("Position",String.valueOf(positionsToDraw.get(i)));
        }*/
        Polyline line = mapTwo.addPolyline(new PolylineOptions()
                .color(Color.GREEN).width(30));
        line.setStartCap(new ButtCap());
        line.setPoints(positionsToDraw);
        mapTwo.animateCamera(CameraUpdateFactory.newLatLngZoom(positionsToDraw.get(0),15));
    }
}
