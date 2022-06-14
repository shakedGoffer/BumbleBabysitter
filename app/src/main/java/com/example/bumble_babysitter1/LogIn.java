package com.example.bumble_babysitter1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LogIn extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    static DatabaseReference reference;
    Button btnLogIn;
    EditText etUserName,etPassword;
    TextView tvSignup ,tvForgotPassword;
    Context context;
    CheckBox cb_RememberMe;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        // Stop background sound
        Intent intent = new Intent(LogIn.this, BackgroundSoundService.class);
        stopService(intent);


        if(!isConnectedToInternet())
        {
            Toast.makeText(LogIn.this, "There is no internet connection", Toast.LENGTH_SHORT).show();
            finish();
            System.exit(0);
        }




        btnLogIn =(Button)findViewById(R.id.btnLogIn);
        btnLogIn.setOnClickListener(this);

        etUserName=(EditText)findViewById(R.id.etUserName);
        etPassword=(EditText)findViewById(R.id.etPassword);
        tvSignup= (TextView) findViewById(R.id.tvSignup);
        tvSignup.setClickable(true);
        tvSignup.setOnClickListener(this);

        tvForgotPassword = (TextView) findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setClickable(true);
        tvForgotPassword.setOnClickListener(this);

        cb_RememberMe = (CheckBox)findViewById(R.id.cb_RememberMe);
        cb_RememberMe.setOnCheckedChangeListener(this);

        context = LogIn.this;

        Check_RememberMe();
        Clear();
    }


    public boolean isConnectedToInternet()
    {
        ConnectivityManager connectivityManager =  (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return  (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
    }

    // Clear edit text UserName text and edit text Password text
    public void Clear()
    {
        etUserName.setText("");
        etPassword.setText("");
    }


    @Override
    public void onClick(View v)
    {
        if(v==tvSignup)
        {
            Intent SignUp = new Intent(LogIn.this, SignUp.class);
            startActivity(SignUp);
        }
        if(v==tvForgotPassword)
        {
            Intent ForgotPassword = new Intent(LogIn.this, ForgotPassword.class);
            startActivity(ForgotPassword);
        }
        if(btnLogIn==v)
        {
            if(etUserName.getText().toString().isEmpty())
                etUserName.setError("please enter your user name");
            else if(etPassword.getText().toString().isEmpty())
                etPassword.setError("please enter your password");
            else
                LogIn(etUserName.getText().toString(), etPassword.getText().toString());
        }
    }



    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        /*
        if(cb_RememberMe.isChecked()) {
            SharedPreferences preferences = getSharedPreferences("CheckBox", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("remember", "true");
            editor.putString("UserName", etUserName.getText().toString());
            editor.apply();
            editor.commit();
        }
        else if(!cb_RememberMe.isChecked())
        {
            SharedPreferences preferences = getSharedPreferences("CheckBox", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("remember", "false");
            editor.apply();
            editor.commit();
        }
         */
    }

    public void Check_RememberMe()
    {
        SharedPreferences preferences = getSharedPreferences("CheckBox", MODE_PRIVATE);
        String Check = preferences.getString("remember","");
        if(Check.equals("true"))
        {
            SharedPreferences preferences_UserName = getSharedPreferences("CheckBox", MODE_PRIVATE);
            String UserName = preferences.getString("UserName","");

            //
            Intent HomeScreen = new Intent(LogIn.this, HomeScreen.class);
            HomeScreen.putExtra("UserName",UserName);
            startActivity(HomeScreen);
        }
    }


    int i=0;
    public void LogIn(String UserName, String Password)
    {
        i++;
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(UserName).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task)
            {

                if (task.isSuccessful())
                {

                    if (task.getResult().exists())
                    {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (String.valueOf(dataSnapshot.child("password").getValue()) == Password ||String.valueOf(dataSnapshot.child("password").getValue()).equals(Password))
                        {
                            if(cb_RememberMe.isChecked())
                            {

                                SharedPreferences preferences = getSharedPreferences("CheckBox", MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("remember", "true");
                                editor.putString("UserName", etUserName.getText().toString());
                                editor.apply();
                                editor.commit();
                            }

                            Intent HomeScreen = new Intent(LogIn.this, HomeScreen.class);
                            HomeScreen.putExtra("UserName",UserName);
                            startActivity(HomeScreen);
                        }
                        else // wrong password
                        {
                            etPassword.setError("wrong password");
                            //Toast.makeText(LogIn.this,"Error, try again",Toast.LENGTH_SHORT).show();
                            etPassword.setText("");
                            //tvError.setVisibility(View.VISIBLE);
                        }
                    }
                    else // User Doesn't Exist
                    {
                        Toast.makeText(LogIn.this, "User Doesn't Exist", Toast.LENGTH_SHORT).show();
                        Clear();

                        if (i > 3)
                        {
                            AlertDialog.Builder adb = new AlertDialog.Builder(context);
                            //adb.setCancelable(false);
                            adb.setTitle("User Doesn't Exist");
                            adb.setMessage("This user is not found, if you do not have a user please register. ");
                            adb.setIcon(R.drawable.ic_error);
                            adb.setPositiveButton("Register", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent SignUp = new Intent(LogIn.this, SignUp.class);
                                    startActivity(SignUp);
                                }
                            });
                            adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            adb.create().show();
                            i=0;
                        }
                    }
                }
                else // Error
                    Toast.makeText(LogIn.this, "Failed to read", Toast.LENGTH_SHORT).show();
            }
        });

    }



    //לחיצה על כפתור החזרה
    int BackPressed=0;
    @Override
    public void onBackPressed()
    {
        BackPressed++;
        if(BackPressed==2)
        {
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
            BackPressed=0;
        }
    }

}