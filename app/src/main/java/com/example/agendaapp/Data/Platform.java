/**
 * Class that represents a platform such as Google Classroom, Schoology, etc.
 */

package com.example.agendaapp.Data;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.DefaultLifecycleObserver;

import com.example.agendaapp.R;
import com.example.agendaapp.Utils.OAuthHelper;
import com.google.api.client.util.DateTime;

import net.openid.appauth.AuthState;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class Platform implements DefaultLifecycleObserver {

    public static final String AUTO_ID = "Auto ID";

    public String ID;

    private Drawable platformIcon;
    private String platformName;

    private String accountIconURL;
    private View signInButton;

    private List<SignedInListener> signedInListeners;
    private List<SignOutRequestListener> signOutRequestListeners;

    protected OAuthHelper oAuthHelper;

    private boolean signedIn;

    /**
     * Constructor to set the platform icon and name with the default sign in icon (not signed in yet)
     * @param platformIcon The platform logo
     * @param platformName The name of the platform
     */
    public Platform(Drawable platformIcon, String platformName) {
        ID = UUID.randomUUID().toString();

        this.platformIcon = platformIcon;
        this.platformName = platformName;

        accountIconURL = "";
        signInButton = null;

        signedInListeners = new ArrayList<SignedInListener>();
        signOutRequestListeners = new ArrayList<SignOutRequestListener>();

        oAuthHelper = null;

        signedIn = false;
    }

    /**
     * Constructor to set aspects of a platform with a custom sign in button (not signed in yet)
     * @param platformIcon The platform logo
     * @param platformName The name of the platform
     * @param signInButton The custom sign in button
     */
    public Platform(Drawable platformIcon, String platformName, View signInButton) {
        this(platformIcon, platformName);

        this.signInButton = signInButton;
    }

    /**
     * Adds a SignedInListener to the list
     * @param listener The listener to add
     */
    public void addSignedInListener(SignedInListener listener) {
        signedInListeners.add(listener);
    }

    /**
     * Adds a SignOutRequestListener to the list
     * @param listener the listener to add
     */
    public void addSignOutRequestListener(SignOutRequestListener listener) {
        signOutRequestListeners.add(listener);
    }

    /**
     * Calls all of the SignInListeners in the list
     */
    public void callSignInListeners() {
        for(SignedInListener l : signedInListeners)
            l.onSignedIn();
    }

    /**
     * Calls all of the SignOutRequestListeners in the list
     */
    public void callSignOutRequestListeners() {
        for(SignOutRequestListener l : signOutRequestListeners)
            l.onSignOutRequest();
    }

    /**
     * Updates the auth state and signs out if an exception occurs
     * @param context The context
     */
    public void updateAndCheckAuthState(Context context, AuthUpdatedListener listener) {
        oAuthHelper.useAuthToken(authState -> {
            if(authState == null) {
                onClickSignOut();
                callSignOutRequestListeners();

                Toast.makeText(context, R.string.import_error, Toast.LENGTH_SHORT).show();
            }

            listener.onAuthUpdated(authState);
        });
    }

    /**
     * Sets all fields holding user data to null and sets isSignedIn to false
     */
    public void signOut() {
        accountIconURL = "";
        signedIn = false;

        oAuthHelper.signOut();
    }

    /**
     * Sets the update millis (System.currentTimeMillis()) for the specific platform and course; the key is the courseId
     * **Make sure to use a different shared prefs key in order to make sure that there are no courseId conflicts
     * @param courseId The specific course id
     * @param editor Shared preferences editor to put the time in
     */
    public void setUpdateMillis(String courseId, SharedPreferences.Editor editor) {
        editor.putLong(courseId, System.currentTimeMillis());
        editor.commit();
    }

    public void setAccountIconURL(String accountIconURL) {
        this.accountIconURL = accountIconURL;
    }

    public void setAuthState(AuthState authState) {
        oAuthHelper.setAuthState(authState);
    }

    public void setSignedIn(boolean signedIn) {
        this.signedIn = signedIn;
    }

    public String getID() {
        return ID;
    }

    public Drawable getPlatformIcon() {
        return platformIcon;
    }

    public String getPlatformName() {
        return platformName;
    }

    public String getAccountIconURL() {
        return accountIconURL;
    }

    public View getSignInButton() {
        return signInButton;
    }
    
    public OAuthHelper getOAuthHelper() {
        return oAuthHelper;
    }

    public boolean getSignedIn() {
        return signedIn;
    }

    /**
     * Callback method when the sign in button gets pressed
     */
    public abstract void onClickSignIn();

    /**
     * Callback method when the sign out button gets pressed (make sure to invalidate auth token)
     */
    public abstract void onClickSignOut();

    /**
     * Gets valid (active) courses from the platform
     * @param listener Listener for when the course list has been retrieved
     */
    public abstract void getCourses(CoursesReceivedListener listener);

    /**
     * Gets the assignments which haven't been added to the list yet
     * @param listener Listener for when the new assignment List has finished filling
     */
    public abstract void getNewAssignments(AssignmentReceivedListener listener);

    /**
     * Reads in the saved auth state from storage
     * @return Returns the auth state or null if one was not found
     */
    public abstract AuthState readAuthState();

    /**
     * An interface for the onSignedIn() method
     */
    public interface SignedInListener {
        /**
         * Gets called when the sign in was was successful and an auth token was gotten
         */
        public void onSignedIn();
    }

    /**
     * An interface for the onSignOutRequest() method
     */
    public interface SignOutRequestListener {
        /**
         * Gets called when the platform has requested for a sign out UI update
         */
        public void onSignOutRequest();
    }

    /**
     * Interface for when the auth state has been updated with the useAuthToken method
     */
    public interface AuthUpdatedListener {
        /**
         * Gets called when the auth state has been updated
         * @param authState The updated auth state
         */
        void onAuthUpdated(AuthState authState);
    }

    /**
     * An interface for the onCoursesReceived() method
     */
    public interface CoursesReceivedListener {
        /**
         * Gets called when the courses have been gotten from the http request
         * @param courses The received courses
         */
        void onCoursesReceived(Map<String, Course> courses);
    }

    /**
     * An interface for the onAssignmentReceived() method
     */
    public interface AssignmentReceivedListener {
        /**
         * Gets called when all the assignment http requests have been returned and the
         * List is filled
         * @param assignments The List of new assignments
         */
        void onAssignmentReceived(List<Assignment> assignments);
    }
}
