package com.example.bumble_babysitter1;

import java.util.Comparator;

public class MyComparator_rating implements Comparator<Babysitter>
{
    @Override
    public int compare(Babysitter b1, Babysitter b2)
    {
        if (b1.getRating() > b2.getRating())
        {
            return 1;
        }
        else if (b1.getRating() < b2.getRating())
        {
            return -1;
        }
        return 0;
    }
}
