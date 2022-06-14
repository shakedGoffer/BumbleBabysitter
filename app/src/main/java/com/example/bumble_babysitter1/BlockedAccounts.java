package com.example.bumble_babysitter1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlockedAccounts extends AppCompatActivity
{
    TextView tvNoBlockedAccounts ;
    String UserName ;
    Uri uri1;

    public static ArrayList<Babysitter> BabysitterList = new ArrayList<Babysitter>();
    public static ArrayList<String> BlockedBabysitters = new ArrayList<String>();
    private ListView listView;


    //Firebase
    StorageReference storageRef;
    FirebaseStorage storage;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_accounts);

        if(!isConnectedToInternet())
        {
            Toast.makeText(BlockedAccounts.this, "There is no internet connection", Toast.LENGTH_SHORT).show();
            finish();
            System.exit(0);
        }

        tvNoBlockedAccounts = (TextView) findViewById(R.id.tvNoBlockedAccounts);
        UserName = getIntent().getStringExtra("UserName");

        BlockedBabysitters.clear();
        BabysitterList.clear();
        listView = (ListView) findViewById(R.id.BabysitterListView);

        //  Toolbar toolbar + arrow
        Toolbar toolbar =  findViewById(R.id.toolbar_babysitter);
        toolbar.setTitle("Blocked accounts");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent NavIc = new Intent(BlockedAccounts.this, HomeScreen.class);
                NavIc.putExtra("UserName", getIntent().getStringExtra("UserName"));
                startActivity(NavIc);
            }
        });


        getBlocked();
        setUpOnclickListener();

    }

    // Check  internet connection
    public boolean isConnectedToInternet()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
    }

    public void getBlocked()
    {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(UserName).child("Babysitters").child("blocked");
        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    BlockedBabysitters.add(dataSnapshot.getValue().toString());
                }
                if(BlockedBabysitters.isEmpty())
                {
                    listView.setVisibility(View.GONE);
                    tvNoBlockedAccounts.setVisibility(View.VISIBLE);
                }
                else
                    setUpData(BlockedBabysitters);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }});
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
                    CircleImageView ivProfile_photo = new CircleImageView(BlockedAccounts.this);
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
        BabysitterDetailsAdapter adapter = new BabysitterDetailsAdapter(BlockedAccounts.this, 0, list, "BlockedAccounts");
        listView.setAdapter(adapter);
    }


    private void setUpOnclickListener()
    {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
            {
                Babysitter selectBabysitter = (Babysitter) (listView.getItemAtPosition(position));

                Intent Babysitter = new Intent(BlockedAccounts.this, BabysitterProfile.class);
                Babysitter.putExtra("BabysitterName",selectBabysitter.getUserName());
                Babysitter.putExtra("UserName",UserName);
                Babysitter.putExtra("Screen","BlockedAccounts");

                // Babysitter.putExtra("CompleteAddress", completeLocation.getAddress());
                // Babysitter.putExtra("Latitude", completeLocation.getLatitude());
                // Babysitter.putExtra("Longitude", completeLocation.getLongitude());

                startActivity(Babysitter);
            }
        });
    }

    public void get_babysitterProfilePhoto( CircleImageView ivProfile_photo, String BabysitterUserName)
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
                    Glide.with(BlockedAccounts.this)
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

    @Override
    public void onBackPressed()
    {
        Intent BP = new Intent(BlockedAccounts.this,HomeScreen.class);
        BP.putExtra("UserName", getIntent().getStringExtra("UserName"));
        startActivity(BP);
    }
}