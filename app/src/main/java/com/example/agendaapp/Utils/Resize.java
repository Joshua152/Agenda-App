/**
 * This class listens for if the ContentView has been resized (ex. keyboard).
 *
 * @author Joshua Au
 * @version 1.0
 * @since 6/24/2020
 */

package com.example.agendaapp.Utils;

import android.app.Activity;
import android.view.ViewGroup;
import android.view.Window;

import java.util.ArrayList;
import java.util.List;

public class Resize {

    // The app's DecorView (encompasses the AppBar and ContentView)
    private ViewGroup decorView;
    // The app's ContentView
    private ViewGroup contentView;

    // All the listeners for when the ContentView has been resized
    private List<ResizeListener> listeners;

    // The previous height of the ContentView
    private int previousHeight;
    // The current height of the ContentView
    private int currentHeight;

    public Resize(Activity activity) {
        decorView = (ViewGroup) activity.getWindow().getDecorView();
        contentView = decorView.findViewById(Window.ID_ANDROID_CONTENT);

        listeners = new ArrayList<ResizeListener>();

        previousHeight = contentView.getHeight();
        currentHeight = contentView.getHeight();

        listen();
    }

    public void addListener(ResizeListener resizeListener) {
        listeners.add(resizeListener);
    }

    /**
     * Listeners for if the ContentView has been resized
     */
    private void listen() {
        // onPreDraw()
        decorView.getViewTreeObserver().addOnPreDrawListener(() -> {
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
     * Gets the height of the ContentView
     * @return Returns the pixel height of the ContentView
     */
    public int getContentHeight() {
        return contentView.getHeight();
    }

    /**
     * Defines the ResizeListener interface
     */
    public interface ResizeListener {
        void onResize(int fromHeight, int toHeight, ViewGroup contentView);
    }
}