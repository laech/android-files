package l.files.ui.base.fs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import l.files.fs.File;

import static java.lang.Math.min;

public final class FileIntents {

    private static final String ACTION_FILES_CHANGED = "l.files.ui.action.FILES_CHANGED";
    private static final String EXTRA_FILES = "files";

    private FileIntents() {
    }

    public static List<File> getChangedFiles(Intent intent) {
        return intent.getParcelableArrayListExtra(EXTRA_FILES);
    }

    public static void broadcastFilesChanged(Collection<File> files, Context context) {
        broadcastFilesChanged(files, LocalBroadcastManager.getInstance(context));
    }

    public static void broadcastFilesChanged(Collection<File> files, LocalBroadcastManager manager) {
        int partitionSize = 100;
        List<File> partition = new ArrayList<>(min(partitionSize, files.size()));
        for (File file : files) {
            partition.add(file);
            if (partition.size() >= partitionSize) {
                broadcastFilesCopy(partition, manager);
                partition.clear();
            }
        }
        if (!partition.isEmpty()) {
            broadcastFilesCopy(partition, manager);
        }
    }

    private static void broadcastFilesCopy(List<File> files, LocalBroadcastManager manager) {
        Intent intent = new Intent(ACTION_FILES_CHANGED);
        intent.putParcelableArrayListExtra(EXTRA_FILES, new ArrayList<Parcelable>(files));
        manager.sendBroadcast(intent);
    }

    public static void registerFilesChangedReceiver(
            BroadcastReceiver receiver, Context context) {

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        registerFilesChangedReceiver(receiver, manager);
    }

    public static void registerFilesChangedReceiver(
            BroadcastReceiver receiver, LocalBroadcastManager manager) {

        manager.registerReceiver(receiver, new IntentFilter(ACTION_FILES_CHANGED));
    }

    public static void unregisterFilesChangedReceiver(
            BroadcastReceiver receiver, LocalBroadcastManager manager) {

        manager.unregisterReceiver(receiver);
    }

    public static void unregisterFilesChangedReceiver(
            BroadcastReceiver receiver, Context context) {

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        unregisterFilesChangedReceiver(receiver, manager);
    }

}
