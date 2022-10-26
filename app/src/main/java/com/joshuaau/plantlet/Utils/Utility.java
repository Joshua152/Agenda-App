/**
 * Defines utility methods and the internal constants
 *
 * @author Joshua AU
 * @version 1.0
 * @since 6/24/2020
 */

package com.joshuaau.plantlet.Utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.core.content.ContextCompat;

import com.joshuaau.plantlet.Data.Assignment;
import com.joshuaau.plantlet.Data.Course;
import com.joshuaau.plantlet.R;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import timber.log.Timber;

public class Utility {

    public final static String HOME_FRAGMENT = "Home Fragment";
    public final static String EDIT_FRAGMENT = "Edit Fragment";
    public final static String VIEW_FRAGMENT = "View Fragment";
    public final static String CLASSES_FRAGMENT = "Classes Fragment";
    public final static String IMPORT_FRAGMENT = "Import Fragment";
    public final static String CAL_OPTIONS_FRAGMENT = "Calendar Options Fragment";
    public final static String PLATFORM_SELECT_FRAGMENT = "Platform Select Fragment";

    public final static String SERIALIZATION_ASSIGNMENT_FILE = "assignments.txt";
    public final static String SERIALIZATION_COURSES_FILE = "courses.txt";

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
     * Checks if an internet connection is available (not necessarily connected)
     * @param context The context
     * @return Returns true if available, false otherwise
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();

        Timber.i("Is network available: %s", (info != null && info.isConnected()));

        return info != null && info.isConnected(); // change to info.isConnectedOrConnecting()?
    }

    /**
     * Inflates a view from the given xml file id
     * @param context The context
     * @param layoutId The layout id
     * @return Returns the inflated view
     */
    public static View getViewFromXML(Context context, int layoutId) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(layoutId, null, false);
    }

    /**
     * Displays a basic Snackbar with LENGTH_SHORT, parent view set as android.R.id.content, and
     * with an OK button to dismiss
     * @param activity The activity
     * @param messageResId The String resource id
     */
    public static void showBasicSnackbar(Activity activity, int messageResId) {
        Snackbar.make(activity.findViewById(android.R.id.content), messageResId, Snackbar.LENGTH_SHORT)
                .setAction(R.string.ok, view -> {})
                .show();
    }

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

    /**
     * Serializes the arrays to the file
     * @param context The context
     * @param priority The priority assignments list
     * @param upcoming The upcoming assignments list
     */
    public static void serializeAssignments(Context context, ArrayList<Assignment> priority, ArrayList<Assignment> upcoming) {
        ArrayList[] serialize = new ArrayList[2];

        serialize[SERIALIZATION_PRIORITY] = priority;
        serialize[SERIALIZATION_UPCOMING] = upcoming;

        Serialize.serialize(serialize, context.getFilesDir() + "/" + SERIALIZATION_ASSIGNMENT_FILE);
    }

    /**
     * Serializes the courses Map
     * @param context The context
     * @param courses The course HashMap
     */
    public static void serializeCourses(Context context, Map<String, Course> courses) {
        Serialize.serialize(courses, context.getFilesDir() + "/" + SERIALIZATION_COURSES_FILE);
    }

    /**
     * Deserializes the courses Map
     * @param context The context
     * @return Returns the saved map
     */
    public static Map<String, Course> deserializeCourses(Context context) {
        return (Map<String, Course>) Serialize.deserialize(context.getFilesDir() + "/" + SERIALIZATION_COURSES_FILE);
    }

    /**
     * Returns the subject name from the array
     * @param context The context
     * @param pos The position of the subject in the spinner
     * @return Returns the subject name
     */
    public static String getSubject(Context context, int pos) {
        return context.getResources().getStringArray(R.array.subject_array)[pos];
    }

    /**
     * Returns the subject drawable id given the subject name
     * @param context The context
     * @param subject The subject name
     * @return Returns the drawable id of the subject icon
     */
    public static int getSubjectDrawableId(Context context, String subject) {
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
            return ContextCompat.getColor(context, R.color.google_blue);
        else if(subject.equals(array[POSITION_SCIENCE]))
            return ContextCompat.getColor(context, R.color.magenta);
        else
            return ContextCompat.getColor(context, R.color.purple);
    }

    /**
     * Gets the subject's spinner index based on the subject title (ex. Math, Other)
     *
     * @param context Context
     * @param subject The subject title
     * @return Returns the subject's spinner index
     */
    public static int getSubjectPositionFromTitle(Context context, String subject) {
        String[] array = context.getResources().getStringArray(R.array.subject_array);

        for(int i = 0; i < array.length; i++) {
            if(array[i].equals(subject))
                return i;
        }

        return -1;
    }
}