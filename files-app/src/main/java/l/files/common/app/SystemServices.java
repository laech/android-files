package l.files.common.app;

import android.app.NotificationManager;
import android.content.ClipboardManager;
import android.content.Context;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;

public final class SystemServices {
    private SystemServices() {
    }

    public static NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
    }

    public static ClipboardManager getClipboardManager(Context context) {
        return (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
    }
}
