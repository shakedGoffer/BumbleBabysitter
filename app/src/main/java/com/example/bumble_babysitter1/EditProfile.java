package com.example.bumble_babysitter1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Locale;

public class EditProfile extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, View.OnTouchListener {
    String UserName;
    EditText etChildren_num,etAges,etAddress;
    Double latitude,longitude;
    Button btnDone;
    TextView tvSeekBar;
    SeekBar seekBar;
    ImageView ivProfile_photo;
    ProgressDialog progressDialog;
    private LocationRequest locationRequest;
    private CompleteLocation completeLocation;


    private  static  final  int REQUEST_CAMERA = 200;
    private  static  final  int REQUEST_STORAGE = 300;
    private  static  final  int REQUEST_LOCATION = 400;

    private  static  final  int CAMERA = 1;
    private  static  final  int STORAGE = 2;
    private  static  final  int LOCATION = 3;

    //

    DatabaseReference reference;
    StorageReference storageRef;
    FirebaseStorage storage;

    Uri uri;
    Bitmap bitmap;

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        if(!isConnectedToInternet())
        {
            Toast.makeText(this, "There is no internet connection", Toast.LENGTH_SHORT).show();
            finish();
            System.exit(0);
        }

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        ivProfile_photo=(ImageView) findViewById(R.id.ivProfile_photo);
        ivProfile_photo.setOnTouchListener(this);

        etChildren_num=(EditText)findViewById(R.id.etChildren_num);
        etAges=(EditText)findViewById(R.id.etAges);

        etAddress=(EditText)findViewById(R.id.etAddress);
        etAddress.setShowSoftInputOnFocus(false);
        etAddress.setOnTouchListener(this);

        btnDone =(Button)findViewById(R.id.btnDone);
        btnDone.setOnClickListener(this);

        progressDialog = new ProgressDialog(EditProfile.this);
        progressDialog.setMessage("Loading...");
        //progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);



        tvSeekBar=(TextView)findViewById(R.id.tvSeekBar);
        tvSeekBar.setText("10");
        seekBar = (SeekBar) findViewById(R.id.SeekBar);
        seekBar.setMax(99);
        seekBar.setProgress(5);
        seekBar.setOnSeekBarChangeListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            seekBar.setMin(5);
        }

        UserName = getIntent().getStringExtra("UserName");

        Clear();
        getFromFirebase();
    }

    // Check  internet connection
    public boolean isConnectedToInternet()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
    }


    public void getProfilePhoto()
    {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        StorageReference ProfilePhoto = storageRef.child("ProfilePhoto/users/" + UserName + ".jpg");
        ProfilePhoto.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri)
            {
                Glide.with(EditProfile.this)
                        .load(uri)
                        .error(R.drawable.ic_launcher_background)
                        .into(ivProfile_photo);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {


            }
        });

    }

    private void getFromFirebase()
    {
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(UserName).child("Profile").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task)
            {
                if(task.isSuccessful())
                {
                    DataSnapshot dataSnapshot = task.getResult();
                    etChildren_num.setText(String.valueOf(dataSnapshot.child("children_num").getValue()));
                    etAges.setText(String.valueOf(dataSnapshot.child("childrenAges").getValue()));
                    seekBar.setProgress(Integer.valueOf(String.valueOf(dataSnapshot.child("wage").getValue())));
                    etAddress.setText(String.valueOf(dataSnapshot.child("completeLocation").child("address").getValue()));
                    latitude = (double) dataSnapshot.child("completeLocation").child("latitude").getValue();
                    longitude = (double) dataSnapshot.child("completeLocation").child("longitude").getValue();
                    completeLocation = new CompleteLocation(etAddress.getText().toString(),latitude,longitude);

                }
                else // Error
                    Toast.makeText(EditProfile.this, "Failed to read", Toast.LENGTH_SHORT).show();
            }
        });
        getProfilePhoto();

    }


    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        etAddress.setShowSoftInputOnFocus(false);

        if(v==ivProfile_photo)
        {
            AlertDialog.Builder adb = new AlertDialog.Builder(EditProfile.this);
            adb.setCancelable(true);
            adb.setTitle("Profile photo");
            adb.setIcon(R.drawable.profile);

            adb.setPositiveButton("take a picture", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    requestPermission(CAMERA);
                }
            });
            adb.setNegativeButton("Choose from gallery", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    requestPermission(STORAGE);
                }
            });
            adb.setNeutralButton("delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ivProfile_photo.setImageBitmap(null);
                    ivProfile_photo.setImageURI(null);
                    storage = FirebaseStorage.getInstance();
                    storageRef = storage.getReference();
                    StorageReference ProfilePhoto = storageRef.child("ProfilePhoto/users/" + getIntent().getStringExtra("UserName") + ".jpg");
                    ProfilePhoto.delete();
                }
            });
            adb.create().show();
        }
        else if(v==etAddress)
        {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            getCurrentLocation();
        }
        return false;
    }


    @Override
    public void onClick(View v)
    {
        etAddress.setShowSoftInputOnFocus(false);
        if(v==btnDone)
            Done();

        if(v==etAddress)
        {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            getCurrentLocation();
        }
    }

    public void Clear()
    {
        ivProfile_photo.setImageBitmap(null);
        ivProfile_photo.setImageURI(null);
        etChildren_num.setText("");
        etAges.setText("");
        tvSeekBar.setText("5");
        seekBar.setProgress(5);
    }

    public void Done()
    {
        if(etChildren_num.getText().toString().isEmpty() || Integer.valueOf(etChildren_num.getText().toString())<1)
        {
            etChildren_num.setError("Error,try again");
            etChildren_num.setText("");
        }
        else  if(etAges.getText().toString().isEmpty())
        {
            etAges.setError("Error,try again");
            etAges.setText("");
        }
        else if(etAddress.getText().toString().equals(""))
        {
            etAddress.setError("Enter your address");
        }
        else
        {
            progressDialog.show();
            PotInFirebase();
        }
    }


    private void PotInFirebase ()
    {

        UserProfile up = new UserProfile(etChildren_num.getText().toString(),etAges.getText().toString(),
                Integer.valueOf(seekBar.getProgress()),completeLocation);

        reference = FirebaseDatabase.getInstance().getReference("Users").child(getIntent().getStringExtra("UserName"));
        reference.child("Profile").setValue(up);

        if(uri!=null)
        {
            UploadPhoto();
        }
        else
        {
            Intent HomeScreen = new Intent(EditProfile.this, HomeScreen.class);
            HomeScreen.putExtra("UserName", getIntent().getStringExtra("UserName"));
            startActivity(HomeScreen);
        }

    }


    public void UploadPhoto()
    {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        StorageReference ProfilePhoto = storageRef.child("ProfilePhoto/users/" + getIntent().getStringExtra("UserName") + ".jpg");
        ProfilePhoto.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {
                Uri downloadUrl = taskSnapshot.getUploadSessionUri();
            }
        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
            {
                progressDialog.dismiss();
                Intent HomeScreen = new Intent(EditProfile.this, HomeScreen.class);
                HomeScreen.putExtra("UserName", getIntent().getStringExtra("UserName"));
                startActivity(HomeScreen);
            }
        });
    }


    @Override
    //seek bar
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        tvSeekBar.setText(String.valueOf(seekBar.getProgress()));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}
    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA)
        {
            bitmap = (Bitmap) data.getExtras().get("data");
            uri =  getImageUri (EditProfile.this ,bitmap);
            ivProfile_photo.setImageURI(uri);
        }

        if (requestCode == REQUEST_STORAGE)
        {
            uri = data.getData();
            ivProfile_photo.setImageURI(uri);
        }

        if (requestCode == 2)
        {
            if (resultCode == Activity.RESULT_OK)
            {

                getCurrentLocation();
            }
        }

    }

    public Uri getImageUri(Context inContext, Bitmap inImage)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }




    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQUEST_CAMERA == requestCode)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_CAMERA);

            }
            else
                Toast.makeText(this, "You Do not have camera permission", Toast.LENGTH_SHORT).show();
        }
        else if(REQUEST_STORAGE == requestCode)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Title"), REQUEST_STORAGE);
            }
            else
                Toast.makeText(this, "You Do not have storage permission", Toast.LENGTH_SHORT).show();
        }
        else if(REQUEST_LOCATION == requestCode)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                getCurrentLocation();
            }
        }
    }


    //---------Request Permissions
    public void requestPermission(int permission)
    {
        if(permission == CAMERA)
        {
            ActivityCompat.requestPermissions(EditProfile.this,
                    new String[]{Manifest.permission.CAMERA},REQUEST_CAMERA);
        }
        else if(permission == STORAGE)
        {
            ActivityCompat.requestPermissions(EditProfile.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
        }
        else if(permission == LOCATION)
        {
            ActivityCompat.requestPermissions(EditProfile.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION );

        }
    }

    //locaition
    private void getCurrentLocation()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (ActivityCompat.checkSelfPermission(EditProfile.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                if (isGPSEnabled())
                {

                    LocationServices.getFusedLocationProviderClient(EditProfile.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);

                                    LocationServices.getFusedLocationProviderClient(EditProfile.this)
                                            .removeLocationUpdates(this);

                                    if (locationResult != null && locationResult.getLocations().size() >0){

                                        int index = locationResult.getLocations().size() - 1;
                                        double latitude = locationResult.getLocations().get(index).getLatitude();
                                        double longitude = locationResult.getLocations().get(index).getLongitude();

                                        completeLocation = new CompleteLocation(getCompleteAddressString(latitude,longitude),latitude,longitude);
                                        etAddress.setText(getCompleteAddressString(latitude,longitude));

                                        /*
                                        Toast.makeText(Profile.this, "Latitude: "+ latitude + "\n" + "Longitude: "+ longitude, Toast.LENGTH_SHORT).show();
                                        Toast.makeText(Profile.this, getCompleteAddressString(latitude,longitude).toString(), Toast.LENGTH_SHORT).show();

                                        Intent Search = new Intent(Profile.this, Search.class);
                                        Search.putExtra("UserName",UserName);
                                        Search.putExtra("Latitude", latitude);
                                        Search.putExtra("Longitude", longitude);
                                        Search.putExtra("CompleteAddress", getCompleteAddressString(latitude,longitude).toString());
                                        startActivity(Search);
                                         */

                                    }
                                }
                            }, Looper.getMainLooper());

                }
                else
                    turnOnGPS();
            }
            else
                requestPermission(LOCATION);
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
                    Toast.makeText(EditProfile.this, "GPS is already tured on", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(EditProfile.this, 2);
                            } catch (IntentSender.SendIntentException ex)
                            {
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

        if (locationManager == null)
        {
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
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("");
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


    //לחיצה על כפתור החזרה
    int i=0;
    @Override
    public void onBackPressed()
    {
        i++;
        if(i==2)
        {
            Intent BackPressed = new Intent(EditProfile.this, HomeScreen.class);
            BackPressed.putExtra("UserName", getIntent().getStringExtra("UserName"));
            startActivity(BackPressed);
            i=0;
        }
    }

}