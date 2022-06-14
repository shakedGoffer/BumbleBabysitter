package com.example.bumble_babysitter1;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class BabysitterProfile extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {
    String UserName , BabysitterUserName ,BabysitterPhone;
    ImageView ivProfile_photo;
    Uri uri1;
    Button btnMessage,btnWhatsApp ,btnFavorite;
    TextView tvBabysitterWage,tvBabysitterAge, tvNumRatings,tvNumber_OfComments,tvBabysitterName,tvBabysitter_specialty;
    RatingBar rb_CurrentRate;
    EditText etComment;
    ImageButton btnAddComment;
    CompleteLocation completeLocation;
    Menu menuSave;

    public static ArrayList<Comment> CommentsArrList = new ArrayList<Comment>();
    private ListView lvComments;

    StorageReference storageRef;
    FirebaseStorage storage;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_babysitter_profile);

        if(!isConnectedToInternet())
        {
            Toast.makeText(this, "There is no internet connection", Toast.LENGTH_SHORT).show();
            finish();
            System.exit(0);
        }

        UserName = getIntent().getStringExtra("UserName");
        BabysitterUserName = getIntent().getStringExtra("BabysitterName");

        Double latitude = (double) getIntent().getDoubleExtra("Latitude", 0);
        Double longitude = (double) getIntent().getDoubleExtra("Longitude", 0);
        String address = getIntent().getStringExtra("CompleteAddress");
        completeLocation = new CompleteLocation(address,latitude,longitude);

        tvBabysitterName = (TextView)findViewById(R.id.tvBabysitterName);
        tvNumRatings = (TextView)findViewById(R.id.tvNumRatings);
        tvNumber_OfComments = (TextView)findViewById(R.id.tvNumber_OfComments);
        tvBabysitterWage = (TextView)findViewById(R.id.tvBabysitterWage);
        tvBabysitterAge = (TextView)findViewById(R.id.tvBabysitterAge);
        tvBabysitter_specialty = (TextView)findViewById(R.id.tvBabysitter_specialty);

        etComment = (EditText)findViewById(R.id.etComment);

        lvComments = (ListView)findViewById(R.id.lvComments);
        CommentsArrList.clear();

        ivProfile_photo=(ImageView) findViewById(R.id.ivProfile_photo);
        btnMessage=(Button)findViewById(R.id.btnMessage);
        btnMessage.setOnClickListener(this);

        btnWhatsApp=(Button)findViewById(R.id.btnWhatsApp);
        btnWhatsApp.setOnClickListener(this);

        btnFavorite=(Button)findViewById(R.id. btnFavorite);
        btnFavorite.setOnClickListener(this);

        btnAddComment=(ImageButton)findViewById(R.id.btnAddComment);
        btnAddComment.setOnClickListener(this);


        rb_CurrentRate =(RatingBar)findViewById(R.id.rb_CurrentRat);
        rb_CurrentRate.setOnTouchListener(this);


        //  Toolbar toolbar + arrow
        Toolbar toolbar =  findViewById(R.id.toolbar_babysitter);
        toolbar.setTitle(BabysitterUserName);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(getIntent().getStringExtra("Screen").equals("HomeScreen"))
                {
                    Intent HomeScreen = new Intent(BabysitterProfile.this, HomeScreen.class);
                    HomeScreen.putExtra("UserName", getIntent().getStringExtra("UserName"));
                    startActivity(HomeScreen);
                }
                else if(getIntent().getStringExtra("Screen").equals("BlockedAccounts"))
                {
                    Intent BlockedAccounts = new Intent(BabysitterProfile.this, BlockedAccounts.class);
                    BlockedAccounts.putExtra("UserName", getIntent().getStringExtra("UserName"));
                    startActivity(BlockedAccounts);
                }
                else
                    BabysitterProfile.super.onBackPressed();
            }
        });

        check_Favorites(BabysitterUserName);
        getBabysitter_details(BabysitterUserName);

        setupData();
    }

    // Check  internet connection
    public boolean isConnectedToInternet()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
    }


    // menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_babysitter, menu);
        menuSave = menu;
        if(getIntent().getStringExtra("Screen").equals("BlockedAccounts"))
        {
            removeFrom_blockList(BabysitterUserName);
            onOptionsItemSelected(menu.findItem(R.id.block));
        }
        return  true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.report)
        {
            Intent Report_and_Help = new Intent(BabysitterProfile.this, Report_and_Help.class);
            Report_and_Help.putExtra("UserName",getIntent().getStringExtra("UserName"));
            Report_and_Help.putExtra("Item","Report");
            Report_and_Help.putExtra("BabysitterUserName",BabysitterUserName);

            startActivity(Report_and_Help);
            return true;
       }
        else if(id == R.id.block)
        {
            // block
            if(item.getTitle().toString().equals("Block"))
            {
                item.setIcon(getResources().getDrawable(R.drawable.ic_unblock));
                item.setTitle("Unblock");
                btnFavorite.setTextColor(Color.WHITE);
                btnFavorite.setBackgroundColor(0x0099ff);
                removeFrom_Favorites(BabysitterUserName);
                btnFavorite.setText("Unblock");
                addTo_blockList(BabysitterUserName);

            }
            else // unblock
            {
                item.setIcon(getResources().getDrawable(R.drawable.ic_block));
                item.setTitle("Block");
                btnFavorite.setText("Favorite");
                removeFrom_blockList(BabysitterUserName);
            }

            return true;
        }
        else if(id == R.id.help)
        {
            Intent Report_and_Help = new Intent(BabysitterProfile.this, Report_and_Help.class);
            Report_and_Help.putExtra("UserName",getIntent().getStringExtra("UserName"));
            Report_and_Help.putExtra("Item","Help");

            startActivity(Report_and_Help);
            return true;
        }
        return true;
    }


    private void removeFrom_blockList(String babysitterUserName)
    {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(UserName).child("Babysitters").child("blocked");
        reference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot dataSnap : dataSnapshot.getChildren())
                {

                    if(String.valueOf(dataSnap.getValue()).equals(babysitterUserName))
                    {
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(UserName)
                                .child("Babysitters")
                                .child("blocked")
                                .child(dataSnap.getKey())
                                .removeValue();
                        //Toast.makeText(Babysitter.this, "remove", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void addTo_blockList(String BabysitterUserName)
    {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(UserName).child("Babysitters").child("blocked");
        reference.push().setValue(BabysitterUserName);
    }


    // Get details
    private void getBabysitter_details(String babysitterUserName)
    {
        reference = FirebaseDatabase.getInstance().getReference("Babysitter");
        reference.child(babysitterUserName).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>()
        {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot)
            {
                tvBabysitter_specialty.setText("Specialty: " + String.valueOf(dataSnapshot.child("Profile").child("specialty").getValue()));
                tvBabysitterWage.setText("Wage: " + String.valueOf(dataSnapshot.child("Profile").child("wage").getValue())+"$");
                BabysitterPhone=String.valueOf(dataSnapshot.child("phone").getValue());
                tvBabysitterName.setText(dataSnapshot.child("name").getValue().toString());


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


                tvBabysitterAge.setText("Age: "+String.valueOf(age));
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(BabysitterProfile.this, "Failed to read", Toast.LENGTH_SHORT).show();
            }
        });

        getProfilePhoto();
        Update_Rating();
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

    // babysitter profile photo
    public void getProfilePhoto()
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
                    Glide.with(BabysitterProfile.this)
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

    // updating rating bar
    private void Update_Rating()
    {
        reference = FirebaseDatabase.getInstance().getReference("Babysitter");
        reference.child(BabysitterUserName).child("Rating").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    float sum=0;
                    int count=0;

                    for (DataSnapshot dataSnap : snapshot.getChildren())
                    {
                        float Rate = Float.valueOf(String.valueOf(dataSnap.getValue()));
                        sum += Rate;
                        count++;
                    }
                    float Rating = sum/count;
                    rb_CurrentRate.setRating((float) Rating);
                    tvNumRatings.setText("("+count+")");
                }
                else
                    rb_CurrentRate.setRating(0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }});
    }

    // check if the babysitter is in the favorite's list
    private void check_Favorites(String babysitterUserName)
    {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(UserName).child("Babysitters").child("favorites");
        reference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot dataSnap : dataSnapshot.getChildren())
                {

                    if(String.valueOf(dataSnap.getValue()).equals(babysitterUserName))
                    {
                        btnFavorite.setTextColor(Color.BLACK);
                        btnFavorite.setBackgroundColor(Color.WHITE);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }});
    }


    // Set comments + number of comments
    private void setupData()
    {
        reference = FirebaseDatabase.getInstance().getReference("Babysitter").child(BabysitterUserName).child("Comments");
        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                CommentsArrList.clear();
                int count=0;

                for (DataSnapshot zoneSnapshot: snapshot.getChildren())
                {
                    Comment comment1 = new Comment(zoneSnapshot.child("userName").getValue().toString(),
                            zoneSnapshot.child("comment").getValue().toString(),
                            zoneSnapshot.child("timeStamp").getValue());

                    CommentsArrList.add(comment1);
                    count++;
                }
                tvNumber_OfComments.setText("Comments ("+count+")");
                //Collections.reverse(CommentsArrList);
                BabysitterCommentsAdapter adapter = new BabysitterCommentsAdapter(BabysitterProfile.this, 0, CommentsArrList);
                lvComments.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Log.w(TAG, "onCancelled", error.toException());
            }
        });
    }


    @Override
    public void onClick(View v)
    {
        if (v == btnMessage)
        {

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setData(Uri.parse("smsto:" + BabysitterPhone)); // This ensures only SMS apps respond
            intent.putExtra("sms_body", "");
            if (intent.resolveActivity(getPackageManager()) != null)
            {
                startActivity(intent);
            }
        }

        if (v == btnWhatsApp)
        {
            Uri uri = Uri.parse("smsto:" + BabysitterPhone);
            Intent i = new Intent(Intent.ACTION_SENDTO, uri);
            i.setPackage("com.whatsapp");
            startActivity(Intent.createChooser(i, ""));
        }


        if (v == btnFavorite)
        {
            if (btnFavorite.getTextColors().toString().endsWith("-1}") && btnFavorite.getText().toString().equals("Unblock"))
            {
                btnFavorite.setTextColor(Color.WHITE);
                btnFavorite.setBackgroundColor(0x0099ff);
                btnFavorite.setText("Favorite");

                onOptionsItemSelected(menuSave.findItem(R.id.block));
                removeFrom_blockList(BabysitterUserName);
            }
            else if (btnFavorite.getTextColors().toString().endsWith("-1}"))
            {
                btnFavorite.setTextColor(Color.BLACK);
                btnFavorite.setBackgroundColor(Color.WHITE);
                addTo_Favorites(BabysitterUserName);
            }
            else if (btnFavorite.getTextColors().toString().endsWith("6}"))
            {
                btnFavorite.setTextColor(Color.WHITE);
                btnFavorite.setBackgroundColor(0x0099ff);
                removeFrom_Favorites(BabysitterUserName);
            }
        }
        if(v == btnAddComment)
            addComment();
    }


    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if(v== rb_CurrentRate)
        {
            //Checking if the user already rate this babysitter
            reference = FirebaseDatabase.getInstance().getReference("Babysitter");
            reference.child(BabysitterUserName).child("Rating").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>()
            {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot)
                {
                    if (dataSnapshot.hasChild(UserName)) {
                        AlertDialog.Builder adb = new AlertDialog.Builder(BabysitterProfile.this);

                        adb.setTitle("You already rated this babysitter");
                        adb.setMessage("You already rated this babysitter, you can cheng your rating");
                        adb.setIcon(R.drawable.ic_error);
                        adb.setPositiveButton("change rating", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) 
                            {
                                Rate();

                            }
                        });
                        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        adb.create().show();
                    }
                    else
                    {
                        Rate();

                    }
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Rate();
                }
            });
        }
        return false;
    }
    
    private void  Rate()
    {
        Dialog dialog = new Dialog(BabysitterProfile.this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.rating);
        dialog.show();

        TextView tvRating = dialog.findViewById(R.id.tvRating);
        TextView tv_BabysitterUserName = dialog.findViewById(R.id.tv_BabysitterUserName);
        tv_BabysitterUserName.setText("Rat "+BabysitterUserName);

        Button btnRate = dialog.findViewById(R.id.btnRate);
        btnRate.setOnClickListener(this);

        RatingBar rb_RatBabysitter = dialog.findViewById(R.id.rb_RatBabysitter);
        rb_RatBabysitter.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener()
        {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser)
            {
                tvRating.setText(String.format("(%s)",rating));
            }
        });

        btnRate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(BabysitterProfile.this, "Rate: ("+rb_RatBabysitter.getRating()+")", Toast.LENGTH_SHORT).show();
                reference.child(BabysitterUserName).child("Rating").child(UserName).setValue(Float.valueOf(rb_RatBabysitter.getRating()));
                Update_Rating();
                dialog.dismiss();
            }
        });

    }


    //add comment to firebase
    private void addComment()
    {
        if(etComment.getText().toString().isEmpty())
            Toast.makeText(BabysitterProfile.this,"You can't make an empty comment",Toast.LENGTH_LONG).show();
        else
        {
            Comment newComment = new Comment(UserName,etComment.getText().toString());
            reference = FirebaseDatabase.getInstance().getReference("Babysitter").child(BabysitterUserName).child("Comments");
            reference.push().setValue(newComment);
            etComment.setText("");
        }
    }



    private void removeFrom_Favorites(String babysitterUserName)
    {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(UserName).child("Babysitters").child("favorites");
        reference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot dataSnap : dataSnapshot.getChildren())
                {

                    if(String.valueOf(dataSnap.getValue()).equals(babysitterUserName))
                    {
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(UserName)
                                .child("Babysitters")
                                .child("favorites")
                                .child(dataSnap.getKey())
                                .removeValue();
                        //Toast.makeText(Babysitter.this, "remove", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void addTo_Favorites(String BabysitterUserName)
    {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(UserName).child("Babysitters").child("favorites");
        reference.push().setValue(BabysitterUserName);
    }


    @Override
    public void onBackPressed()
    {
        Intent BP = new Intent(BabysitterProfile.this, HomeScreen.class);
        BP.putExtra("UserName", getIntent().getStringExtra("UserName"));
        startActivity(BP);

    }
}