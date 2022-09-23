package com.joshuaau.plantlet.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Connectivity {
    private ConnectivityManager manager;
    private ConnectivityManager.NetworkCallback networkCallback;

    private List<ConnectivityListener> listeners;

    public Connectivity(Context context) {
        manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkCallback = null;

//        listeners = new ArrayList<ConnectivityListener>();
//        listeners = Collections.synchronizedList(new ArrayList<ConnectivityListener>());
        listeners = new CopyOnWriteArrayList<ConnectivityListener>();

        initCallbacks();
    }

    private void initCallbacks() {
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);

                for(ConnectivityListener l : listeners)
                    l.onAvailable(network);
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);

                for(ConnectivityListener l : listeners)
                    l.onLost(network);
            }
        };

        manager.requestNetwork(networkRequest, networkCallback);
    }

    public void addListener(ConnectivityListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ConnectivityListener listener) {
        listeners.remove(listener);
    }

    public interface ConnectivityListener {
        void onAvailable(Network network);
        void onLost(Network network);
    }
}
