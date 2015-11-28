package l.files.ui.browser;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.io.IOException;

import l.files.fs.File;
import l.files.fs.Visitor;

import static l.files.base.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Visitor.Result.CONTINUE;
import static l.files.fs.Visitor.Result.TERMINATE;

final class CalculateSizeLoader extends AsyncTaskLoader<Long> implements Visitor {

    private boolean started;
    private volatile long currentSize;
    private volatile boolean finished;

    private final File file;

    CalculateSizeLoader(Context context, File file) {
        super(context);
        this.file = requireNonNull(file);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (finished) {
            deliverResult(currentSize);
        } else if (!started) {
            started = true;
            forceLoad();
        }
    }

    @Override
    public Long loadInBackground() {

        finished = false;
        currentSize = 0;

        try {
            file.traverse(FOLLOW, this);
        } catch (IOException ignore) {
        } finally {
            finished = true;
        }

        if (isLoadInBackgroundCanceled()) {
            currentSize = 0;
        }

        return currentSize;
    }

    @Override
    public Result onPreVisit(File file) throws IOException {
        if (isLoadInBackgroundCanceled()) {
            return TERMINATE;
        }
        currentSize += file.stat(NOFOLLOW).size();
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
        // Ignore
    }

    long currentSize() {
        return currentSize;
    }

    boolean finished() {
        return finished;
    }
}
