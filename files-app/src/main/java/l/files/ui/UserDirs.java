package l.files.ui;

import android.os.Environment;

import java.io.File;

import l.files.fs.Resource;
import l.files.fs.local.LocalResource;

import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStoragePublicDirectory;

public final class UserDirs {

    private UserDirs() {
    }

    public static final Resource DIR_ROOT = LocalResource.create(new File("/"));
    public static final Resource DIR_HOME = LocalResource.create(getExternalStorageDirectory());
    public static final Resource DIR_ALARMS = dir(Environment.DIRECTORY_ALARMS);
    public static final Resource DIR_ANDROID = dir("Android");
    public static final Resource DIR_DCIM = dir(Environment.DIRECTORY_DCIM);
    public static final Resource DIR_DOWNLOADS = dir(Environment.DIRECTORY_DOWNLOADS);
    public static final Resource DIR_MOVIES = dir(Environment.DIRECTORY_MOVIES);
    public static final Resource DIR_MUSIC = dir(Environment.DIRECTORY_MUSIC);
    public static final Resource DIR_PICTURES = dir(Environment.DIRECTORY_PICTURES);
    public static final Resource DIR_PODCASTS = dir(Environment.DIRECTORY_PODCASTS);
    public static final Resource DIR_NOTIFICATIONS = dir(Environment.DIRECTORY_NOTIFICATIONS);
    public static final Resource DIR_RINGTONES = dir(Environment.DIRECTORY_RINGTONES);

    private static Resource dir(String type) {
        return LocalResource.create(getExternalStoragePublicDirectory(type));
    }

}
