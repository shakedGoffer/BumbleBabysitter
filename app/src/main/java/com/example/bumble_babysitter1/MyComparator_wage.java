package com.example.bumble_babysitter1;

import java.util.Comparator;

public class MyComparator_wage implements Comparator<Babysitter>
{
    @Override
    public int compare(Babysitter b1, Babysitter b2)
    {
        if (b1.getWage() > b2.getWage())
        {
            return -1;
        }
        else if (b1.getWage() < b2.getWage())
        {
            return 1;
        }
        return 0;
    }
}
