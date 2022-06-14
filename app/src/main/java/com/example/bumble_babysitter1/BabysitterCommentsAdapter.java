package com.example.bumble_babysitter1;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class BabysitterCommentsAdapter extends ArrayAdapter<Comment>
{
    public BabysitterCommentsAdapter(Context context, int resource, List<Comment> CommentsArrList)
    {
        super(context,resource,CommentsArrList);
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        Comment comment = getItem(position);

        if(convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.babysitter_comments_cell, parent, false);
        }

        TextView tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
        TextView tvTimeStamp = (TextView) convertView.findViewById(R.id.tvTimeStamp);
        TextView tvComment = (TextView) convertView.findViewById(R.id.tvComment);

        tvUserName.setText(comment.getUserName());
        tvComment.setText(comment.getComment());

        tvTimeStamp.setText(timestampToString((Long)comment.getTimeStamp()));

        return convertView;
    }


    private String timestampToString(long time)
    {

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        //String date = DateFormat.format("hh:mm",calendar).toString();
        String date = DateFormat.format("dd-MM-yyyy HH:mm:ss",calendar).toString();
        return date;


    }

}
