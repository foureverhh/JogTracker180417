package com.nackademin.foureverhh.jogtracker180417;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;

public class SaveTrainingData extends DialogFragment implements View.OnClickListener{
    private Button yes;
    private Button no;
    Communication communication;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialogfragment_save_training,container);
        yes = view.findViewById(R.id.buttonYes);
        no = view.findViewById(R.id.buttonNo);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.buttonYes){

            Log.e("On click", "Before save data.");
            communication.saveDataToFirebaseHistory();
            dismiss();
        }
        if(v.getId() == R.id.buttonNo){
            dismiss();
            Toast.makeText(getActivity(),"Go on training",Toast.LENGTH_SHORT).show();
            communication.restartUpdateLocations();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
       communication = (Communication) context;
    }

    interface Communication{
        void saveDataToFirebaseHistory();
        void restartUpdateLocations();
    }
}
