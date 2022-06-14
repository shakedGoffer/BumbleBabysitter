package com.example.bumble_babysitter1 ;


public class UserProfile extends User
{

    protected String Children_num,ChildrenAges;
    protected int wage;
    protected User user;
    protected CompleteLocation completeLocation;

    public UserProfile(){}

    public UserProfile(String children_num, String childrenAges, int wage,CompleteLocation completeLocation)
    {

        this.Children_num = children_num;
        this.ChildrenAges = childrenAges;
        this.wage = wage;
        this.completeLocation= completeLocation;
    }

    public UserProfile(String children_num, String childrenAges, int wage, String Address, User user)
    {

        this.Children_num = children_num;
        this.ChildrenAges = childrenAges;
        this.wage = wage;
        this.completeLocation= completeLocation;
        this.user= user;
    }

    public String getChildren_num() { return this.Children_num; }
    public void setChildren_num(String children_num) { this.Children_num = children_num; }

    public String getChildrenAges() { return this.ChildrenAges; }
    public void setChildrenAges(String childrenAges) { this.ChildrenAges = childrenAges; }

    public int getWage() { return this.wage; }
    public void setWage(int wage) { this.wage = wage; }

    public CompleteLocation getCompleteLocation() { return this.completeLocation; }
    public void setCompleteLocation(CompleteLocation completeLocation) { this.completeLocation = completeLocation; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
