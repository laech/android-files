package l.files.ui.browser;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.google.auto.value.AutoValue;

import java.io.IOException;

import l.files.fs.File;
import l.files.fs.Stat;
import l.files.fs.Visitor;
import l.files.ui.browser.CalculateSizeLoader.Size;

import static l.files.base.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Visitor.Result.CONTINUE;
import static l.files.fs.Visitor.Result.TERMINATE;

final class CalculateSizeLoader extends AsyncTaskLoader<Size> implements Visitor {

    private boolean started;
    private volatile int currentCount;
    private volatile long currentSize;
    private volatile long currentSizeOnDisk;
    private volatile Size result;

    private final File file;

    CalculateSizeLoader(Context context, File file) {
        super(context);
        this.file = requireNonNull(file);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (finished()) {
            deliverResult(result);
        } else if (!started) {
            started = true;
            forceLoad();
        }
    }

    @Override
    public Size loadInBackground() {

        currentCount = 0;
        currentSize = 0;
        currentSizeOnDisk = 0;

        try {
            file.traverse(FOLLOW, this);
        } catch (IOException ignore) {
        }

        if (isLoadInBackgroundCanceled()) {
            currentCount = 0;
            currentSize = 0;
            currentSizeOnDisk = 0;
        }

        result = Size.of(currentCount, currentSize, currentSizeOnDisk);
        return result;
    }

    @Override
    public Result onPreVisit(File file) throws IOException {
        if (isLoadInBackgroundCanceled()) {
            return TERMINATE;
        }
        Stat stat = file.stat(NOFOLLOW);
        currentSize += stat.size();
        currentSizeOnDisk += stat.sizeOnDisk();
        currentCount++;
        return CONTINUE;
    }

    @Override
    public Result onPostVisit(File file) throws IOException {
        if (isLoadInBackgroundCanceled()) {
            return TERMINATE;
        }
        return CONTINUE;
    }

    @Override
    public void onException(File file, IOException e) throws IOException {
        // Ignore, not count it
    }

    int currentCount() {
        return currentCount;
    }

    long currentSize() {
        return currentSize;
    }

    long currentSizeOnDisk() {
        return currentSizeOnDisk;
    }

    boolean finished() {
        return result != null;
    }

    @AutoValue
    static abstract class Size {
        Size() {
        }

        abstract int count();

        abstract long size();

        abstract long sizeOnDisk();

        static Size of(int count, long size, long sizeOnDisk) {
            return new AutoValue_CalculateSizeLoader_Size(count, size, sizeOnDisk);
        }
    }
}
