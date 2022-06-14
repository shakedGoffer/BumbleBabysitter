package com.example.bumble_babysitter1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditPassword extends AppCompatActivity implements View.OnClickListener
{
    String UserName;
    static DatabaseReference reference;
    Button btnDone;
    EditText etPassword, etNewPassword, etCheckNewPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);

        if(!isConnectedToInternet())
        {
            Toast.makeText(this, "There is no internet connection", Toast.LENGTH_SHORT).show();
            finish();
            System.exit(0);
        }

        UserName = getIntent().getStringExtra("UserName");

        btnDone = (Button) findViewById(R.id.btnDone);
        btnDone.setOnClickListener(this);

        etPassword = (EditText) findViewById(R.id.etPassword);
        etNewPassword = (EditText) findViewById(R.id.etNewPassword);
        etCheckNewPassword = (EditText) findViewById(R.id.etCheckNewPassword);

        Clear();
    }

    // Check  internet connection
    public boolean isConnectedToInternet()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
    }

    public void Clear() {
        etNewPassword.setText("");
        etPassword.setText("");
        etCheckNewPassword.setText("");
    }


    @Override
    public void onClick(View v) {
        if (v == btnDone)
            getPassword(UserName, etPassword.getText().toString());
    }

    public void getPassword(String UserName, String Password)
    {
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(UserName).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if (task.isSuccessful()) {

                    if (task.getResult().exists()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (String.valueOf(dataSnapshot.child("password").getValue()) == Password || String.valueOf(dataSnapshot.child("password").getValue()).equals(Password)) {

                            if(etNewPassword.getText().toString().length()<6)
                            {
                                etNewPassword.setError("Password must be at least 6 characters long");
                                etCheckNewPassword.setError("Password must be at least 6 characters long");
                            }
                            else if (etNewPassword.getText().toString().equals(etCheckNewPassword.getText().toString()) == false)
                            {
                                etCheckNewPassword.setError("Error, try again");
                                etCheckNewPassword.setText("");
                            }
                            else if(etNewPassword.getText().toString().equals(etPassword.getText().toString()))
                            {
                                etNewPassword.setError("The new password can not be same");
                                etCheckNewPassword.setError("The new password can not be same");
                                etCheckNewPassword.setText("");
                                etNewPassword.setText("");
                            }
                            else
                                setNewPassword();

                        } else // wrong password
                        {
                            etPassword.setError("Wrong password ,try again");
                        }
                    } else // User Doesn't Exist
                    {
                        Toast.makeText(EditPassword.this, "User Doesn't Exist", Toast.LENGTH_SHORT).show();
                        Clear();
                    }
                } else // Error
                    Toast.makeText(EditPassword.this, "Failed to read", Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void setNewPassword()
    {
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(UserName).child("password").setValue(etNewPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                Intent HomeScreen = new Intent(EditPassword.this, HomeScreen.class);
                HomeScreen.putExtra("UserName",UserName);
                startActivity(HomeScreen);
            }
        });
    }

    //לחיצה על כפתור החזרה
    int i=0;
    @Override
    public void onBackPressed()
    {
        i++;
        if(i==2)
        {
            Intent BackPressed = new Intent(EditPassword.this, HomeScreen.class);
            BackPressed.putExtra("UserName",UserName);
            startActivity(BackPressed);
            i=0;
        }
    }
}