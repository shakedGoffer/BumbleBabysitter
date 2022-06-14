package com.example.bumble_babysitter1;

import de.hdodenhof.circleimageview.CircleImageView;

public class Babysitter
{
    private String UserName, Name ,Specialty;
    private int Wage, tvNumberOfRatings, Age;
    private float Rating;
    private CircleImageView Profile_photo;


    public Babysitter(String userName,String Name, int wage,int Age, float rating, int numberOfRats, CircleImageView profile_photo)
    {
        this.UserName = userName;
        this.Name = Name;
        this.Wage = wage;
        this.Rating = rating;
        this.tvNumberOfRatings = numberOfRats;
        this.Profile_photo = profile_photo;
        this.Age = Age;
        //this.Specialty = "null";
    }

    public Babysitter(String UserName, int wage, CircleImageView profile_photo)
    {
        this.UserName = UserName;
        this.Wage = wage;
        this.Profile_photo = profile_photo;

    }

    public Babysitter(String UserName, int wage)
    {
        this.UserName = UserName;
        this.Wage = wage;
    }

    public int getWage() { return this.Wage; }
    public void setWage(int wage) { this.Wage = wage; }


    public CircleImageView getProfile_photo() { return this.Profile_photo; }
    public void setProfile_photo(CircleImageView profile_photo) { this.Profile_photo = profile_photo; }


    public String getUserName() { return this.UserName; }
    public void setUserName(String UserName) { this.UserName = UserName; }


    public float getRating() { return this.Rating; }
    public void setRating(float rating) { this.Rating = rating; }


    public int getTvNumberOfRatings() { return this.tvNumberOfRatings; }
    public void setTvNumberOfRatings(int tvNumberOfRatings) { this.tvNumberOfRatings = tvNumberOfRatings; }

    public String getName() { return this.Name; }
    public void setName(String Name) { this.Name = Name; }

    public String getSpecialty() { return this.Specialty; }
    public void setSpecialty(String specialty) { this.Specialty = specialty; }

    public int getAge() { return this.Age; }
    public void setAge(int age) { this.Age = age; }
}
