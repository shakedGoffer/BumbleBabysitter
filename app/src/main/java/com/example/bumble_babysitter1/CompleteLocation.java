package com.example.bumble_babysitter1;

public class CompleteLocation extends UserProfile
{
   private String address;
   private double latitude;
   private double longitude;

   public CompleteLocation(String address, double latitude, double longitude) 
   {
      this.address = address;
      this.latitude = latitude;
      this.longitude = longitude;
   }

   public String getAddress() { return this.address; }
   public void setAddress(String address) { this.address = address; }

   public double getLatitude() { return this.latitude; }
   public void setLatitude(double latitude) { this.latitude = latitude; }

   public double getLongitude() { return this.longitude; }
   public void setLongitude(double longitude) { this.longitude = longitude; }
}
