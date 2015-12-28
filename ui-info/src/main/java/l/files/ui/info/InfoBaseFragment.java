package l.files.ui.info;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import l.files.fs.Name;
import l.files.fs.Path;
import l.files.ui.info.CalculateSizeLoader.Size;

import static android.text.format.Formatter.formatFileSize;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static l.files.ui.base.view.Views.find;

public abstract class InfoBaseFragment
        extends DialogFragment
        implements LoaderCallbacks<Size> {

    public static final String FRAGMENT_TAG = "info-dialog";

    static final Handler handler = new Handler();

    static final String ARG_DIR = "dir";
    static final String ARG_CHILDREN = "children";

    Path dir;
    List<Name> children;

    TextView size;
    TextView sizeOnDisk;
    ProgressBar calculatingSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.Theme_Dialog_NoTitle);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        size = find(R.id.size, this);
        sizeOnDisk = find(R.id.size_on_disk, this);
        calculatingSize = find(R.id.calculate_size_progress_bar, this);

        dir = getArguments().getParcelable(ARG_DIR);
        children = getArguments().getParcelableArrayList(ARG_CHILDREN);
    }

    void initLoader() {
        getLoaderManager().initLoader(0, null, this);

        handler.post(new Runnable() {
            @Override
            public void run() {
                CalculateSizeLoader loader = sizeLoader();
                if (loader != null && !loader.finished()) {
                    size.setText(formatSizeCount(loader.currentSize(), loader.currentCount()));
                    sizeOnDisk.setText(formatSizeOnDisk(loader.currentSizeOnDisk()));
                    calculatingSize.setVisibility(VISIBLE);
                    handler.postDelayed(this, 100);
                }
            }
        });
    }

    String formatSize(long size) {
        return formatFileSize(getActivity(), size);
    }

    private String formatSizeCount(long size, int count) {
        return getResources().getQuantityString(
                R.plurals.x_size_y_items, count, formatSize(size), count);
    }

    String formatSizeOnDisk(long size) {
        return getString(R.string.x_size_on_disk, formatSize(size));
    }

    private CalculateSizeLoader sizeLoader() {
        if (getActivity() == null) {
            return null;
        }
        Loader<?> loader = getLoaderManager().getLoader(0);
        return (CalculateSizeLoader) loader;
    }

    @Override
    public Loader<Size> onCreateLoader(int id, Bundle args) {
        return new CalculateSizeLoader(getActivity(), dir, children);
    }

    @Override
    public void onLoadFinished(Loader<Size> loader, Size data) {
        size.setText(formatSizeCount(data.size(), data.count()));
        sizeOnDisk.setText(formatSizeOnDisk(data.sizeOnDisk()));
        calculatingSize.setVisibility(GONE);
    }

    @Override
    public void onLoaderReset(Loader<Size> loader) {
    }

}
