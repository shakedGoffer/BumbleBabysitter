package com.example.bumble_babysitter1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditUserName extends AppCompatActivity implements View.OnClickListener {

    String UserName;
    Button btnDone;
    EditText etPassword, etNew_UserName;
    ImageView ivProfile_photo;
    ProgressDialog progressDialog;


    static DatabaseReference reference;
    StorageReference storageRef;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_name);

        if(!isConnectedToInternet())
        {
            Toast.makeText(this, "There is no internet connection", Toast.LENGTH_SHORT).show();
            finish();
            System.exit(0);
        }

        UserName = getIntent().getStringExtra("UserName");

        ivProfile_photo=(ImageView) findViewById(R.id.ivProfile_photo);

        btnDone = (Button) findViewById(R.id.btnDone);
        btnDone.setOnClickListener(this);

        etPassword = (EditText) findViewById(R.id.etPassword);
        etNew_UserName = (EditText) findViewById(R.id.etNew_UserName);

        progressDialog = new ProgressDialog(EditUserName.this);
        progressDialog.setMessage("Loading...");
        //progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);


        Clear();
        get_ProfilePhoto();
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
        etNew_UserName.setText("");
        etPassword.setText("");
    }

    @Override
    public void onClick(View v) {
        if (v == btnDone)
        {
            if(etPassword.getText().toString().isEmpty())
                etPassword.setError("Pleas enter your password");
            else
                getPassword(UserName, etPassword.getText().toString());
        }

    }

    public void getPassword(String UserName, String Password)
    {
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(UserName).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if (task.isSuccessful()) {

                    if (task.getResult().exists())
                    {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (String.valueOf(dataSnapshot.child("password").getValue()) == Password || String.valueOf(dataSnapshot.child("password").getValue()).equals(Password))
                        {
                            if(etNew_UserName.getText().toString().equals(UserName))
                                etNew_UserName.setError("Pls enter a new user name");
                            else if(etNew_UserName.getText().toString().length()<6)
                                etNew_UserName.setError("User name must contain at least 6 characters");
                            else
                            {
                                setNewUserName(etNew_UserName.getText().toString());
                            }

                        }
                        else // wrong password
                            etPassword.setError("Wrong password ,try again");
                    }
                    else // User Doesn't Exist
                    {
                        Toast.makeText(EditUserName.this, "User Doesn't Exist", Toast.LENGTH_SHORT).show();
                        Clear();
                    }
                } else // Error
                    Toast.makeText(EditUserName.this, "Failed to read", Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void  setNewUserName(String NewUserName)
    {
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(UserName).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task)
            {
                if(task.isSuccessful())
                {
                    DataSnapshot dataSnapshot1 = task.getResult();

                    //copy user details
                    User u = new User(String.valueOf(dataSnapshot1.child("name").getValue()),
                            String.valueOf(dataSnapshot1.child("birthDate").getValue()),
                            String.valueOf(dataSnapshot1.child("emailAddress").getValue()),
                            String.valueOf(dataSnapshot1.child("phone").getValue())
                            , NewUserName, String.valueOf(dataSnapshot1.child("password").getValue()));

                    String Address = String.valueOf(dataSnapshot1.child("Profile").child("completeLocation").child("address").getValue());
                    Double latitude = (Double) dataSnapshot1.child("Profile").child("completeLocation").child("latitude").getValue();
                    Double longitude = (Double) dataSnapshot1.child("Profile").child("completeLocation").child("longitude").getValue();

                    CompleteLocation completeLocation = new CompleteLocation(Address,latitude,longitude);

                    //copy UserProfile details
                    UserProfile up = new UserProfile(String.valueOf(dataSnapshot1.child("Profile").child("children_num").getValue()),
                            String.valueOf(dataSnapshot1.child("Profile").child("childrenAges").getValue()),
                            Integer.valueOf(String.valueOf(dataSnapshot1.child("Profile").child("wage").getValue())), completeLocation);

                    reference.child(NewUserName).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task)
                        {
                            if (task.getResult().exists())
                            {
                                //user name already exist
                                etNew_UserName.setError("This user name already exist");
                            }
                            else
                            {
                                progressDialog.show();

                                //new child(user) withe the new user name
                                reference.child(NewUserName).setValue(u);
                                reference.child(NewUserName).child("Profile").setValue(up);

                                //copy favorites to the user(withe the new user name)
                                for (DataSnapshot zoneSnapshot: dataSnapshot1.child("Babysitters").child("favorites").getChildren())
                                {
                                    reference.child(NewUserName).child("Babysitters").child("favorites").push().setValue(zoneSnapshot.getValue());
                                }
                                for (DataSnapshot zoneSnapshot: dataSnapshot1.child("Babysitters").child("blocked").getChildren())
                                {
                                    reference.child(NewUserName).child("Babysitters").child("blocked").push().setValue(zoneSnapshot.getValue());
                                }

                                //check RememberMe- SharedPreferences
                                checkRememberMe(NewUserName);

                                //Delete user(withe the old user name)
                                FirebaseDatabase.getInstance().getReference("Users").child(UserName).removeValue();

                                // get end set profile photo
                                //get_ProfilePhoto(NewUserName);
                                copyImage(NewUserName);
                            }
                        }
                    });
                }

            }
        });
    }

    private void checkRememberMe(String NewUserName)
    {
        SharedPreferences preferences = getSharedPreferences("CheckBox", MODE_PRIVATE);
        String Check = preferences.getString("remember","");
        if(Check.equals("true"))
        {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("remember", "true");
            editor.putString("UserName", NewUserName);
            editor.apply();
            editor.commit();
        }
    }


    // download profile photo (with the old user name)
    private void get_ProfilePhoto()
    {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        StorageReference DownloadUrl = storageRef.child("ProfilePhoto/users/" + UserName + ".jpg");
        DownloadUrl.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri uri)
            {
                Glide.with(EditUserName.this)
                        .load(uri)
                        .error(R.drawable.ic_launcher_background)
                        .into(ivProfile_photo);
            }
        });
    }


    // upload profile photo (with the new user name) + delete the profile photo with the old user name
    public void copyImage(String NewUserName )
    {
        Bitmap bm=((BitmapDrawable)ivProfile_photo.getDrawable()).getBitmap();
        Uri uriSave = getImageUri(EditUserName.this, bm);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        StorageReference ProfilePhoto = storageRef.child("ProfilePhoto/users/" + NewUserName + ".jpg");
        ProfilePhoto.putFile(uriSave).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
            {
                if(task.isSuccessful())
                {
                    storageRef.child("ProfilePhoto/users/" + UserName + ".jpg").delete();
                    progressDialog.dismiss();

                    Intent BackPressed = new Intent(EditUserName.this, HomeScreen.class);
                    BackPressed.putExtra("UserName",NewUserName);
                    startActivity(BackPressed);
                }
                else
                {
                    Toast.makeText(EditUserName.this, "Something went wrong: ", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                    Intent BackPressed = new Intent(EditUserName.this, HomeScreen.class);
                    BackPressed.putExtra("UserName",NewUserName);
                    startActivity(BackPressed);
                }
            }
        });
    }

    public Uri getImageUri(Context inContext, Bitmap inImage)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }




    //לחיצה על כפתור החזרה
    int i=0;
    @Override
    public void onBackPressed()
    {
        i++;
        if(i==2)
        {
            Intent BackPressed = new Intent(EditUserName.this, HomeScreen.class);
            BackPressed.putExtra("UserName",UserName);
            startActivity(BackPressed);
            i=0;
        }
    }
}