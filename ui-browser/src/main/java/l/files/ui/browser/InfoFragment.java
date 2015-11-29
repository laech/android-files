package l.files.ui.browser;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import l.files.fs.File;
import l.files.fs.Stat;
import l.files.ui.browser.CalculateSizeLoader.Size;
import l.files.ui.preview.Preview;
import l.files.ui.preview.PreviewCallback;
import l.files.ui.preview.Rect;

import static android.graphics.Color.TRANSPARENT;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_TIME;
import static android.text.format.DateUtils.formatDateTime;
import static android.text.format.Formatter.formatFileSize;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.ui.base.view.Views.find;
import static l.files.ui.preview.Preview.darkColor;

public final class InfoFragment extends DialogFragment implements
        PreviewCallback, LoaderCallbacks<Size> {

    // TODO observe?

    public static final String FRAGMENT_TAG = "info-dialog";

    private static final Handler handler = new Handler();

    private static final String ARG_FILE = "file";
    private static final String ARG_STAT = "stat";

    public static InfoFragment create(File file, Stat stat) {

        Bundle bundle = new Bundle(2);
        bundle.putParcelable(ARG_FILE, file);
        bundle.putParcelable(ARG_STAT, stat);

        InfoFragment fragment = new InfoFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private File file;
    private Stat stat;

    private Rect constraint;
    private View root;
    private TextView size;
    private TextView sizeOnDisk;
    private ImageView image;
    private ProgressBar calculatingSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.Theme_Dialog_NoTitle);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.info_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        root = find(R.id.root, this);
        size = find(R.id.size, this);
        image = find(R.id.image, this);
        sizeOnDisk = find(R.id.size_on_disk, this);
        calculatingSize = find(R.id.calculate_size_progress_bar, this);

        file = getArguments().getParcelable(ARG_FILE);
        stat = getArguments().getParcelable(ARG_STAT);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        constraint = Rect.of((int) (metrics.widthPixels * 0.75), metrics.heightPixels);

        initImage();

        TextView nameView = find(R.id.name, this);
        nameView.setMaxWidth(constraint.width());
        nameView.setText(file.name().toString());

        TextView dateView = find(R.id.modified, this);
        dateView.setText(formatDate());
        size.setText(formatSize(stat.size()));
        sizeOnDisk.setText(formatSizeOnDisk(stat.sizeOnDisk()));

        if (stat.isDirectory() || stat.isSymbolicLink()) {
            getLoaderManager().initLoader(0, null, this);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    CalculateSizeLoader loader = sizeLoader();
                    if (loader != null && !loader.finished()) {
                        calculatingSize.setVisibility(VISIBLE);
                        size.setText(formatSizeCount(loader.currentSize(), loader.currentCount()));
                        sizeOnDisk.setText(formatSizeOnDisk(loader.currentSizeOnDisk()));
                        handler.postDelayed(this, 100);
                    }
                }
            });
        }
    }

    private void initImage() {
        Preview preview = Preview.get(getActivity());
        Palette palette = preview.getPalette(file, stat, constraint, false);
        if (palette != null) {
            setBackground(palette);
        }

        Bitmap thumbnail = preview.getThumbnail(file, stat, constraint, true);
        if (thumbnail != null) {
            image.setImageBitmap(thumbnail);

        } else {
            Rect size = preview.getSize(file, stat, constraint, false);
            if (size != null) {
                setImageViewMinSize(size);
            }
            preview.get(file, stat, constraint, this);
        }
    }

    private String formatSize(long size) {
        return formatFileSize(getActivity(), size);
    }

    private String formatSizeCount(long size, int count) {
        return getResources().getQuantityString(
                R.plurals.x_size_y_items, count, formatSize(size), count);
    }

    private String formatSizeOnDisk(long size) {
        return getString(R.string.x_size_on_disk, formatSize(size));
    }

    private String formatDate() {
        long millis = stat.lastModifiedTime().to(MILLISECONDS);
        int flags = FORMAT_SHOW_DATE | FORMAT_SHOW_TIME;
        return formatDateTime(getActivity(), millis, flags);
    }

    private CalculateSizeLoader sizeLoader() {
        if (getActivity() == null) {
            return null;
        }
        Loader<?> loader = getLoaderManager().getLoader(0);
        return (CalculateSizeLoader) loader;
    }

    private Rect scaleSize(Rect size) {

        // TODO scale up too for small pics to avoid jumping

        boolean tooBig = size.width() > constraint.width()
                || size.height() > constraint.height();

        return tooBig
                ? size.scale(constraint)
                : size;

    }

    @Override
    public void onSizeAvailable(File file, Stat stat, Rect size) {
        setImageViewMinSize(size);
    }

    private void setImageViewMinSize(Rect size) {
        Rect scaled = scaleSize(size);
        setImageViewMinSize(scaled.width(), scaled.height());
    }

    private void setImageViewMinSize(int width, int height) {
        image.setMinimumWidth(width);
        image.setMinimumHeight(height);
    }

    @Override
    public void onPaletteAvailable(File file, Stat stat, Palette palette) {
        setBackground(palette);
    }

    private void setBackground(Palette palette) {
        root.setBackgroundColor(darkColor(palette, TRANSPARENT));
    }

    @Override
    public void onPreviewAvailable(File file, Stat stat, Bitmap thumbnail) {
        image.setImageBitmap(thumbnail);
        image.setAlpha(0F);
        image.animate().alpha(1).setDuration(animationDuration());
    }

    private int animationDuration() {
        return getResources().getInteger(android.R.integer.config_mediumAnimTime);
    }

    @Override
    public void onPreviewFailed(File file, Stat stat) {
        image.setVisibility(GONE);
    }

    @Override
    public Loader<Size> onCreateLoader(int id, Bundle args) {
        return new CalculateSizeLoader(getActivity(), file);
    }

    @Override
    public void onLoadFinished(Loader<Size> loader, Size data) {
        calculatingSize.setVisibility(GONE);
        size.setText(formatSizeCount(data.size(), data.count()));
        sizeOnDisk.setText(formatSizeOnDisk(data.sizeOnDisk()));
    }

    @Override
    public void onLoaderReset(Loader<Size> loader) {
    }

}
