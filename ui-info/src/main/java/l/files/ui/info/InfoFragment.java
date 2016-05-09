package l.files.ui.info;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.preview.Preview;
import l.files.ui.preview.Preview.Using;
import l.files.ui.preview.Rect;

import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_TIME;
import static android.text.format.DateUtils.formatDateTime;
import static android.view.View.GONE;
import static java.util.Collections.singleton;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.ui.base.view.Views.find;

public final class InfoFragment
        extends InfoBaseFragment
        implements Preview.Callback {

    private static final String ARG_STAT = "stat";

    public static InfoFragment create(Path path, Stat stat) {

        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_DIR, path.parent());
        bundle.putParcelableArrayList(ARG_CHILDREN, new ArrayList<>(singleton(path.name())));
        bundle.putParcelable(ARG_STAT, stat);

        InfoFragment fragment = new InfoFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private Rect constraint;
    private View root;
    private TextView name;
    private TextView date;
    private ImageView image;

    public TextView getNameView() {
        return name;
    }

    public TextView getDateView() {
        return date;
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
        image = find(R.id.image, this);

        Path file = dir.resolve(children.get(0));
        Stat stat = getArguments().getParcelable(ARG_STAT);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        constraint = Rect.of((int) (metrics.widthPixels * 0.75), metrics.heightPixels);

        name = find(R.id.name, this);
        name.setMaxWidth(constraint.width());
        name.setText(file.name().toString());

        date = find(R.id.modified, this);
        if (stat != null) {

            initImage(file, stat);

            date.setText(formatDate(stat));
            size.setText(formatSize(stat.size()));
            sizeOnDisk.setText(formatSizeOnDisk(stat.sizeOnDisk()));

            if (stat.isDirectory()) {
                initLoader();
            }

        } else {
            date.setText(R.string.__);
            size.setText(R.string.__);
        }
    }

    private void initImage(Path file, Stat stat) {
        Preview preview = Preview.get(getActivity());
        Bitmap blurred = preview.getBlurredThumbnail(file, stat, constraint, true);
        if (blurred != null) {
            setBlurBackground(blurred);
        }

        Bitmap thumbnail = preview.getThumbnail(file, stat, constraint, true);
        if (thumbnail != null) {
            if (blurred == null) {
                // TODO
            }
            image.setImageBitmap(thumbnail);

        } else {
            Rect size = preview.getSize(file, stat, constraint, false);
            if (size != null) {
                setImageViewMinSize(size);
            }
            preview.get(file, stat, constraint, this, Using.MEDIA_TYPE);
        }
    }

    private void setBlurBackground(Bitmap bitmap) {
        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
        drawable.setAlpha((int) (0.3f * 255));
        root.setBackground(drawable);
    }

    private String formatDate(Stat stat) {
        long millis = stat.lastModifiedTime().to(MILLISECONDS);
        int flags = FORMAT_SHOW_DATE | FORMAT_SHOW_TIME;
        return formatDateTime(getActivity(), millis, flags);
    }

    private Rect scaleSize(Rect size) {
        return size.scale(constraint);
    }

    @Override
    public void onSizeAvailable(Path file, Stat stat, Rect size) {
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
    public void onPreviewAvailable(Path file, Stat stat, Bitmap thumbnail) {
        image.setImageBitmap(thumbnail);
        image.setAlpha(0F);
        image.animate().alpha(1).setDuration(animationDuration());
    }

    @Override
    public void onBlurredThumbnailAvailable(Path path, Stat stat, Bitmap thumbnail) {
        // TODO
    }

    private int animationDuration() {
        return getResources().getInteger(android.R.integer.config_mediumAnimTime);
    }

    @Override
    public void onPreviewFailed(Path file, Stat stat, Using used) {
        image.setVisibility(GONE);
    }

}
