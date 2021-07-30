/**
 * This class represents a date. The DateInfo
 * class breaks down the date for easier access.
 * The date is broken into its day, month, and
 * year components. The full date is also
 * represented in a String.
 *
 * @author Joshua Au
 * @version 1.0
 * @since 11/26/2020
 */

package com.example.agendaapp.Data;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class DateInfo implements Parcelable, Serializable {

    private static final long serialVersionUID = 1116145860712571266L;

    // Constants for comparing Dates
    public static final int FURTHER = 0;
    public static final int SAME = 1;
    public static final int CLOSER = 2;

    // Day of the month of the date
    protected int day;
    // Month of the date
    protected int month;
    // Year of the date
    protected int year;
    // Date in the from of a String
    protected String date;

    /**
     * No args constructor
     */
    public DateInfo() {
        date = "";
        day = 0;
        month = 0;
        year = 0;
    }

    /**
     * 4 arg constructor to initialize all fields
     * @param date Date in String form
     * @param day Day of the month of the date
     * @param month date month
     * @param year date year
     */
    public DateInfo(String date, int day, int month, int year) {
        this.date = date;
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public static final Parcelable.Creator<DateInfo> CREATOR =
            new Parcelable.Creator<DateInfo>() {

        /**
         * Creates a new DateInfo from the information contained in the
         * given Parcel
         * @param in Parcel from which to create a new DateInfo
         * @return Return s anew DateInfo rom the given Parcel
         */
        @Override
        public DateInfo createFromParcel(Parcel in) {
            return new DateInfo(in);
        }

        /**
         * Create a new DateInfo array of the given size
         * @param size The size of the DateInfo array
         * @return Returns an empty DateInfo array of the given size
         */
        @Override
        public DateInfo[] newArray(int size) {
            return new DateInfo[size];
        }
    };

    /**
     * Private constructor to be used by Parcelable Creator
     * @param in Parcel to be read in
     */
    private DateInfo(Parcel in) {
        date = in.readString();
        day = in.readInt();
        month = in.readInt();
        year = in.readInt();
    }

    /**
     * Writes data out to the Parcel
     * @param out Parcel to be written to
     * @param flags Flags (describe data)
     */
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(date);
        out.writeInt(day);
        out.writeInt(month);
        out.writeInt(year);
    }

    /**
     * Describes the class (ex. subclass vs superclass)
     * @return Returns 0
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Gets the date 
     * @return Returns a String containing the date
     */
    public String getDate() {
        return date;
    }

    /**
     * Gets the day of the month of the date
     * @return Returns an int containing the day
     */
    public int getDay() {
        return day;
    }

    /**
     * Gets the month of the date
     * @return Returns an int containing the month
     */
    public int getMonth() {
        return month;
    }

    /**
     * Gets the year of the date
     * @return Returns an int containing the year
     */
    public int getYear() {
        return year;
    }
}
