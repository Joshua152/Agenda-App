package com.example.agendaapp.Utils;

import java.io.Serializable;

public class DateInfo implements Serializable {

    protected String date;
    protected int day;
    protected int month;
    protected int year;

    public DateInfo() {

    }

    public DateInfo(String date, int day, int month, int year) {
        this.date = date;
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public String getDate() {
        return date;
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }
}
