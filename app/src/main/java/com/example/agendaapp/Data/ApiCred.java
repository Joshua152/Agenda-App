/**
 * Class to connect with native CPP lib in order to get the Google Client ID
 */

package com.example.agendaapp.Data;

public class ApiCred {
    public ApiCred() {
        System.loadLibrary("native-lib");
    }

    public native String clientId();
}
