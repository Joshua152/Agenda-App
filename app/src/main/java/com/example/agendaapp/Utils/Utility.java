/**
 * Defines utility methods and the internal constants
 *
 * @author Joshua AU
 * @version 1.0
 * @since 6/24/2020
 */

package com.example.agendaapp.Utils;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.core.content.ContextCompat;

import com.example.agendaapp.Data.Assignment;
import com.example.agendaapp.Data.DateInfo;
import com.example.agendaapp.Data.Serialize;
import com.example.agendaapp.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

public class Utility {

    public final static String HOME_FRAGMENT = "Home Fragment";
    public final static String EDIT_FRAGMENT = "Edit Fragment";
    public final static String VIEW_FRAGMENT = "View Fragment";
    public final static String CLASSES_FRAGMENT = "Classes Fragment";
    public final static String IMPORT_FRAGMENT = "Import Fragment";
    public final static String PLATFORM_SELECT_FRAGMENT = "Platform Select Fragment";

    public final static String SERIALIZATION_ASSIGNMENT_FILE = "assignments.txt";

    public final static int SERIALIZATION_PRIORITY = 0;
    public final static int SERIALIZATION_UPCOMING = 1;

    // For FragmentResults
    public final static String HOME_RESULT_KEY = "Home Result Key";
    public final static String VIEW_RESULT_KEY = "View Result Key";

    public final static String SAVE_INFO = "Save Info";

    public final static String TRANSITION_BACKGROUND = "Transition Background";

    public final static int POSITION_ART = 0; // TODO : hashmap instead?
    public final static int POSITION_HISTORY = 1;
    public final static int POSITION_LANGUAGE = 2;
    public final static int POSITION_LITERATURE = 3;
    public final static int POSITION_MATH = 4;
    public final static int POSITION_MUSIC = 5;
    public final static int POSITION_SCIENCE = 6;
    public final static int POSITION_OTHER = 7;

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
        else if(subject.equals(array[POSITION_MUSIC]))
            return R.drawable.ic_music_note_black_24dp;
        else if(subject.equals(array[POSITION_SCIENCE]))
            return R.drawable.ic_science_black_24dp;
        else
            return R.drawable.ic_miscellaneous_services_black_24dp;
    }

    /**
     * Gets the subject's corresponding color
     * @param context The activity context
     * @param subject The subject to get the color for
     * @return Returns the color which goes along with the given subject
     */
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
            return ContextCompat.getColor(context, R.color.turquoise);
        else if(subject.equals(array[POSITION_MUSIC]))
            return ContextCompat.getColor(context, R.color.blue);
        else if(subject.equals(array[POSITION_SCIENCE]))
            return ContextCompat.getColor(context, R.color.magenta);
        else
            return ContextCompat.getColor(context, R.color.purple);
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
            if(array[i].equals(subject))
                return i;
        }

        return -1;
    }

    public static View getViewFromXML(Context context, int layoutId) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(layoutId, null, false);
    }
}