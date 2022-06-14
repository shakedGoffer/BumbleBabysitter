package com.example.bumble_babysitter1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.location.Address;
import android.location.Geocoder;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;


import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.os.Build;

import android.os.Looper;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import de.hdodenhof.circleimageview.CircleImageView;


import android.widget.TextView;



public class HomeScreen extends AppCompatActivity implements View.OnClickListener
{
    ImageButton  btnSearch;
    String UserName, Address;
    Double latitude,longitude;
    ImageView ivProfile_photo;
    Uri uri1;
    TextView tvName;
    LinearLayout lyNoFavorites;

    //ProgressDialog progressDialog;

    private LocationRequest locationRequest;
    public static ArrayList<Babysitter> BabysitterList = new ArrayList<Babysitter>();
    private ListView listView;


    //Firebase
    StorageReference storageRef;
    FirebaseStorage storage;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        // Stop background sound
        Intent intent = new Intent(HomeScreen.this, BackgroundSoundService.class);
        stopService(intent);

        if(!isConnectedToInternet())
        {
            Toast.makeText(this, "There is no internet connection", Toast.LENGTH_SHORT).show();
            finish();
            System.exit(0);
        }


        tvName = (TextView)findViewById(R.id.tvName);
        UserName = getIntent().getStringExtra("UserName");


        BabysitterList.clear();
        listView = (ListView) findViewById(R.id.BabysitterListView);

        lyNoFavorites = (LinearLayout)findViewById(R.id.lyNoFavorites);

        btnSearch=(ImageButton)findViewById(R.id.btnSearch);
        btnSearch.setClickable(true);
        btnSearch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                locationMassage();
            }
        });

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        ivProfile_photo=(ImageView) findViewById(R.id.ivProfile_photo);

        //progressDialog = new ProgressDialog(HomeScreen.this);
        //progressDialog.setMessage("Loading...");
        //progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //progressDialog.show();


        Toolbar toolbar =  findViewById(R.id.toolbar);
        toolbar.setTitle(UserName);
        setSupportActionBar(toolbar);


        getProfilePhoto();
        getDetailsFromFirebase();
        getFavorites();
        setUpOnclickListener();



    }

    // Get favorites list from firebase
    public void getFavorites()
    {
        ArrayList<String> Favorites  = new ArrayList<String>();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(UserName).child("Babysitters").child("favorites");
        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Favorites.add(dataSnapshot.getValue().toString());
                }
                if(!Favorites.isEmpty())
                    setUpData(Favorites);
                else
                    lyNoFavorites.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }});
    }

    // Check  internet connection
    public boolean isConnectedToInternet()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
    }



    // Set babysitter list
    private void setUpData(ArrayList<String> list)
    {
        for (String BabysitterUserName : list)
        {
            reference = FirebaseDatabase.getInstance().getReference("Babysitter");
            reference.child(BabysitterUserName).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>()
            {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot)
                {
                    CircleImageView ivProfile_photo = new CircleImageView(HomeScreen.this);
                    ivProfile_photo.setBackgroundResource(R.drawable.profile);
                    LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(80, 80);
                    buttonLayoutParams.setMargins(20, 20, 20, 0);
                    ivProfile_photo.setLayoutParams(buttonLayoutParams);
                    get_babysitterProfilePhoto(ivProfile_photo, dataSnapshot.child("userName").getValue(String.class));


                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
                    Date date = null;
                    try
                    {
                        date = sdf.parse(dataSnapshot.child("birthDate").getValue().toString());
                    }
                    catch (ParseException e)
                    {
                        e.printStackTrace();
                    }
                    Calendar myCalendar = Calendar.getInstance();
                    myCalendar.setTime(date);
                    int age = getAge(myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH));

                    Babysitter babysitter = new Babysitter(dataSnapshot.child("userName").getValue(String.class),
                            dataSnapshot.child("name").getValue(String.class),
                            Integer.valueOf(dataSnapshot.child("Profile").child("wage").getValue().toString()),age,
                            0, 0, ivProfile_photo);

                    Update_Rating(dataSnapshot.child("userName").getValue(String.class), babysitter);
                    BabysitterList.add(babysitter);
                }
            });
        }
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                //This method will be executed once the timer is over
                // Start
                setUpList(BabysitterList);
            }
        }, 1000);
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


    private void Update_Rating(String BabysitterUserName,Babysitter babysitter)
    {
        reference = FirebaseDatabase.getInstance().getReference("Babysitter");
        reference.child(BabysitterUserName).child("Rating").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (snapshot.exists())
                {
                    int count = 0;
                    float sum = 0;
                    for (DataSnapshot dataSnap : snapshot.getChildren())
                    {
                        float Rate = Float.valueOf(String.valueOf(dataSnap.getValue()));
                        sum += Rate;
                        count++;
                    }
                    sum = sum / count;
                    babysitter.setRating(sum);
                    babysitter.setTvNumberOfRatings(count);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
            }
        });
    }

    private void setUpList(ArrayList<Babysitter> list)
    {
        BabysitterDetailsAdapter adapter = new BabysitterDetailsAdapter(HomeScreen.this, 0, list, "HomeScreen");
        listView.setAdapter(adapter);
    }


    private void setUpOnclickListener()
    {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
            {
                Babysitter selectBabysitter = (Babysitter) (listView.getItemAtPosition(position));

                Intent Babysitter = new Intent(HomeScreen.this, BabysitterProfile.class);
                Babysitter.putExtra("BabysitterName",selectBabysitter.getUserName());
                Babysitter.putExtra("UserName",getIntent().getStringExtra("UserName"));
                Babysitter.putExtra("Screen","HomeScreen");

                //Babysitter.putExtra("CompleteAddress", completeLocation.getAddress());
                //Babysitter.putExtra("Latitude", completeLocation.getLatitude());
                //Babysitter.putExtra("Longitude", completeLocation.getLongitude());

                startActivity(Babysitter);

            }
        });

    }


    @Override
    public void onClick(View v)
    {
    }

    private void get_babysitterProfilePhoto( CircleImageView ivProfile_photo, String BabysitterUserName)
    {
        ivProfile_photo.setImageBitmap(null);
        ivProfile_photo.setImageURI(null);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        StorageReference ProfilePhoto = storageRef.child("ProfilePhoto/babysitter/" + BabysitterUserName + ".jpg");
        ProfilePhoto.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri)
            {
                uri1 = uri;
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>()
        {
            @Override
            public void onComplete(@NonNull Task<Uri> task)
            {
                if(task.isSuccessful())
                {
                    Glide.with(HomeScreen.this)
                            .load(uri1)
                            .error(R.drawable.ic_launcher_background)
                            .into(ivProfile_photo);
                }
                else
                {
                }
            }
        });
    }


    private void getDetailsFromFirebase()
    {
        DatabaseReference reference;
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(UserName).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task)
            {
                if(task.isSuccessful() )
                {
                    DataSnapshot dataSnapshot = task.getResult();

                    if(dataSnapshot.child("Profile").exists())
                    {
                        Address = String.valueOf(dataSnapshot.child("Profile").child("completeLocation").child("address").getValue());
                        latitude = (Double) dataSnapshot.child("Profile").child("completeLocation").child("latitude").getValue();
                        longitude = (Double) dataSnapshot.child("Profile").child("completeLocation").child("longitude").getValue();
                        CompleteLocation completeLocation = new CompleteLocation(Address, latitude, longitude);

                        tvName.setText(dataSnapshot.child("name").getValue().toString());
                    }
                    else
                    {
                        Toast.makeText(HomeScreen.this, "Failed to read", Toast.LENGTH_SHORT).show();
                        SharedPreferences preferences = getSharedPreferences("CheckBox", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("remember", "false");
                        editor.apply();
                        editor.commit();

                        Intent MainActivity = new Intent(HomeScreen.this, LogIn.class);
                        startActivity(MainActivity);
                    }

                }
                else // Error
                {
                    Toast.makeText(HomeScreen.this, "Failed to read", Toast.LENGTH_SHORT).show();
                    SharedPreferences preferences = getSharedPreferences("CheckBox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember", "false");
                    editor.apply();
                    editor.commit();

                    Intent MainActivity = new Intent(HomeScreen.this, LogIn.class);
                    startActivity(MainActivity);
                }
            }
        });

    }


    // menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu, menu);
        return  true;
    }
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.log_out)
        {
            SharedPreferences preferences = getSharedPreferences("CheckBox", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("remember", "false");
            editor.apply();
            editor.commit();

            Intent MainActivity = new Intent(HomeScreen.this, LogIn.class);
            startActivity(MainActivity);
            return true;
        }
        else if(id == R.id.editProfile)
        {
            Intent EditProfile = new Intent(HomeScreen.this, EditProfile.class);
            EditProfile.putExtra("UserName",getIntent().getStringExtra("UserName"));
            startActivity(EditProfile);
            return true;
        }
        else if(id == R.id.change_password)
        {
            Intent change_password = new Intent(HomeScreen.this,EditPassword.class);
            change_password.putExtra("UserName",getIntent().getStringExtra("UserName"));
            startActivity(change_password);
            return true;
        }
        else if(id == R.id.change_UserName)
        {
            Intent change_Username = new Intent(HomeScreen.this, EditUserName.class);
            change_Username.putExtra("UserName",getIntent().getStringExtra("UserName"));
            startActivity(change_Username);
            return true;
        }
        else if(id == R.id.blocked_accounts)
        {
            Intent BlockedAccounts = new Intent(HomeScreen.this, BlockedAccounts.class);
            BlockedAccounts.putExtra("UserName",getIntent().getStringExtra("UserName"));
            startActivity(BlockedAccounts);
            return true;
        }
        else if(id == R.id.help)
        {
            Intent Report_and_Help = new Intent(HomeScreen.this, Report_and_Help.class);
            Report_and_Help.putExtra("UserName",getIntent().getStringExtra("UserName"));
            Report_and_Help.putExtra("Item","Help");
            startActivity(Report_and_Help);
            return true;
        }
        return true;
    }


    private void locationMassage()
    {
        AlertDialog.Builder adb = new AlertDialog.Builder(HomeScreen.this);
        adb.setCancelable(false);
        adb.setTitle("Select location");
        adb.setMessage("Where would you like to look for babysitting services? " +
                "your address (" + Address + ") or your current location");
        adb.setIcon(R.drawable.ic_location);

        adb.setPositiveButton("My Current location", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                getCurrentLocation();
            }
        });
        adb.setNegativeButton("My address", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Intent Search = new Intent(HomeScreen.this, Search.class);
                Search.putExtra("UserName",UserName);
                Search.putExtra("CompleteAddress", getCompleteAddressString(latitude,longitude));
                Search.putExtra("Latitude", latitude);
                Search.putExtra("Longitude", longitude);

                startActivity(Search);
            }
        });

        adb.setNeutralButton("cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        adb.create().show();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                if (isGPSEnabled())
                    getCurrentLocation();
                else
                    turnOnGPS();
            }
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {

                getCurrentLocation();
            }
        }
    }

    private void getCurrentLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(HomeScreen.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (isGPSEnabled()) {

                    LocationServices.getFusedLocationProviderClient(HomeScreen.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);

                                    LocationServices.getFusedLocationProviderClient(HomeScreen.this)
                                            .removeLocationUpdates(this);

                                    if (locationResult != null && locationResult.getLocations().size() >0){

                                        int index = locationResult.getLocations().size() - 1;
                                        double latitude = locationResult.getLocations().get(index).getLatitude();
                                        double longitude = locationResult.getLocations().get(index).getLongitude();


                                        Toast.makeText(HomeScreen.this, "Latitude: "+ latitude + "\n" + "Longitude: "+ longitude, Toast.LENGTH_SHORT).show();
                                        Toast.makeText(HomeScreen.this, getCompleteAddressString(latitude,longitude), Toast.LENGTH_SHORT).show();

                                        Intent Search = new Intent(HomeScreen.this, Search.class);
                                        Search.putExtra("UserName",UserName);
                                        Search.putExtra("CompleteAddress", getCompleteAddressString(latitude,longitude));
                                        Search.putExtra("Latitude", latitude);
                                        Search.putExtra("Longitude", longitude);
                                        startActivity(Search);
                                    }
                                }
                            }, Looper.getMainLooper());

                }
                else
                    turnOnGPS();
            }
            else
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void turnOnGPS()
    {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(HomeScreen.this, "GPS is already tured on", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(HomeScreen.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });

    }

    private boolean isGPSEnabled()
    {
        LocationManager locationManager = null;
        boolean isEnabled = false;

        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;

    }


    @SuppressLint("LongLogTag")
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE)
    {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();

                Log.w("My Current loction address", strReturnedAddress.toString());
            } else {
                Log.w("My Current loction address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("My Current loction address", "Canont get Address!");
        }
        return strAdd;
    }

    public void getProfilePhoto()
    {
        ivProfile_photo.setImageBitmap(null);
        ivProfile_photo.setImageURI(null);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        StorageReference ProfilePhoto = storageRef.child("ProfilePhoto/users/" + UserName + ".jpg");
        ProfilePhoto.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) 
            {
                uri1 = uri;
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>()
        {
            @Override
            public void onComplete(@NonNull Task<Uri> task)
            {
                if(task.isSuccessful())
                {
                    Glide.with(HomeScreen.this)
                            .load(uri1)
                            .error(R.drawable.ic_launcher_background)
                            .into(ivProfile_photo);

                    //progressDialog.dismiss();
                }
                else
                {
                    //progressDialog.dismiss();
                }
            }
        });
    }


    //לחיצה על כפתור החזרה
    int BackP=0;
    @Override
    public void onBackPressed()
    {
        BackP++;
        if(BackP==2)
        {
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
            BackP=0;
        }

    }

}