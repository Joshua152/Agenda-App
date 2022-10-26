package com.joshuaau.plantlet.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import timber.log.Timber;

public class Connectivity {
    private Context context;

    private ConnectivityManager manager;
    private ConnectivityManager.NetworkCallback networkCallback;

    private List<ConnectivityListener> listeners;

    public Connectivity(Context context) {
        this.context = context;

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

                isInternetConnected(context, ok -> {
                    Timber.i("Ping %s", ok);

//                    for(ConnectivityListener l : listeners)
//                        l.onAvailable(network);
//                    ArrayList<ConnectivityListener> list = list

                    for(Iterator<ConnectivityListener> iterator = listeners.iterator(); iterator.hasNext(); )
                        iterator.next().onAvailable(network);
                });
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

    /**
     * Checks if the device is connected to the internet by pinging https://clients3.google.com/generate_204
     * @param listener listener for when the ping response has been gotten
     */
    public static void isInternetConnected(Context context, PingListener listener) {
        new Thread(() -> {
            if(Utility.isNetworkAvailable(context)) {
                try {
                    HttpURLConnection urlc = (HttpURLConnection) new URL("http://clients3.google.com/generate_204")
                            .openConnection();

                    urlc.setRequestProperty("User-Agent", "Android");
                    urlc.setRequestProperty("Connection", "close");
                    urlc.setConnectTimeout(1500);
                    urlc.connect();

                    listener.onPing(urlc.getResponseCode() == 204 && urlc.getContentLength() == 0);

                    return;
                } catch (IOException e) {
                    Timber.e(e, "Error checking internet connection");
                }
            } else {
                Timber.e("No network available!");
            }

            listener.onPing(false);
        }).start();
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

    public interface PingListener {
        void onPing(boolean ok);
    }
}
