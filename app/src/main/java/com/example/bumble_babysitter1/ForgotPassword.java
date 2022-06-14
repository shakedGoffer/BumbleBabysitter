package com.example.bumble_babysitter1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ForgotPassword extends AppCompatActivity implements View.OnClickListener
{

    static DatabaseReference reference;
    Button btnDone;
    EditText etUserName, etNewPassword, etCheckNewPassword,etPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        if(!isConnectedToInternet())
        {
            Toast.makeText(this, "There is no internet connection", Toast.LENGTH_SHORT).show();
            finish();
            System.exit(0);
        }

        btnDone = (Button) findViewById(R.id.btnDone);
        btnDone.setOnClickListener(this);

        etUserName = (EditText) findViewById(R.id.etUserName);
        etPhone = (EditText) findViewById(R.id.etPhone);
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


    public void Clear()
    {
        etNewPassword.setText("");
        etUserName.setText("");
        etCheckNewPassword.setText("");
        etPhone.setText("");
    }


    @Override
    public void onClick(View v)
    {
        if(v==btnDone)
        {
            reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(etUserName.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task)
                {
                    if (task.isSuccessful())
                    {
                        DataSnapshot dataSnapshot = task.getResult();
                        if(String.valueOf(dataSnapshot.child("phone").getValue()).equals(etPhone.getText().toString()))
                            getPassword(etUserName.getText().toString(), String.valueOf(dataSnapshot.child("password").getValue()));
                        else
                        {
                            etPhone.setError("Wrong phone number");
                            etPhone.setText("");
                        }
                    }
                    else
                        etUserName.setError("Wrong user name");
                }
            });
        }
    }



    public void getPassword(String UserName, String Password)
    {
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(UserName).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task)
            {

                if (task.isSuccessful())
                {

                    if (task.getResult().exists())
                    {
                        DataSnapshot dataSnapshot = task.getResult();
                        if(etNewPassword.getText().toString().length()<6)
                        {
                            Toast.makeText(ForgotPassword.this,"Password must be at least 6 characters long",Toast.LENGTH_LONG).show();
                            etNewPassword.setError("Password must be at least 6 characters long");
                            etCheckNewPassword.setError("Password must be at least 6 characters long");
                        }
                        else if (etNewPassword.getText().toString().equals(etCheckNewPassword.getText().toString()) == false)
                        {
                            etCheckNewPassword.setError("Error, try again");
                            etCheckNewPassword.setText("");
                        }
                        else if(etNewPassword.getText().toString().equals(Password))
                        {
                            etNewPassword.setError("The new password can not be password that have been usd");
                            etCheckNewPassword.setError("The new password can not be password that have been usd");
                            etCheckNewPassword.setText("");
                            etNewPassword.setText("");
                        }
                        else
                            setNewPassword();
                    }

                }
                else // Error
                    Toast.makeText(ForgotPassword.this, "Failed to read", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setNewPassword()
    {
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(etUserName.getText().toString()).child("password").setValue(etNewPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                Intent HomeScreen = new Intent(ForgotPassword.this, HomeScreen.class);
                HomeScreen.putExtra("UserName",etUserName.getText().toString());
                startActivity(HomeScreen);
            }
        });
    }

    //לחיצה על כפתור החזרה
    @Override
    public void onBackPressed()
    {
        Intent BackPressed = new Intent(ForgotPassword.this, LogIn.class);
        startActivity(BackPressed);
    }

}