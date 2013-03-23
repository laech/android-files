package com.example.files.util;

import android.os.Environment;

import java.io.File;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class FileSystem {

    public static final FileSystem INSTANCE = new FileSystem();

    public static final File DIRECTORY_ALARMS = dir(Environment.DIRECTORY_ALARMS);
    public static final File DIRECTORY_ANDROID = dir("Android");
    public static final File DIRECTORY_DCIM = dir(Environment.DIRECTORY_DCIM);
    public static final File DIRECTORY_DOWNLOADS = dir(Environment.DIRECTORY_DOWNLOADS);
    public static final File DIRECTORY_MOVIES = dir(Environment.DIRECTORY_MOVIES);
    public static final File DIRECTORY_MUSIC = dir(Environment.DIRECTORY_MUSIC);
    public static final File DIRECTORY_PICTURES = dir(Environment.DIRECTORY_PICTURES);
    public static final File DIRECTORY_PODCASTS = dir(Environment.DIRECTORY_PODCASTS);
    public static final File DIRECTORY_NOTIFICATIONS = dir(Environment.DIRECTORY_NOTIFICATIONS);
    public static final File DIRECTORY_RINGTONES = dir(Environment.DIRECTORY_RINGTONES);

    private static File dir(String type) {
        return getExternalStoragePublicDirectory(type);
    }

    FileSystem() {
    }

    public boolean hasPermissionToRead(File file) {
        return file.canRead() && (!file.isDirectory() || file.canExecute());
    }
}
