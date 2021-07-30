/**
 * Class that represents a platform such as Google Classroom, Schoology, etc.
 */

package com.example.agendaapp.Data;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.lifecycle.DefaultLifecycleObserver;

import com.google.api.client.util.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Platform implements DefaultLifecycleObserver {

    public final String ID;

    private Drawable platformIcon;
    private String platformName;

    private String accountIconURL;
    private View signInButton;

    private List<SignedInListener> signedInListeners;
    private List<SignOutRequestListener> signOutRequestListeners;

    protected String authToken;

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

        authToken = "";
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
     * Returns if the user is currently signed in to the platform
     * @return Returns if the auth token is an empty String
     */
    public boolean isSignedIn() {
        return !authToken.equals("");
    }

    /**
     * Sets all fields holding user data to null
     */
    public void clearData() {
        accountIconURL = "";
        authToken = "";
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

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
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
    
    public String getToken() {
        return authToken;
    }

    public String getID() {
        return ID;
    }

    /**
     * Checks to make sure the auth token hasn't expired yet and handles the result if it has
     */
    public abstract void checkAuthTokenValid();

    /**
     * Sign in to platform (get photo) with previous auth token
     */
    public abstract void signInWithPrevAuth();

    /**
     * Callback method when the sign in button gets pressed
     */
    public abstract void onClickSignIn();

    /**
     * Callback method when the sign out button gets pressed (make sure to invalidate auth token)
     */
    public abstract void onClickSignOut();

    /**
     * Gets the assignments which haven't been added to the list yet
     * @param listener Listener for when the new assignment List has finished filling
     */
    public abstract void getNewAssignments(AssignmentReceivedListener listener);

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
     * An interface for the onAssignmentReceived() method
     */
    public interface AssignmentReceivedListener {
        /**
         * Gets called when all the assignment http requests have been returned and the
         * List is filled
         * @param assignments The List of new assignments
         */
        public void onAssignmentReceived(List<Assignment> assignments);
    }
}
