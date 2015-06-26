package l.files.ui.browser;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Function;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;
import l.files.R;
import l.files.common.view.ActionModeProvider;
import l.files.fs.Instant;
import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.ui.Icons;
import l.files.ui.OpenFileRequest;
import l.files.ui.StableAdapter;
import l.files.ui.browser.FileListItem.File;
import l.files.ui.browser.FileListItem.Header;
import l.files.ui.mode.Selectable;
import l.files.ui.preview.Preview;
import l.files.ui.selection.Selection;
import l.files.ui.selection.SelectionModeViewHolder;

import static android.graphics.Typeface.BOLD;
import static android.graphics.Typeface.SANS_SERIF;
import static android.text.format.DateFormat.getDateFormat;
import static android.text.format.DateFormat.getTimeFormat;
import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
import static android.text.format.DateUtils.FORMAT_NO_YEAR;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.formatDateTime;
import static android.text.format.Formatter.formatShortFileSize;
import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.applyDimension;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.text.DateFormat.MEDIUM;
import static java.text.DateFormat.getDateTimeInstance;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static l.files.R.layout.files_item;
import static l.files.R.layout.files_item_header;
import static l.files.common.view.Views.find;
import static l.files.ui.FilesApp.getBitmapCache;
import static l.files.ui.Icons.defaultFileIconStringId;
import static l.files.ui.Icons.directoryIconStringId;
import static l.files.ui.Icons.fileIconStringId;

final class FilesAdapter extends StableAdapter<FileListItem, ViewHolder>
        implements Selectable
{
    final Preview decorator;
    final DateFormatter formatter;
    final ActionModeProvider actionModeProvider;
    final ActionMode.Callback actionModeCallback;
    final Selection<Resource> selection;
    final EventBus bus;

    boolean setItemsCalled;

    FilesAdapter(
            final Context context,
            final Selection<Resource> selection,
            final ActionModeProvider actionModeProvider,
            final ActionMode.Callback actionModeCallback,
            final EventBus bus)
    {
        this.actionModeProvider = requireNonNull(actionModeProvider, "actionModeProvider");
        this.actionModeCallback = requireNonNull(actionModeCallback, "actionModeCallback");
        this.bus = requireNonNull(bus, "bus");
        this.selection = requireNonNull(selection, "selection");
        this.formatter = new DateFormatter(context);


        final Resources res = context.getResources();
        final DisplayMetrics metrics = res.getDisplayMetrics();
        final int maxThumbnailWidth = metrics.widthPixels - (int) (applyDimension(COMPLEX_UNIT_DIP, 90, metrics) + 0.5f);
        final int maxThumbnailHeight = (int) (metrics.heightPixels * 0.6f);
        this.decorator = new Preview(
                getBitmapCache(context),
                maxThumbnailWidth,
                maxThumbnailHeight);
    }

    @Override
    public void setItems(final List<? extends FileListItem> items)
    {
        super.setItems(items);
        setItemsCalled = true;
    }

    @Override
    public int getItemViewType(final int position)
    {
        return getItem(position).isFile() ? 0 : 1;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType)
    {
        final Context context = parent.getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        return viewType == 0
                ? new FileHolder(inflater.inflate(files_item, parent, false))
                : new HeaderHolder(inflater.inflate(files_item_header, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position)
    {
        final FileListItem item = getItem(position);
        if (item.isHeader())
        {
            ((HeaderHolder) holder).set((Header) item);
        }
        else
        {
            ((FileHolder) holder).set((File) item);
        }
    }

    @Override
    public Object getItemIdObject(final int position)
    {
        final FileListItem item = getItem(position);
        if (item instanceof File)
        {
            return ((File) item).resource();
        }
        return item;
    }

    @Override
    public void selectAll()
    {
        final List<FileListItem> items = items();
        final List<Resource> resources = new ArrayList<>(items.size());
        for (final FileListItem item : items)
        {
            if (item.isFile())
            {
                resources.add(((File) item).resource());
            }
        }
        selection.addAll(resources);
    }

    static final class DateFormatter implements Function<Stat, CharSequence>
    {
        final Context context;
        final DateFormat futureFormat;
        final DateFormat dateFormat;
        final DateFormat timeFormat;
        final Date date = new Date();
        final Time currentTime = new Time();
        final Time thatTime = new Time();
        final int flags
                = FORMAT_SHOW_DATE
                | FORMAT_ABBREV_MONTH
                | FORMAT_NO_YEAR;

        DateFormatter(final Context context)
        {
            this.context = requireNonNull(context, "context");
            this.futureFormat = getDateTimeInstance(MEDIUM, MEDIUM);
            this.dateFormat = getDateFormat(context);
            this.timeFormat = getTimeFormat(context);
        }

        @Override
        public CharSequence apply(final Stat file)
        {
            final Instant instant = file.modified();
            final long millis = instant.to(MILLISECONDS);
            date.setTime(millis);
            currentTime.setToNow();
            thatTime.set(millis);
            if (currentTime.before(thatTime))
            {
                return futureFormat.format(date);
            }
            if (currentTime.year == thatTime.year)
            {
                return currentTime.yearDay == thatTime.yearDay
                        ? timeFormat.format(date)
                        : formatDateTime(context, millis, flags);
            }
            return dateFormat.format(date);
        }
    }

    final class FileHolder extends SelectionModeViewHolder<Resource>
    {
        private final TextView icon;
        private final TextView title;
        private final TextView summary;
        private final TextView symlink;
        private final ImageView preview;

        FileHolder(final View itemView)
        {
            super(itemView, selection, actionModeProvider, actionModeCallback);
            this.icon = find(R.id.icon, this);
            this.title = find(R.id.title, this);
            this.summary = find(R.id.summary, this);
            this.symlink = find(R.id.symlink, this);
            this.preview = find(R.id.preview, this);
            this.itemView.setOnClickListener(this);
            this.itemView.setOnLongClickListener(this);
        }

        @Override
        protected Resource item(final int position)
        {
            return ((File) getItem(position)).resource();
        }

        @Override
        protected void onClick(final View v, final Resource item)
        {
            bus.post(OpenFileRequest.create(item));
        }

        void set(final File file)
        {
            setTitle(file);
            setIcon(file);
            setSymlink(file);
            setSummary(file);
            setPreview(file);
            itemView.setActivated(selection.contains(file.resource()));
        }

        private void setTitle(final File file)
        {
            title.setText(file.resource().name());
            title.setEnabled(file.stat() != null && file.isReadable());
        }

        private void setIcon(final File file)
        {
            icon.setEnabled(file.targetStat() != null && file.isReadable());

            if (file.targetStat() != null
                    && setLocalIcon(icon, file.targetStat()))
            {
                icon.setTypeface(SANS_SERIF, BOLD);
            }
            else
            {
                icon.setText(iconTextId(file));
                icon.setTypeface(Icons.font(icon.getResources().getAssets()));
            }
        }

        private boolean setLocalIcon(final TextView icon, final Stat stat)
        {
            if (stat.isBlockDevice())
            {
                icon.setText("B");
            }
            else if (stat.isCharacterDevice())
            {
                icon.setText("C");
            }
            else if (stat.isSocket())
            {
                icon.setText("S");
            }
            else if (stat.isFifo())
            {
                icon.setText("P");
            }
            else
            {
                return false;
            }
            return true;
        }

        private int iconTextId(final File file)
        {
            final Stat stat = file.targetStat();
            if (stat == null)
            {
                return defaultFileIconStringId();
            }

            if (stat.isDirectory())
            {
                return directoryIconStringId(file.resource());
            }
            else
            {
                return fileIconStringId(file.basicMediaType());
            }
        }

        private void setSummary(final File file)
        {
            final Stat stat = file.stat();
            if (stat == null)
            {
                summary.setText("");
                summary.setVisibility(GONE);
            }
            else
            {
                summary.setVisibility(VISIBLE);
                summary.setEnabled(file.isReadable());
                final CharSequence date = formatter.apply(stat);
                final CharSequence size = formatShortFileSize(summary.getContext(), stat.size());
                final boolean hasDate = stat.modified().to(MINUTES) > 0;
                final boolean isFile = stat.isRegularFile();
                if (hasDate && isFile)
                {
                    final Context context = summary.getContext();
                    summary.setText(context.getString(R.string.x_dot_y, date, size));
                }
                else if (hasDate)
                {
                    summary.setText(date);
                }
                else if (isFile)
                {
                    summary.setText(size);
                }
                else
                {
                    summary.setVisibility(GONE);
                }
            }
        }

        private void setPreview(final File file)
        {
            if (file.stat() == null)
            {
                preview.setImageDrawable(null);
                preview.setVisibility(GONE);
            }
            else
            {
                decorator.set(preview, file.resource(), file.stat());
            }
        }

        private void setSymlink(final File file)
        {
            final Stat stat = file.stat();
            if (stat == null || !stat.isSymbolicLink())
            {
                symlink.setVisibility(GONE);
            }
            else
            {
                symlink.setEnabled(file.isReadable());
                symlink.setVisibility(VISIBLE);
            }
        }
    }

    final class HeaderHolder extends ViewHolder
    {
        private final TextView title;

        HeaderHolder(final View itemView)
        {
            super(itemView);
            title = find(android.R.id.title, this);
        }

        void set(final Header header)
        {
            title.setText(header.toString());
        }
    }

}
