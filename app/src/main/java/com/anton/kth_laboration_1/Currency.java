package com.anton.kth_laboration_1;

/**
 * Created by Anton on 2016-11-14.
 */

public class Currency {

    public Currency(String name, double rate){
        this.rate = rate;
        this.name = name;
    }

    public double getRate() {
        return rate;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString(){
        return name;
    }

    private String name;
    private double rate;
}
