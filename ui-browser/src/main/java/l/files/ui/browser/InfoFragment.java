package l.files.ui.browser;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import l.files.fs.File;
import l.files.fs.Stat;
import l.files.ui.preview.Preview;
import l.files.ui.preview.PreviewCallback;
import l.files.ui.preview.Rect;

import static android.graphics.Color.TRANSPARENT;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_TIME;
import static android.text.format.DateUtils.formatDateTime;
import static android.view.View.GONE;
import static java.util.Collections.singleton;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.ui.base.view.Views.find;
import static l.files.ui.preview.Preview.darkColor;

public final class InfoFragment
        extends InfoBaseFragment
        implements PreviewCallback {

    private static final String ARG_STAT = "stat";

    public static InfoFragment create(File file, Stat stat) {

        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_DIR, file.parent());
        bundle.putParcelableArrayList(ARG_CHILDREN, new ArrayList<>(singleton(file.name())));
        bundle.putParcelable(ARG_STAT, stat);

        InfoFragment fragment = new InfoFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private Rect constraint;
    private View root;
    private ImageView image;

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
        image = find(R.id.image, this);

        File file = dir.resolve(children.get(0));
        Stat stat = getArguments().getParcelable(ARG_STAT);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        constraint = Rect.of((int) (metrics.widthPixels * 0.75), metrics.heightPixels);

        TextView name = find(R.id.name, this);
        name.setMaxWidth(constraint.width());
        name.setText(file.name().toString());

        TextView date = find(R.id.modified, this);
        if (stat != null) {

            initImage(file, stat);

            date.setText(formatDate(stat));
            size.setText(formatSize(stat.size()));
            sizeOnDisk.setText(formatSizeOnDisk(stat.sizeOnDisk()));

            if (stat.isDirectory() || stat.isSymbolicLink()) {
                initLoader();
            }

        } else {
            date.setText(R.string.__);
            size.setText(R.string.__);
        }
    }

    private void initImage(File file, Stat stat) {
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

    private String formatDate(Stat stat) {
        long millis = stat.lastModifiedTime().to(MILLISECONDS);
        int flags = FORMAT_SHOW_DATE | FORMAT_SHOW_TIME;
        return formatDateTime(getActivity(), millis, flags);
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

}
