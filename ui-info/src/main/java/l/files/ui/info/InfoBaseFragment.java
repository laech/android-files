package l.files.ui.info;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import javax.annotation.Nullable;

import l.files.fs.Name;
import l.files.fs.Path;
import l.files.ui.info.CalculateSizeLoader.Size;

import static android.text.format.Formatter.formatFileSize;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public abstract class InfoBaseFragment
        extends AppCompatDialogFragment
        implements LoaderCallbacks<Size> {

    public static final String FRAGMENT_TAG = "info-dialog";

    private static final Handler handler = new Handler();

    static final String ARG_DIR = "dir";
    static final String ARG_CHILDREN = "children";

    @Nullable
    private Path dir;

    @Nullable
    private List<Name> children;

    @Nullable
    private TextView size;

    @Nullable
    private TextView sizeOnDisk;

    @Nullable
    private ProgressBar calculatingSize;

    Path getDirectory() {
        assert dir != null;
        return dir;
    }

    List<Name> getChildren() {
        assert children != null;
        return children;
    }

    public TextView getSizeView() {
        assert size != null;
        return size;
    }

    public TextView getSizeOnDiskView() {
        assert sizeOnDisk != null;
        return sizeOnDisk;
    }

    private ProgressBar getCalculatingSizeProgressBar() {
        assert calculatingSize != null;
        return calculatingSize;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.Theme_Dialog_NoTitle);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View root = getView();
        assert root != null;
        size = root.findViewById(R.id.size);
        sizeOnDisk = root.findViewById(R.id.size_on_disk);
        calculatingSize = root.findViewById(R.id.calculate_size_progress_bar);

        dir = getArguments().getParcelable(ARG_DIR);
        children = getArguments().getParcelableArrayList(ARG_CHILDREN);
    }

    void initLoader() {
        getLoaderManager().initLoader(0, null, this);

        handler.post(new Runnable() {
            @Override
            public void run() {
                CalculateSizeLoader loader = sizeLoader();
                if (loader != null && loader.isRunning()) {
                    getSizeView().setText(formatSizeCount(loader.currentSize(), loader.currentCount()));
                    getSizeOnDiskView().setText(formatSizeOnDisk(loader.currentSizeOnDisk()));
                    getCalculatingSizeProgressBar().setVisibility(VISIBLE);
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
        assert dir != null;
        assert children != null;
        return new CalculateSizeLoader(getActivity(), dir, children);
    }

    @Override
    public void onLoadFinished(Loader<Size> loader, Size data) {
        getSizeView().setText(formatSizeCount(data.size(), data.count()));
        getSizeOnDiskView().setText(formatSizeOnDisk(data.sizeOnDisk()));
        getCalculatingSizeProgressBar().setVisibility(GONE);
    }

    @Override
    public void onLoaderReset(Loader<Size> loader) {
    }

}
