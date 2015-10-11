package l.files.ui.browser;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import l.files.R;
import l.files.common.view.ActionModeProvider;
import l.files.fs.File;
import l.files.fs.Stat;
import l.files.ui.Icons;
import l.files.ui.StableAdapter;
import l.files.ui.browser.FileListItem.Header;
import l.files.ui.mode.Selectable;
import l.files.preview.Decode;
import l.files.preview.Preview;
import l.files.preview.PreviewCallback;
import l.files.preview.Rect;
import l.files.preview.SizedColorDrawable;
import l.files.ui.selection.Selection;
import l.files.ui.selection.SelectionModeViewHolder;

import static android.R.attr.textColorPrimary;
import static android.R.attr.textColorPrimaryInverse;
import static android.R.attr.textColorTertiary;
import static android.R.attr.textColorTertiaryInverse;
import static android.R.integer.config_shortAnimTime;
import static android.graphics.Color.TRANSPARENT;
import static android.graphics.Typeface.BOLD;
import static android.graphics.Typeface.SANS_SERIF;
import static android.text.format.DateFormat.getDateFormat;
import static android.text.format.DateFormat.getTimeFormat;
import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
import static android.text.format.DateUtils.FORMAT_NO_YEAR;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.formatDateRange;
import static android.text.format.Formatter.formatShortFileSize;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.lang.System.currentTimeMillis;
import static java.util.Calendar.DAY_OF_YEAR;
import static java.util.Calendar.YEAR;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static l.files.R.dimen.files_item_card_inner_space;
import static l.files.R.dimen.files_item_space_horizontal;
import static l.files.R.dimen.files_list_space;
import static l.files.R.integer.files_grid_columns;
import static l.files.R.layout.files_grid_header;
import static l.files.R.layout.files_grid_item;
import static l.files.common.content.res.Styles.getColorStateList;
import static l.files.common.view.Views.find;
import static l.files.ui.Icons.defaultDirectoryIconStringId;
import static l.files.ui.Icons.defaultFileIconStringId;
import static l.files.ui.Icons.fileIconStringId;

final class FilesAdapter extends StableAdapter<FileListItem, ViewHolder>
        implements Selectable {

    private final Preview decorator;
    private final DateFormatter formatter;
    private final ActionModeProvider actionModeProvider;
    private final ActionMode.Callback actionModeCallback;
    private final Selection<File> selection;
    private final OnOpenFileListener listener;
    private final Rect constraint;

    FilesAdapter(
            Context context,
            Selection<File> selection,
            ActionModeProvider actionModeProvider,
            ActionMode.Callback actionModeCallback,
            OnOpenFileListener listener) {

        this.actionModeProvider = requireNonNull(actionModeProvider);
        this.actionModeCallback = requireNonNull(actionModeCallback);
        this.listener = requireNonNull(listener);
        this.selection = requireNonNull(selection);
        this.formatter = new DateFormatter(context);

        Resources res = context.getResources();
        DisplayMetrics metrics = res.getDisplayMetrics();
        int columns = res.getInteger(files_grid_columns);
        int maxThumbnailWidth = (int) (((float) metrics.widthPixels)
                - res.getDimension(files_item_space_horizontal) * columns * 2
                - res.getDimension(files_item_card_inner_space) * columns * 2
                - res.getDimension(files_list_space) * 2) / columns;
        int maxThumbnailHeight = (int) (metrics.heightPixels * 1.5);
        this.constraint = Rect.of(maxThumbnailWidth, maxThumbnailHeight);
        this.decorator = Preview.get(context);
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isFile() ? 0 : 1;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        return viewType == 0
                ? new FileHolder(inflater.inflate(files_grid_item, parent, false))
                : new HeaderHolder(inflater.inflate(files_grid_header, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FileListItem item = getItem(position);
        if (item.isHeader()) {
            ((HeaderHolder) holder).bind((Header) item);
        } else {
            ((FileHolder) holder).bind((FileListItem.File) item);
        }
    }

    @Override
    public Object getItemIdObject(int position) {
        FileListItem item = getItem(position);
        if (item instanceof FileListItem.File) {
            return ((FileListItem.File) item).file();
        }
        return item;
    }

    @Override
    public void selectAll() {
        List<FileListItem> items = items();
        List<File> files = new ArrayList<>(items.size());
        for (FileListItem item : items) {
            if (item.isFile()) {
                files.add(((FileListItem.File) item).file());
            }
        }
        selection.addAll(files);
    }

    static class DateFormatter {

        final Context context;

        final DateFormat dateFormat;
        final DateFormat timeFormat;

        static final Date tempDate = new Date();
        static final StringBuffer tempBuffer = new StringBuffer();
        static final FieldPosition tempField = new FieldPosition(0);

        final Formatter tempFormatter = new Formatter(tempBuffer, Locale.getDefault());

        static final Calendar currentTime = Calendar.getInstance();
        static final Calendar thatTime = Calendar.getInstance();

        static final int flags
                = FORMAT_SHOW_DATE
                | FORMAT_ABBREV_MONTH
                | FORMAT_NO_YEAR;

        DateFormatter(Context context) {
            this.context = requireNonNull(context, "context");
            this.dateFormat = getDateFormat(context);
            this.timeFormat = getTimeFormat(context);
        }

        String apply(Stat file) {
            long millis = file.lastModifiedTime().to(MILLISECONDS);

            tempDate.setTime(millis);
            tempField.setBeginIndex(0);
            tempField.setEndIndex(0);
            tempBuffer.setLength(0);

            thatTime.setTimeInMillis(millis);
            currentTime.setTimeInMillis(currentTimeMillis());

            if (currentTime.get(YEAR) == thatTime.get(YEAR)) {
                if (currentTime.get(DAY_OF_YEAR) == thatTime.get(DAY_OF_YEAR)) {
                    return timeFormat.format(tempDate, tempBuffer, tempField).toString();
                } else {
                    return formatDateRange(context, tempFormatter, millis, millis, flags).toString();
                }
            }

            return dateFormat.format(tempDate, tempBuffer, tempField).toString();
        }
    }

    final class FileHolder extends SelectionModeViewHolder<File, FileListItem.File>
            implements PreviewCallback {

        private final TextView icon;
        private final View iconContainer;
        private final TextView title;
        private final TextView summary;
        private final TextView symlink;
        private final ImageView preview;
        private final CardView previewContainer;
        private final View previewContainerSpaceTop;
        private final View paletteContainer;

        private final int animateDuration;

        private final ColorStateList primaryText;
        private final ColorStateList primaryTextInverse;
        private final ColorStateList tertiaryText;
        private final ColorStateList tertiaryTextInverse;

        private Decode task;

        FileHolder(View itemView) {
            super(itemView, selection, actionModeProvider, actionModeCallback);
            this.paletteContainer = find(R.id.palette, this);
            this.icon = find(R.id.icon, this);
            this.iconContainer = find(R.id.icon_container, this);
            this.title = find(R.id.title, this);
            this.summary = find(R.id.summary, this);
            this.symlink = find(R.id.symlink, this);
            this.preview = find(R.id.preview, this);
            this.previewContainer = find(R.id.preview_container, this);
            this.previewContainerSpaceTop = find(R.id.preview_container_space_top, this);
            this.itemView.setOnClickListener(this);
            this.itemView.setOnLongClickListener(this);
            this.animateDuration = itemView.getResources().getInteger(config_shortAnimTime);

            Context context = itemView.getContext();
            this.primaryText = getColorStateList(textColorPrimary, context);
            this.primaryTextInverse = getColorStateList(textColorPrimaryInverse, context);
            this.tertiaryText = getColorStateList(textColorTertiary, context);
            this.tertiaryTextInverse = getColorStateList(textColorTertiaryInverse, context);
        }

        @Override
        protected File itemId(FileListItem.File file) {
            return file.file();
        }

        @Override
        protected void onClick(View v, FileListItem.File file) {
            listener.onOpen(file.file(), file.stat());
        }

        @Override
        public void bind(FileListItem.File file) {
            super.bind(file);
            setTitle(file);
            setIcon(file);
            setSymlink(file);
            setSummary(file);
            setPreview(file);
        }

        private void setTitle(FileListItem.File file) {
            title.setText(file.file().name());
            title.setEnabled(file.stat() != null && file.isReadable());
        }

        private void setIcon(FileListItem.File file) {
            icon.setEnabled(file.targetStat() != null && file.isReadable());

            if (file.targetStat() != null
                    && setLocalIcon(icon, file.targetStat())) {
                icon.setTypeface(SANS_SERIF, BOLD);
            } else {
                icon.setText(iconTextId(file));
                icon.setTypeface(Icons.font(icon.getResources().getAssets()));
            }
        }

        private boolean setLocalIcon(TextView icon, Stat stat) {
            // TODO
            if (stat.isBlockDevice()) {
                icon.setText("B");
            } else if (stat.isCharacterDevice()) {
                icon.setText("C");
            } else if (stat.isSocket()) {
                icon.setText("S");
            } else if (stat.isFifo()) {
                icon.setText("P");
            } else {
                return false;
            }
            return true;
        }

        private int iconTextId(FileListItem.File file) {
            Stat stat = file.targetStat();
            if (stat == null) {
                return defaultFileIconStringId();
            }

            if (stat.isDirectory()) {
                return defaultDirectoryIconStringId();
            } else {
                return fileIconStringId(file.basicMediaType());
            }
        }

        private void setSummary(FileListItem.File file) {
            Stat stat = file.stat();
            if (stat == null) {
                summary.setText("");
                summary.setVisibility(GONE);
            } else {
                summary.setVisibility(VISIBLE);
                summary.setEnabled(file.isReadable());
                CharSequence date = formatter.apply(stat);
                CharSequence size = formatShortFileSize(summary.getContext(), stat.size());
                boolean hasDate = stat.lastModifiedTime().to(MINUTES) > 0;
                boolean isFile = stat.isRegularFile();
                if (hasDate && isFile) {
                    Context context = summary.getContext();
                    summary.setText(context.getString(R.string.x_dot_y, date, size));
                } else if (hasDate) {
                    summary.setText(date);
                } else if (isFile) {
                    summary.setText(size);
                } else {
                    summary.setVisibility(GONE);
                }
            }
        }

        private void setPreview(FileListItem.File file) {
            previewContainerSpaceTop.setVisibility(GONE);

            if (task != null) {
                task.cancelAll();
            }

            File res = file.file();
            Stat stat = file.stat();
            if (stat == null || !decorator.isPreviewable(res, stat, constraint)) {
                preview.setImageDrawable(null);
                showPreviewContainer(false);
                updatePaletteColor(TRANSPARENT);
                return;
            }

            Palette palette = decorator.getPalette(res, stat, constraint);
            if (palette != null) {
                updatePaletteColor(backgroundColor(palette));
            } else {
                updatePaletteColor(TRANSPARENT);
            }

            Bitmap thumbnail = decorator.getThumbnail(res, stat, constraint);
            if (thumbnail != null) {
                setPreviewImage(thumbnail);
                showPreviewContainer(true);
                return;
            }

            Rect size = decorator.getSize(res, stat, constraint);
            if (size != null) {
                setPreviewImage(newSizedColorDrawable(size));
                onSizeAvailable(file.file(), size);
                showPreviewContainer(true);
            } else {
                setPreviewImage((Drawable) null);
                showPreviewContainer(false);
            }

            task = decorator.set(res, stat, constraint, this);
        }

        private SizedColorDrawable newSizedColorDrawable(Rect size) {

            boolean tooBig = size.width() > constraint.width()
                    || size.height() > constraint.height();

            Rect scaledDown = tooBig
                    ? size.scale(constraint)
                    : size;

            return new SizedColorDrawable(TRANSPARENT, scaledDown);

        }

        private void showPreviewContainer(boolean show) {
            if (show) {
                previewContainer.setVisibility(VISIBLE);
                iconContainer.setVisibility(GONE);
            } else {
                previewContainer.setVisibility(GONE);
                iconContainer.setVisibility(VISIBLE);
            }
        }

        private void updatePaletteColor(int color) {
            paletteContainer.setBackgroundColor(color);
            if (color == TRANSPARENT) {
                title.setTextColor(primaryText);
                icon.setTextColor(tertiaryText);
                summary.setTextColor(tertiaryText);
                symlink.setTextColor(tertiaryText);
            } else {
                title.setTextColor(primaryTextInverse);
                icon.setTextColor(tertiaryTextInverse);
                summary.setTextColor(tertiaryTextInverse);
                symlink.setTextColor(tertiaryTextInverse);
            }
        }

        private void setSymlink(FileListItem.File file) {
            Stat stat = file.stat();
            if (stat == null || !stat.isSymbolicLink()) {
                symlink.setVisibility(GONE);
            } else {
                symlink.setEnabled(file.isReadable());
                symlink.setVisibility(VISIBLE);
            }
        }

        @Override
        public void onSizeAvailable(File item, Rect size) {
            if (Objects.equals(item, itemId())) {
                setPreviewImage(newSizedColorDrawable(size));
                showPreviewContainer(true);
            }
        }

        @Override
        public void onPaletteAvailable(File item, Palette palette) {
            if (Objects.equals(item, itemId())) {
                int color = backgroundColor(palette);
                updatePaletteColor(color);
                if (color != TRANSPARENT) {
                    paletteContainer.setAlpha(0);
                    paletteContainer.animate().alpha(1).setDuration(animateDuration);
                }
            }
        }

        private int backgroundColor(Palette palette) {
            int color = palette.getDarkVibrantColor(TRANSPARENT);
            if (color == TRANSPARENT) {
                color = palette.getDarkMutedColor(TRANSPARENT);
            }
            return color;
        }

        @Override
        public void onPreviewAvailable(File item, Bitmap thumbnail) {
            if (Objects.equals(item, itemId())) {
                showPreviewContainer(true);
                setPreviewImage(thumbnail);
                preview.setAlpha(0f);
                preview.animate().alpha(1).setDuration(animateDuration);
            }
        }

        private void setPreviewImage(Drawable drawable) {
            preview.setImageDrawable(drawable);
            if (drawable != null) {
                boolean small = drawable.getIntrinsicWidth() < constraint.width();
                previewContainerSpaceTop.setVisibility(small ? VISIBLE : GONE);
            }
        }

        private Resources resources() {
            return itemView.getResources();
        }

        private void setPreviewImage(Bitmap bitmap) {
            if (bitmap != null) {
                setPreviewImage(new BitmapDrawable(resources(), bitmap));
            } else {
                setPreviewImage((Drawable) null);
            }
        }

        @Override
        public void onPreviewFailed(File item) {
            if (Objects.equals(item, itemId())) {
                showPreviewContainer(false);
            }
        }
    }

    final class HeaderHolder extends ViewHolder {
        private TextView title;

        HeaderHolder(View itemView) {
            super(itemView);
            title = find(android.R.id.title, this);
        }

        void bind(Header header) {
            title.setText(header.toString());
            LayoutParams params = itemView.getLayoutParams();
            if (params instanceof StaggeredGridLayoutManager.LayoutParams) {
                ((StaggeredGridLayoutManager.LayoutParams) params).setFullSpan(true);
            }
        }
    }

}
