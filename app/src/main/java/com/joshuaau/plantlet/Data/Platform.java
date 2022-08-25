/**
 * Class that represents a platform such as Google Classroom, Schoology, etc.
 */

package com.joshuaau.plantlet.Data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import androidx.lifecycle.DefaultLifecycleObserver;

import com.joshuaau.plantlet.R;
import com.joshuaau.plantlet.Utils.OAuthHelper;
import com.joshuaau.plantlet.Utils.Utility;

import net.openid.appauth.AuthState;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import timber.log.Timber;

public abstract class Platform implements DefaultLifecycleObserver, Serializable {

    public static final String AUTO_ID = "Auto ID";

    public static final String ACCOUNT_SHARED_PREFS = "Account Shared Preferences";
    public static final String ACCOUNT_JSON = "Account JSON";

    // TODO: USELESS (replace with accountID?)?
    public String ID;
    public String accountID;

    private int platformIconId;
    private String platformName;

    private String accountIconURL;
    private View signInButton;

    private List<SignedInListener> signedInListeners;
    private List<SignOutRequestListener> signOutRequestListeners;

    protected OAuthHelper oAuthHelper;

    private boolean signedIn;

    private ArrayList<String> exclusions;

    private boolean hasOptions;

    /**
     * Constructor to set the platform icon and name with the default sign in icon (not signed in yet)
     * @param platformIconId The platform logo drawable id
     * @param platformName The name of the platform
     * @param hasOptions Whether the platform has additional options in the import fragment
     */
    public Platform(int platformIconId, String platformName, boolean hasOptions) {
        ID = UUID.randomUUID().toString();

        this.platformIconId = platformIconId;
        this.platformName = platformName;

        accountIconURL = "";
        signInButton = null;

        signedInListeners = new ArrayList<SignedInListener>();
        signOutRequestListeners = new ArrayList<SignOutRequestListener>();

        oAuthHelper = null;

        signedIn = false;

        exclusions = new ArrayList<String>();

        this.hasOptions = hasOptions;
    }

    /**
     * Constructor to set aspects of a platform with a custom sign in button (not signed in yet)
     * @param platformIconId The platform logo drawable id
     * @param platformName The name of the platform
     * @param signInButton The custom sign in button
     * @param hasOptions Whether the platform has additional options in the import fragment
     */
    public Platform(int platformIconId, String platformName, View signInButton, boolean hasOptions) {
        this(platformIconId, platformName, hasOptions);

        this.signInButton = signInButton;

        this.hasOptions = hasOptions;
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
     * @param activity The activity
     */
    public void updateAndCheckAuthState(Activity activity, AuthUpdatedListener listener) {
        oAuthHelper.useAuthToken(authState -> {
            if(authState == null) {
                onClickSignOut();
                callSignOutRequestListeners();

                Utility.showBasicSnackbar(activity, R.string.import_error);
            }

            listener.onAuthUpdated(authState);
        });
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

    /**
     * Adds an account to the shared preferences. Returns if successful.
     * @param context The context
     * @param id The account id (use a combination of a user id and platform name)
     * @return Returns true if successful and false if the user is already signed in
     */
    public boolean addAccount(Context context, String id) {
        try {
            SharedPreferences accountPreferences = context.getSharedPreferences(ACCOUNT_SHARED_PREFS, Context.MODE_PRIVATE);

            JSONArray accounts = new JSONArray(accountPreferences.getString(ACCOUNT_JSON, "[]"));

            for(int i = 0; i < accounts.length(); i++) {
                String acc = accounts.getString(i);

                if(acc.equals(id))
                    return false;
            }

            accounts.put(id);

            accountPreferences.edit().putString(ACCOUNT_JSON, accounts.toString()).apply();

            setAccountID(id);

            return true;
        } catch(JSONException e) {
            Timber.e(e, "Unable to retrieve account shared preferences");

            return false;
        }
    }

    /**
     * Removes the account from shared preferences
     * @param context The context
     * @param id The account id
     */
    public void removeAccount(Context context, String id) {
        try {
            SharedPreferences accountPreferences = context.getSharedPreferences(ACCOUNT_SHARED_PREFS, Context.MODE_PRIVATE);

            JSONArray accounts = new JSONArray(accountPreferences.getString(ACCOUNT_JSON, "[]"));

            for(int i = accounts.length() - 1; i >= 0; i--) {
                String acc = accounts.getString(i);

                if(acc.equals(id)) {
                    accounts.remove(i);

                    return;
                }
            }
        } catch(JSONException e) {
            Timber.e(e, "Unable to retrieve account shared preferences");
        }
    }

    /**
     * Sets all fields holding user data to null and sets isSignedIn to false
     */
    public void signOut() {
        accountIconURL = "";
        signedIn = false;

        oAuthHelper.signOut();
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    public void setAccountIconURL(String accountIconURL) {
        this.accountIconURL = accountIconURL;
    }

    public void setAuthState(AuthState authState) {
        oAuthHelper.setAuthState(authState);
    }

    public void setExclusions(ArrayList<String> exclusions) {
        this.exclusions = exclusions;
    }

    public void setSignedIn(boolean signedIn) {
        this.signedIn = signedIn;
    }

    public String getID() {
        return ID;
    }

    public String getAccountID() {
        return accountID;
    }

    public int getPlatformIconId() {
        return platformIconId;
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
     * Contains the course ids that are excluded from being imported
     * @return Gets a list of the exclusions
     */
    public ArrayList<String> getExclusions() {
        return exclusions;
    }

    public boolean hasOptions() {
        return hasOptions;
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
         * @param coursesWithExclusion The courses with excluded ones removed
         */
        void onCoursesReceived(Map<String, Course> courses, Map<String, Course> coursesWithExclusion);
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
