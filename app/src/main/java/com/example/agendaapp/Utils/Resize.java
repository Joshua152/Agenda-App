/**
 * This class listens for if the ContentView has been resized (ex. keyboard).
 *
 * @author Joshua Au
 * @version 1.0
 * @since 6/24/2020
 */

package com.example.agendaapp.Utils;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
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

    // The previous height of the content view
    private int previousHeight;
    // The current height of the content view
    private int currentHeight;

    public Resize(View listenerView, View contentView) {
        this.listenerView = listenerView;
        this.contentView = contentView;

        listeners = new ArrayList<ResizeListener>();

        previousHeight = contentView.getHeight();
        currentHeight = contentView.getHeight();

        listen();
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
     * Defines the ResizeListener interface
     */
    public interface ResizeListener {
        void onResize(int fromHeight, int toHeight, View contentView);
    }
}