package l.files.ui.operations;

import android.content.Context;

import java.util.concurrent.atomic.AtomicBoolean;

import l.files.operations.Clock;

import static l.files.operations.OperationService.addListener;

public final class OperationsUi {

    private static final AtomicBoolean init = new AtomicBoolean(false);

    public static void init(Context context) {
        if (init.compareAndSet(false, true)) {
            addListener(new NotificationProvider(context, Clock.system()));
            addListener(new FilesChangedBroadcaster(context));
        }
    }

}
