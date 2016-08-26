package l.files.base.content;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import static android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE;

public final class Contexts {

    private Contexts() {
    }

    public static boolean isDebugBuild(Context context) {
        ApplicationInfo app = context.getApplicationInfo();
        return 0 != (app.flags & FLAG_DEBUGGABLE);
    }
}
