package com.nackademin.foureverhh.jogtracker180417;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SportHistory extends AppCompatActivity  {

    ListView historyList;
    List<HistoryItem> historyItems = new ArrayList<>();
    SportHistoryListAdapter sportHistoryListAdapter;

    DatabaseReference historyRef;
    FirebaseUser user ;

    static final String SPORT_SPEED_KEY = "SPORT_SPEED";
    static final String SPORT_LOCATIONS_KEY = "SPORT_LOCATION";
    static final String SPORT_DURATION_KEY = "SPORT_DURATION";
    static final String SPORT_DISTANCE_KEY = "SPORT_DISTANCE";
    static final String SPORT_DATE_KEY = "SPORT_DATE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport_history);

        getIntent();

        user = FirebaseAuth.getInstance().getCurrentUser();
        historyRef = FirebaseDatabase.getInstance().getReference("SportHistoryTest").child(user.getUid());
        historyRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("Onchange","It works here");
                Log.e("Get Children ", String.valueOf(dataSnapshot.getChildren() ));
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                   // Log.e("Inside for","It works here");
                    HistoryItem historyItem = snapshot.getValue(HistoryItem.class);
                   // Log.e("historyItem",historyItem.getSportID());
                    historyItems.add(historyItem);
                }

                historyList = findViewById(R.id.jogHistory);
                sportHistoryListAdapter =
                        new SportHistoryListAdapter(SportHistory.this,historyItems);
                historyList.setAdapter(sportHistoryListAdapter);
                //To have a reversed listview
                historyList.setStackFromBottom(true);

                historyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        HistoryItem historyItem = historyItems.get(position);
                        Intent toSportHistoryDetails = new Intent(SportHistory.this,SportHistoryDetails.class);
                        toSportHistoryDetails.putExtra(SPORT_SPEED_KEY, historyItem.getSportSpeed()) ;
                        toSportHistoryDetails.putParcelableArrayListExtra(SPORT_LOCATIONS_KEY, (ArrayList<MyLatLng>) historyItem.getSportLocations());
                        toSportHistoryDetails.putExtra(SPORT_DURATION_KEY,historyItem.getSportDuration());
                        toSportHistoryDetails.putExtra(SPORT_DISTANCE_KEY ,historyItem.getSportDistance());
                        toSportHistoryDetails.putExtra(SPORT_DATE_KEY,historyItem.getSportDate());
                        startActivity(toSportHistoryDetails);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
