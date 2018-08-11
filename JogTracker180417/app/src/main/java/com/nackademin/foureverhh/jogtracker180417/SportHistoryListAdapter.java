package com.nackademin.foureverhh.jogtracker180417;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nackademin.foureverhh.jogtracker180417.datamodel.HistoryItem;

import java.util.List;

public class SportHistoryListAdapter extends ArrayAdapter<HistoryItem>{

    private  Activity  context;
    private  List<HistoryItem> historyItems;

     SportHistoryListAdapter(Activity context, List<HistoryItem> historyItems) {
        super(context,R.layout.sport_history_list,historyItems);
        this.context = context;
        this.historyItems = historyItems;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView,
                        @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View ListViewItem = inflater.inflate(R.layout.sport_history_list,null,true);
        TextView textDate = ListViewItem.findViewById(R.id.sportDateHistory);
        TextView textDistance = ListViewItem.findViewById(R.id.sportDistanceHistory);
        TextView textSpeed = ListViewItem.findViewById(R.id.sportSpeedHistory);
        TextView textDuration = ListViewItem.findViewById(R.id.sportDurationHistory);

        Log.e("Get view ","It works here");
        HistoryItem historyItem = historyItems.get(position);
        textDate.setText(historyItem.getSportDate());
        textDistance.setText(historyItem.getSportDistance());
        textSpeed.setText(historyItem.getSportSpeed());
        textDuration.setText(historyItem.getSportDuration());
        return ListViewItem;
    }
}
