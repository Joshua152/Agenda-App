package com.example.agendaapp.Utils;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import androidx.core.content.ContextCompat;

import com.example.agendaapp.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class Utility {

    public final static String HOME_FRAGMENT = "Home Fragment";
    public final static String CREATE_FRAGMENT = "Create Fragment";
    public final static String EDIT_FRAGMENT = "Edit Fragment";

    public final static String SERIALIZATION_ASSIGNMENT_FILE = "assignments.txt";

    public final static String SAVE_BUNDLE_TITLE_KEY = "Save Bundle Title Key";
    public final static String SAVE_BUNDLE_DUE_DATE_KEY = "Save Bundle Due Date Key";
    public final static String SAVE_BUNDLE_SUBJECT_KEY = "Save Bundle Subject Key";
    public final static String SAVE_BUNDLE_DESCRIPTION_KEY = "Save Bundle Description Key";
    public final static String SAVE_BUNDLE_DAY_KEY = "Save Bundle Day Key";
    public final static String SAVE_BUNDLE_MONTH_KEY = "Save Bundle Month Key";
    public final static String SAVE_BUNDLE_YEAR_KEY = "Save Bundle Year Key";
    public final static String SAVE_BUNDLE_CREATE_NEW_KEY = "Save Bundle Create New Key";
    public final static String SAVE_BUNDLE_POSITION_KEY = "Save Bundle Position Key";
    public final static String SAVE_RESULT_KEY = "Save Result Key";

    public final static String EDIT_TITLE_KEY = "Edit Bundle Key";
    public final static String EDIT_DUE_DATE_KEY = "Edit Due Date Key";
    public final static String EDIT_DESCRIPTION_KEY = "Edit Description Key";
    public final static String EDIT_SUBJECT_KEY = "Edit Subject Key";
    public final static String EDIT_ORIGINAL_POSITION_KEY = "Edit Original Position Key";

    public final static int SERIALIZATION_P_TITLES = 0;
    public final static int SERIALIZATION_P_DUE_DATE = 1;
    public final static int SERIALIZATION_P_SUBJECT = 2;
    public final static int SERIALIZATION_P_DESCRIPTION = 3;
    public final static int SERIALIZATION_P_DATE_INFO = 4;
    public final static int SERIALIZATION_U_TITLES = 5;
    public final static int SERIALIZATION_U_DUE_DATE = 6;
    public final static int SERIALIZATION_U_SUBJECT = 7;
    public final static int SERIALIZATION_U_DESCRIPTION = 8;
    public final static int SERIALIZATION_U_DATE_INFO = 9;

    public final static int POSITION_ART = 0;
    public final static int POSITION_HISTORY = 1;
    public final static int POSITION_LANGUAGE = 2;
    public final static int POSITION_LITERATURE = 3;
    public final static int POSITION_MATH = 4;
    public final static int POSITION_MUSIC = 5;
    public final static int POSITION_SCIENCE = 6;
    public final static int POSITION_OTHER = 7;

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager manager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        try {
            manager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        } catch(Exception e){}
    }

    public static DateInfo getDay (Context context, int inDaysTime) {
        LocalDate localDate = LocalDate.now();
        int day = localDate.getDayOfMonth() + inDaysTime;
        int month = localDate.getMonthValue();
        int year = localDate.getYear();

        return getLocalDateFormat(context, day, month, year);
    }

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

    public static int getSubjectPosition(int drawableId) {
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

    public static int getColor(Context context, int drawable) {
        int position = getSubjectPosition(drawable);

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

    public static int[] toIntArray(List<Integer> list) {
        int[] array = new int[list.size()];

        for(int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }

        return array;
    }
}