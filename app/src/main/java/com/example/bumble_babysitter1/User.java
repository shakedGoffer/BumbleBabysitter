package com.example.bumble_babysitter1;

public class User
{
    protected String Name,BirthDate,EmailAddress,Phone,UserName,Password;

    public User(){}

    public User(String name, String birthDate, String emailAddress, String phone, String userName, String password)
    {
        this.Name = name;
        this.BirthDate = birthDate;
        this.EmailAddress = emailAddress;
        this.Phone = phone;
        this.UserName = userName;
        this.Password = password;
    }

    public String getName() { return this.Name; }
    public void setName(String name) { this.Name = name; }

    public String getBirthDate() { return this.BirthDate; }
    public void setBirthDate(String birthDate) { this.BirthDate = birthDate; }

    public String getEmailAddress() { return this.EmailAddress; }
    public void setEmailAddress(String emailAddress) { this.EmailAddress = emailAddress; }

    public String getPhone() { return this.Phone; }
    public void setPhone(String phone) { this.Phone = phone; }

    public String getUserName() { return this.UserName; }
    public void setUserName(String userName) { this.UserName = userName; }

    public String getPassword() { return this.Password; }
    public void setPassword(String password) { this.Password = password; }
}
