package l.files.ui.browser;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.google.auto.value.AutoValue;

import java.io.IOException;
import java.util.Collection;

import l.files.fs.File;
import l.files.fs.Name;
import l.files.ui.browser.CalculateSizeLoader.Size;

import static l.files.base.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;

final class CalculateSizeLoader
        extends AsyncTaskLoader<Size>
        implements File.SizeVisitor {

    private boolean started;
    private volatile int currentCount;
    private volatile long currentSize;
    private volatile long currentSizeOnDisk;
    private volatile Size result;

    private final File dir;
    private final Collection<Name> children;

    CalculateSizeLoader(Context context, File dir, Collection<Name> children) {
        super(context);
        this.dir = requireNonNull(dir);
        this.children = requireNonNull(children);
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

        for (Name child : children) {
            try {
                dir.resolve(child).traverseSize(FOLLOW, this);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
    public boolean onSize(long size, long sizeOnDisk) throws RuntimeException {
        currentCount++;
        currentSize += size;
        currentSizeOnDisk += sizeOnDisk;
        return !isLoadInBackgroundCanceled();
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
