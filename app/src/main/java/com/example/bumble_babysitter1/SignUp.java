package com.example.bumble_babysitter1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.Locale;

public class SignUp extends AppCompatActivity implements View.OnClickListener
{
    Button btnSignUp;
    EditText etFName,etLName,etEmailAddress,etPhone,etUserName,etPassword,etCheckPassword,etDate;
    final Calendar myCalendar = Calendar.getInstance();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        if(!isConnectedToInternet())
        {
            Toast.makeText(SignUp.this, "There is no internet connection", Toast.LENGTH_SHORT).show();
            finish();
            System.exit(0);
        }

        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(this);

        etDate = (EditText) findViewById(R.id.etDate);
        etDate.setShowSoftInputOnFocus(false);


        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
            {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                etDate.setText(dayOfMonth +"/"+ monthOfYear + year);
                updateLabel();
            }
        };


        etDate.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN)
            {
                etDate.setShowSoftInputOnFocus(false);

                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

                new DatePickerDialog(SignUp.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
            return false;
        });


        //datePicker = (DatePicker) findViewById(R.id.datePicker);

        etFName = (EditText) findViewById(R.id.etFName);
        etLName = (EditText) findViewById(R.id.etLName);


        etEmailAddress = (EditText) findViewById(R.id.etEmailAddress);

        etPhone = (EditText) findViewById(R.id.etPhone);
        etUserName = (EditText) findViewById(R.id.etUserName);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etCheckPassword = (EditText) findViewById(R.id.etCheckPassword);


        Clear();


    }

    // Check  internet connection
    public boolean isConnectedToInternet()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
    }


    private void updateLabel()
    {
        String myFormat="MM/dd/yy";
        SimpleDateFormat dateFormat=new SimpleDateFormat(myFormat, Locale.US);
        etDate.setText(dateFormat.format(myCalendar.getTime()));
        etDate.setError(null);
    }




    @Override
    public void onClick(View v)
    {

        if(btnSignUp ==v)
        {
            Sign_Up();
        }
    }

    public void Clear()
    {
        etFName.setText("");
        etLName.setText("");
        etEmailAddress.setText("");
        etPhone.setText("");
        etUserName.setText("");
        etPassword.setText("");
        etCheckPassword.setText("");
        etDate.setText("");
    }


    public void Sign_Up()
    {
        //First name error
        if(etFName.getText().toString().equals(""))
            etFName.setError("Enter your first name");

        //Last name error
        else if(etLName.getText().toString().equals(""))
            etLName.setError("Enter your last name");

        //Date error
        else if(etDate.getText().toString().length()<1)
            etDate.setError("Enter Date of Birth");
        else if(myCalendar.get(Calendar.YEAR) > 2004)
        {
            etDate.setError("This app is for adults only");
            etDate.setText("");
        }
        else if(getAge(myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)) < 18)
        {
            etDate.setError("This app is for adults only");
            etDate.setText("");
        }
        //EmailAddress error
        else if(etEmailAddress.getText().toString().equals(""))
            etEmailAddress.setError("You mast enter an email address");
        else if(!isEmailValid(etEmailAddress.getText().toString()))
            etEmailAddress.setError("This email address doesn't exist");

        //phone number error
        else if(etPhone.getText().toString().length()<10 )
            etPhone.setError("Incorrect phone number, please try again");

        //User name error
        else if(etUserName.getText().toString().length()<6)
            etUserName.setError("User name must contain at least 6 characters");

        //Password error
        else if(etPassword.getText().toString().length()<6)
        {
            etPassword.setError("Password must contain at least 6 characters");
            etPassword.setText("");
            etCheckPassword.setText("");
        }
        //CheckPassword error
        else if(!etPassword.getText().toString().equals(etCheckPassword.getText().toString()))
        {
            etCheckPassword.setError("The passwords doesn't mach");
            etCheckPassword.setText("");
        }
        else
        {
            PotInFirebase();
        }
    }

    public int getAge(int year, int month, int dayOfMonth)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            return Period.between(LocalDate.of(year, month, dayOfMonth),
                    LocalDate.now()).getYears();
        }
        return -1;
    }



    boolean isEmailValid(CharSequence email)
    {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void PotInFirebase()
    {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users");
        reference1.child(etUserName.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
       {
           @Override
           public void onComplete(@NonNull Task<DataSnapshot> task)
           {
               if(task.isSuccessful())
               {
                   DataSnapshot dataSnapshot = task.getResult();
                   if (task.getResult().exists() ||
                           String.valueOf(dataSnapshot.child("userName").getValue()) == etUserName.getText().toString() ||
                           String.valueOf(dataSnapshot.child("userName").getValue()).equals(etUserName.getText().toString()))
                   {
                       //user name already exist
                       etUserName.setError("This user name already exist");
                   }
                   else
                   {

                       User u = new User(etFName.getText().toString() + " " + etLName.getText().toString(),
                               etDate.getText().toString(), etEmailAddress.getText().toString(),
                               etPhone.getText().toString(), etUserName.getText().toString(),
                               etPassword.getText().toString());
                       FirebaseDatabase db = FirebaseDatabase.getInstance();
                       DatabaseReference reference = db.getReference("Users");
                       reference.child(etUserName.getText().toString()).setValue(u).addOnCompleteListener(new OnCompleteListener<Void>()
                       {
                           @Override
                           public void onComplete(@NonNull Task<Void> task)
                           {
                           }
                       });
                       Intent Profile = new Intent(SignUp.this, Profile.class);
                       Profile.putExtra("UserName",etUserName.getText().toString());
                       startActivity(Profile);
                   }
               }
           }
       });
    }



    //לחיצה על כפתור החזרה
    int i=0;
    public void onBackPressed()
    {
        i++;
        if(i==2)
        {
            super.onBackPressed();
            Intent BackPressed = new Intent(SignUp.this, LogIn.class);
            startActivity(BackPressed);
            i=0;
        }
    }


}