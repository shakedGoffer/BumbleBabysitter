package com.example.bumble_babysitter1;

import com.google.firebase.database.ServerValue;

public class Comment
{
    private String userName, comment;
    private Object timeStamp ;

    public Comment(String userName, String comment)
    {
        this.userName = userName;
        this.comment = comment;
        this.timeStamp = ServerValue.TIMESTAMP;
    }

    public Comment(String userName, String comment, Object timeStamp)
    {
        this.userName = userName;
        this.comment = comment;
        this.timeStamp = timeStamp;
    }

    public String getUserName() { return this.userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getComment() { return this.comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Object getTimeStamp() { return this.timeStamp; }
    public void setTimeStamp(Object timeStamp) { this.timeStamp = timeStamp; }
}
