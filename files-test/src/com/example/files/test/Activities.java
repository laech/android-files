package com.example.files.test;

import android.app.Activity;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

public final class Activities {

    public static void rotate(Activity activity) {
        int orientation = activity.getRequestedOrientation();
        activity.setRequestedOrientation(
                orientation == SCREEN_ORIENTATION_LANDSCAPE
                        ? SCREEN_ORIENTATION_PORTRAIT
                        : SCREEN_ORIENTATION_LANDSCAPE);
    }

    private Activities() {
    }
}
