package com.example.bumble_babysitter1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Report_and_Help extends AppCompatActivity implements View.OnClickListener {
    EditText etComplain;
    TextView textView;
    Button btnDone;
    String UserName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_and_help);

        if (!isConnectedToInternet()) {
            Toast.makeText(this, "There is no internet connection", Toast.LENGTH_SHORT).show();
            finish();
            System.exit(0);
        }

        this.setFinishOnTouchOutside(false);

        etComplain = (EditText) findViewById(R.id.etComplain);
        textView = (TextView) findViewById(R.id.textView);
        btnDone = (Button) findViewById(R.id.btnDone);
        btnDone.setOnClickListener(this);

        UserName = getIntent().getStringExtra("UserName");

        if (getIntent().getStringExtra("Item").equals("Help")) // Help
        {
            //  Toolbar toolbar + arrow
            Toolbar toolbar = findViewById(R.id.toolbar_babysitter);
            toolbar.setTitle("Help");
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    Report_and_Help.super.onBackPressed();
                }
            });
        }
        else if (getIntent().getStringExtra("Item").equals("Report")) // Report
        {
            String BabysitterUserName = getIntent().getStringExtra("BabysitterUserName");
            textView.setText("Explain how "+BabysitterUserName +" causes damage, abuse and/or harm you, others and/or BumbleBabysitter app. Please report any real issues");
            //  Toolbar toolbar + arrow
            Toolbar toolbar = findViewById(R.id.toolbar_babysitter);
            toolbar.setTitle("Report");
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    Report_and_Help.super.onBackPressed();
                }
            });
        }
    }

    // Check  internet connection
    public boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
    }

    @Override
    public void onClick(View v) {
        if (v == btnDone)
        {
            if (etComplain.getText().toString().isEmpty())
                etComplain.setError("Pleas fill up your complain so we can help you");
            else {
                if (getIntent().getStringExtra("Item").equals("Help")) //Help
                {
                    Report report = new Report(UserName, etComplain.getText().toString());
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Complaints/Help");
                    reference.push().setValue(report);
                    Toast.makeText(Report_and_Help.this,
                            "Your question / complain is being cheat, we will get back to you soon",Toast.LENGTH_LONG).show();
                }
                else if (getIntent().getStringExtra("Item").equals("Report")) //Report
                {
                    Report report = new Report(UserName, getIntent().getStringExtra("BabysitterUserName"), etComplain.getText().toString());
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Reports");
                    reference.push().setValue(report);

                    Toast.makeText(Report_and_Help.this,"Thanks for the report, it will be reviewed soon",Toast.LENGTH_LONG).show();
                }
                Intent BackPressed = new Intent(Report_and_Help.this, HomeScreen.class);
                BackPressed.putExtra("UserName", UserName);
                startActivity(BackPressed);
            }


        }
    }

    //לחיצה על כפתור החזרה
    int i = 0;

    @Override
    public void onBackPressed()
    {
        i++;
        if (i == 2)
        {
            Intent NavIc = new Intent(Report_and_Help.this, HomeScreen.class);
            NavIc.putExtra("UserName", getIntent().getStringExtra("UserName"));
            startActivity(NavIc);
        }
    }
}