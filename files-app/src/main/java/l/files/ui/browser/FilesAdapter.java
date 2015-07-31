package l.files.ui.browser;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import l.files.R;
import l.files.common.graphics.Rect;
import l.files.common.graphics.drawable.SizedColorDrawable;
import l.files.common.view.ActionModeProvider;
import l.files.fs.Instant;
import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.ui.Icons;
import l.files.ui.StableAdapter;
import l.files.ui.browser.FileListItem.File;
import l.files.ui.browser.FileListItem.Header;
import l.files.ui.mode.Selectable;
import l.files.ui.preview.Decode;
import l.files.ui.preview.Preview;
import l.files.ui.preview.PreviewCallback;
import l.files.ui.selection.Selection;
import l.files.ui.selection.SelectionModeViewHolder;

import static android.R.integer.config_mediumAnimTime;
import static android.graphics.Color.TRANSPARENT;
import static android.graphics.Typeface.BOLD;
import static android.graphics.Typeface.SANS_SERIF;
import static android.text.format.DateFormat.getDateFormat;
import static android.text.format.DateFormat.getTimeFormat;
import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
import static android.text.format.DateUtils.FORMAT_NO_YEAR;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.formatDateTime;
import static android.text.format.Formatter.formatShortFileSize;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static java.text.DateFormat.MEDIUM;
import static java.text.DateFormat.getDateTimeInstance;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static l.files.R.dimen.files_item_space_horizontal;
import static l.files.R.dimen.files_list_space;
import static l.files.R.integer.files_grid_columns;
import static l.files.R.layout.files_grid_item;
import static l.files.R.layout.files_grid_header;
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
  private final Selection<Resource> selection;
  private final OnOpenFileListener listener;
  private final Rect constraint;

  FilesAdapter(
      Context context,
      Selection<Resource> selection,
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
        - res.getDimension(files_list_space) * 2) / columns;
    int maxThumbnailHeight = (int) (metrics.heightPixels * 1.5);
    this.constraint = Rect.of(maxThumbnailWidth, maxThumbnailHeight);
    this.decorator = Preview.get(context);
  }

  @Override public int getItemViewType(int position) {
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

  @Override public void onBindViewHolder(ViewHolder holder, int position) {
    FileListItem item = getItem(position);
    if (item.isHeader()) {
      ((HeaderHolder) holder).bind((Header) item);
    } else {
      ((FileHolder) holder).bind((File) item);
    }
  }

  @Override public Object getItemIdObject(int position) {
    FileListItem item = getItem(position);
    if (item instanceof File) {
      return ((File) item).resource();
    }
    return item;
  }

  @Override public void selectAll() {
    List<FileListItem> items = items();
    List<Resource> resources = new ArrayList<>(items.size());
    for (FileListItem item : items) {
      if (item.isFile()) {
        resources.add(((File) item).resource());
      }
    }
    selection.addAll(resources);
  }

  static class DateFormatter {
    Context context;
    DateFormat futureFormat;
    DateFormat dateFormat;
    DateFormat timeFormat;
    Date date = new Date();
    Time currentTime = new Time();
    Time thatTime = new Time();
    int flags
        = FORMAT_SHOW_DATE
        | FORMAT_ABBREV_MONTH
        | FORMAT_NO_YEAR;

    DateFormatter(Context context) {
      this.context = requireNonNull(context, "context");
      this.futureFormat = getDateTimeInstance(MEDIUM, MEDIUM);
      this.dateFormat = getDateFormat(context);
      this.timeFormat = getTimeFormat(context);
    }

    CharSequence apply(Stat file) {
      Instant instant = file.mtime();
      long millis = instant.to(MILLISECONDS);
      date.setTime(millis);
      currentTime.setToNow();
      thatTime.set(millis);
      if (currentTime.before(thatTime)) {
        return futureFormat.format(date);
      }
      if (currentTime.year == thatTime.year) {
        return currentTime.yearDay == thatTime.yearDay
            ? timeFormat.format(date)
            : formatDateTime(context, millis, flags);
      }
      return dateFormat.format(date);
    }
  }

  final class FileHolder extends SelectionModeViewHolder<Resource, File>
      implements PreviewCallback {

    private final TextView icon;
    private final TextView title;
    private final TextView summary;
    private final TextView symlink;
    private final ImageView preview;
    private final View container;

    private final int animateDuration;

    private Decode task;

    FileHolder(View itemView) {
      super(itemView, selection, actionModeProvider, actionModeCallback);
      this.container = find(R.id.container, this);
      this.icon = find(R.id.icon, this);
      this.title = find(R.id.title, this);
      this.summary = find(R.id.summary, this);
      this.symlink = find(R.id.symlink, this);
      this.preview = find(R.id.preview, this);
      this.itemView.setOnClickListener(this);
      this.itemView.setOnLongClickListener(this);
      this.animateDuration = itemView.getResources()
          .getInteger(config_mediumAnimTime);
    }

    @Override protected Resource itemId(File file) {
      return file.resource();
    }

    @Override protected void onClick(View v, File file) {
      listener.onOpen(file.resource());
    }

    @Override public void bind(File file) {
      super.bind(file);
      setTitle(file);
      setIcon(file);
      setSymlink(file);
      setSummary(file);
      setPreview(file);
    }

    private void setTitle(File file) {
      title.setText(file.resource().name());
      title.setEnabled(file.stat() != null && file.isReadable());
    }

    private void setIcon(File file) {
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

    private int iconTextId(File file) {
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

    private void setSummary(File file) {
      Stat stat = file.stat();
      if (stat == null) {
        summary.setText("");
        summary.setVisibility(INVISIBLE);
      } else {
        summary.setVisibility(VISIBLE);
        summary.setEnabled(file.isReadable());
        CharSequence date = formatter.apply(stat);
        CharSequence size = formatShortFileSize(summary.getContext(), stat.size());
        boolean hasDate = stat.mtime().to(MINUTES) > 0;
        boolean isFile = stat.isRegularFile();
        if (hasDate && isFile) {
          Context context = summary.getContext();
          summary.setText(context.getString(R.string.x_dot_y, date, size));
        } else if (hasDate) {
          summary.setText(date);
        } else if (isFile) {
          summary.setText(size);
        } else {
          summary.setVisibility(INVISIBLE);
        }
      }
    }

    private void setPreview(File file) {
      if (task != null) {
        task.cancelAll();
      }

      Resource res = file.resource();
      Stat stat = file.stat();
      if (stat == null || !decorator.isPreviewable(res, stat, constraint)) {
        preview.setImageDrawable(null);
        preview.setVisibility(GONE);
        container.setBackgroundColor(TRANSPARENT);
        return;
      }

      Palette palette = decorator.getPalette(res, stat, constraint);
      if (palette != null) {
        container.setBackgroundColor(backgroundColor(palette));
      } else {
        container.setBackgroundColor(TRANSPARENT);
      }

      Bitmap bitmap = decorator.getBitmap(res, stat, constraint);
      if (bitmap != null) {
        preview.setImageBitmap(bitmap);
        preview.setVisibility(VISIBLE);
        return;
      }

      Rect size = decorator.getSize(res, stat, constraint);
      if (size != null) {
        preview.setImageDrawable(
            new SizedColorDrawable(TRANSPARENT, size.scale(constraint)));
        preview.setVisibility(VISIBLE);
      } else {
        preview.setImageDrawable(null);
        preview.setVisibility(GONE);
      }

      task = decorator.set(res, stat, constraint, this);
    }

    private void setSymlink(File file) {
      Stat stat = file.stat();
      if (stat == null || !stat.isSymbolicLink()) {
        symlink.setVisibility(GONE);
      } else {
        symlink.setEnabled(file.isReadable());
        symlink.setVisibility(VISIBLE);
      }
    }

    @Override public void onSizeAvailable(Resource item, Rect size) {
      if (Objects.equals(item, itemId())) {
        preview.setVisibility(VISIBLE);
        preview.setImageDrawable(
            new SizedColorDrawable(TRANSPARENT, size.scale(constraint)));
      }
    }

    @Override public void onPaletteAvailable(Resource item, Palette palette) {
      if (Objects.equals(item, itemId())) {
        TransitionDrawable background = new TransitionDrawable(new Drawable[]{
            new ColorDrawable(TRANSPARENT),
            new ColorDrawable(backgroundColor(palette))
        });
        container.setBackground(background);
        background.startTransition(animateDuration);
      }
    }

    private int backgroundColor(Palette palette) {
      return palette.getDarkMutedColor(TRANSPARENT);
    }

    @Override public void onPreviewAvailable(Resource item, Bitmap bitmap) {
      if (Objects.equals(item, itemId())) {
        preview.setImageBitmap(bitmap);
        preview.setVisibility(VISIBLE);
        preview.setAlpha(0f);
        preview.animate().alpha(1).setDuration(animateDuration);
      }
    }

    @Override public void onPreviewFailed(Resource item) {
      if (Objects.equals(item, itemId())) {
        preview.setVisibility(GONE);
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
