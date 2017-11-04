package l.files.ui.info;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import l.files.fs.Name;
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
import static l.files.base.Objects.requireNonNull;

public final class InfoFragment
        extends InfoBaseFragment
        implements Preview.Callback {

    private static final String ARG_STAT = "stat";

    public static InfoFragment create(Path path, @Nullable Stat stat) {
        requireNonNull(path);
        return newFragment(newArgs(path, stat));
    }

    private static Bundle newArgs(Path path, @Nullable Stat stat) {
        ArrayList<Name> names = new ArrayList<>(singleton(path.name()));
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(ARG_CHILDREN, names);
        bundle.putParcelable(ARG_PARENT_DIRECTORY, path.parent());
        bundle.putParcelable(ARG_STAT, stat);
        return bundle;
    }

    private static InfoFragment newFragment(Bundle bundle) {
        InfoFragment fragment = new InfoFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    private Stat stat;
    private Path path;

    private Rect constraint;
    private View backgroundView;
    private TextView nameView;
    private TextView lastModifiedView;
    private ImageView thumbnailView;

    public CharSequence getDisplayedName() {
        return nameView.getText();
    }

    public CharSequence getDisplayedLastModifiedTime() {
        return lastModifiedView.getText();
    }

    @Override
    int layoutResourceId() {
        return R.layout.info_fragment;
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        findArgs(getArguments());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateViews();
    }

    private void findViews(View view) {
        backgroundView = view.findViewById(R.id.root);
        nameView = view.findViewById(R.id.name);
        thumbnailView = view.findViewById(R.id.image);
        lastModifiedView = view.findViewById(R.id.modified);
        constraint = calculateConstraint();
    }

    private void findArgs(Bundle args) {
        path = getParentDirectory().concat(getChildren().get(0));
        stat = args.getParcelable(ARG_STAT);
    }

    private void updateViews() {
        updateNameView(path);
        if (stat != null) {
            updateBackgroundView(path, stat);
            updateThumbnailView(path, stat);
            updateLastModifiedView(stat);
        }
    }

    private void updateNameView(Path path) {
        Name name = path.name();
        nameView.setMaxWidth(constraint.width());
        nameView.setText(name != null
                ? name.toString()
                : path.toString()
        );
    }

    @Override
    String formatSize(long size, int count) {
        if (stat == null || stat.isDirectory()) {
            return super.formatSize(size, count);
        } else {
            return formatSize(size);
        }
    }

    private void updateLastModifiedView(Stat stat) {
        long millis = stat.lastModifiedTime().to(MILLISECONDS);
        int flags = FORMAT_SHOW_DATE | FORMAT_SHOW_TIME;
        String text = formatDateTime(getActivity(), millis, flags);
        lastModifiedView.setText(text);
    }

    private Rect calculateConstraint() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = (int) (metrics.widthPixels * 0.75);
        int height = metrics.heightPixels;
        return Rect.of(width, height);
    }

    private void updateBackgroundView(Path file, Stat stat) {
        Preview preview = Preview.get(getActivity());
        Bitmap bg = preview.getBlurredThumbnail(file, stat, constraint, true);
        if (bg != null) {
            updateBackgroundView(bg);
        }
    }

    private void updateBackgroundView(Bitmap bitmap) {
        Drawable bg = new BitmapDrawable(getResources(), bitmap);
        bg.setAlpha((int) (0.3f * 255));
        backgroundView.setBackground(bg);
    }

    private void updateThumbnailView(Path file, Stat stat) {
        Preview preview = Preview.get(getActivity());

        Bitmap thumbnail = preview.getThumbnail(file, stat, constraint, true);
        if (thumbnail != null) {
            thumbnailView.setImageBitmap(thumbnail);
            return;
        }

        Rect size = preview.getSize(file, stat, constraint, false);
        if (size != null) {
            setImageViewMinSize(size);
        }
        preview.get(file, stat, constraint, this, getContext());

    }

    private Rect scaleSize(Rect size) {
        return size.scaleDown(constraint);
    }

    private void setImageViewMinSize(Rect size) {
        Rect scaled = scaleSize(size);
        thumbnailView.setMinimumWidth(scaled.width());
        thumbnailView.setMinimumHeight(scaled.height());
    }

    @Override
    public void onPreviewAvailable(Path file, Stat stat, Bitmap thumbnail) {
        showImageView(thumbnail);
    }

    private void showImageView(Bitmap thumbnail) {
        thumbnailView.setImageBitmap(thumbnail);
        thumbnailView.setAlpha(0F);
        thumbnailView.animate().alpha(1).setDuration(animationDuration());
    }

    private int animationDuration() {
        int id = android.R.integer.config_mediumAnimTime;
        return getResources().getInteger(id);
    }

    @Override
    public void onBlurredThumbnailAvailable(Path path, Stat stat, Bitmap bm) {
    }

    @Override
    public void onPreviewFailed(Path path, Stat stat, Object cause) {
        hindImageView();
    }

    private void hindImageView() {
        thumbnailView.setImageDrawable(null);
        thumbnailView.setVisibility(GONE);
    }

}
