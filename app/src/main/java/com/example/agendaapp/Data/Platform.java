/**
 * Class that represents a platform such as Google Classroom, Schoology, etc.
 */

package com.example.agendaapp.Data;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import androidx.lifecycle.DefaultLifecycleObserver;

import java.util.ArrayList;
import java.util.List;

public abstract class Platform implements DefaultLifecycleObserver {

    private Drawable platformIcon;
    private String platformName;

    private String accountIconURL;
    private View signInButton;

    private List<SignedInListener> listeners;

    /**
     * Constructor to set the platform icon and name with the default sign in icon (not signed in yet)
     * @param platformIcon The platform logo
     * @param platformName The name of the platform
     */
    public Platform(Drawable platformIcon, String platformName) {
        this.platformIcon = platformIcon;
        this.platformName = platformName;

        listeners = new ArrayList<SignedInListener>();
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

        listeners = new ArrayList<SignedInListener>();
    }

    /**
     * Constructor to set aspects of a platform with the default sign in button (already signed in)
     * @param platformIcon The platform logo
     * @param platformName The name of the platform
     * @param accountIconURL The view which holds the profile picture
     */
    public Platform(Drawable platformIcon, String platformName, String accountIconURL) {
        this(platformIcon, platformName);

        this.accountIconURL = accountIconURL;

        listeners = new ArrayList<SignedInListener>();
    }

    /**
     * Adds a SignedInListener to the list
     * @param listener The listener to add
     */
    public void addListener(SignedInListener listener) {
        listeners.add(listener);
    }

    /**
     * Calls all of the SignInListeners in the list
     */
    public void callSignInListeners() {
        for(SignedInListener l : listeners)
            l.onSignedIn();
    }

    /**
     * Callback method when the sign in button gets pressed
     */
    public abstract void onClickSignIn();

    /**
     * Callback method when the sign out button gets pressed
     */
    public abstract void onClickSignOut();

    /**
     * Sets all fields holding user data to null
     */
    public void clearData() {
        accountIconURL = null;
    }

    public void setAccountIcon(String accountIconURL) {
        this.accountIconURL = accountIconURL;
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

    /**
     * An interface for the onSignedIn() method
     */
    public interface SignedInListener {
        /**
         * Gets called when the sign in was was successful and an auth token was gotten
         */
        public void onSignedIn();
    }
}
