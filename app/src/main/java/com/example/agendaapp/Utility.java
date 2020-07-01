package com.example.agendaapp;

import android.app.Activity;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.List;

public class Utility {

    public final static String HOME_FRAGMENT = "Home Fragment";
    public final static String CREATE_FRAGMENT = "Create Fragment";

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager manager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        try {
            manager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        } catch(Exception e){}
    }
}

class Resize {
    private Activity activity;
    private ViewGroup decorView;
    private ViewGroup contentView;

    private List<ResizeListener> listeners;

    private int previousHeight;
    private int currentHeight;

    public Resize(Activity activity) {
        this.activity = activity;
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

    private void listen() {
        decorView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                currentHeight = contentView.getHeight();

                if(currentHeight != previousHeight) {
                    for(ResizeListener listener : listeners) {
                        listener.onResize(previousHeight, currentHeight, contentView);
                    }
                }

                previousHeight = currentHeight;

                return true;
            }
        });
    }

    interface ResizeListener {
        void onResize(int fromHeight, int toHeight, ViewGroup contentView);
    }
}