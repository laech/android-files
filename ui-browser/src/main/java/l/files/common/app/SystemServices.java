package l.files.common.app;

import android.content.ClipboardManager;
import android.content.Context;

import static android.content.Context.CLIPBOARD_SERVICE;

public final class SystemServices {
    private SystemServices() {
    }

    public static ClipboardManager getClipboardManager(Context context) {
        return (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
    }
}
