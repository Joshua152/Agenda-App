/**
 * This class listens for if the ContentView has been resized (ex. keyboard).
 *
 * @author Joshua Au
 * @version 1.0
 * @since 6/24/2020
 */

package com.joshuaau.plantlet.Utils;

import android.app.Activity;
import android.view.View;
import android.view.Window;

import java.util.ArrayList;
import java.util.List;

public class Resize {

    // The view the listener will be added to
    private View listenerView;
    // The view to listen for resize
    private View contentView;

    // All the listeners for when the content view has been resized
    private List<ResizeListener> listeners;

    // The height of the content view when Resize is first initialized
    private int originalContentHeight;
    // The previous height of the content view
    private int previousHeight;
    // The current height of the content view
    private int currentHeight;

    public Resize(View listenerView, View contentView) {
        this.listenerView = listenerView;
        this.contentView = contentView;

        listeners = new ArrayList<ResizeListener>();

        originalContentHeight = contentView.getHeight();
        previousHeight = contentView.getHeight();
        currentHeight = contentView.getHeight();

        listen();
    }

    /**
     * Creates a new instance where the listenerView is the decor view and the content view is Window.ID_ANDROID_CONTENT
     * @param activity The activity
     * @return Returns a new Resize object
     */
    public static Resize newInstance(Activity activity) {
        return new Resize(activity.getWindow().getDecorView(), activity.getWindow().getDecorView().findViewById(Window.ID_ANDROID_CONTENT));
    }

    public void addListener(ResizeListener resizeListener) {
        listeners.add(resizeListener);
    }

    /**
     * Listeners for if the content view has been resized
     */
    private void listen() {
        // onPreDraw()
        listenerView.getViewTreeObserver().addOnPreDrawListener(() -> {
            currentHeight = contentView.getHeight();

            if(currentHeight != previousHeight) {
                for(ResizeListener listener : listeners) {
                    listener.onResize(previousHeight, currentHeight, contentView);
                }
            }

            previousHeight = currentHeight;

            return true;
        });
    }

    /**
     * Gets the height of the content view
     * @return Returns the pixel height of the content view
     */
    public int getContentHeight() {
        return contentView.getHeight();
    }

    /**
     * Gets the height of the content view when the object was first instantiated
     * @return Returns the pixel height of the original content view
     */
    public int getOriginalContentHeight() {
        return originalContentHeight;
    }

    /**
     * Defines the ResizeListener interface
     */
    public interface ResizeListener {
        void onResize(int fromHeight, int toHeight, View contentView);
    }
}