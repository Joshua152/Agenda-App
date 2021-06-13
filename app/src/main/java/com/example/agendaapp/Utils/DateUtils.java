/**
 * Date utility methods such as month number to String, get a date in a certain
 * number of days time, etc
 */

package com.example.agendaapp.Utils;

import android.content.Context;

import com.example.agendaapp.Data.DateInfo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public class DateUtils {

    /**
     * Gets the DateInfo for the current day + inDaysTime.
     *
     * @param context Context
     * @param inDaysTime Days after the current day
     * @return Returns a DateInfo object which holds the requested date in the local date format
     */
    public static DateInfo getDay (Context context, int inDaysTime) {
        LocalDate localDate = LocalDate.now();
        int day = localDate.getDayOfMonth() + inDaysTime;
        int month = localDate.getMonthValue();
        int year = localDate.getYear();

        return getLocalDateFormat(context, day, month, year);
    }

    /**
     * Gets the date format of the user's area (ex. mm-dd-yyyy)
     *
     * @param context Context
     * @param day Day of the month
     * @param month Month (0 - 11)
     * @param year Year
     * @return Returns a DateInfo object localized for the user's area
     */
    public static DateInfo getLocalDateFormat(Context context, int day, int month, int year) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date parsedDate = null;

        try {
            parsedDate = simpleDateFormat.parse(day + "/" + month + "/" + year);
        } catch (ParseException e) {}

        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
        String date = dateFormat.format(parsedDate);

        DateInfo info = new DateInfo(date, day, month, year);

        return info;
    }

    /**
     * Compares the dates of di1 to di2 in terms of di1.
     *
     * @param di1 Main DateInfo
     * @param di2 Second DateInfo (will be compared against di1)
     * @return Returns 0 (further), 1 (same), or 2 (closer) depending on how di1 compares to di2.
     * 0 - di1 is further than di2
     * 1 - di1 is the same as di2
     * 2 - di1 is closer than di2
     */
    public static int compareDates(DateInfo di1, DateInfo di2) {
        if(di1.getYear() > di2.getYear()) {
            return DateInfo.FURTHER;
        } else if(di1.getYear() == di2.getYear()) {
            if(di1.getMonth() > di2.getMonth()) {
                return DateInfo.FURTHER;
            } else if(di1.getMonth() == di2.getMonth()) {
                if(di1.getDay() > di2.getDay())
                    return DateInfo.FURTHER;
                else if(di1.getDay() == di2.getDay())
                    return DateInfo.SAME;

                return DateInfo.CLOSER;
            } else {
                return DateInfo.CLOSER;
            }
        } else {
            return DateInfo.CLOSER;
        }
    }

    /**
     * Checks if a date is should be placed in priority (if current day or day after)
     *
     * @param dateInfo Date to be checked
     * @param context Context
     * @return Returns true or false whether or not the date given is in the priority range
     */
    public static boolean inPriorityRange(Context context, DateInfo dateInfo) {
        return compareDates(dateInfo, getDay(context, 2)) == DateInfo.CLOSER;
    }

    /**
     * Returns whether or an assignment should be marked as late (date has passed the current date)
     *
     * @param dateInfo Assignment DateInfo
     * @param context Context
     * @return Returns true or false depending on whether or not the due date of the assignment has
     * passed
     */
    public static boolean isLate(Context context, DateInfo dateInfo) {
        return compareDates(dateInfo, getDay(context, 0)) == DateInfo.CLOSER;
    }

    /**
     * Returns the int version of the month (1-12) given the String
     * @param month The month name
     * @return Returns an int from 1-12 representing the month number
     */
    public static int stringToInt(String month) {
        switch(month) {
            case "January" :
                return 1;
            case "February" :
                return 2;
            case "March" :
                return 3;
            case "April" :
                return 4;
            case "May" :
                return 5;
            case "June" :
                return 6;
            case "July" :
                return 7;
            case "August" :
                return 8;
            case "September" :
                return 9;
            case "October" :
                return 10;
            case "November" :
                return 11;
            case "December" :
                return 12;
            default:
                return -1;
        }
    }
}
