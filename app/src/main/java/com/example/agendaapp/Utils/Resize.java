package com.example.agendaapp.Utils;

import android.app.Activity;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;

import java.util.ArrayList;
import java.util.List;

public class Resize {
    private ViewGroup decorView;
    private ViewGroup contentView;

    private List<ResizeListener> listeners;

    private int previousHeight;
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

    public int getContentHeight() {
        return contentView.getHeight();
    }

    public interface ResizeListener {
        void onResize(int fromHeight, int toHeight, ViewGroup contentView);
    }
}