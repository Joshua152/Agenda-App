/**
 * Class to represent basic Platform info :
 *
 * Platform Icon,
 * Platform Name
 */

package com.joshuaau.plantlet.Data;

import android.graphics.drawable.Drawable;

public class PlatformInfo {

    private int platformIconId;
    private String platformName;

    public PlatformInfo(int platformIconId, String platformName) {
        this.platformIconId = platformIconId;
        this.platformName = platformName;
    }

    public int getPlatformIconId() {
        return platformIconId;
    }

    public String getPlatformName() {
        return platformName;
    }
}
