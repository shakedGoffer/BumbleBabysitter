package com.example.bumble_babysitter1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

public class MainActivity extends AppCompatActivity
{

    // BroadCast receiver - Battery
    BroadCastBattery broadCastBattery;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        broadCastBattery = new BroadCastBattery();

        // Start background sound
        Intent intent = new Intent(MainActivity.this, BackgroundSoundService.class);
        startService(intent);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //This method will be executed once the timer is over
                // Start
                Intent intent = new Intent(MainActivity.this, BackgroundSoundService.class);
                stopService(intent);
                // Stop background sound
            }
        }, 2000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //This method will be executed once the timer is over
                // Start
                Intent i = new Intent(MainActivity.this, LogIn.class);
                startActivity(i);
                // close this activity
                finish();
            }
        }, 2500);


    }

    // BroadCast receiver - Battery
    private class BroadCastBattery extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            int battery = intent.getIntExtra("level",0);
            if(battery<15 )
            {
                Toast.makeText(MainActivity.this, "Your battery is low please charge your phone", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume()
    { super.onResume();registerReceiver(broadCastBattery,new IntentFilter(Intent.ACTION_BATTERY_CHANGED)); }
    @Override
    protected void onPause() { super.onPause();unregisterReceiver(broadCastBattery); }

}