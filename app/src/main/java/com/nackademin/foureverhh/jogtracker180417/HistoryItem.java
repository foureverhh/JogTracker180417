package com.nackademin.foureverhh.jogtracker180417;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryItem {

    private String sportDate;
    private String sportDistance;
    private String sportDuration;
    private String sportSpeed;
    private String sportID;
    private List<MyLatLng> sportLocations = new ArrayList<>();


    public HistoryItem(){}

    public HistoryItem(String sportDate,String sportDistance,String sportDuration,String itemID,List<MyLatLng> sportLocations){
        this.sportDate = sportDate;
        this.sportDistance = sportDistance;
        this.sportDuration = sportDuration;
        double distance = Double.parseDouble(sportDistance);
        String time [] = sportDuration.split(":");
        double timeDuration = 0;
        if(time.length == 3){
            timeDuration = Double.parseDouble(time[0]) + Double.parseDouble(time[1])/60 + Double.parseDouble(time[2])/60/60;
        }else if(time.length ==2){
            timeDuration = Double.parseDouble(time[0])/60 + Double.parseDouble(time[1])/60/60;
        }else if(time.length == 1){
            timeDuration = Double.parseDouble(time[0]);
        }
        Log.w("timeDuration",String.valueOf(timeDuration));
        this.sportSpeed = String.format(Locale.US,"%.2f", distance/timeDuration);
        this.sportID = itemID;
        this.sportLocations = sportLocations;
    }

    public String getSportDate() {
        return sportDate;
    }

    public String getSportDistance() {
        return sportDistance;
    }

    public String getSportDuration() {
        return sportDuration;
    }

    public String getSportSpeed() {
        return sportSpeed;
    }

    public String getSportID() {
        return sportID;
    }

    public List <MyLatLng> getSportLocations() {
        return sportLocations;
    }
}
