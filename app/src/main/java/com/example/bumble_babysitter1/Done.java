package com.example.bumble_babysitter1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


public class Done extends AppCompatActivity implements View.OnClickListener
{
    Button Continue;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done);

        if(!isConnectedToInternet())
        {
            Toast.makeText(this, "There is no internet connection", Toast.LENGTH_SHORT).show();
            finish();
            System.exit(0);
        }

        Continue =(Button)findViewById(R.id.btnContinue);
        Continue.setOnClickListener(this);

        imageView = (ImageView) findViewById(R.id.imageview);



        // To add zoom animation
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.vi_animation);
        imageView.startAnimation(animation);

    }

    // Check  internet connection
    public boolean isConnectedToInternet()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
    }

    @Override
    public void onClick(View v)
    {
        if(v==Continue)
        {
            Intent HomeScreen = new Intent(Done.this, HomeScreen.class);
            HomeScreen.putExtra("UserName",getIntent().getStringExtra("UserName"));
            startActivity(HomeScreen);
        }

    }



    //לחיצה על כפתור החזרה
    int BackPressed=0;
    @Override
    public void onBackPressed()
    {
        BackPressed++;
        if(BackPressed==2)
        {
            Intent HomeScreen = new Intent(Done.this, HomeScreen.class);
            HomeScreen.putExtra("UserName",getIntent().getStringExtra("UserName"));
            startActivity(HomeScreen);
            BackPressed=0;
        }
    }


}