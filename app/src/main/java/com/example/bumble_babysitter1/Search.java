package com.example.bumble_babysitter1;

import static java.util.Comparator.comparing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
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
import java.util.Collections;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class Search extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    String UserName;
    CompleteLocation completeLocation, babysitter_CompleteLocation;
    Spinner spSort,spFilter;

    String[] SortBy = {"","Highest rating","Price: high to low","Price: low to high","User Name: A->Z",
            "User Name: Z->A","Age: big to small","Age: small to big"};

    String[] FilterBy = {"All","Toddlers","Special needs","None",};

    // Firebase
    DatabaseReference reference;
    StorageReference storageRef;
    FirebaseStorage storage;
    Uri uri1;

    // Babysitters list
    public static ArrayList<Babysitter> BabysitterList = new ArrayList<Babysitter>();
    public static ArrayList<String> BlockedBabysitters = new ArrayList<String>();
    private ListView listView;
    private SearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if(!isConnectedToInternet())
        {
            Toast.makeText(Search.this, "There is no internet connection", Toast.LENGTH_SHORT).show();
            finish();
            System.exit(0);
        }
        BlockedBabysitters.clear();
        BabysitterList.clear();

        spFilter = (Spinner)findViewById(R.id.spFilter);
        spFilter.getBackground().setColorFilter(Color.parseColor("#FF000000"), PorterDuff.Mode.SRC_ATOP);
        spFilter.setOnItemSelectedListener(this);

        ArrayAdapter Filter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, FilterBy);
        Filter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilter.setAdapter(Filter);

        spSort = (Spinner)findViewById(R.id.spSort);
        spSort.getBackground().setColorFilter(Color.parseColor("#FF000000"), PorterDuff.Mode.SRC_ATOP);
        spSort.setOnItemSelectedListener(this);
        ArrayAdapter Sort = new ArrayAdapter(this, android.R.layout.simple_spinner_item, SortBy);
        Sort.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSort.setAdapter(Sort);


        searchView = (SearchView) findViewById(R.id.Babysitter_ListSearchView);

        UserName = getIntent().getStringExtra("UserName");
        listView = (ListView) findViewById(R.id.BabysitterListView);


        getBlocked();
        setUpOnclickListener();


        Double latitude = (double) getIntent().getDoubleExtra("Latitude", 0);
        Double longitude = (double) getIntent().getDoubleExtra("Longitude", 0);
        String address = getIntent().getStringExtra("CompleteAddress");
        completeLocation = new CompleteLocation(address,latitude,longitude);

    }


    // Check  internet connection
    public boolean isConnectedToInternet()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
    }


    // Search for babysitters
    private void initSearchWidgets()
    {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                ArrayList<Babysitter> filteredBabysitters = new ArrayList<Babysitter>();
                for(Babysitter b: BabysitterList)
                {
                    if(b.getUserName().toLowerCase().contains(newText.toLowerCase()))
                    {
                        filteredBabysitters.add(b);
                    }
                }
                setUpList(filteredBabysitters);
                return false;
            }
        });
    }

    public void getBlocked()
    {
        BlockedBabysitters.clear();
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }});
    }

    private void getData(String filter)
    {
        ArrayList<String> filteredBabysitters = new ArrayList<String>();
        reference = FirebaseDatabase.getInstance().getReference("Babysitter");
        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    if(filter.equals("all"))
                        filteredBabysitters.add(dataSnapshot.child("userName").getValue().toString());
                    else if(dataSnapshot.child("Profile").child("specialty").getValue().toString().equals(filter))
                        filteredBabysitters.add(dataSnapshot.child("userName").getValue().toString());
                }
                if(!filteredBabysitters.isEmpty())
                {
                    setUpData(filteredBabysitters);
                    spSort.setSelection(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }});
    }

    // Set babysitter list
    private void setUpData(ArrayList<String> list)
    {
        BabysitterList.clear();
        for (String BabysitterUserName : list)
        {

                reference = FirebaseDatabase.getInstance().getReference("Babysitter");
                reference.child(BabysitterUserName).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        CircleImageView ivProfile_photo = new CircleImageView(Search.this);
                        ivProfile_photo.setImageURI(null);
                        ivProfile_photo.setImageBitmap(null);
                        ivProfile_photo.setBackgroundResource(R.drawable.profile);
                        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(80, 80);
                        buttonLayoutParams.setMargins(20, 20, 20, 0);
                        ivProfile_photo.setLayoutParams(buttonLayoutParams);
                        get_babysitterProfilePhoto(ivProfile_photo, dataSnapshot.child("userName").getValue(String.class));

                        Double b_latitude = (Double) dataSnapshot.child("Profile").child("completeLocation").child("latitude").getValue();
                        Double b_longitude = (Double) dataSnapshot.child("Profile").child("completeLocation").child("longitude").getValue();
                        String b_address = getIntent().getStringExtra("CompleteAddress");
                        babysitter_CompleteLocation = new CompleteLocation(b_address, b_latitude, b_longitude);

                        double Distance = distance(completeLocation.getLatitude(), completeLocation.getLongitude(),
                                babysitter_CompleteLocation.getLatitude(), babysitter_CompleteLocation.getLongitude());


                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
                        Date date = null;
                        try {
                            date = sdf.parse(dataSnapshot.child("birthDate").getValue().toString());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        Calendar myCalendar = Calendar.getInstance();
                        myCalendar.setTime(date);
                        int age = getAge(myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));


                        Babysitter babysitter = new Babysitter(dataSnapshot.child("userName").getValue(String.class),
                                dataSnapshot.child("name").getValue(String.class),
                                Integer.valueOf(dataSnapshot.child("Profile").child("wage").getValue().toString()), age,
                                0, 0, ivProfile_photo);


                        Update_Rating(dataSnapshot.child("userName").getValue(String.class), babysitter);


                        if (Distance < 5 && !Babysitter_isBlocked(babysitter.getUserName()))
                            BabysitterList.add(babysitter);
                    }
                });
        }
        new Handler().postDelayed(new Runnable()
        {@Override public void run() { setUpList(BabysitterList); }}, 1000); }

    private double distance(double lat1, double lon1, double lat2, double lon2)
    {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
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

    private boolean Babysitter_isBlocked(String BabysitterUserName)
    {
      for(String s : BlockedBabysitters)
      {
          if( BabysitterUserName == s || BabysitterUserName.equals(s))
              return true;
      }
      return false;
    }

    private void setUpList(ArrayList<Babysitter> list)
    {
        BabysitterDetailsAdapter adapter = new BabysitterDetailsAdapter(Search.this, 0, list ,"Search");
        listView.setAdapter(adapter);
        initSearchWidgets();
    }

    private void setUpOnclickListener()
    {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
            {
                Babysitter selectBabysitter = (Babysitter) (listView.getItemAtPosition(position));

                Intent Babysitter = new Intent(Search.this, BabysitterProfile.class);
                Babysitter.putExtra("BabysitterName",selectBabysitter.getUserName());
                Babysitter.putExtra("UserName",getIntent().getStringExtra("UserName"));
                Babysitter.putExtra("Screen","Search");
                Babysitter.putExtra("CompleteAddress", completeLocation.getAddress());
                Babysitter.putExtra("Latitude", completeLocation.getLatitude());
                Babysitter.putExtra("Longitude", completeLocation.getLongitude());


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
                    Glide.with(Search.this)
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        TextView textView = (TextView) view;
        ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);

        if(parent.getId() == R.id.spSort)
        {

            switch (SortBy[position])
            {
                case "Highest rating":
                    Collections.sort(BabysitterList, new MyComparator_rating());
                    Collections.reverse(BabysitterList);
                    break;
                case "Price: high to low":
                    Collections.sort(BabysitterList, new MyComparator_wage());
                    break;
                case "Price: low to high":
                    Collections.sort(BabysitterList, new MyComparator_wage());
                    Collections.reverse(BabysitterList);
                    break;
                case "User Name: A->Z":
                    Collections.sort(BabysitterList, new MyComparator_name());
                    Collections.reverse(BabysitterList);
                    break;
                case "User Name: Z->A":
                    Collections.sort(BabysitterList, new MyComparator_name());
                    break;
                case "Age: big to small":
                    Collections.sort(BabysitterList, new MyComparator_age());
                    Collections.reverse(BabysitterList);
                    break;
                case "Age: small to big":
                    Collections.sort(BabysitterList, new MyComparator_age());
                    break;
                default:
                    break;
            }
            setUpList(BabysitterList);
        }
        else if(parent.getId() == R.id.spFilter)
        {

            switch(FilterBy[position])
            {
                case "All":
                    getData("all");
                    break;
                case "Toddlers":
                    getData("toddlers");
                    break;
                case "Special needs":
                    getData("special needs");
                    break;
                case "None":
                    getData("none");
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }

    @Override
    public void onClick(View v)
    {

    }


    int i=0;
    @Override
    public void onBackPressed()
    {
        i++;
        if(i==2)
        {
            Intent BP = new Intent(Search.this, HomeScreen.class);
            BP.putExtra("UserName", getIntent().getStringExtra("UserName"));
            startActivity(BP);
            int i=0;
        }
    }
}