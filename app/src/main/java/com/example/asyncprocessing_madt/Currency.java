package com.example.asyncprocessing_madt;

public class Currency {
    private String code;
    private double rate;
    private String name;


    public Currency(String code, double rate) {
        this.code = code;
        this.rate = rate;
    }


    public Currency(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public double getRate() {
        return rate;
    }

    public String getName() {
        return name;
    }
}
