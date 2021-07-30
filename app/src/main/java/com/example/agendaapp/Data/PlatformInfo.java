/**
 * Class to represent basic Platform info :
 *
 * Platform Icon,
 * Platform Name
 */

package com.example.agendaapp.Data;

import android.graphics.drawable.Drawable;

public class PlatformInfo {

    private Drawable platformIcon;
    private String platformName;

    public PlatformInfo(Drawable platformIcon, String platformName) {
        this.platformIcon = platformIcon;
        this.platformName = platformName;
    }

    public Drawable getPlatformIcon() {
        return platformIcon;
    }

    public String getPlatformName() {
        return platformName;
    }
}
