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

import javax.annotation.Nullable;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.graphics.Rect;
import l.files.ui.preview.Preview;

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

    public static InfoFragment create(Path path, @Nullable Stat stat) {

        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_DIR, path.parent());
        bundle.putParcelableArrayList(ARG_CHILDREN, new ArrayList<>(singleton(path.name())));
        bundle.putParcelable(ARG_STAT, stat);

        InfoFragment fragment = new InfoFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    private Rect constraint;

    @Nullable
    private View root;

    @Nullable
    private TextView name;

    @Nullable
    private TextView date;

    @Nullable
    private ImageView image;

    public TextView getNameView() {
        assert name != null;
        return name;
    }

    public TextView getDateView() {
        assert date != null;
        return date;
    }

    private View getRootView() {
        assert root != null;
        return root;
    }

    private ImageView getImageView() {
        assert image != null;
        return image;
    }

    private Rect getConstraint() {
        assert constraint != null;
        return constraint;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.info_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        root = find(R.id.root, this);
        image = find(R.id.image, this);

        Path file = getDirectory().concat(getChildren().get(0).toPath());
        Stat stat = getArguments().getParcelable(ARG_STAT);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        constraint = Rect.of((int) (metrics.widthPixels * 0.75), metrics.heightPixels);

        name = find(R.id.name, this);
        name.setMaxWidth(constraint.width());
        name.setText(String.valueOf(file.getName().orObject(file)));

        date = find(R.id.modified, this);
        if (stat != null) {

            initImage(file, stat);

            date.setText(formatDate(stat));
            getSizeView().setText(formatSize(stat.size()));
            getSizeOnDiskView().setText(formatSizeOnDisk(stat.sizeOnDisk()));

            if (stat.isDirectory()) {
                initLoader();
            }

        } else {
            date.setText(R.string.__);
            getSizeOnDiskView().setText(R.string.__);
        }
    }

    private void initImage(Path file, Stat stat) {
        Preview preview = Preview.get(getActivity());
        Rect constraint = getConstraint();
        Bitmap blurred = preview.getBlurredThumbnail(file, stat, constraint, true);
        if (blurred != null) {
            setBlurBackground(blurred);
        }

        Bitmap thumbnail = preview.getThumbnail(file, stat, constraint, true);
        if (thumbnail != null) {
            getImageView().setImageBitmap(thumbnail);

        } else {
            Rect size = preview.getSize(file, stat, constraint, false);
            if (size != null) {
                setImageViewMinSize(size);
            }
            preview.get(file, stat, constraint, this, getContext());
        }
    }

    private void setBlurBackground(Bitmap bitmap) {
        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
        drawable.setAlpha((int) (0.3f * 255));
        getRootView().setBackground(drawable);
    }

    private String formatDate(Stat stat) {
        long millis = stat.lastModifiedTime().to(MILLISECONDS);
        int flags = FORMAT_SHOW_DATE | FORMAT_SHOW_TIME;
        return formatDateTime(getActivity(), millis, flags);
    }

    private Rect scaleSize(Rect size) {
        return size.scaleDown(getConstraint());
    }

    private void setImageViewMinSize(Rect size) {
        Rect scaled = scaleSize(size);
        setImageViewMinSize(scaled.width(), scaled.height());
    }

    private void setImageViewMinSize(int width, int height) {
        getImageView().setMinimumWidth(width);
        getImageView().setMinimumHeight(height);
    }

    @Override
    public void onPreviewAvailable(Path file, Stat stat, Bitmap thumbnail) {
        getImageView().setImageBitmap(thumbnail);
        getImageView().setAlpha(0F);
        getImageView().animate().alpha(1).setDuration(animationDuration());
    }

    @Override
    public void onBlurredThumbnailAvailable(Path path, Stat stat, Bitmap thumbnail) {
    }

    private int animationDuration() {
        return getResources().getInteger(android.R.integer.config_mediumAnimTime);
    }

    @Override
    public void onPreviewFailed(Path path, Stat stat, Object cause) {
        getImageView().setVisibility(GONE);
    }

}
