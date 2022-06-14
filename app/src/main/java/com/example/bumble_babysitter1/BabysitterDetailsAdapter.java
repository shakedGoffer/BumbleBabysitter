package com.example.bumble_babysitter1;

import android.net.Uri;
import android.widget.ArrayAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class BabysitterDetailsAdapter extends ArrayAdapter<Babysitter>
{
    StorageReference storageRef;
    FirebaseStorage storage;
    Uri uri1;
    Context context;
    String Screen;

    public BabysitterDetailsAdapter(Context context, int resource, List<Babysitter> BabysitterList,String Screen)
    {
        super(context,resource,BabysitterList);
        this.Screen = Screen;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Babysitter babysitter = getItem(position);

        if(convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.babysitter_details_cell, parent, false);
        }


        TextView tvBabysitterUserName = (TextView) convertView.findViewById(R.id.tvBabysitterUserName);
        TextView tvBabysitterWage = (TextView) convertView.findViewById(R.id.tvBabysitterWage);
        TextView tvNumberOfRatings = (TextView) convertView.findViewById(R.id.tvNumberOfRatings);
        TextView tvBabysitterName = (TextView) convertView.findViewById(R.id.tvBabysitterName);
        TextView tvBabysitterAge = (TextView) convertView.findViewById(R.id.tvBabysitterAge);
        RatingBar rb_Rating = (RatingBar) convertView.findViewById(R.id.rb_Rating);
        CircleImageView ivProfile_photo = (CircleImageView) convertView.findViewById(R.id.ivProfile_photo);

        tvBabysitterUserName.setText(babysitter.getUserName());
        tvBabysitterWage.setText("Wage: "+babysitter.getWage()+"$");
        tvNumberOfRatings.setText("("+babysitter.getTvNumberOfRatings()+")");
        tvBabysitterAge.setText("Age: "+babysitter.getAge());
        tvBabysitterName.setText(babysitter.getName());
        rb_Rating.setRating(babysitter.getRating());

        if(this.Screen == "HomeScreen" || this.Screen.equals("HomeScreen"))
        {
            ivProfile_photo.setLayoutParams(new LinearLayout.LayoutParams(210,210));
            LinearLayout rateLayout = (LinearLayout) convertView.findViewById(R.id.rateLayout);
            LinearLayout Layout = (LinearLayout) convertView.findViewById(R.id.Layout);
            //Layout.removeView(rateLayout);
        }
        else if(this.Screen == "BlockedAccounts" || this.Screen.equals("BlockedAccounts"))
        {
            ivProfile_photo.setLayoutParams(new LinearLayout.LayoutParams(210,210));
            LinearLayout rateLayout = (LinearLayout) convertView.findViewById(R.id.rateLayout);
            LinearLayout Layout = (LinearLayout) convertView.findViewById(R.id.Layout);
            Layout.removeView(rateLayout);
            tvBabysitterWage.setVisibility(View.INVISIBLE);
            tvBabysitterAge.setVisibility(View.INVISIBLE);

        }

        context = convertView.getContext();
        get_babysitterProfilePhoto(ivProfile_photo,tvBabysitterUserName.getText().toString());
        return convertView;
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
                    Glide.with(context)
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

}
