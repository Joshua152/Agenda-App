package com.example.agendaapp.Utils;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utility {

    public final static String HOME_FRAGMENT = "Home Fragment";
    public final static String CREATE_FRAGMENT = "Create Fragment";

    public final static String SAVE_BUNDLE_TITLE_KEY = "Save Bundle Title Key";
    public final static String SAVE_BUNDLE_DUE_DATE_KEY = "Save Bundle Due Date Key";
    public final static String SAVE_BUNDLE_SUBJECT_KEY = "Save Bundle Subject Key";
    public final static String SAVE_BUNDLE_DESCRIPTION_KEY = "Save Bundle Description Key";
    public final static String SAVE_RESULT_KEY = "Save Result Key";

    public final static String SERIALIZATION_ASSIGNMENT_FILE = "assignments.txt";

    public final static int SERIALIZATION_P_TITLES = 0;
    public final static int SERIALIZATION_P_DUE_DATE = 1;
    public final static int SERIALIZATION_P_SUBJECT = 2;
    public final static int SERIALIZATION_P_DESCRIPTION = 3;
    public final static int SERIALIZATION_U_TITLES = 4;
    public final static int SERIALIZATION_U_DUE_DATE = 5;
    public final static int SERIALIZATION_U_SUBJECT = 6;
    public final static int SERIALIZATION_U_DESCRIPTION = 7;

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager manager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        try {
            manager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        } catch(Exception e){}
    }

    public static String getTomorrow(Context context) {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH) + 1;
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        return getLocalDateFormat(context, day, month, year);
    }

    public static String getLocalDateFormat(Context context, int day, int month, int year) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date parsedDate = null;

        try {
            parsedDate = simpleDateFormat.parse(day + "/" + month + "/" + year);
        } catch (ParseException e) {}

        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
        String date = dateFormat.format(parsedDate);

        return date;
    }
}