package l.files.app.os;

import android.content.res.Resources;
import com.squareup.otto.Bus;
import l.files.common.os.AsyncTaskExecutor;

public final class Monitors {

    public static Monitor create(Bus bus, Resources res, AsyncTaskExecutor executor) {
        return DefaultMonitor.create(bus, res, executor);
    }

    private Monitors() {
    }
}
