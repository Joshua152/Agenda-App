package com.example.agendaapp.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.method.HideReturnsTransformationMethod;
import android.view.inputmethod.InputMethodManager;

import androidx.core.content.ContextCompat;

import com.example.agendaapp.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Utility {

    public final static String HOME_FRAGMENT = "Home Fragment";
    public final static String CREATE_FRAGMENT = "Create Fragment";
    public final static String EDIT_FRAGMENT = "Edit Fragment";
    public final static String VIEW_FRAGMENT = "View Fragment";

    public final static String SERIALIZATION_ASSIGNMENT_FILE = "assignments.txt";

    public final static String HOME_RESULT_KEY = "Home Result Key";
    public final static String EDIT_RESULT_KEY = "Edit Result Key";

    public final static String ASSIGNMENT_KEY = "Assignment Key";
    public final static String PRIORITY_KEY = "Priority Key";
    public final static String CREATE_NEW_KEY = "Create New";
    public final static String POSITION_KEY = "Position Key";

    public final static int SERIALIZATION_PRIORITY = 0;
    public final static int SERIALIZATION_UPCOMING = 1;

    public final static int POSITION_ART = 0; // TODO : hashmap instead?
    public final static int POSITION_HISTORY = 1;
    public final static int POSITION_LANGUAGE = 2;
    public final static int POSITION_LITERATURE = 3;
    public final static int POSITION_MATH = 4;
    public final static int POSITION_MUSIC = 5;
    public final static int POSITION_SCIENCE = 6;
    public final static int POSITION_OTHER = 7;

    public final static int FURTHER = 0;
    public final static int SAME = 1;
    public final static int CLOSER = 2;

    /**
     * Hide keyboard from phone
     *
     * @param activity Activity which holds the keyboard
     */
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager manager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        try {
            manager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        } catch(Exception e) {}
    }

    public static void serializeArrays(Context context, ArrayList<Assignment> priority, ArrayList<Assignment> upcoming) {
        ArrayList[] serialize = new ArrayList[2];

        serialize[Utility.SERIALIZATION_PRIORITY] = priority;
        serialize[Utility.SERIALIZATION_UPCOMING] = upcoming;

        Serialize.serialize(serialize, context.getFilesDir() + "/" + Utility.SERIALIZATION_ASSIGNMENT_FILE);
    }

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
            return FURTHER;
        } else if(di1.getYear() == di2.getYear()) {
            if(di1.getMonth() > di2.getMonth()) {
                return FURTHER;
            } else if(di1.getMonth() == di2.getMonth()) {
                if(di1.getDay() > di2.getDay()) {
                    return FURTHER;
                } else if(di1.getDay() == di2.getDay()) {
                    return SAME;
                }

                return CLOSER;
            } else {
                return CLOSER;
            }
        } else {
            return CLOSER;
        }
    }

    /**
     * Checks if a date is should be placed in priority (if current day or day after)
     *
     * @param dateInfo Date to be checked
     * @param context Context
     * @return Returns true or false whether or not the date given is in the priority range
     */
    public static boolean inPriorityRange(DateInfo dateInfo, Context context) {
        return compareDates(dateInfo, getDay(context, 2)) == CLOSER;
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
        return compareDates(dateInfo, getDay(context, 0)) == CLOSER;
    }

    public static int getSubjectDrawable(Context context, String subject) {
        String[] array = context.getResources().getStringArray(R.array.subject_array);

        if(subject.equals(array[POSITION_ART]))
            return R.drawable.ic_brush_black_24dp;
        else if(subject.equals(array[POSITION_HISTORY]))
            return R.drawable.ic_history_edu_black_24dp;
        else if(subject.equals(array[POSITION_LANGUAGE]))
            return R.drawable.ic_language_black_24dp;
        else if(subject.equals(array[POSITION_LITERATURE]))
            return R.drawable.ic_book_black_24dp;
        else if(subject.equals(array[POSITION_MATH]))
            return R.drawable.ic_calculate_black_24dp;
        else if(subject.equals(array[POSITION_SCIENCE]))
            return R.drawable.ic_science_black_24dp;
        else
            return R.drawable.ic_miscellaneous_services_black_24dp;
    }

    public static int getSubjectColor(Context context, String subject) {
        String[] array = context.getResources().getStringArray(R.array.subject_array);

        if(subject.equals(array[POSITION_ART]))
            return ContextCompat.getColor(context, R.color.red);
        else if(subject.equals(array[POSITION_HISTORY]))
            return ContextCompat.getColor(context, R.color.orange);
        else if(subject.equals(array[POSITION_LANGUAGE]))
            return ContextCompat.getColor(context, R.color.yellow);
        else if(subject.equals(array[POSITION_LITERATURE]))
            return ContextCompat.getColor(context, R.color.green);
        else if(subject.equals(array[POSITION_MATH]))
            return ContextCompat.getColor(context, R.color.blue);
        else if(subject.equals(array[POSITION_SCIENCE]))
            return ContextCompat.getColor(context, R.color.magenta);
        else
            return ContextCompat.getColor(context, R.color.purple);
    }

    /**
     * Gets the spinner position of a subject based on the drawable ID
     *
     * @param drawableId Subject drawable ID
     * @return Returns an int representing the index of the subject in the spinner
     */
    public static int getSubjectPositionFromId(int drawableId) {
        switch(drawableId) {
            case R.drawable.ic_brush_black_24dp :
                return POSITION_ART;
            case R.drawable.ic_history_edu_black_24dp :
                return POSITION_HISTORY;
            case R.drawable.ic_language_black_24dp :
                return POSITION_LANGUAGE;
            case R.drawable.ic_book_black_24dp :
                return POSITION_LITERATURE;
            case R.drawable.ic_calculate_black_24dp :
                return POSITION_MATH;
            case R.drawable.ic_music_note_black_24dp :
                return POSITION_MUSIC;
            case R.drawable.ic_science_black_24dp :
                return POSITION_SCIENCE;
            case R.drawable.ic_miscellaneous_services_black_24dp :
                return POSITION_OTHER;
            default :
                return -1;
        }
    }

    /**
     * Gets the subject title from the subject's drawable ID
     *
     * @param id Subject's drawable ID
     * @param context Context
     * @return Returns a String containing the subject title (ex. Math, Other)
     */
    public static String getSubjectFromId(int id, Context context) {
        switch(id) {
            case R.drawable.ic_brush_black_24dp :
                return context.getResources().getStringArray(R.array.subject_array)[POSITION_ART];
            case R.drawable.ic_history_edu_black_24dp :
                return context.getResources().getStringArray(R.array.subject_array)[POSITION_HISTORY];
            case R.drawable.ic_language_black_24dp :
                return context.getResources().getStringArray(R.array.subject_array)[POSITION_LANGUAGE];
            case R.drawable.ic_book_black_24dp :
                return context.getResources().getStringArray(R.array.subject_array)[POSITION_LITERATURE];
            case R.drawable.ic_calculate_black_24dp :
                return context.getResources().getStringArray(R.array.subject_array)[POSITION_MATH];
            case R.drawable.ic_music_note_black_24dp :
                return context.getResources().getStringArray(R.array.subject_array)[POSITION_MUSIC];
            case R.drawable.ic_science_black_24dp :
                return context.getResources().getStringArray(R.array.subject_array)[POSITION_SCIENCE];
            case R.drawable.ic_miscellaneous_services_black_24dp :
                return context.getResources().getStringArray(R.array.subject_array)[POSITION_OTHER];
            default :
                return null;
        }
    }

    /**
     * Gets the subject's spinner index based on the subject title (ex. Math, Other)
     *
     * @param subject The subject title
     * @param context Context
     * @return Returns the subject's spinner index
     */
    public static int getSubjectPositionFromTitle(String subject, Context context) {
        String[] array = context.getResources().getStringArray(R.array.subject_array);

        for(int i = 0; i < array.length; i++) {
            if(array[i].equals(subject)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Gets the subject title from its spinner index
     *
     * @param position Spinner index of the subject
     * @param context Context
     * @return Returns a String holding the subject title (ex. Math, Other)
     */
    public static String getSubjectFromPosition(int position, Context context) {
        return context.getResources().getStringArray(R.array.subject_array)[position];
    }

    /**
     * Gets the respective color form the given drawable ID
     *
     * @param context Context
     * @param drawable Subject drawable ID
     * @return Returns the color to go along with the subject in the form 0xAARRGGBB
     */
    public static int getColor(Context context, int drawable) {
        int position = getSubjectPositionFromId(drawable);

        switch(position) {
            case POSITION_ART :
                return ContextCompat.getColor(context, R.color.red);
            case POSITION_HISTORY :
                return ContextCompat.getColor(context, R.color.orange);
            case POSITION_LANGUAGE :
                return ContextCompat.getColor(context, R.color.yellow);
            case POSITION_LITERATURE :
                return ContextCompat.getColor(context, R.color.green);
            case POSITION_MATH :
                return ContextCompat.getColor(context, R.color.turquoise);
            case POSITION_MUSIC :
                return ContextCompat.getColor(context, R.color.blue);
            case POSITION_SCIENCE :
                return ContextCompat.getColor(context, R.color.magenta);
            case POSITION_OTHER :
                return ContextCompat.getColor(context, R.color.purple);
        }

        return 0;
    }

    /**
     * Converts a List<Integer> to an int[]
     *
     * @param list The list to be converted
     * @return Returns the int[] converted fro the List<Integer>
     */
    public static int[] toIntArray(List<Integer> list) {
        int[] array = new int[list.size()];

        for(int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }

        return array;
    }

    /**
     * Converts dp to px
     *
     * @param context Context
     * @param dp dp to be converted
     * @return Returns the px equivalent of the dp value given
     */
    public static int toPixels(Context context, int dp) {
        float scale = context.getResources().getDisplayMetrics().density;

        return (int) (dp * scale + 0.5f);
    }
}